package com.abrarshakhi.mishti.core.ui.utils

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class UiText(val resource: StringResource, val args: List<Any> = emptyList()) {
    suspend fun resolve(): String = if (args.isEmpty()) {
        getString(resource)
    } else {
        getString(resource, *args.toTypedArray())
    }
}

fun uiText(resource: StringResource, vararg args: Any) = UiText(resource, args.toList())

@Composable
fun UiText.asString(): String = if (args.isEmpty()) {
    stringResource(resource)
} else {
    stringResource(resource, *args.toTypedArray())
}
