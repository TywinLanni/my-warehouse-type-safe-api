package com.github.tywinlanni.mywarehouse.core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {  }

private const val MY_WAREHOUSE_API = "https://api.moysklad.ru/api/remap/1.2"

class MyWarehouseImpl(
    private val myWarehouseLogin: String,
    private val myWarehousePassword: String
) : MyWarehouse.Mutation, MyWarehouse.Retrieve {
    private val tokenBuffer = mutableListOf<BearerTokens>()

    override val client = HttpClient(CIO) {
        install(plugin = DefaultRequest) {
            url(MY_WAREHOUSE_API)
        }
        install(plugin = ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        install(plugin = HttpRequestRetry) {
            retryOnException(maxRetries = 2, retryOnTimeout = true)

            exponentialDelay()
            this.modifyRequest {
                logger.warn { "Retry to connect to Mywarehause api" }
            }
        }
        install(plugin = HttpTimeout) {
            requestTimeoutMillis = 30_000L
            connectTimeoutMillis = 30_000L
        }
        install(plugin = Auth) {
            bearer {
                refreshTokens {
                    login()
                    tokenBuffer.last()
                }
            }
        }
        install(plugin = ContentEncoding) {
            gzip()
        }
    }

    private val tokenClient = HttpClient(CIO) {
        install(plugin = DefaultRequest) {
            url(MY_WAREHOUSE_API)
        }
        install(plugin = ContentNegotiation) {
            json()
        }
        install(plugin = Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(
                        username = myWarehouseLogin,
                        password = myWarehousePassword,
                    )
                }
            }
        }
        install(plugin = HttpRequestRetry) {
            retryOnException(maxRetries = 3, retryOnTimeout = true)

            exponentialDelay()
            this.modifyRequest {
                logger.warn { "Retry to connect to Mywarehause security" }
            }
        }
        install(plugin = HttpTimeout) {
            requestTimeoutMillis = 15_000L
            connectTimeoutMillis = 15_000L
        }
    }

    private suspend fun login() {
        tokenClient.post("/security/token").body<Token>()
            .let { token ->
                tokenBuffer.add(
                    BearerTokens(accessToken = token.access_token, refreshToken = "")
                )

                if (tokenBuffer.size > 5)
                    tokenBuffer.removeFirst()
            }
    }

    private data class Token(
        val access_token: String,
    )
}
