//
// llm_bridge.cpp
//
// This file is the "translator" between Kotlin and llama.cpp.
//
// How JNI naming works:
//   Java_<package with _ instead of .>_<ClassName>_<methodName>
//
// So:  com.abrarshakhi.mishti.llm.LlamaEngine.nativeLoadModel(...)
// →    Java_com_abrarshakhi_mishti_llm_LlamaEngine_nativeLoadModel(...)
//

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <cstring>

#include "llama.h"

#define LOG_TAG "MishtiLLM"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ── State held between JNI calls ─────────────────────────────────────────────
// One model + one context live for the duration of a chat session.
// We store them as raw pointers because JNI has no concept of C++ objects.

static llama_model*   g_model   = nullptr;
static llama_context* g_ctx     = nullptr;
static llama_sampler* g_sampler = nullptr;

// ── Helper: convert jstring → std::string ────────────────────────────────────
static std::string jstring_to_str(JNIEnv* env, jstring js) {
  const char* chars = env->GetStringUTFChars(js, nullptr);
  std::string result(chars);
  env->ReleaseStringUTFChars(js, chars);
  return result;
}

// ─────────────────────────────────────────────────────────────────────────────
// nativeLoadModel(modelPath: String): Boolean
//
// Loads a .gguf model file from disk into memory.
// Returns true on success, false on failure.
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT jboolean JNICALL
Java_com_abrarshakhi_mishti_llm_LlamaEngine_nativeLoadModel(
        JNIEnv* env, jobject /* this */, jstring modelPath) {

  // Clean up any previously loaded model first
  if (g_sampler) { llama_sampler_free(g_sampler); g_sampler = nullptr; }
  if (g_ctx)     { llama_free(g_ctx);              g_ctx     = nullptr; }
  if (g_model)   { llama_model_free(g_model);      g_model   = nullptr; }

  std::string path = jstring_to_str(env, modelPath);
  LOGI("Loading model: %s", path.c_str());

  // Model params: how many GPU layers to offload (0 = CPU only on Android)
  llama_model_params model_params = llama_model_default_params();
  model_params.n_gpu_layers = 0;  // Android has no CUDA; keep on CPU

  g_model = llama_model_load_from_file(path.c_str(), model_params);
  if (!g_model) {
    LOGE("Failed to load model from: %s", path.c_str());
    return JNI_FALSE;
  }

  // Context params: controls memory use and max sequence length
  llama_context_params ctx_params = llama_context_default_params();
  ctx_params.n_ctx    = 2048;  // context window (tokens). Raise for longer chats
  ctx_params.n_batch  = 512;   // how many tokens to process in one batch
  ctx_params.n_threads = 4;    // CPU threads for inference

  g_ctx = llama_init_from_model(g_model, ctx_params);
  if (!g_ctx) {
    LOGE("Failed to create llama context");
    llama_model_free(g_model);
    g_model = nullptr;
    return JNI_FALSE;
  }

  // Sampler chain: controls how the next token is picked from the distribution
  g_sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
  llama_sampler_chain_add(g_sampler, llama_sampler_init_min_p(0.05f, 1));
  llama_sampler_chain_add(g_sampler, llama_sampler_init_temp(0.8f));
  llama_sampler_chain_add(g_sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

  LOGI("Model loaded successfully");
  return JNI_TRUE;
}

// ─────────────────────────────────────────────────────────────────────────────
// nativeCompletion(prompt: String, callback: TokenCallback): Unit
//
// Runs inference on `prompt` and calls callback.onToken(token) for each
// generated piece of text. This lets Kotlin display tokens as they stream in.
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_llm_LlamaEngine_nativeCompletion(
        JNIEnv* env, jobject /* this */, jstring prompt, jobject callback) {

  if (!g_model || !g_ctx || !g_sampler) {
    LOGE("nativeCompletion called but model is not loaded");
    return;
  }

  std::string prompt_str = jstring_to_str(env, prompt);

  // ── Get vocab — needed for tokenise AND token-to-piece ───────────────────
  // As of recent llama.cpp, llama_tokenize takes llama_vocab* not llama_model*
  const llama_vocab* vocab = llama_model_get_vocab(g_model);

  // ── Tokenise the prompt ───────────────────────────────────────────────────
  // llama works on integer token IDs, not raw text.
  // We estimate the max tokens needed (prompt length / 4 bytes + 128 safety buffer).
  int n_prompt_tokens_max = (int)(prompt_str.size() / 4) + 128;
  std::vector<llama_token> prompt_tokens(n_prompt_tokens_max);

  int n_prompt_tokens = llama_tokenize(
          vocab,                      // ← vocab*, not model* (API changed in recent llama.cpp)
          prompt_str.c_str(),
          (int)prompt_str.size(),
          prompt_tokens.data(),
          n_prompt_tokens_max,
          /*add_special=*/true,       // add BOS token
          /*parse_special=*/true
  );

  if (n_prompt_tokens < 0) {
    LOGE("Tokenisation failed (buffer too small?)");
    return;
  }
  prompt_tokens.resize(n_prompt_tokens);

  // ── Prepare a batch with the prompt tokens ────────────────────────────────
  llama_batch batch = llama_batch_get_one(prompt_tokens.data(), n_prompt_tokens);

  // Evaluate the prompt (fills the KV cache)
  if (llama_decode(g_ctx, batch) != 0) {
    LOGE("llama_decode failed on prompt");
    return;
  }

  // ── Look up the Java callback method we'll call per token ─────────────────
  jclass  cb_class  = env->GetObjectClass(callback);
  jmethodID on_token = env->GetMethodID(cb_class, "onToken", "(Ljava/lang/String;)V");

  // ── Generation loop ───────────────────────────────────────────────────────
  // vocab already fetched above
  int n_ctx = llama_n_ctx(g_ctx);
  int n_cur = n_prompt_tokens;      // how many tokens we've processed so far

  while (n_cur < n_ctx) {
    // Sample the next token
    llama_token new_token = llama_sampler_sample(g_sampler, g_ctx, -1);

    // EOG = "End Of Generation" — model signals it's done
    if (llama_vocab_is_eog(vocab, new_token)) {
      LOGI("EOG reached after %d tokens", n_cur);
      break;
    }

    // Convert the token ID back to text
    char piece_buf[256];
    int piece_len = llama_token_to_piece(
            vocab, new_token, piece_buf, sizeof(piece_buf), 0, true);

    if (piece_len > 0) {
      piece_buf[piece_len] = '\0';
      jstring piece = env->NewStringUTF(piece_buf);
      env->CallVoidMethod(callback, on_token, piece);
      env->DeleteLocalRef(piece);

      // Stop if Java side threw an exception (e.g. user cancelled)
      if (env->ExceptionCheck()) {
        env->ExceptionClear();
        LOGI("Exception in onToken callback — stopping generation");
        break;
      }
    }

    // Feed the new token back in for the next iteration
    batch = llama_batch_get_one(&new_token, 1);
    if (llama_decode(g_ctx, batch) != 0) {
      LOGE("llama_decode failed during generation");
      break;
    }

    n_cur++;
  }

  llama_sampler_reset(g_sampler);
}

// ─────────────────────────────────────────────────────────────────────────────
// nativeFreeModel(): Unit
//
// Releases all native memory. Call this when the user switches models
// or the app is about to be destroyed.
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT void JNICALL
Java_com_abrarshakhi_mishti_llm_LlamaEngine_nativeReset(
        JNIEnv* /* env */, jobject /* this */) {
  if (g_sampler) { llama_sampler_free(g_sampler); g_sampler = nullptr; }
  if (g_ctx)     { llama_free(g_ctx);              g_ctx     = nullptr; }
  if (g_model)   { llama_model_free(g_model);      g_model   = nullptr; }
  LOGI("Native model freed");
}

// ─────────────────────────────────────────────────────────────────────────────
// nativeIsModelLoaded(): Boolean
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT jboolean JNICALL
Java_com_abrarshakhi_mishti_llm_LlamaEngine_nativeIsModelLoaded(
        JNIEnv* /* env */, jobject /* this */) {
  return (g_model != nullptr && g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}