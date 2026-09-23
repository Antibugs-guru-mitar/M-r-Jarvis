package com.jarvis.ai

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.Settings

class SystemActions(
    private val context: Context
) {

    fun execute(action: String): Boolean {
        return when (action.trim().uppercase()) {

            "FLASHLIGHT_ON",
            "TORCH_ON",
            "FLASH_ON" -> {
                setFlashlight(true)
            }

            "FLASHLIGHT_OFF",
            "TORCH_OFF",
            "FLASH_OFF" -> {
                setFlashlight(false)
            }

            "HOME",
            "HOME_SCREEN",
            "GO_HOME" -> {
                goHome()
            }

            "SETTINGS",
            "OPEN_SETTINGS" -> {
                openSettings()
            }

            else -> {
                false
            }
        }
    }

    // ==========================================
    // FLASHLIGHT
    // ==========================================

    private fun setFlashlight(enabled: Boolean): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        return try {

            val cameraManager =
                context.getSystemService(
                    Context.CAMERA_SERVICE
                ) as CameraManager

            val cameraId = findFlashCameraId(cameraManager)
                ?: return false

            cameraManager.setTorchMode(
                cameraId,
                enabled
            )

            true

        } catch (_: Exception) {

            false
        }
    }

    // ==========================================
    // FIND CAMERA WITH FLASH
    // ==========================================

    private fun findFlashCameraId(
        cameraManager: CameraManager
    ): String? {

        return try {

            for (cameraId in cameraManager.cameraIdList) {

                val characteristics =
                    cameraManager.getCameraCharacteristics(cameraId)

                val hasFlash =
                    characteristics.get(
                        CameraCharacteristics.FLASH_INFO_AVAILABLE
                    ) == true

                if (hasFlash) {
                    return cameraId
                }
            }

            null

        } catch (_: Exception) {

            null
        }
    }

    // ==========================================
    // HOME SCREEN
    // ==========================================

    private fun goHome(): Boolean {

        return try {

            val intent = Intent(
                Intent.ACTION_MAIN
            ).apply {

                addCategory(
                    Intent.CATEGORY_HOME
                )

                addCategory(
                    Intent.CATEGORY_DEFAULT
                )

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }

            context.startActivity(intent)

            true

        } catch (_: Exception) {

            false
        }
    }

    // ==========================================
    // ANDROID SETTINGS
    // ==========================================

    private fun openSettings(): Boolean {

        return try {

            val intent = Intent(
                Settings.ACTION_SETTINGS
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)

            true

        } catch (_: Exception) {

            false
        }
    }
}
