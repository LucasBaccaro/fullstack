package com.baccaro.lucas.profile.domain

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String? = null,
    val name: String? = null,
    val profile_picture_url: String? = null,
    val english_level: String? = null
)
