package com.baccaro.lucas.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface KtorApi {
    val client: HttpClient

    companion object {
        const val BASE_URL = "https://8131-181-116-177-35.ngrok-free.app/"
    }
}

class KtorApiImpl : KtorApi {
    override val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
}
