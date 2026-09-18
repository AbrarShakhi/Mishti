package com.abrarshakhi.mishti.features.models.domain.model


object ModelCatalog {

    val models: List<LlmModel> = listOf(
        LlmModel(
            id = "smollm2-360m-instruct-q4km",
            name = "SmolLM2 360M Instruct",
            description = "Smallest and fastest. A sensible first download on a modest phone.",
            parameters = "360M",
            quantization = "Q4_K_M",
            sizeBytes = 270_590_880L,
            url = "https://huggingface.co/bartowski/SmolLM2-360M-Instruct-GGUF/resolve/main/" +
                "SmolLM2-360M-Instruct-Q4_K_M.gguf",
            sha256 = "2fa3f013dcdd7b99f9b237717fa0b12d75bbb89984cc1274be1471a465bac9c2",
            minRamBytes = 3_200_000_000L,
        ),
        LlmModel(
            id = "qwen2.5-0.5b-instruct-q4km",
            name = "Qwen2.5 0.5B Instruct",
            description = "A good balance of quality and size for everyday use.",
            parameters = "0.5B",
            quantization = "Q4_K_M",
            sizeBytes = 397_808_192L,
            url = "https://huggingface.co/bartowski/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/" +
                "Qwen2.5-0.5B-Instruct-Q4_K_M.gguf",
            sha256 = "6eb923e7d26e9cea28811e1a8e852009b21242fb157b26149d3b188f3a8c8653",
            minRamBytes = 3_200_000_000L,
        ),
        LlmModel(
            id = "llama-3.2-1b-instruct-q4km",
            name = "Llama 3.2 1B Instruct",
            description = "Strongest of the three, and the heaviest. Wants a roomier device.",
            parameters = "1B",
            quantization = "Q4_K_M",
            sizeBytes = 807_694_464L,
            url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/" +
                "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            sha256 = "6f85a640a97cf2bf5b8e764087b1e83da0fdb51d7c9fab7d0fece9385611df83",
            minRamBytes = 4_000_000_000L,
        ),
    )

    fun byId(id: String): LlmModel? = models.find { it.id == id }
}
