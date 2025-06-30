package com.baccaro.lucas.progress.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressReport(
    @SerialName("session_date") val session_date: String, // Añadido como String
    @SerialName("duration_minutes") val duration_minutes: Int, // Añadido
    @SerialName("topics_discussed") val topics_discussed: List<String>,
    @SerialName("new_vocabulary") val new_vocabulary: List<String>,
    @SerialName("grammar_points")  val grammar_points: List<GrammarPoint>,
    @SerialName("ai_summary")  val ai_summary: String,
    @SerialName("suggested_level")  val suggested_level: String
)

@Serializable
data class GrammarPoint(
    @SerialName("point") val point: String,
    @SerialName("examples") val examples: List<String>,
    @SerialName("status") val status: String
)


@Serializable
data class PartialProgressReport(
    val topics_discussed: List<String>,
    val new_vocabulary: List<String>,
    val grammar_points: List<GrammarPoint>,
    val ai_summary: String,
    val suggested_level: String
)
