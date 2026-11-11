#include "llama_bridge_common.h"

#include <algorithm>
#include <cstring>
#include <string>
#include <vector>

void common_llama_init(void) {
    llama_backend_init();
}

const char *common_llama_version(void) {
    return llama_version();
}

void common_llama_free(void) {
    llama_backend_free();
}

int64_t common_llama_load_model(const char *path, int32_t context_tokens, int32_t threads) {
    if (path == nullptr || context_tokens <= 0 || threads <= 0) return 0;

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    llama_model *model = llama_model_load_from_file(path, model_params);
    if (model == nullptr) return 0;

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = static_cast<uint32_t>(context_tokens);
    ctx_params.n_threads = threads;
    ctx_params.n_threads_batch = threads;

    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        llama_model_free(model);
        return 0;
    }

    auto *session = new LlamaSession{model, ctx, llama_model_get_vocab(model)};
    return reinterpret_cast<int64_t>(session);
}

void common_llama_free_model(int64_t handle) {
    auto *session = reinterpret_cast<LlamaSession *>(handle);
    if (session == nullptr) return;
    if (session->ctx) llama_free(session->ctx);
    if (session->model) llama_model_free(session->model);
    delete session;
}

const char *common_llama_chat_template(int64_t handle) {
    auto *session = reinterpret_cast<LlamaSession *>(handle);
    if (session == nullptr) return nullptr;
    return llama_model_chat_template(session->model, nullptr);
}

std::string common_llama_token_text(const llama_vocab *vocab, llama_token token) {
    char stack_buf[128];
    int32_t n = llama_token_to_piece(vocab, token, stack_buf, sizeof(stack_buf), 0, false);
    if (n >= 0) return std::string(stack_buf, n);

    std::vector<char> heap_buf(static_cast<size_t>(-n));
    n = llama_token_to_piece(vocab, token, heap_buf.data(),
                             static_cast<int32_t>(heap_buf.size()), 0, false);
    return n > 0 ? std::string(heap_buf.data(), n) : std::string();
}

std::string common_llama_build_prompt(const llama_model *model,
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
    std::vector<char> buf(budget * 2 + 1024);

    int32_t written = llama_chat_apply_template(
            tmpl, messages.data(), messages.size(), true,
            buf.data(), static_cast<int32_t>(buf.size()));

    if (written > static_cast<int32_t>(buf.size())) {
        buf.resize(static_cast<size_t>(written));
        written = llama_chat_apply_template(
                tmpl, messages.data(), messages.size(), true,
                buf.data(), static_cast<int32_t>(buf.size()));
    }
    return written > 0 ? std::string(buf.data(), written) : std::string();
}

int32_t common_llama_generate(
    int64_t handle,
    const std::vector<llama_chat_message> &messages,
    float temperature,
    int32_t top_k,
    float top_p,
    int32_t max_tokens,
    const char *assistant_prefix,
    const LlamaTokenConsumer &callback
) {
    auto *session = reinterpret_cast<LlamaSession *>(handle);
    if (session == nullptr || messages.empty() || max_tokens <= 0) return -1;

    std::string prompt = common_llama_build_prompt(session->model, messages);
    if (prompt.empty()) return -1;
    if (assistant_prefix != nullptr) {
        prompt += assistant_prefix;
    }

    const int32_t n_prompt = -llama_tokenize(
            session->vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()),
            nullptr, 0, true, true);

    if (n_prompt <= 0) return -1;
    std::vector<llama_token> tokens(n_prompt);
    if (llama_tokenize(session->vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()),
                       tokens.data(), n_prompt, true, true) < 0) {
        return -1;
    }

    const uint32_t n_ctx = llama_n_ctx(session->ctx);
    if (static_cast<uint32_t>(n_prompt) >= n_ctx) return -1;

    llama_memory_clear(llama_get_memory(session->ctx), true);

    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(top_k));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(top_p, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    llama_batch batch = llama_batch_get_one(tokens.data(), n_prompt);
    int32_t generated = 0;
    bool failed = false;
    const int32_t limit = std::min(max_tokens, static_cast<int32_t>(n_ctx) - n_prompt);

    while (generated < limit) {
        if (llama_decode(session->ctx, batch) != 0) {
            failed = true;
            break;
        }

        const llama_token id = llama_sampler_sample(sampler, session->ctx, -1);
        if (llama_vocab_is_eog(session->vocab, id)) break;

        llama_sampler_accept(sampler, id);
        ++generated;

        const std::string piece = common_llama_token_text(session->vocab, id);
        if (callback != nullptr) {
            const bool keep_going = callback(piece.c_str());
            if (!keep_going) break;
        }

        tokens.assign(1, id);
        batch = llama_batch_get_one(tokens.data(), 1);
    }

    llama_sampler_free(sampler);
    return failed ? -1 : generated;
}
