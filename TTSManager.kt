package com.jarvis.ai

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TTSManager(
    private val context: Context
) {
p
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        initialize()
    }

    // ==========================================
    // INITIALIZE TTS
    // ==========================================

    private fun initialize() {

        textToSpeech = TextToSpeech(
            context.applicationContext
        ) { status ->

            if (status == TextToSpeech.SUCCESS) {

                isInitialized = true

                textToSpeech?.language =
                    Locale.US

                textToSpeech?.setSpeechRate(
                    0.95f
                )

                textToSpeech?.setPitch(
                    0.85f
                )

                textToSpeech?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {

                        override fun onStart(
                            utteranceId: String?
                        ) {
                            // Speech started
                        }

                        override fun onDone(
                            utteranceId: String?
                        ) {
                            // Speech completed
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(
                            utteranceId: String?
                        ) {
                            // Speech error
                        }
                    }
                )

            } else {

                isInitialized = false
            }
        }
    }

    // ==========================================
    // SPEAK
    // ==========================================

    fun speak(text: String) {

        if (text.isBlank()) {
            return
        }

        if (!isInitialized) {
            initialize()
            return
        }

        try {

            val params = Bundle().apply {

                putFloat(
                    TextToSpeech.Engine.KEY_PARAM_VOLUME,
                    1.0f
                )
            }

            textToSpeech?.speak(
                text.trim(),
                TextToSpeech.QUEUE_FLUSH,
                params,
                "JARVIS_${System.currentTimeMillis()}"
            )

        } catch (_: Exception) {

            // Ignore TTS failure
        }
    }

    // ==========================================
    // QUEUE SPEECH
    // ==========================================

    fun speakQueued(text: String) {

        if (text.isBlank()) {
            return
        }

        if (!isInitialized) {
            initialize()
            return
        }

        try {

            textToSpeech?.speak(
                text.trim(),
                TextToSpeech.QUEUE_ADD,
                null,
                "JARVIS_QUEUE_${System.currentTimeMillis()}"
            )

        } catch (_: Exception) {

            // Ignore TTS failure
        }
    }

    // ==========================================
    // STOP SPEAKING
    // ==========================================

    fun stop() {

        try {

            textToSpeech?.stop()

        } catch (_: Exception) {

            // Ignore
        }
    }

    // ==========================================
    // CHANGE LANGUAGE
    // ==========================================

    fun setLanguage(
        language: String
    ): Boolean {

        if (!isInitialized) {
            return false
        }

        return try {

            val locale = when (
                language.trim().lowercase()
            ) {

                "english",
                "en",
                "en-us" ->
                    Locale.US

                "uk",
                "en-gb" ->
                    Locale.UK

                "urdu",
                "ur",
                "ur-pk" ->
                    Locale(
                        "ur",
                        "PK"
                    )

                else ->
                    Locale.US
            }

            val result =
                textToSpeech?.setLanguage(
                    locale
                )

            result != TextToSpeech.LANG_NOT_SUPPORTED &&
            result != TextToSpeech.LANG_MISSING_DATA

        } catch (_: Exception) {

            false
        }
    }

    // ==========================================
    // SPEECH RATE
    // ==========================================

    fun setSpeechRate(
        rate: Float
    ) {

        if (!isInitialized) {
            return
        }

        try {

            val safeRate =
                rate.coerceIn(
                    0.5f,
                    2.0f
                )

            textToSpeech?.setSpeechRate(
                safeRate
            )

        } catch (_: Exception) {

            // Ignore
        }
    }

    // ==========================================
    // VOICE PITCH
    // ==========================================

    fun setPitch(
        pitch: Float
    ) {

        if (!isInitialized) {
            return
        }

        try {

            val safePitch =
                pitch.coerceIn(
                    0.5f,
                    2.0f
                )

            textToSpeech?.setPitch(
                safePitch
            )

        } catch (_: Exception) {

            // Ignore
        }
    }

    // ==========================================
    // IS SPEAKING
    // ==========================================

    fun isSpeaking(): Boolean {

        return try {

            textToSpeech?.isSpeaking == true

        } catch (_: Exception) {

            false
        }
    }

    // ==========================================
    // RELEASE
    // ==========================================

    fun shutdown() {

        try {

            textToSpeech?.stop()
            textToSpeech?.shutdown()

        } catch (_: Exception) {

            // Ignore
        }

        textToSpeech = null
        isInitialized = false
    }
}
