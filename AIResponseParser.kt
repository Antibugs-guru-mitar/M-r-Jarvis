package com.jarvis.ai

import org.json.JSONArray
import org.json.JSONObject

/**
 * =========================================================
 * JARVIS — PERSONAL AI ASSISTANT
 * PHASE 3 — AI RESPONSE PARSER
 * =========================================================
 *
 * Responsibilities:
 *
 * 1. Parse raw AI responses.
 * 2. Detect normal conversational responses.
 * 3. Detect structured Android commands.
 * 4. Extract action parameters safely.
 * 5. Prevent malformed AI output from directly
 *    reaching the Android control layer.
 *
 * IMPORTANT:
 *
 * AIResponseParser does NOT execute any Android action.
 *
 * It only converts AI output into a structured object.
 *
 * Actual execution will be handled by:
 *
 * AI
 *   ↓
 * AIResponseParser
 *   ↓
 * CommandRouter
 *   ↓
 * AndroidBridge
 *   ↓
 * Native Android Action
 * =========================================================
 */

class AIResponseParser {


    /* =====================================================
       SUPPORTED ACTIONS
    ===================================================== */

    companion object {

        private const val ACTION_OPEN_APP =
            "OPEN_APP"

        private const val ACTION_FLASHLIGHT =
            "FLASHLIGHT"

        private const val ACTION_HOME =
            "HOME"

        private const val ACTION_OPEN_SETTINGS =
            "OPEN_SETTINGS"

        private const val ACTION_OPEN_URL =
            "OPEN_URL"

        private const val ACTION_STOP =
            "STOP"

        private const val ACTION_SPEAK =
            "SPEAK"

        private const val ACTION_NONE =
            "NONE"


        private const val MAX_TEXT_LENGTH =
            20_000

        private const val MAX_APP_NAME_LENGTH =
            100

        private const val MAX_URL_LENGTH =
            2_000
    }


    /* =====================================================
       MAIN PARSER
       ===================================================== */

    fun parse(
        rawResponse: String
    ): ParsedAIResponse {

        val cleanedResponse =
            rawResponse
                .trim()
                .take(MAX_TEXT_LENGTH)


        if (cleanedResponse.isBlank()) {

            return ParsedAIResponse(
                type = ResponseType.ERROR,
                text = "AI returned an empty response."
            )

        }


        /*
         * First try structured JSON.
         */

        val jsonResult =
            tryParseJson(
                cleanedResponse
            )


        if (jsonResult != null) {

            return jsonResult

        }


        /*
         * If the AI returned normal text instead of
         * structured JSON, treat it as conversation.
         */

        return ParsedAIResponse(
            type = ResponseType.CONVERSATION,
            text = cleanedResponse
        )

    }


    /* =====================================================
       JSON PARSER
       ===================================================== */

    private fun tryParseJson(
        response: String
    ): ParsedAIResponse? {

        val jsonText =
            extractJsonObject(
                response
            )
            ?: return null


        return try {

            val root =
                JSONObject(
                    jsonText
                )


            val action =
                root.optString(
                    "action",
                    ""
                )
                    .trim()
                    .uppercase()


            /*
             * No action means this is probably a
             * normal conversational JSON response.
             */

            if (action.isBlank()) {

                val text =
                    root.optString(
                        "text",
                        ""
                    ).trim()


                if (text.isBlank()) {

                    return null

                }


                return ParsedAIResponse(
                    type = ResponseType.CONVERSATION,
                    text = text
                )

            }


            when (action) {

                ACTION_OPEN_APP ->
                    parseOpenApp(
                        root
                    )


                ACTION_FLASHLIGHT ->
                    parseFlashlight(
                        root
                    )


                ACTION_HOME ->
                    parseHome(
                        root
                    )


                ACTION_OPEN_SETTINGS ->
                    parseOpenSettings(
                        root
                    )


                ACTION_OPEN_URL ->
                    parseOpenUrl(
                        root
                    )


                ACTION_STOP ->
                    parseStop(
                        root
                    )


                ACTION_SPEAK ->
                    parseSpeak(
                        root
                    )


                ACTION_NONE -> {

                    val text =
                        root.optString(
                            "text",
                            ""
                        ).trim()


                    ParsedAIResponse(
                        type =
                            ResponseType.CONVERSATION,
                        text =
                            text.ifBlank {
                                "I'm ready."
                            }
                    )

                }


                else -> {

                    ParsedAIResponse(
                        type =
                            ResponseType.UNKNOWN_COMMAND,
                        text =
                            "I understood the request, but that command is not supported yet.",
                        action =
                            action
                    )

                }

            }


        } catch (_: Exception) {

            null

        }

    }


    /* =====================================================
       OPEN APP
       ===================================================== */

    private fun parseOpenApp(
        root: JSONObject
    ): ParsedAIResponse {

        val app =
            root.optString(
                "app",
                ""
            )
                .trim()
                .take(
                    MAX_APP_NAME_LENGTH
                )


        if (app.isBlank()) {

            return ParsedAIResponse(
                type =
                    ResponseType.INVALID_COMMAND,
                text =
                    "The app name is missing.",
                action =
                    ACTION_OPEN_APP
            )

        }


        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                "Opening $app.",
            action =
                ACTION_OPEN_APP,
            parameters =
                mapOf(
                    "app" to app
                )
        )

    }


    /* =====================================================
       FLASHLIGHT
       ===================================================== */

    private fun parseFlashlight(
        root: JSONObject
    ): ParsedAIResponse {

        val value =
            root.optString(
                "value",
                ""
            )
                .trim()
                .uppercase()


        if (
            value != "ON" &&
            value != "OFF"
        ) {

            return ParsedAIResponse(
                type =
                    ResponseType.INVALID_COMMAND,
                text =
                    "Flashlight command must specify ON or OFF.",
                action =
                    ACTION_FLASHLIGHT
            )

        }


        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                if (value == "ON") {
                    "Turning the flashlight on."
                } else {
                    "Turning the flashlight off."
                },
            action =
                ACTION_FLASHLIGHT,
            parameters =
                mapOf(
                    "value" to value
                )
        )

    }


    /* =====================================================
       HOME
       ===================================================== */

    private fun parseHome(
        root: JSONObject
    ): ParsedAIResponse {

        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                "Going to the home screen.",
            action =
                ACTION_HOME
        )

    }


    /* =====================================================
       SETTINGS
       ===================================================== */

    private fun parseOpenSettings(
        root: JSONObject
    ): ParsedAIResponse {

        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                "Opening Android settings.",
            action =
                ACTION_OPEN_SETTINGS
        )

    }


    /* =====================================================
       OPEN URL
       ===================================================== */

    private fun parseOpenUrl(
        root: JSONObject
    ): ParsedAIResponse {

        val url =
            root.optString(
                "url",
                ""
            )
                .trim()
                .take(
                    MAX_URL_LENGTH
                )


        if (url.isBlank()) {

            return ParsedAIResponse(
                type =
                    ResponseType.INVALID_COMMAND,
                text =
                    "The URL is missing.",
                action =
                    ACTION_OPEN_URL
            )

        }


        /*
         * Only allow normal HTTP/HTTPS URLs.
         */

        if (
            !url.startsWith(
                "https://",
                ignoreCase = true
            ) &&
            !url.startsWith(
                "http://",
                ignoreCase = true
            )
        ) {

            return ParsedAIResponse(
                type =
                    ResponseType.INVALID_COMMAND,
                text =
                    "Only HTTP and HTTPS URLs are supported.",
                action =
                    ACTION_OPEN_URL
            )

        }


        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                "Opening the requested website.",
            action =
                ACTION_OPEN_URL,
            parameters =
                mapOf(
                    "url" to url
                )
        )

    }


    /* =====================================================
       STOP
       ===================================================== */

    private fun parseStop(
        root: JSONObject
    ): ParsedAIResponse {

        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                "Stopping the current operation.",
            action =
                ACTION_STOP
        )

    }


    /* =====================================================
       SPEAK
       ===================================================== */

    private fun parseSpeak(
        root: JSONObject
    ): ParsedAIResponse {

        val text =
            root.optString(
                "text",
                ""
            )
                .trim()
                .take(
                    MAX_TEXT_LENGTH
                )


        if (text.isBlank()) {

            return ParsedAIResponse(
                type =
                    ResponseType.INVALID_COMMAND,
                text =
                    "There is nothing to speak.",
                action =
                    ACTION_SPEAK
            )

        }


        return ParsedAIResponse(
            type =
                ResponseType.COMMAND,
            text =
                text,
            action =
                ACTION_SPEAK,
            parameters =
                mapOf(
                    "text" to text
                )
        )

    }


    /* =====================================================
       JSON OBJECT EXTRACTION
       ===================================================== */

    private fun extractJsonObject(
        response: String
    ): String? {

        /*
         * Sometimes AI models return:

         ```json
         {
             "action": "OPEN_APP",
             "app": "YouTube"
         }
         ```

         So we remove markdown code fences first.
         */

        val cleaned =
            response
                .replace(
                    "```json",
                    "",
                    ignoreCase = true
                )
                .replace(
                    "```",
                    ""
                )
                .trim()


        /*
         * Find the first JSON object.
         */

        val start =
            cleaned.indexOf("{")

        val end =
            cleaned.lastIndexOf("}")


        if (
            start == -1 ||
            end == -1 ||
            end <= start
        ) {

            return null

        }


        return cleaned.substring(
            start,
            end + 1
        )

    }


    /* =====================================================
       ARRAY SUPPORT
       ===================================================== */

    fun parseCommandArray(
        rawResponse: String
    ): List<ParsedAIResponse> {

        val results =
            mutableListOf<ParsedAIResponse>()


        try {

            val cleaned =
                rawResponse
                    .replace(
                        "```json",
                        "",
                        ignoreCase = true
                    )
                    .replace(
                        "```",
                        ""
                    )
                    .trim()


            val jsonArray =
                JSONArray(
                    cleaned
                )


            for (
                index in 0 until jsonArray.length()
            ) {

                val item =
                    jsonArray.optJSONObject(
                        index
                    )
                        ?: continue


                val parsed =
                    parse(
                        item.toString()
                    )


                results.add(
                    parsed
                )

            }

        } catch (_: Exception) {

            /*
             * Invalid array.
             * Return empty list rather than crashing JARVIS.
             */

        }


        return results

    }


    /* =====================================================
       VALIDATE COMMAND
       ===================================================== */

    fun isSafeCommand(
        response: ParsedAIResponse
    ): Boolean {

        if (
            response.type !=
            ResponseType.COMMAND
        ) {

            return false

        }


        return when (
            response.action.uppercase()
        ) {

            ACTION_OPEN_APP ->
                response.parameters
                    .containsKey("app")


            ACTION_FLASHLIGHT ->
                response.parameters
                    .containsKey("value")


            ACTION_HOME ->
                true


            ACTION_OPEN_SETTINGS ->
                true


            ACTION_OPEN_URL ->
                response.parameters
                    .containsKey("url")


            ACTION_STOP ->
                true


            ACTION_SPEAK ->
                response.parameters
                    .containsKey("text")


            else ->
                false

        }

    }

}


/* =========================================================
   RESPONSE TYPE
========================================================= */

enum class ResponseType {

    CONVERSATION,

    COMMAND,

    INVALID_COMMAND,

    UNKNOWN_COMMAND,

    ERROR

}


/* =========================================================
   PARSED AI RESPONSE
========================================================= */

data class ParsedAIResponse(

    val type: ResponseType,

    val text: String = "",

    val action: String = "",

    val parameters: Map<String, String> =
        emptyMap()

)
