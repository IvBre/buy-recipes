package com.recipes.controller.response

import com.recipes.domain.model.DomainError
import com.recipes.domain.model.DomainResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

private val logger = LoggerFactory.getLogger(DomainResult::class.java)

fun <T, R> DomainResult<T>.toResponseEntity(
    successStatus: HttpStatus = HttpStatus.OK,
    transform: (T) -> R
): ResponseEntity<R> {
    return when (this) {
        is DomainResult.Success -> ResponseEntity.status(successStatus).body(transform(data))
        is DomainResult.Error -> {
            val path = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
                ?.request?.requestURI

            val status = when (error) {
                is DomainError.NotFound -> {
                    logger.warn("Resource not found: {}", this.error.message)
                    HttpStatus.NOT_FOUND
                }
                is DomainError.ValidationError -> {
                    logger.warn("Validation error: {}", this.error.message)
                    HttpStatus.BAD_REQUEST
                }
                is DomainError.BusinessError -> {
                    logger.error("Business error occurred: {}", this.error.message)
                    HttpStatus.INTERNAL_SERVER_ERROR
                }
            }

            @Suppress("UNCHECKED_CAST")
            ResponseEntity
                .status(status)
                .body(ErrorResponse(
                    status = status.value(),
                    message = error.message,
                    path = path
                ) as R)
        }
    }
}