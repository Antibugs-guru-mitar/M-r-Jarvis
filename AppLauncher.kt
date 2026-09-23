package com.jarvis.ai

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * ============================================================
 * JARVIS - App Launcher
 * Phase 2
 * ============================================================
 *
 * Responsible for:
 *
 *  • Finding installed applications
 *  • Opening supported applications
 *  • Opening application-specific destinations
 *  • Safely handling apps that are not installed
 *
 * This class does NOT allow arbitrary Android commands.
 * Only explicitly supported destinations are handled.
 * ============================================================
 */

class AppLauncher(
    private val context: Context
) {

    private val packageManager: PackageManager =
        context.packageManager


    // ========================================================
    // PUBLIC APP LAUNCHER
    // ========================================================

    /**
     * Opens an application using a human-friendly name.
     *
     * Examples:
     *
     * openApp("YouTube")
     * openApp("Chrome")
     * openApp("TikTok")
     */
    fun openApp(appName: String): Boolean {

        val normalizedName =
            normalizeAppName(appName)

        return when (normalizedName) {

            "youtube" ->
                launchPackage(
                    "com.google.android.youtube"
                )

            "chrome" ->
                launchPackage(
                    "com.android.chrome"
                ) ||
                launchPackage(
                    "com.google.android.apps.chrome"
                )

            "tiktok" ->
                launchPackage(
                    "com.zhiliaoapp.musically"
                ) ||
                launchPackage(
                    "com.ss.android.ugc.trill"
                )

            "instagram" ->
                launchPackage(
                    "com.instagram.android"
                )

            "facebook" ->
                launchPackage(
                    "com.facebook.katana"
                )

            "whatsapp" ->
                launchPackage(
                    "com.whatsapp"
                )

            "telegram" ->
                launchPackage(
                    "org.telegram.messenger"
                )

            "spotify" ->
                launchPackage(
                    "com.spotify.music"
                )

            "settings" ->
                openAndroidSettings()

            "playstore",
            "play store",
            "google play",
            "google play store" ->
                openPlayStore()

            else ->
                openByApplicationLabel(
                    appName
                )
        }
    }


    // ========================================================
    // PACKAGE LAUNCHER
    // ========================================================

    /**
     * Launches an application using its package name.
     */
    private fun launchPackage(
        packageName: String
    ): Boolean {

        return try {

            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (launchIntent == null) {
                return false
            }

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(
                launchIntent
            )

            true

        } catch (_: Exception) {

            false
        }
    }


    // ========================================================
    // APPLICATION LABEL SEARCH
    // ========================================================

    /**
     * Fallback method.
     *
     * If the command doesn't match one of our predefined apps,
     * JARVIS searches installed applications by their visible
     * Android label.
     *
     * Example:
     *
     * "Calculator"
     * "Clock"
     * "Gallery"
     *
     * This allows some device-specific applications to work
     * without hardcoding every package name.
     */
    private fun openByApplicationLabel(
        requestedName: String
    ): Boolean {

        return try {

            val normalizedRequest =
                normalizeAppName(requestedName)

            val installedApplications =
                packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA
                )

            for (applicationInfo in installedApplications) {

                val label =
                    packageManager.getApplicationLabel(
                        applicationInfo
                    ).toString()

                if (
                    normalizeAppName(label) ==
                    normalizedRequest
                ) {

                    val packageName =
                        applicationInfo.packageName

                    val launchIntent =
                        packageManager.getLaunchIntentForPackage(
                            packageName
                        )

                    if (launchIntent != null) {

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        context.startActivity(
                            launchIntent
                        )

                        return true
                    }
                }
            }

            false

        } catch (_: Exception) {

            false
        }
    }


    // ========================================================
    // OPEN ANDROID SETTINGS
    // ========================================================

    private fun openAndroidSettings(): Boolean {

        return try {

            val intent = Intent(
                android.provider.Settings
                    .ACTION_SETTINGS
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(
                intent
            )

            true

        } catch (_: Exception) {

            false
        }
    }


    // ========================================================
    // GOOGLE PLAY STORE
    // ========================================================

    private fun openPlayStore(): Boolean {

        return try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "market://details?id=com.android.vending"
                )
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(
                intent
            )

            true

        } catch (_: Exception) {

            // If Play Store application isn't available,
            // fallback to the web version.

            return try {

                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://play.google.com/store"
                    )
                )

                webIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(
                    webIntent
                )

                true

            } catch (_: Exception) {

                false
            }
        }
    }


    // ========================================================
    // NAME NORMALIZATION
    // ========================================================

    /**
     * Converts different ways of saying an app name into
     * a predictable form.
     *
     * Example:
     *
     * " YouTube "
     * "YOUTUBE"
     * "youtube"
     *
     * all become:
     *
     * "youtube"
     */
    private fun normalizeAppName(
        name: String
    ): String {

        return name
            .trim()
            .lowercase()
            .replace(
                Regex("\\s+"),
                " "
            )
    }
}
