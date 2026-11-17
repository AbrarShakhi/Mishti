package com.abrarshakhi.mishti.core.session.model

data class SelectableModel(
    val id: String,
    val name: String,
    val parametersLabel: String? = null,
    val quantization: String? = null,
    val isSelected: Boolean = false,
)
