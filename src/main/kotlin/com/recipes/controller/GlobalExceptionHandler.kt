package com.recipes.controller

import com.recipes.controller.response.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.beans.TypeMismatchException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestValueException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.slf4j.LoggerFactory

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(
        NoHandlerFoundException::class,
        NoSuchElementException::class
    )
    fun handleNotFoundException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val message = "Resource Not Found"

        logger.warn("$message ${ex.message}")

        return createErrorResponse(HttpStatus.NOT_FOUND, message, request)
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        TypeMismatchException::class,
        MissingRequestValueException::class,
        HttpMessageNotReadableException::class,
        ConstraintViolationException::class
    )
    fun handleBadRequestExceptions(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val message = when (ex) {
            is MethodArgumentNotValidException -> {
                ex.bindingResult.fieldErrors.joinToString(", ") {
                    "${it.field}: ${it.defaultMessage}"
                }
            }
            is MethodArgumentTypeMismatchException -> "Problem with parameter '${ex.name}'. ${ex.cause?.cause?.message}"
            is TypeMismatchException -> "Parameter '${ex.value}' should be of type ${ex.requiredType?.simpleName}"
            is MissingRequestValueException -> "Required parameter is missing"
            is HttpMessageNotReadableException -> "Invalid request body format"
            is ConstraintViolationException -> {
                ex.constraintViolations.joinToString(", ") {
                    "${it.propertyPath}: ${it.message}"
                }
            }
            else -> "Bad request"
        }

        logger.warn("${ex::class.simpleName} occurred", message)
        
        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("405 Method Not Allowed: {} for path {}",
            ex.message, request.getDescription(false))
        return createErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED,
            ex.message ?: "Method not allowed",
            request
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("409 Conflict - Database constraint violation: {}", ex.message, ex)
        return createErrorResponse(
            HttpStatus.CONFLICT,
            "Database constraint violation",
            request
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("500 Internal Server Error - Uncaught exception: ", ex)
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            request
        )
    }

    private fun createErrorResponse(
        status: HttpStatus,
        message: String,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    message = message,
                    path = request.getDescription(false).substring(4)
                )
            )
}