package com.abrarshakhi.mishti.features.models.presentation

import androidx.annotation.StringRes
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.features.models.domain.model.CatalogProblem
import com.abrarshakhi.mishti.features.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.features.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.features.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.features.models.domain.model.TransferError

@get:StringRes
val CatalogSource.labelRes: Int
    get() =
        when (this) {
            CatalogSource.MishtirBhandar -> R.string.models_source_mistir_bhandar
            CatalogSource.PocketPal -> R.string.models_source_pocketpal
        }

@get:StringRes
val ModelOrigin.labelRes: Int
    get() =
        when (this) {
            ModelOrigin.MishtirBhandar -> R.string.models_source_mistir_bhandar
            ModelOrigin.PocketPal -> R.string.models_source_pocketpal
            ModelOrigin.Imported -> R.string.models_origin_imported
        }

@get:StringRes
val CatalogFilter.labelRes: Int
    get() =
        when (this) {
            CatalogFilter.All -> R.string.filter_all
            CatalogFilter.Fast -> R.string.filter_fast
            CatalogFilter.Balanced -> R.string.filter_balanced
            CatalogFilter.Smart -> R.string.filter_smart
            CatalogFilter.Coding -> R.string.filter_coding
        }

@get:StringRes
val MemoryFit.labelRes: Int?
    get() =
        when (this) {
            MemoryFit.Fits -> R.string.fit_fits
            MemoryFit.Tight -> R.string.fit_tight
            MemoryFit.TooBig -> R.string.fit_too_big
            MemoryFit.Unknown -> null
        }

@get:StringRes
val TransferError.messageRes: Int
    get() =
        when (this) {
            TransferError.NotEnoughSpace -> R.string.transfer_error_space
            TransferError.VerificationFailed -> R.string.transfer_error_verification
            TransferError.ServerError -> R.string.transfer_error_server
            TransferError.Network -> R.string.transfer_error_network
            TransferError.CannotOpen -> R.string.transfer_error_open
            TransferError.NotGguf -> R.string.transfer_error_not_gguf
            TransferError.CannotSave -> R.string.transfer_error_save
            TransferError.Unknown -> R.string.transfer_error_unknown
        }

@get:StringRes
val CatalogProblem.messageRes: Int
    get() =
        when (this) {
            CatalogProblem.Offline -> R.string.models_catalog_offline
            CatalogProblem.Unreadable -> R.string.models_catalog_unreadable
            CatalogProblem.Unavailable -> R.string.models_catalog_unavailable
        }

@StringRes
fun sectionTitleRes(sectionId: String): Int =
    when (sectionId) {
        "low" -> R.string.models_tier_low
        "mid" -> R.string.models_tier_mid
        "high" -> R.string.models_tier_high
        "flagship" -> R.string.models_tier_flagship
        else -> R.string.models_section_all
    }
