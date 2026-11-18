#include <jni.h>
#include <android/log.h>

#include <string>
#include <vector>

#include "llama_bridge_common.h"

namespace {

constexpr const char *kTag = "MishtiLlama";

void forward_log(ggml_log_level level, const char *text, void *) {
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

}

extern "C" {

JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeInit(JNIEnv *, jobject) {
    llama_log_set(forward_log, nullptr);
    common_llama_init();
    __android_log_print(ANDROID_LOG_INFO, kTag, "llama backend initialised (%s)", common_llama_version());
}

JNIEXPORT jstring JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeVersion(JNIEnv *env, jobject) {
    return env->NewStringUTF(common_llama_version());
}

JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeFree(JNIEnv *, jobject) {
    common_llama_free();
}

JNIEXPORT jlong JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeLoadModel(
        JNIEnv *env, jobject, jstring path, jint context_tokens, jint threads) {

    const std::string model_path = to_string(env, path);
    int64_t session = common_llama_load_model(model_path.c_str(), context_tokens, threads);
    if (session == 0) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "failed to load %s", model_path.c_str());
        return 0;
    }

    __android_log_print(ANDROID_LOG_INFO, kTag, "loaded %s (n_ctx=%d, threads=%d)",
                        model_path.c_str(), context_tokens, threads);
    return session;
}

JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeFreeModel(
        JNIEnv *, jobject, jlong handle) {
    common_llama_free_model(handle);
}

JNIEXPORT jstring JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeChatTemplate(
        JNIEnv *env, jobject, jlong handle) {
    const char *tmpl = common_llama_chat_template(handle);
    return tmpl == nullptr ? nullptr : env->NewStringUTF(tmpl);
}

JNIEXPORT jint JNICALL
Java_com_abrarshakhi_mishti_core_llm_data_LlamaNative_nativeGenerate(
        JNIEnv *env, jobject, jlong handle,
        jobjectArray roles, jobjectArray contents,
        jfloat temperature, jint top_k, jfloat top_p, jint max_tokens,
        jstring assistant_prefix, jobject callback) {

    if (handle == 0 || roles == nullptr || contents == nullptr || callback == nullptr) {
        return -1;
    }

    const jsize count = env->GetArrayLength(roles);
    if (count <= 0) return -1;

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

    jclass callback_class = env->GetObjectClass(callback);
    jmethodID on_token = env->GetMethodID(callback_class, "onToken", "(Ljava/lang/String;)Z");
    const std::string prefix = to_string(env, assistant_prefix);

    return common_llama_generate(
        handle,
        messages,
        temperature,
        top_k,
        top_p,
        max_tokens,
        prefix.empty() ? nullptr : prefix.c_str(),
        [&](const char *piece) -> bool {
            jstring js = env->NewStringUTF(piece);
            const jboolean keep_going = env->CallBooleanMethod(callback, on_token, js);
            env->DeleteLocalRef(js);
            return keep_going != JNI_FALSE;
        }
    );
}

}
