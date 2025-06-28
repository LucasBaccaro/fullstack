package com.baccaro.lucas.home.domain

import kotlinx.serialization.Serializable

@Serializable
data class Topic(
    val id: Int,
    val title: String,
    val description: String? = null,
    val prompt_context: String,
    val difficulty_level: String? = null,
    val created_at: String? = null
) 