package com.abrarshakhi.mishti.models.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberModelFilePicker(onPicked: (String) -> Unit): () -> Unit {
    val controller = LocalUIViewController.current
    val callback = rememberUpdatedState(onPicked)
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString?.let { callback.value(it) }
            }
        }
    }
    return {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeData), asCopy = true)
        picker.delegate = delegate
        controller.presentViewController(picker, animated = true, completion = null)
    }
}

@Composable
internal actual fun rememberTransferNotificationRequest(): () -> Unit = { }
