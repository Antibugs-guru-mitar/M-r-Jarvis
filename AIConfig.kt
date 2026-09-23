package com.jarvis.ai

/**
 * =========================================================
 * JARVIS — PERSONAL AI ASSISTANT
 * PHASE 3 — AI CONFIGURATION
 * =========================================================
 *
 * AI ki configuration ko central location par rakhta hai.
 *
 * IMPORTANT:
 *
 * API key source code mein hard-code nahi ki gayi.
 *
 * Expected BuildConfig fields:
 *
 * BuildConfig.GEMINI_API_KEY
 *
 * Ye value Gradle/local.properties se provide ki jayegi.
 *
 * Production version mein API key ko APK ke andar rakhna
 * secure solution nahi hai. Production mein backend/proxy
 * use karna better hoga.
 * =========================================================
 */

object AIConfig {


    /* =====================================================
       API KEY
    ===================================================== */

    val apiKey: String
        get() {

            return try {

                BuildConfig.GEMINI_API_KEY
                    .trim()

            } catch (_: Exception) {

                ""

            }

        }


    /* =====================================================
       API BASE URL
    ===================================================== */

    const val GEMINI_BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta"


    /* =====================================================
       DEFAULT MODEL
    ===================================================== */

    const val DEFAULT_MODEL =
        "gemini-2.5-flash"


    /* =====================================================
       CONNECTION SETTINGS
    ===================================================== */

    const val CONNECT_TIMEOUT_MS =
        15_000

    const val READ_TIMEOUT_MS =
        30_000

    const val WRITE_TIMEOUT_MS =
        30_000


    /* =====================================================
       RESPONSE LIMIT
    ===================================================== */

    const val MAX_RESPONSE_LENGTH =
        20_000


    /* =====================================================
       CONVERSATION LIMIT
    ===================================================== */

    const val MAX_CONTEXT_MESSAGES =
        20


    /* =====================================================
       API KEY CHECK
    ===================================================== */

    fun hasApiKey(): Boolean {

        return apiKey.isNotBlank()

    }


    /* =====================================================
       API ENDPOINT
    ===================================================== */

    fun getGenerateContentUrl(): String {

        if (
            apiKey.isBlank()
        ) {

            return ""

        }


        return "$GEMINI_BASE_URL/models/" +
                "$DEFAULT_MODEL:generateContent" +
                "?key=$apiKey"

    }


    /* =====================================================
       CONFIGURATION STATUS
    ===================================================== */

    fun getStatus(): AIConfigStatus {

        return if (
            hasApiKey()
        ) {

            AIConfigStatus(
                configured = true,
                model =
                    DEFAULT_MODEL,
                message =
                    "AI configuration is ready."
            )

        } else {

            AIConfigStatus(
                configured = false,
                model =
                    DEFAULT_MODEL,
                message =
                    "Gemini API key is not configured."
            )

        }

    }


    /* =====================================================
       DEBUG-SAFE INFORMATION
    ===================================================== */

    fun getSafeInfo():
        Map<String, String> {

        return mapOf(

            "provider" to
                "Google Gemini",

            "model" to
                DEFAULT_MODEL,

            "configured" to
                hasApiKey().toString(),

            "endpoint" to
                GEMINI_BASE_URL,

            "apiKey" to
                if (hasApiKey()) {
                    "CONFIGURED"
                } else {
                    "NOT_CONFIGURED"
                }

        )

    }

}


/* =========================================================
   AI CONFIG STATUS
========================================================= */

data class AIConfigStatus(

    val configured: Boolean,

    val model: String,

    val message: String

)
