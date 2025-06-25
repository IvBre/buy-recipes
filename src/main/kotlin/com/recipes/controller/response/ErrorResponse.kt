package com.recipes.controller.response

import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val path: String? = null
)