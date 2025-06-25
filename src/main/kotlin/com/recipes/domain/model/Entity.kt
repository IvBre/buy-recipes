package com.recipes.domain.model

abstract class Entity<ID : Id<*>> {
    private var _id: ID? = null

    val id: ID
        get() = _id ?: throw IllegalStateException("Entity ID has not been assigned yet")

    fun assignId(id: ID) {
        if (_id != null) {
            throw IllegalStateException("ID has already been assigned")
        }
        _id = id
    }

    fun hasId(): Boolean = _id != null
}
