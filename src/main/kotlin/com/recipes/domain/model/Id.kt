package com.recipes.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

abstract class Id<T> (@JsonValue val value: Long) {
    init {
        require(value > 0) { "ID must be positive" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Id<*>
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}

class CartId @JsonCreator constructor(value: Long) : Id<Cart>(value) {
    constructor(value: String) : this(value.toLong())
}
class CartItemId @JsonCreator constructor(value: Long) : Id<CartItem>(value)
class ProductId @JsonCreator constructor(value: Long) : Id<Product>(value)
class RecipeId @JsonCreator constructor(value: Long) : Id<Recipe>(value) {
    constructor(value: String) : this(value.toLong())
}
