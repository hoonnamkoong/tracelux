package com.tracelux.models

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val displayName: String) {
    KO("한국어"),
    EN("English")
}

@Serializable
enum class AppUnit(val displayName: String) {
    METRIC("Metric (m)"),
    IMPERIAL("Imperial (ft)")
}

@Serializable
data class AppOptions(
    val language: AppLanguage = AppLanguage.KO,
    val unit: AppUnit = AppUnit.METRIC
)
