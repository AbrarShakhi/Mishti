package com.abrarshakhi.mishti.common.ui.text

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiText(@param:StringRes val id: Int, val args: List<Any> = emptyList()) {
    fun resolve(resources: Resources): String = if (args.isEmpty()) {
        resources.getString(id)
    } else {
        resources.getString(
            id,
            *args.toTypedArray(),
        )
    }
}

fun uiText(@StringRes id: Int, vararg args: Any) = UiText(id, args.toList())

@Composable
fun UiText.asString(): String = if (args.isEmpty()) stringResource(id) else stringResource(id, *args.toTypedArray())
