package com.abrarshakhi.mishti.models.ui

import com.abrarshakhi.mishti.feature.model.resources.filter_all
import com.abrarshakhi.mishti.feature.model.resources.filter_balanced
import com.abrarshakhi.mishti.feature.model.resources.filter_coding
import com.abrarshakhi.mishti.feature.model.resources.filter_fast
import com.abrarshakhi.mishti.feature.model.resources.filter_smart
import com.abrarshakhi.mishti.feature.model.resources.fit_fits
import com.abrarshakhi.mishti.feature.model.resources.fit_tight
import com.abrarshakhi.mishti.feature.model.resources.fit_too_big
import com.abrarshakhi.mishti.feature.model.resources.models_catalog_offline
import com.abrarshakhi.mishti.feature.model.resources.models_catalog_unavailable
import com.abrarshakhi.mishti.feature.model.resources.models_catalog_unreadable
import com.abrarshakhi.mishti.feature.model.resources.models_origin_imported
import com.abrarshakhi.mishti.feature.model.resources.models_section_all
import com.abrarshakhi.mishti.feature.model.resources.models_source_mistir_bhandar
import com.abrarshakhi.mishti.feature.model.resources.models_source_pocketpal
import com.abrarshakhi.mishti.feature.model.resources.models_tier_flagship
import com.abrarshakhi.mishti.feature.model.resources.models_tier_high
import com.abrarshakhi.mishti.feature.model.resources.models_tier_low
import com.abrarshakhi.mishti.feature.model.resources.models_tier_mid
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_network
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_not_gguf
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_open
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_save
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_server
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_space
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_unknown
import com.abrarshakhi.mishti.feature.model.resources.transfer_error_verification
import com.abrarshakhi.mishti.models.domain.model.CatalogProblem
import com.abrarshakhi.mishti.models.domain.model.CatalogSource
import com.abrarshakhi.mishti.models.domain.model.MemoryFit
import com.abrarshakhi.mishti.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.models.domain.model.TransferError
import org.jetbrains.compose.resources.StringResource
import com.abrarshakhi.mishti.feature.model.resources.Res as FeatureModelRes

val CatalogSource.labelRes: StringResource
    get() =
        when (this) {
            CatalogSource.MishtirBhandar -> FeatureModelRes.string.models_source_mistir_bhandar
            CatalogSource.PocketPal -> FeatureModelRes.string.models_source_pocketpal
        }

val ModelOrigin.labelRes: StringResource
    get() =
        when (this) {
            ModelOrigin.MishtirBhandar -> FeatureModelRes.string.models_source_mistir_bhandar
            ModelOrigin.PocketPal -> FeatureModelRes.string.models_source_pocketpal
            ModelOrigin.Imported -> FeatureModelRes.string.models_origin_imported
        }

val CatalogFilter.labelRes: StringResource
    get() =
        when (this) {
            CatalogFilter.All -> FeatureModelRes.string.filter_all
            CatalogFilter.Fast -> FeatureModelRes.string.filter_fast
            CatalogFilter.Balanced -> FeatureModelRes.string.filter_balanced
            CatalogFilter.Smart -> FeatureModelRes.string.filter_smart
            CatalogFilter.Coding -> FeatureModelRes.string.filter_coding
        }

val MemoryFit.labelRes: StringResource?
    get() =
        when (this) {
            MemoryFit.Fits -> FeatureModelRes.string.fit_fits
            MemoryFit.Tight -> FeatureModelRes.string.fit_tight
            MemoryFit.TooBig -> FeatureModelRes.string.fit_too_big
            MemoryFit.Unknown -> null
        }

val TransferError.messageRes: StringResource
    get() =
        when (this) {
            TransferError.NotEnoughSpace -> FeatureModelRes.string.transfer_error_space
            TransferError.VerificationFailed -> FeatureModelRes.string.transfer_error_verification
            TransferError.ServerError -> FeatureModelRes.string.transfer_error_server
            TransferError.Network -> FeatureModelRes.string.transfer_error_network
            TransferError.CannotOpen -> FeatureModelRes.string.transfer_error_open
            TransferError.NotGguf -> FeatureModelRes.string.transfer_error_not_gguf
            TransferError.CannotSave -> FeatureModelRes.string.transfer_error_save
            TransferError.Unknown -> FeatureModelRes.string.transfer_error_unknown
        }

val CatalogProblem.messageRes: StringResource
    get() =
        when (this) {
            CatalogProblem.Offline -> FeatureModelRes.string.models_catalog_offline
            CatalogProblem.Unreadable -> FeatureModelRes.string.models_catalog_unreadable
            CatalogProblem.Unavailable -> FeatureModelRes.string.models_catalog_unavailable
        }

fun sectionTitleRes(sectionId: String): StringResource = when (sectionId) {
    "low" -> FeatureModelRes.string.models_tier_low
    "mid" -> FeatureModelRes.string.models_tier_mid
    "high" -> FeatureModelRes.string.models_tier_high
    "flagship" -> FeatureModelRes.string.models_tier_flagship
    else -> FeatureModelRes.string.models_section_all
}
