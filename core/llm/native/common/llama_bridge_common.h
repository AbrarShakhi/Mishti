#pragma once

#include <stdint.h>
#include <stdbool.h>
#include <functional>
#include <string>
#include <vector>

#include "llama.h"

struct LlamaSession {
    llama_model *model = nullptr;
    llama_context *ctx = nullptr;
    const llama_vocab *vocab = nullptr;
};

using LlamaTokenConsumer = std::function<bool(const char *piece)>;

void common_llama_init(void);
const char *common_llama_version(void);
void common_llama_free(void);

int64_t common_llama_load_model(const char *path, int32_t context_tokens, int32_t threads);
void common_llama_free_model(int64_t handle);
const char *common_llama_chat_template(int64_t handle);

std::string common_llama_token_text(const llama_vocab *vocab, llama_token token);
std::string common_llama_build_prompt(const llama_model *model, const std::vector<llama_chat_message> &messages);

int32_t common_llama_generate(
    int64_t handle,
    const std::vector<llama_chat_message> &messages,
    float temperature,
    int32_t top_k,
    float top_p,
    int32_t max_tokens,
    const char *assistant_prefix,
    const LlamaTokenConsumer &callback
);
