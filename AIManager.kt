package com.jarvis.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * =========================================================
 * JARVIS — PERSONAL AI ASSISTANT
 * PHASE 3 — AI MANAGER
 * =========================================================
 *
 * Responsibilities:
 *
 * 1. Send user messages to the AI backend/API.
 * 2. Receive the AI response.
 * 3. Return a clean response to the caller.
 * 4. Handle network errors safely.
 * 5. Keep API communication separate from UI.
 *
 * IMPORTANT:
 *
 * This class is designed so that the AI provider can be
 * changed later without rewriting the whole JARVIS system.
 *
 * Do NOT place a permanent production API key directly
 * inside the APK. For production, use a secure backend/
 * proxy server.
 * =========================================================
 */

class AIManager(
    private val context: Context
) {

    companion object {

        /*
         * =================================================
         * AI CONFIGURATION
         * =================================================
         *
         * These values are placeholders for now.
         *
         * Phase 3 testing can use a supported AI API.
         * The final production version should preferably
         * communicate through our own secure backend.
         */

        private const val DEFAULT_MODEL =
            "gemini-2.5-flash"

        private const val CONNECTION_TIMEOUT =
            15_000

        private const val READ_TIMEOUT =
            30_000

        private const val MAX_MESSAGE_LENGTH =
            8_000

        private const val MAX_RESPONSE_LENGTH =
            20_000
    }


    /* =====================================================
       AI SESSION STATE
    ===================================================== */

    @Volatile
    private var isProcessing = false


    @Volatile
    private var lastError: String? = null


    /* =====================================================
       PUBLIC STATE
    ===================================================== */

    fun isProcessing(): Boolean {
        return isProcessing
    }


    fun getLastError(): String? {
        return lastError
    }


    /* =====================================================
       MAIN AI REQUEST
       ===================================================== */

    suspend fun askAI(
        message: String
    ): AIResult {

        val cleanMessage =
            message
                .trim()
                .take(MAX_MESSAGE_LENGTH)


        if (cleanMessage.isBlank()) {

            return AIResult.Error(
                "Empty message."
            )

        }


        if (isProcessing) {

            return AIResult.Error(
                "JARVIS is already processing another request."
            )

        }


        isProcessing = true

        lastError = null


        return try {

            /*
             * Network work must NOT run on the
             * Android main/UI thread.
             */

            withContext(Dispatchers.IO) {

                performAIRequest(
                    cleanMessage
                )

            }

        } catch (error: Exception) {

            lastError =
                error.message
                    ?: "Unknown AI error."

            AIResult.Error(
                lastError
                    ?: "Unable to contact AI."
            )

        } finally {

            isProcessing = false

        }

    }


    /* =====================================================
       AI REQUEST ENGINE
    ===================================================== */

    private fun performAIRequest(
        message: String
    ): AIResult {

        /*
         * -------------------------------------------------
         * IMPORTANT
         * -------------------------------------------------
         *
         * API endpoint and authentication will be connected
         * after we decide the exact AI provider/backend.
         *
         * Keeping this separated means we can later connect:
         *
         * JARVIS
         *    ↓
         * Secure Backend
         *    ↓
         * AI Provider
         *
         * without changing the rest of JARVIS.
         */

        val apiKey =
            getApiKey()


        if (apiKey.isBlank()) {

            return AIResult.Error(
                "AI API key is not configured yet."
            )

        }


        /*
         * -------------------------------------------------
         * TEMPORARY GEMINI API ENDPOINT
         * -------------------------------------------------
         *
         * This is kept isolated so it can easily be changed
         * later if the API version/model changes.
         */

        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/" +
            "$DEFAULT_MODEL:generateContent?key=$apiKey"


        var connection:
            HttpURLConnection? = null


        return try {

            val url =
                URL(endpoint)


            connection =
                (url.openConnection()
                    as HttpURLConnection)


            connection.requestMethod =
                "POST"


            connection.connectTimeout =
                CONNECTION_TIMEOUT


            connection.readTimeout =
                READ_TIMEOUT


            connection.doInput =
                true


            connection.doOutput =
                true


            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )


            connection.setRequestProperty(
                "Accept",
                "application/json"
            )


            /*
             * =================================================
             * REQUEST BODY
             * =================================================
             *
             * We explicitly tell the AI that JARVIS is both:
             *
             * 1. A conversational assistant
             * 2. An Android command assistant
             *
             * The future CommandRouter will handle actual
             * Android execution.
             */

            val requestBody =
                JSONObject().apply {

                    put(
                        "contents",
                        org.json.JSONArray().apply {

                            put(
                                JSONObject().apply {

                                    put(
                                        "role",
                                        "user"
                                    )

                                    put(
                                        "parts",
                                        org.json.JSONArray().apply {

                                            put(
                                                JSONObject().apply {

                                                    put(
                                                        "text",
                                                        buildSystemPrompt(
                                                            message
                                                        )
                                                    )

                                                }
                                            )

                                        }
                                    )

                                }
                            )

                        }

                    )

                }.toString()


            connection.outputStream.use { output ->

                output.write(
                    requestBody.toByteArray(
                        Charsets.UTF_8
                    )
                )

                output.flush()

            }


            val responseCode =
                connection.responseCode


            if (
                responseCode !in
                200..299
            ) {

                val errorMessage =
                    readErrorResponse(
                        connection
                    )


                return AIResult.Error(
                    "AI server returned HTTP $responseCode. " +
                    errorMessage
                )

            }


            val responseText =
                connection.inputStream
                    .bufferedReader()
                    .use { reader ->
                        reader.readText()
                    }


            if (responseText.isBlank()) {

                return AIResult.Error(
                    "AI returned an empty response."
                )

            }


            parseAIResponse(
                responseText
            )


        } catch (error: IOException) {

            lastError =
                error.message
                    ?: "Network connection failed."

            AIResult.Error(
                "Network error: ${lastError}"
            )

        } catch (error: Exception) {

            lastError =
                error.message
                    ?: "Unexpected AI error."

            AIResult.Error(
                "AI request failed: ${lastError}"
            )

        } finally {

            connection?.disconnect()

        }

    }


    /* =====================================================
       SYSTEM PROMPT
    ===================================================== */

    private fun buildSystemPrompt(
        userMessage: String
    ): String {

        return """
            You are JARVIS, a professional personal AI
            assistant running on an Android device.

            Your responsibilities:

            1. Understand normal human conversation.
            2. Answer general questions clearly.
            3. Understand English, simple Roman Urdu,
               and mixed English/Roman Urdu commands.
            4. Understand Android-related commands.
            5. Never claim that an Android action was
               completed unless the Android command layer
               actually confirms it.
            6. Do not invent information.
            7. Keep responses natural and useful.
            8. When a request requires an Android action,
               clearly identify the required action so the
               CommandRouter can process it later.

            Supported future Android actions include:

            OPEN_APP
            FLASHLIGHT
            HOME
            OPEN_SETTINGS
            OPEN_URL
            STOP
            SPEAK

            Important:

            The AI itself does NOT directly control the phone.

            The architecture is:

            USER
              ↓
            JARVIS AI
              ↓
            STRUCTURED COMMAND
              ↓
            COMMAND ROUTER
              ↓
            ANDROID NATIVE ACTION

            User message:

            $userMessage
        """.trimIndent()

    }


    /* =====================================================
       RESPONSE PARSER
    ===================================================== */

    private fun parseAIResponse(
        responseText: String
    ): AIResult {

        return try {

            val root =
                JSONObject(
                    responseText
                )


            val candidates =
                root.optJSONArray(
                    "candidates"
                )


            if (
                candidates == null ||
                candidates.length() == 0
            ) {

                return AIResult.Error(
                    "AI response did not contain candidates."
                )

            }


            val firstCandidate =
                candidates.optJSONObject(0)
                    ?: return AIResult.Error(
                        "Invalid AI candidate."
                    )


            val content =
                firstCandidate.optJSONObject(
                    "content"
                )
                    ?: return AIResult.Error(
                        "AI response content is missing."
                    )


            val parts =
                content.optJSONArray(
                    "parts"
                )
                    ?: return AIResult.Error(
                        "AI response parts are missing."
                    )


            if (parts.length() == 0) {

                return AIResult.Error(
                    "AI returned no response parts."
                )

            }


            val textBuilder =
                StringBuilder()


            for (
                index in 0 until parts.length()
            ) {

                val part =
                    parts.optJSONObject(index)
                        ?: continue


                val text =
                    part.optString(
                        "text",
                        ""
                    )


                if (text.isNotBlank()) {

                    textBuilder.append(
                        text
                    )

                }

            }


            val finalText =
                textBuilder
                    .toString()
                    .trim()
                    .take(
                        MAX_RESPONSE_LENGTH
                    )


            if (finalText.isBlank()) {

                return AIResult.Error(
                    "AI returned an empty text response."
                )

            }


            AIResult.Success(
                finalText
            )


        } catch (error: Exception) {

            lastError =
                error.message
                    ?: "Unable to parse AI response."

            AIResult.Error(
                "AI response parsing failed."
            )

        }

    }


    /* =====================================================
       API KEY
    ===================================================== */

    private fun getApiKey(): String {

        /*
         * TEMPORARY TESTING ONLY
         *
         * We intentionally do NOT put an actual key here.
         *
         * Later this can be replaced with:
         *
         * Secure backend
         * BuildConfig
         * encrypted configuration
         * server-side API key
         *
         * Do NOT publish a real production API key inside
         * the GitHub repository or APK.
         */

        return ""

    }


    /* =====================================================
       ERROR RESPONSE READER
    ===================================================== */

    private fun readErrorResponse(
        connection: HttpURLConnection
    ): String {

        return try {

            val stream =
                connection.errorStream
                    ?: return "No error details."


            stream
                .bufferedReader()
                .use { reader ->
                    reader.readText()
                        .take(2_000)
                }

        } catch (_: Exception) {

            "No additional error details."

        }

    }


    /* =====================================================
       CLEANUP
    ===================================================== */

    fun clearError() {

        lastError = null

    }

}


/* =========================================================
   AI RESULT MODEL
========================================================= */

sealed class AIResult {

    data class Success(
        val text: String
    ) : AIResult()


    data class Error(
        val message: String
    ) : AIResult()

}
