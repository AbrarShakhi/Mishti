// JNI bridge between Mishti and llama.cpp.
//
// Deliberately thin: it owns the llama_model / llama_context pair and streams tokens back
// through a Kotlin callback. Everything above it — history, persistence, cancellation policy
// — stays in Kotlin, so the amount of C++ that can go wrong is bounded.
//
// Thread safety: a llama_context is NOT safe to use concurrently. Nothing here locks; the
// Kotlin side confines every call to a single-threaded dispatcher instead.

#include <jni.h>

#include <android/log.h>

#include <cstring>
#include <string>
#include <vector>

#include "llama.h"

namespace {

constexpr const char *kTag = "MishtiLlama";

struct Session {
    llama_model *model = nullptr;
    llama_context *ctx = nullptr;
    const llama_vocab *vocab = nullptr;
};

/// Routes llama.cpp's own logging into logcat.
///
/// Without this llama.cpp writes to stderr, which Android discards — so a model that fails to
/// load would do so silently, with nothing to debug.
void forward_log(ggml_log_level level, const char *text, void * /*user_data*/) {
    int priority;
    switch (level) {
        case GGML_LOG_LEVEL_ERROR: priority = ANDROID_LOG_ERROR; break;
        case GGML_LOG_LEVEL_WARN:  priority = ANDROID_LOG_WARN;  break;
        case GGML_LOG_LEVEL_DEBUG: priority = ANDROID_LOG_DEBUG; break;
        default:                   priority = ANDROID_LOG_INFO;  break;
    }
    __android_log_write(priority, kTag, text);
}

std::string to_string(JNIEnv *env, jstring value) {
    if (value == nullptr) return {};
    const char *chars = env->GetStringUTFChars(value, nullptr);
    std::string result(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(value, chars);
    return result;
}

/// Detokenises one token, growing the buffer on the rare piece that does not fit.
std::string token_text(const llama_vocab *vocab, llama_token token) {
    char stack_buf[128];
    int32_t n = llama_token_to_piece(vocab, token, stack_buf, sizeof(stack_buf), 0, false);
    if (n >= 0) return std::string(stack_buf, n);

    std::vector<char> heap_buf(static_cast<size_t>(-n));
    n = llama_token_to_piece(vocab, token, heap_buf.data(),
                             static_cast<int32_t>(heap_buf.size()), 0, false);
    return n > 0 ? std::string(heap_buf.data(), n) : std::string();
}

/// Formats the conversation with the model's own chat template.
///
/// Instruct models are trained against a specific turn format; feeding them raw concatenated
/// text produces rambling continuations rather than answers. Models without an embedded
/// template fall back to a plain transcript, which is the best that can be done blind.
std::string build_prompt(const llama_model *model,
                         const std::vector<llama_chat_message> &messages) {
    const char *tmpl = llama_model_chat_template(model, nullptr);

    if (tmpl == nullptr) {
        std::string joined;
        for (const auto &msg : messages) {
            joined += msg.role;
            joined += ": ";
            joined += msg.content;
            joined += "\n";
        }
        joined += "assistant: ";
        return joined;
    }

    size_t budget = 0;
    for (const auto &msg : messages) budget += strlen(msg.role) + strlen(msg.content);
    // The header recommends twice the raw character count; template markup can exceed that on
    // short conversations, so start generously and resize if the call still reports more.
    std::vector<char> buf(budget * 2 + 1024);

    int32_t written = llama_chat_apply_template(
            tmpl, messages.data(), messages.size(), /*add_ass=*/true,
            buf.data(), static_cast<int32_t>(buf.size()));

    if (written > static_cast<int32_t>(buf.size())) {
        buf.resize(static_cast<size_t>(written));
        written = llama_chat_apply_template(
                tmpl, messages.data(), messages.size(), /*add_ass=*/true,
                buf.data(), static_cast<int32_t>(buf.size()));
    }
    return written > 0 ? std::string(buf.data(), written) : std::string();
}

} // namespace

extern "C" {

JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_common_llm_LlamaNative_nativeInit(JNIEnv * /*env*/, jobject /*thiz*/) {
    llama_log_set(forward_log, nullptr);
    llama_backend_init();
    __android_log_print(ANDROID_LOG_INFO, kTag, "llama backend initialised (%s)", llama_version());
}

JNIEXPORT jstring JNICALL
Java_com_abrarshakhi_mishti_common_llm_LlamaNative_nativeVersion(JNIEnv *env, jobject /*thiz*/) {
    return env->NewStringUTF(llama_version());
}

JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_common_llm_LlamaNative_nativeFree(JNIEnv * /*env*/, jobject /*thiz*/) {
    llama_backend_free();
}

/// Loads a GGUF and creates its context. Returns 0 on failure.
JNIEXPORT jlong JNICALL
Java_com_abrarshakhi_mishti_common_llm_LlamaNative_nativeLoadModel(
        JNIEnv *env, jobject /*thiz*/, jstring path, jint context_tokens, jint threads) {

    const std::string model_path = to_string(env, path);

    llama_model_params model_params = llama_model_default_params();
    // No GPU backend is compiled in, so everything runs on the CPU.
    model_params.n_gpu_layers = 0;

    llama_model *model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "failed to load %s", model_path.c_str());
        return 0;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = static_cast<uint32_t>(context_tokens);
    ctx_params.n_threads = threads;
    ctx_params.n_threads_batch = threads;

    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "failed to create context");
        llama_model_free(model);
        return 0;
    }

    auto *session = new Session{model, ctx, llama_model_get_vocab(model)};
    __android_log_print(ANDROID_LOG_INFO, kTag, "loaded %s (n_ctx=%d, threads=%d)",
                        model_path.c_str(), context_tokens, threads);
    return reinterpret_cast<jlong>(session);
}

JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_common_llm_LlamaNative_nativeFreeModel(
        JNIEnv * /*env*/, jobject /*thiz*/, jlong handle) {
    auto *session = reinterpret_cast<Session *>(handle);
    if (session == nullptr) return;
    if (session->ctx) llama_free(session->ctx);
    if (session->model) llama_model_free(session->model);
    delete session;
}

/// Streams a reply. Returns the number of tokens generated, or -1 on failure.
///
/// `callback.onToken(String): Boolean` receives each piece and returns false to stop, which is
/// how cancellation from Kotlin reaches the decode loop.
JNIEXPORT jint JNICALL
Java_com_abrarshakhi_mishti_common_llm_LlamaNative_nativeGenerate(
        JNIEnv *env, jobject /*thiz*/, jlong handle,
        jobjectArray roles, jobjectArray contents,
        jfloat temperature, jint top_k, jfloat top_p, jint max_tokens, jobject callback) {

    auto *session = reinterpret_cast<Session *>(handle);
    if (session == nullptr) return -1;

    // --- format the conversation -------------------------------------------------------
    const jsize count = env->GetArrayLength(roles);
    std::vector<std::string> role_store(count);
    std::vector<std::string> content_store(count);
    std::vector<llama_chat_message> messages(count);

    for (jsize i = 0; i < count; ++i) {
        auto role = reinterpret_cast<jstring>(env->GetObjectArrayElement(roles, i));
        auto content = reinterpret_cast<jstring>(env->GetObjectArrayElement(contents, i));
        role_store[i] = to_string(env, role);
        content_store[i] = to_string(env, content);
        messages[i] = llama_chat_message{role_store[i].c_str(), content_store[i].c_str()};
        env->DeleteLocalRef(role);
        env->DeleteLocalRef(content);
    }

    const std::string prompt = build_prompt(session->model, messages);
    if (prompt.empty()) return -1;

    // --- tokenise ----------------------------------------------------------------------
    const int32_t n_prompt = -llama_tokenize(
            session->vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()),
            nullptr, 0, /*add_special=*/true, /*parse_special=*/true);

    std::vector<llama_token> tokens(n_prompt);
    if (llama_tokenize(session->vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()),
                       tokens.data(), n_prompt, true, true) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "tokenisation failed");
        return -1;
    }

    const uint32_t n_ctx = llama_n_ctx(session->ctx);
    if (static_cast<uint32_t>(n_prompt) >= n_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, kTag,
                            "prompt %d tokens exceeds context %u", n_prompt, n_ctx);
        return -1;
    }

    // Each call re-sends the whole conversation, so the cache from the previous turn is stale
    // and its positions would collide. Clearing is cheaper to reason about than reusing it.
    llama_memory_clear(llama_get_memory(session->ctx), true);

    // --- sampler ------------------------------------------------------------------------
    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(top_k));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(top_p, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    jclass callback_class = env->GetObjectClass(callback);
    jmethodID on_token = env->GetMethodID(callback_class, "onToken", "(Ljava/lang/String;)Z");

    // --- decode -------------------------------------------------------------------------
    llama_batch batch = llama_batch_get_one(tokens.data(), n_prompt);
    int32_t generated = 0;
    bool failed = false;

    while (generated < max_tokens) {
        if (llama_decode(session->ctx, batch) != 0) {
            __android_log_print(ANDROID_LOG_ERROR, kTag, "decode failed");
            failed = true;
            break;
        }

        const llama_token id = llama_sampler_sample(sampler, session->ctx, -1);
        if (llama_vocab_is_eog(session->vocab, id)) break;

        llama_sampler_accept(sampler, id);
        ++generated;

        const std::string piece = token_text(session->vocab, id);
        jstring js = env->NewStringUTF(piece.c_str());
        const jboolean keep_going = env->CallBooleanMethod(callback, on_token, js);
        env->DeleteLocalRef(js);
        if (keep_going == JNI_FALSE) break;

        tokens.assign(1, id);
        batch = llama_batch_get_one(tokens.data(), 1);
    }

    llama_sampler_free(sampler);
    return failed ? -1 : generated;
}

} // extern "C"
