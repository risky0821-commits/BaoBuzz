package com.msdc.baobuzz.models

import kotlinx.serialization.Serializable

@Serializable
data class Venue(
    val id: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val surface: String? = null,
    val image: String? = null
)
