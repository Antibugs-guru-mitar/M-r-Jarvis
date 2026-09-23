package com.jarvis.ai

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

/**
 * ============================================================
 * JARVIS - Android Bridge
 * Phase 2
 * ============================================================
 *
 * Purpose:
 *
 * JavaScript  <------>  Android Kotlin
 *
 * Existing JARVIS Web UI can communicate with the Android
 * native layer through the object:
 *
 *      window.JarvisAndroid
 *
 * Example from JavaScript:
 *
 *      JarvisAndroid.openApp("YouTube");
 *
 * AndroidBridge receives the request and passes it to the
 * appropriate native component.
 *
 * IMPORTANT:
 * This bridge does NOT give JavaScript unrestricted Android
 * access. Commands are explicitly exposed one by one.
 * ============================================================
 */

class AndroidBridge(
    private val context: Context
) {

    // ========================================================
    // BASIC INFORMATION
    // ========================================================

    /**
     * Returns the current Android platform version.
     */
    @JavascriptInterface
    fun getAndroidVersion(): String {

        return android.os.Build.VERSION.RELEASE
    }


    /**
     * Returns the device manufacturer.
     */
    @JavascriptInterface
    fun getDeviceManufacturer(): String {

        return android.os.Build.MANUFACTURER
    }


    /**
     * Returns the device model.
     */
    @JavascriptInterface
    fun getDeviceModel(): String {

        return android.os.Build.MODEL
    }


    /**
     * Returns the Android SDK level.
     */
    @JavascriptInterface
    fun getAndroidSdk(): Int {

        return android.os.Build.VERSION.SDK_INT
    }


    // ========================================================
    // JARVIS CONNECTION STATUS
    // ========================================================

    /**
     * Called by JavaScript when the Android bridge is ready.
     */
    @JavascriptInterface
    fun androidReady() {

        showToast(
            "JARVIS Android Core Connected"
        )
    }


    /**
     * Sends a simple test response back to the UI.
     *
     * This is useful while testing the bridge before we add
     * real phone actions.
     */
    @JavascriptInterface
    fun ping(): String {

        return "JARVIS_ANDROID_BRIDGE_ONLINE"
    }


    // ========================================================
    // TEXT / UI COMMUNICATION
    // ========================================================

    /**
     * Allows JavaScript to request a native toast.
     *
     * Example:
     *
     * JarvisAndroid.showToast("Hello from JARVIS");
     */
    @JavascriptInterface
    fun showToast(message: String?) {

        if (message.isNullOrBlank()) {
            return
        }

        showToast(message)
    }


    // ========================================================
    // APP COMMANDS
    // ========================================================

    /**
     * Opens an Android application.
     *
     * NOTE:
     * The actual application-launching logic will be moved
     * into AppLauncher.kt.
     *
     * This bridge only forwards the request.
     */
    @JavascriptInterface
    fun openApp(appName: String?): Boolean {

        if (appName.isNullOrBlank()) {
            return false
        }

        return try {

            AppLauncher(context).openApp(
                appName.trim()
            )

        } catch (error: Exception) {

            showToast(
                "Unable to open $appName"
            )

            false
        }
    }


    // ========================================================
    // SYSTEM ACTIONS
    // ========================================================

    /**
     * Controls supported JARVIS system actions.
     *
     * Example JavaScript:
     *
     * JarvisAndroid.systemAction("FLASHLIGHT_ON");
     */
    @JavascriptInterface
    fun systemAction(action: String?): Boolean {

        if (action.isNullOrBlank()) {
            return false
        }

        return try {

            SystemActions(context).execute(
                action.trim().uppercase()
            )

        } catch (error: Exception) {

            showToast(
                "System action failed"
            )

            false
        }
    }


    // ========================================================
    // TEXT TO SPEECH
    // ========================================================

    /**
     * Makes JARVIS speak through Android Text-to-Speech.
     *
     * Example:
     *
     * JarvisAndroid.speak("Opening YouTube");
     */
    @JavascriptInterface
    fun speak(text: String?): Boolean {

        if (text.isNullOrBlank()) {
            return false
        }

        return try {

            TTSManager(context).speak(
                text.trim()
            )

            true

        } catch (error: Exception) {

            false
        }
    }


    /**
     * Stops current JARVIS speech.
     */
    @JavascriptInterface
    fun stopSpeaking() {

        try {

            TTSManager(context).stop()

        } catch (_: Exception) {

            // Ignore cleanup errors.
        }
    }


    // ========================================================
    // SERVICE CONTROL
    // ========================================================

    /**
     * Requests JARVIS voice service to start.
     *
     * The actual foreground-service implementation will be
     * completed inside JarvisService.kt.
     */
    @JavascriptInterface
    fun startJarvisService(): Boolean {

        return try {

            JarvisService.start(
                context
            )

            true

        } catch (error: Exception) {

            showToast(
                "JARVIS service could not start"
            )

            false
        }
    }


    /**
     * Requests JARVIS voice service to stop.
     */
    @JavascriptInterface
    fun stopJarvisService(): Boolean {

        return try {

            JarvisService.stop(
                context
            )

            true

        } catch (error: Exception) {

            false
        }
    }


    // ========================================================
    // SERVICE STATUS
    // ========================================================

    /**
     * Returns whether JARVIS service is currently marked
     * as running.
     *
     * The final implementation will be connected to the
     * actual service state.
     */
    @JavascriptInterface
    fun isJarvisServiceRunning(): Boolean {

        return JarvisService.isRunning()
    }


    // ========================================================
    // SECURITY
    // ========================================================

    /**
     * Returns whether this bridge is available.
     *
     * This is deliberately simple for Phase 2.
     *
     * Voice authentication will be added later and will sit
     * BEFORE sensitive commands are executed.
     */
    @JavascriptInterface
    fun isSecurityLayerAvailable(): Boolean {

        return true
    }


    // ========================================================
    // INTERNAL TOAST HELPER
    // ========================================================

    private fun showToast(message: String) {

        Toast.makeText(
            context.applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
