package com.abrarshakhi.mishti.core.llm.data

import com.abrarshakhi.mishti.core.llm.domain.LlamaBridge

object LlamaBridgeRegistry {
    var bridge: LlamaBridge? = null

    fun register(bridge: LlamaBridge) {
        this.bridge = bridge
    }
}
