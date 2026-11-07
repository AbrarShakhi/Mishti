package com.abrarshakhi.mishti.models.ui

import com.abrarshakhi.mishti.models.domain.model.CatalogProblem
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.models.domain.model.TransferError
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.filter_all
import com.abrarshakhi.mishti.shared.resources.filter_balanced
import com.abrarshakhi.mishti.shared.resources.filter_coding
import com.abrarshakhi.mishti.shared.resources.filter_fast
import com.abrarshakhi.mishti.shared.resources.filter_smart
import com.abrarshakhi.mishti.shared.resources.fit_fits
import com.abrarshakhi.mishti.shared.resources.fit_tight
import com.abrarshakhi.mishti.shared.resources.fit_too_big
import com.abrarshakhi.mishti.shared.resources.models_catalog_offline
import com.abrarshakhi.mishti.shared.resources.models_catalog_unavailable
import com.abrarshakhi.mishti.shared.resources.models_catalog_unreadable
import com.abrarshakhi.mishti.shared.resources.models_origin_imported
import com.abrarshakhi.mishti.shared.resources.models_section_all
import com.abrarshakhi.mishti.shared.resources.models_source_mistir_bhandar
import com.abrarshakhi.mishti.shared.resources.models_source_pocketpal
import com.abrarshakhi.mishti.shared.resources.models_tier_flagship
import com.abrarshakhi.mishti.shared.resources.models_tier_high
import com.abrarshakhi.mishti.shared.resources.models_tier_low
import com.abrarshakhi.mishti.shared.resources.models_tier_mid
import com.abrarshakhi.mishti.shared.resources.transfer_error_network
import com.abrarshakhi.mishti.shared.resources.transfer_error_not_gguf
import com.abrarshakhi.mishti.shared.resources.transfer_error_open
import com.abrarshakhi.mishti.shared.resources.transfer_error_save
import com.abrarshakhi.mishti.shared.resources.transfer_error_server
import com.abrarshakhi.mishti.shared.resources.transfer_error_space
import com.abrarshakhi.mishti.shared.resources.transfer_error_unknown
import com.abrarshakhi.mishti.shared.resources.transfer_error_verification
import org.jetbrains.compose.resources.StringResource

val CatalogSource.labelRes: StringResource
    get() =
        when (this) {
            CatalogSource.MishtirBhandar -> Res.string.models_source_mistir_bhandar
            CatalogSource.PocketPal -> Res.string.models_source_pocketpal
        }

val ModelOrigin.labelRes: StringResource
    get() =
        when (this) {
            ModelOrigin.MishtirBhandar -> Res.string.models_source_mistir_bhandar
            ModelOrigin.PocketPal -> Res.string.models_source_pocketpal
            ModelOrigin.Imported -> Res.string.models_origin_imported
        }

val CatalogFilter.labelRes: StringResource
    get() =
        when (this) {
            CatalogFilter.All -> Res.string.filter_all
            CatalogFilter.Fast -> Res.string.filter_fast
            CatalogFilter.Balanced -> Res.string.filter_balanced
            CatalogFilter.Smart -> Res.string.filter_smart
            CatalogFilter.Coding -> Res.string.filter_coding
        }

val MemoryFit.labelRes: StringResource?
    get() =
        when (this) {
            MemoryFit.Fits -> Res.string.fit_fits
            MemoryFit.Tight -> Res.string.fit_tight
            MemoryFit.TooBig -> Res.string.fit_too_big
            MemoryFit.Unknown -> null
        }

val TransferError.messageRes: StringResource
    get() =
        when (this) {
            TransferError.NotEnoughSpace -> Res.string.transfer_error_space
            TransferError.VerificationFailed -> Res.string.transfer_error_verification
            TransferError.ServerError -> Res.string.transfer_error_server
            TransferError.Network -> Res.string.transfer_error_network
            TransferError.CannotOpen -> Res.string.transfer_error_open
            TransferError.NotGguf -> Res.string.transfer_error_not_gguf
            TransferError.CannotSave -> Res.string.transfer_error_save
            TransferError.Unknown -> Res.string.transfer_error_unknown
        }

val CatalogProblem.messageRes: StringResource
    get() =
        when (this) {
            CatalogProblem.Offline -> Res.string.models_catalog_offline
            CatalogProblem.Unreadable -> Res.string.models_catalog_unreadable
            CatalogProblem.Unavailable -> Res.string.models_catalog_unavailable
        }

fun sectionTitleRes(sectionId: String): StringResource = when (sectionId) {
    "low" -> Res.string.models_tier_low
    "mid" -> Res.string.models_tier_mid
    "high" -> Res.string.models_tier_high
    "flagship" -> Res.string.models_tier_flagship
    else -> Res.string.models_section_all
}
