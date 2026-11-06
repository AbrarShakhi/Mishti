#pragma once

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef bool (*LlamaTokenCallback)(const char *piece, void *userData);

void llama_bridge_init(void);

const char *llama_bridge_version(void);

void llama_bridge_free(void);

int64_t llama_bridge_load_model(const char *path, int32_t context_tokens, int32_t threads);

void llama_bridge_free_model(int64_t handle);

const char *llama_bridge_chat_template(int64_t handle);

int32_t llama_bridge_generate(
    int64_t handle,
    const char * const *roles,
    const char * const *contents,
    int32_t message_count,
    float temperature,
    int32_t top_k,
    float top_p,
    int32_t max_tokens,
    const char *assistant_prefix,
    LlamaTokenCallback callback,
    void *user_data
);

#ifdef __cplusplus
}
#endif
