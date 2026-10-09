package com.abrarshakhi.mishti.features.models.data.catalog

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

sealed interface FetchResult {
    data class Fresh(
        val text: String,
        val etag: String?,
    ) : FetchResult

    data object NotModified : FetchResult
}

class CatalogFetchException(
    message: String,
) : Exception(message)

fun interface CatalogFetcher {
    suspend fun fetch(
        url: String,
        etag: String?,
    ): FetchResult
}

class KtorCatalogFetcher(
    private val client: HttpClient,
) : CatalogFetcher {
    override suspend fun fetch(
        url: String,
        etag: String?,
    ): FetchResult {
        val response =
            client.get(url) {
                timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
                etag?.let { header(HttpHeaders.IfNoneMatch, it) }
            }
        return when {
            response.status == HttpStatusCode.NotModified -> {
                FetchResult.NotModified
            }

            response.status.isSuccess() -> {
                FetchResult.Fresh(
                    text = response.bodyAsText(),
                    etag = response.headers[HttpHeaders.ETag],
                )
            }

            else -> {
                throw CatalogFetchException("The server answered ${response.status.value}.")
            }
        }
    }

    private companion object {
        const val REQUEST_TIMEOUT_MILLIS = 15_000L
    }
}

object CatalogUrls {
    const val MISTIR_BHANDAR =
        "https://cdn.jsdelivr.net/gh/AbrarShakhi/Mishti@main/hub/catalog.v1.json"
    const val POCKETPAL =
        "https://cdn.jsdelivr.net/gh/a-ghorbani/pocketpal-device-rules@main/rules.android.v2.json"
}
