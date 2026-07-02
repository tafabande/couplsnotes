package com.example.noteshare.config

enum class AppEnvironment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

object EnvironmentConfig {
    var current: AppEnvironment = AppEnvironment.DEVELOPMENT

    val firestoreCollection: String
        get() = when (current) {
            AppEnvironment.DEVELOPMENT -> "notes_dev"
            AppEnvironment.STAGING -> "notes_stage"
            AppEnvironment.PRODUCTION -> "notes_prod"
        }

    val isAnalyticsEnabled: Boolean
        get() = current == AppEnvironment.PRODUCTION
}
