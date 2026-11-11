#include "llama_bridge_ios.h"
#include "llama_bridge_common.h"

#include <string>
#include <vector>

extern "C" {

void llama_bridge_init(void) {
    common_llama_init();
}

const char *llama_bridge_version(void) {
    return common_llama_version();
}

void llama_bridge_free(void) {
    common_llama_free();
}

int64_t llama_bridge_load_model(const char *path, int32_t context_tokens, int32_t threads) {
    return common_llama_load_model(path, context_tokens, threads);
}

void llama_bridge_free_model(int64_t handle) {
    common_llama_free_model(handle);
}

const char *llama_bridge_chat_template(int64_t handle) {
    return common_llama_chat_template(handle);
}

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
) {
    if (handle == 0 || message_count <= 0 || roles == nullptr || contents == nullptr || max_tokens <= 0) {
        return -1;
    }

    std::vector<llama_chat_message> messages(message_count);
    for (int32_t i = 0; i < message_count; ++i) {
        messages[i] = llama_chat_message{roles[i] ? roles[i] : "", contents[i] ? contents[i] : ""};
    }

    return common_llama_generate(
        handle,
        messages,
        temperature,
        top_k,
        top_p,
        max_tokens,
        assistant_prefix,
        [&](const char *piece) -> bool {
            if (callback != nullptr) {
                return callback(piece, user_data);
            }
            return false;
        }
    );
}

}
