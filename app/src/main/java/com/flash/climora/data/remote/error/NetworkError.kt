package com.flash.climora.data.remote.error

sealed class NetworkError {

    object InvalidResponse : NetworkError()

    data class InvalidStatusCode(val code: Int) : NetworkError()

    object RateLimitExceeded : NetworkError()

    data class Network(val throwable: Throwable) : NetworkError()

    data class Decoding(val throwable: Throwable) : NetworkError()
}