package com.recipes.domain.model

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Error(val error: DomainError) : DomainResult<Nothing>
}

sealed class DomainError(open val message: String) {
    data class NotFound(override val message: String) : DomainError(message)
    data class ValidationError(override val message: String) : DomainError(message)
    data class BusinessError(override val message: String) : DomainError(message)
}
