package com.arin.pocketcompanion.data.remote

import com.arin.pocketcompanion.domain.InterpretRequest
import com.arin.pocketcompanion.domain.InterpretResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class CompanionApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun interpret(request: InterpretRequest): Result<InterpretResponse> {
        val remoteResult = runCatching {
            val response = client.post("$baseUrl/api/v1/interpret") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            check(response.status.isSuccess()) { "Server said ${response.status}" }
            response.body<InterpretResponse>()
        }

        return if (remoteResult.isSuccess) {
            remoteResult
        } else {
            // Fallback to local on-device interpreter when backend server is offline
            Result.success(OfflineInterpreter.interpret(request))
        }
    }
}
