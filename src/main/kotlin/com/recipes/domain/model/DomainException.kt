package com.recipes.domain.model

sealed class DomainException(override val message: String) : Exception(message) {
    class NotFoundException(message: String) : DomainException(message)
    class ValidationException(message: String) : DomainException(message)
}