package com.flash.climora.domain.error

sealed class DomainError {

    object NetworkUnavailable : DomainError()

    object RateLimitExceeded : DomainError()

    object InvalidRequest : DomainError()

    object LocationUnavailable : DomainError()

    object Unknown : DomainError()
}