package com.jarvis.ai

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // =========================================================
    // JARVIS MAIN ACTIVITY
    // Phase 2
    //
    // Responsibilities:
    // 1. Load JARVIS HTML interface
    // 2. Enable JavaScript
    // 3. Connect Android <-> JavaScript
    // 4. Request microphone permission
    // 5. Prepare Android voice service
    // =========================================================

    private lateinit var webView: WebView

    // ---------------------------------------------------------
    // Permission launcher
    // ---------------------------------------------------------

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val microphoneGranted =
                permissions[Manifest.permission.RECORD_AUDIO] == true

            if (microphoneGranted) {

                sendToJarvis(
                    """
                    window.JARVIS &&
                    window.JARVIS.androidPermissionGranted &&
                    window.JARVIS.androidPermissionGranted("MICROPHONE");
                    """.trimIndent()
                )

            } else {

                sendToJarvis(
                    """
                    window.JARVIS &&
                    window.JARVIS.androidPermissionDenied &&
                    window.JARVIS.androidPermissionDenied("MICROPHONE");
                    """.trimIndent()
                )
            }
        }


    // =========================================================
    // ACTIVITY CREATED
    // =========================================================

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // -----------------------------------------------------
        // Full screen / normal Android window
        // -----------------------------------------------------

        setContentView(R.layout.activity_main)

        // -----------------------------------------------------
        // Find WebView
        // -----------------------------------------------------

        webView = findViewById(R.id.jarvisWebView)

        // -----------------------------------------------------
        // WebView settings
        // -----------------------------------------------------

        webView.settings.apply {

            javaScriptEnabled = true

            domStorageEnabled = true

            databaseEnabled = true

            allowFileAccess = true

            allowContentAccess = true

            mediaPlaybackRequiresUserGesture = false

            javaScriptCanOpenWindowsAutomatically = true
        }

        // -----------------------------------------------------
        // WebView clients
        // -----------------------------------------------------

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {

                super.onPageFinished(view, url)

                // Tell JavaScript that Android bridge is ready
                sendToJarvis(
                    """
                    window.JARVIS &&
                    window.JARVIS.androidReady &&
                    window.JARVIS.androidReady();
                    """.trimIndent()
                )
            }
        }

        webView.webChromeClient = WebChromeClient()

        // -----------------------------------------------------
        // IMPORTANT:
        // JavaScript -> Android bridge
        // -----------------------------------------------------

        webView.addJavascriptInterface(
            AndroidBridge(this),
            "JarvisAndroid"
        )

        // -----------------------------------------------------
        // Load our existing JARVIS UI
        // -----------------------------------------------------

        webView.loadUrl(
            "file:///android_asset/index.html"
        )

        // -----------------------------------------------------
        // Request microphone permission
        // -----------------------------------------------------

        requestRequiredPermissions()
    }


    // =========================================================
    // PERMISSION HANDLING
    // =========================================================

    private fun requestRequiredPermissions() {

        val permissionsToRequest =
            mutableListOf<String>()

        // Microphone
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            permissionsToRequest.add(
                Manifest.permission.RECORD_AUDIO
            )
        }

        // Camera permission can be needed for flashlight
        // on some Android devices.
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            permissionsToRequest.add(
                Manifest.permission.CAMERA
            )
        }

        // Android 13+
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                permissionsToRequest.add(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        // Request only missing permissions
        if (permissionsToRequest.isNotEmpty()) {

            permissionLauncher.launch(
                permissionsToRequest.toTypedArray()
            )
        }
    }


    // =========================================================
    // SEND COMMAND TO JAVASCRIPT
    // =========================================================

    private fun sendToJarvis(script: String) {

        runOnUiThread {

            webView.evaluateJavascript(
                script,
                null
            )
        }
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }


    // =========================================================
    // ACTIVITY DESTROYED
    // =========================================================

    override fun onDestroy() {

        // Clean WebView
        webView.apply {

            stopLoading()

            clearHistory()

            removeAllViews()

            destroy()
        }

        super.onDestroy()
    }
}
