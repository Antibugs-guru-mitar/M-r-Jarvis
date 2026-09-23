package com.jarvis.ai

import android.content.Context

/**
 * =========================================================
 * JARVIS — PERSONAL AI ASSISTANT
 * PHASE 3 — COMMAND ROUTER
 * =========================================================
 *
 * Flow:
 *
 * User
 *   ↓
 * AIManager
 *   ↓
 * AIResponseParser
 *   ↓
 * CommandRouter
 *   ↓
 * AndroidBridge / Native Android
 *
 * IMPORTANT:
 *
 * AI kabhi directly Android action execute nahi karta.
 *
 * CommandRouter:
 * 1. Parsed command receive karta hai
 * 2. Command validate karta hai
 * 3. Allowed action identify karta hai
 * 4. Required native component call karta hai
 * 5. Result return karta hai
 *
 * Future:
 * - Voice authentication
 * - Permission checks
 * - Biometric confirmation
 * - Sensitive command protection
 * yahan add ki ja sakti hai.
 * =========================================================
 */

class CommandRouter(
    private val context: Context
) {

    private val appLauncher =
        AppLauncher(context)

    private val systemActions =
        SystemActions(context)

    private val ttsManager =
        TTSManager(context)


    /* =====================================================
       ROUTE COMMAND
       ===================================================== */

    fun route(
        response: ParsedAIResponse
    ): CommandResult {

        /*
         * Conversation ko router execute nahi karega.
         */

        if (
            response.type ==
            ResponseType.CONVERSATION
        ) {

            return CommandResult(
                success = true,
                message = response.text,
                action = "CONVERSATION"
            )

        }


        /*
         * Invalid AI command.
         */

        if (
            response.type !=
            ResponseType.COMMAND
        ) {

            return CommandResult(
                success = false,
                message =
                    response.text.ifBlank {
                        "I could not understand that command."
                    },
                action = response.action
            )

        }


        /*
         * Extra safety validation.
         */

        if (
            !AIResponseParser()
                .isSafeCommand(response)
        ) {

            return CommandResult(
                success = false,
                message =
                    "This command is not allowed.",
                action = response.action
            )

        }


        /*
         * Route the command.
         */

        return when (
            response.action
                .trim()
                .uppercase()
        ) {

            ACTION_OPEN_APP ->
                openApp(response)


            ACTION_FLASHLIGHT ->
                flashlight(response)


            ACTION_HOME ->
                goHome()


            ACTION_OPEN_SETTINGS ->
                openSettings()


            ACTION_OPEN_URL ->
                openUrl(response)


            ACTION_STOP ->
                stopCurrentOperation()


            ACTION_SPEAK ->
                speak(response)


            else ->
                CommandResult(
                    success = false,
                    message =
                        "That command is not supported yet.",
                    action =
                        response.action
                )

        }

    }


    /* =====================================================
       OPEN APP
       ===================================================== */

    private fun openApp(
        response: ParsedAIResponse
    ): CommandResult {

        val app =
            response.parameters["app"]
                ?.trim()


        if (app.isNullOrBlank()) {

            return CommandResult(
                success = false,
                message =
                    "Please specify an app name.",
                action =
                    ACTION_OPEN_APP
            )

        }


        return try {

            val opened =
                appLauncher.openApp(
                    app
                )


            if (opened) {

                CommandResult(
                    success = true,
                    message =
                        "Opening $app.",
                    action =
                        ACTION_OPEN_APP,
                    parameters =
                        mapOf(
                            "app" to app
                        )
                )

            } else {

                CommandResult(
                    success = false,
                    message =
                        "I could not find or open $app.",
                    action =
                        ACTION_OPEN_APP,
                    parameters =
                        mapOf(
                            "app" to app
                        )
                )

            }

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "There was a problem opening $app.",
                action =
                    ACTION_OPEN_APP
            )

        }

    }


    /* =====================================================
       FLASHLIGHT
       ===================================================== */

    private fun flashlight(
        response: ParsedAIResponse
    ): CommandResult {

        val value =
            response.parameters["value"]
                ?.trim()
                ?.uppercase()


        if (
            value != "ON" &&
            value != "OFF"
        ) {

            return CommandResult(
                success = false,
                message =
                    "Please specify flashlight ON or OFF.",
                action =
                    ACTION_FLASHLIGHT
            )

        }


        val systemCommand =
            if (value == "ON") {
                "FLASHLIGHT_ON"
            } else {
                "FLASHLIGHT_OFF"
            }


        return try {

            val success =
                systemActions.execute(
                    systemCommand
                )


            if (success) {

                CommandResult(
                    success = true,
                    message =
                        if (value == "ON") {
                            "Flashlight turned on."
                        } else {
                            "Flashlight turned off."
                        },
                    action =
                        ACTION_FLASHLIGHT,
                    parameters =
                        mapOf(
                            "value" to value
                        )
                )

            } else {

                CommandResult(
                    success = false,
                    message =
                        "I could not control the flashlight.",
                    action =
                        ACTION_FLASHLIGHT
                )

            }

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "Flashlight control failed.",
                action =
                    ACTION_FLASHLIGHT
            )

        }

    }


    /* =====================================================
       HOME SCREEN
       ===================================================== */

    private fun goHome(): CommandResult {

        return try {

            val success =
                systemActions.execute(
                    "HOME"
                )


            if (success) {

                CommandResult(
                    success = true,
                    message =
                        "Going to the home screen.",
                    action =
                        ACTION_HOME
                )

            } else {

                CommandResult(
                    success = false,
                    message =
                        "I could not open the home screen.",
                    action =
                        ACTION_HOME
                )

            }

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "Home screen command failed.",
                action =
                    ACTION_HOME
            )

        }

    }


    /* =====================================================
       SETTINGS
       ===================================================== */

    private fun openSettings(): CommandResult {

        return try {

            val success =
                systemActions.execute(
                    "OPEN_SETTINGS"
                )


            if (success) {

                CommandResult(
                    success = true,
                    message =
                        "Opening Android settings.",
                    action =
                        ACTION_OPEN_SETTINGS
                )

            } else {

                CommandResult(
                    success = false,
                    message =
                        "I could not open Android settings.",
                    action =
                        ACTION_OPEN_SETTINGS
                )

            }

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "Settings command failed.",
                action =
                    ACTION_OPEN_SETTINGS
            )

        }

    }


    /* =====================================================
       OPEN URL
       ===================================================== */

    private fun openUrl(
        response: ParsedAIResponse
    ): CommandResult {

        val url =
            response.parameters["url"]
                ?.trim()


        if (url.isNullOrBlank()) {

            return CommandResult(
                success = false,
                message =
                    "The website address is missing.",
                action =
                    ACTION_OPEN_URL
            )

        }


        /*
         * URL execution will be implemented through
         * the controlled Android layer.
         *
         * For now this router validates the URL and
         * delegates it to UrlLauncher.
         */

        return try {

            val launcher =
                UrlLauncher(context)

            val opened =
                launcher.openUrl(
                    url
                )


            if (opened) {

                CommandResult(
                    success = true,
                    message =
                        "Opening the requested website.",
                    action =
                        ACTION_OPEN_URL,
                    parameters =
                        mapOf(
                            "url" to url
                        )
                )

            } else {

                CommandResult(
                    success = false,
                    message =
                        "I could not open that website.",
                    action =
                        ACTION_OPEN_URL
                )

            }

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "Website opening failed.",
                action =
                    ACTION_OPEN_URL
            )

        }

    }


    /* =====================================================
       STOP
       ===================================================== */

    private fun stopCurrentOperation(): CommandResult {

        return try {

            ttsManager.stop()

            CommandResult(
                success = true,
                message =
                    "Stopped.",
                action =
                    ACTION_STOP
            )

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "I could not stop the current operation.",
                action =
                    ACTION_STOP
            )

        }

    }


    /* =====================================================
       SPEAK
       ===================================================== */

    private fun speak(
        response: ParsedAIResponse
    ): CommandResult {

        val text =
            response.parameters["text"]
                ?.trim()


        if (text.isNullOrBlank()) {

            return CommandResult(
                success = false,
                message =
                    "There is nothing to say.",
                action =
                    ACTION_SPEAK
            )

        }


        return try {

            ttsManager.speak(
                text
            )


            CommandResult(
                success = true,
                message =
                    text,
                action =
                    ACTION_SPEAK,
                parameters =
                    mapOf(
                        "text" to text
                    )
            )

        } catch (error: Exception) {

            CommandResult(
                success = false,
                message =
                    "I could not speak the response.",
                action =
                    ACTION_SPEAK
            )

        }

    }


    /* =====================================================
       COMMAND CONSTANTS
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
    }

}


/* =========================================================
   COMMAND RESULT
========================================================= */

data class CommandResult(

    val success: Boolean,

    val message: String,

    val action: String = "",

    val parameters: Map<String, String> =
        emptyMap()

)
