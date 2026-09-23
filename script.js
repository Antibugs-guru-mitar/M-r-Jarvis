/* =========================================================
   JARVIS — PERSONAL AI ASSISTANT
   PHASE 1 + PHASE 2 — FRONTEND + ANDROID BRIDGE ENGINE
========================================================= */

"use strict";


/* =========================================================
   01. DOM ELEMENTS
========================================================= */

const app = document.getElementById("jarvisApp");

const listenButton = document.getElementById("listenButton");
const listenText = document.getElementById("listenText");

const mainStatus = document.getElementById("mainStatus");
const assistantMessage =
    document.getElementById("assistantMessage");

const assistantSubMessage =
    document.getElementById("assistantSubMessage");

const commandText =
    document.getElementById("commandText");

const voiceStatus =
    document.getElementById("voiceStatus");

const voiceIndicator =
    document.getElementById("voiceIndicator");

const aiStatus =
    document.getElementById("aiStatus");

const securityStatus =
    document.getElementById("securityStatus");

const systemStatus =
    document.getElementById("systemStatus");

const footerVoice =
    document.getElementById("footerVoice");

const footerAI =
    document.getElementById("footerAI");

const authText =
    document.getElementById("authText");

const modeText =
    document.getElementById("modeText");

const backgroundBadge =
    document.getElementById("backgroundBadge");

const currentTime =
    document.getElementById("currentTime");

const thinkingOverlay =
    document.getElementById("thinkingOverlay");

const toast =
    document.getElementById("toast");

const particlesContainer =
    document.getElementById("particles");

const hologram =
    document.getElementById("hologram");

const quickCommands =
    document.querySelectorAll(".quick-command");


/* =========================================================
   02. JARVIS STATE
========================================================= */

const JARVIS_STATE = {

    IDLE: "idle",

    LISTENING: "listening",

    THINKING: "thinking",

    SPEAKING: "speaking",

    EXECUTING: "executing",

    OFFLINE: "offline",

    AUTH_FAILED: "auth-failed"

};


let currentState = JARVIS_STATE.IDLE;

let recognition = null;

let recognitionAvailable = false;

let isListening = false;

let isProcessing = false;

let toastTimer = null;

let speechTimer = null;


/* =========================================================
   ANDROID PHASE 2 STATUS
========================================================= */

let androidConnected = false;

let androidMicrophoneGranted = false;


/* =========================================================
   03. INITIALIZATION
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    initializeJarvis();

});


function initializeJarvis() {

    updateClock();

    setInterval(updateClock, 1000);

    createParticles();

    initializeVoiceRecognition();

    initializeQuickCommands();

    initializeKeyboardControls();

    initializeAndroidBridge();

    setState(JARVIS_STATE.IDLE);

    showToast("JARVIS system initialized.");

}


/* =========================================================
   04. CLOCK
========================================================= */

function updateClock() {

    if (!currentTime) return;

    const now = new Date();

    const hours =
        String(now.getHours()).padStart(2, "0");

    const minutes =
        String(now.getMinutes()).padStart(2, "0");

    const seconds =
        String(now.getSeconds()).padStart(2, "0");

    currentTime.textContent =
        `${hours}:${minutes}:${seconds}`;

}


/* =========================================================
   05. PARTICLE SYSTEM
========================================================= */

function createParticles() {

    if (!particlesContainer) return;

    particlesContainer.innerHTML = "";

    const screenWidth = window.innerWidth;

    let particleCount = 35;

    if (screenWidth < 600) {
        particleCount = 18;
    }

    for (let i = 0; i < particleCount; i++) {

        const particle =
            document.createElement("span");

        particle.className =
            "jarvis-particle";

        particle.style.position = "absolute";

        particle.style.width =
            `${Math.random() * 2 + 1}px`;

        particle.style.height =
            particle.style.width;

        particle.style.borderRadius =
            "50%";

        particle.style.background =
            "rgba(0, 234, 255, 0.7)";

        particle.style.boxShadow =
            "0 0 8px rgba(0, 234, 255, 0.7)";

        particle.style.left =
            `${Math.random() * 100}%`;

        particle.style.top =
            `${Math.random() * 100}%`;

        particle.style.opacity =
            `${Math.random() * 0.7 + 0.2}`;

        const duration =
            Math.random() * 8 + 6;

        const delay =
            Math.random() * 6;

        particle.animate(

            [
                {
                    transform:
                        "translate3d(0, 0, 0)",
                    opacity: 0.15
                },

                {
                    transform:
                        `translate3d(
                            ${Math.random() * 80 - 40}px,
                            ${Math.random() * 100 - 50}px,
                            0
                        )`,
                    opacity: 0.8
                },

                {
                    transform:
                        "translate3d(0, 0, 0)",
                    opacity: 0.15
                }
            ],

            {
                duration:
                    duration * 1000,

                delay:
                    delay * 1000,

                iterations:
                    Infinity,

                easing:
                    "ease-in-out"
            }

        );

        particlesContainer.appendChild(particle);

    }

}


/* =========================================================
   06. STATE ENGINE
========================================================= */

function setState(state, data = {}) {

    currentState = state;

    if (!app) return;

    removeAllStates();

    switch (state) {

        case JARVIS_STATE.IDLE:
            applyIdleState();
            break;

        case JARVIS_STATE.LISTENING:
            applyListeningState();
            break;

        case JARVIS_STATE.THINKING:
            applyThinkingState();
            break;

        case JARVIS_STATE.SPEAKING:
            applySpeakingState();
            break;

        case JARVIS_STATE.EXECUTING:
            applyExecutingState(data);
            break;

        case JARVIS_STATE.OFFLINE:
            applyOfflineState();
            break;

        case JARVIS_STATE.AUTH_FAILED:
            applyAuthFailedState();
            break;

        default:
            applyIdleState();

    }

}


/* =========================================================
   REMOVE STATES
========================================================= */

function removeAllStates() {

    app.classList.remove(
        "listening",
        "thinking",
        "speaking",
        "executing",
        "offline",
        "voice-auth-failed"
    );

}


/* =========================================================
   IDLE
========================================================= */

function applyIdleState() {

    app.classList.remove(
        "listening",
        "thinking",
        "speaking",
        "executing"
    );

    mainStatus.textContent =
        "JARVIS IS READY";

    assistantMessage.textContent =
        "I'm your personal AI assistant.";

    assistantSubMessage.textContent =
        "How may I help you?";

    listenText.textContent =
        "LISTEN";

    voiceStatus.textContent =
        "STANDBY";

    voiceIndicator.textContent =
        "OFF";

    aiStatus.textContent =
        "READY";

    systemStatus.textContent =
        androidConnected
            ? "ANDROID READY"
            : "READY";

    footerVoice.textContent =
        "STANDBY";

    footerAI.textContent =
        "READY";

    authText.textContent =
        "Voice authentication ready";

    modeText.textContent =
        "ASSISTANT";

    backgroundBadge.textContent =
        androidConnected
            ? "ANDROID"
            : "READY";

}


/* =========================================================
   LISTENING
========================================================= */

function applyListeningState() {

    app.classList.add("listening");

    mainStatus.textContent =
        "LISTENING";

    assistantMessage.textContent =
        "I'm listening.";

    assistantSubMessage.textContent =
        "Speak your command...";

    listenText.textContent =
        "LISTENING";

    voiceStatus.textContent =
        "ACTIVE";

    voiceIndicator.textContent =
        "ON";

    footerVoice.textContent =
        "LISTENING";

    authText.textContent =
        "Waiting for authorized voice";

}


/* =========================================================
   THINKING
========================================================= */

function applyThinkingState() {

    app.classList.add("thinking");

    mainStatus.textContent =
        "PROCESSING REQUEST";

    assistantMessage.textContent =
        "Let me think.";

    assistantSubMessage.textContent =
        "Processing your command...";

    listenText.textContent =
        "THINKING";

    voiceStatus.textContent =
        "PROCESSING";

    voiceIndicator.textContent =
        "WAIT";

    aiStatus.textContent =
        "PROCESSING";

    systemStatus.textContent =
        "THINKING";

    footerVoice.textContent =
        "PROCESSING";

    footerAI.textContent =
        "PROCESSING";

    authText.textContent =
        "Voice verified";

    if (thinkingOverlay) {

        thinkingOverlay.classList.add("active");

        thinkingOverlay.setAttribute(
            "aria-hidden",
            "false"
        );

    }

}


/* =========================================================
   SPEAKING
========================================================= */

function applySpeakingState() {

    app.classList.add("speaking");

    mainStatus.textContent =
        "JARVIS IS SPEAKING";

    listenText.textContent =
        "SPEAKING";

    voiceStatus.textContent =
        "OUTPUT";

    voiceIndicator.textContent =
        "ON";

    footerVoice.textContent =
        "SPEAKING";

    systemStatus.textContent =
        "SPEAKING";

}


/* =========================================================
   EXECUTING
========================================================= */

function applyExecutingState(data = {}) {

    app.classList.add("executing");

    const action =
        data.action || "COMMAND";

    mainStatus.textContent =
        "EXECUTING COMMAND";

    assistantMessage.textContent =
        "Executing.";

    assistantSubMessage.textContent =
        action;

    listenText.textContent =
        "EXECUTING";

    aiStatus.textContent =
        "EXECUTING";

    systemStatus.textContent =
        "EXECUTING";

}


/* =========================================================
   OFFLINE
========================================================= */

function applyOfflineState() {

    app.classList.add("offline");

    mainStatus.textContent =
        "SYSTEM OFFLINE";

    assistantMessage.textContent =
        "Connection unavailable.";

    assistantSubMessage.textContent =
        "Some AI features may not be available.";

    listenText.textContent =
        "OFFLINE";

    voiceStatus.textContent =
        "OFFLINE";

    voiceIndicator.textContent =
        "OFF";

    aiStatus.textContent =
        "OFFLINE";

    systemStatus.textContent =
        "OFFLINE";

    footerVoice.textContent =
        "OFFLINE";

    footerAI.textContent =
        "OFFLINE";

}


/* =========================================================
   AUTH FAILED
========================================================= */

function applyAuthFailedState() {

    app.classList.add("voice-auth-failed");

    mainStatus.textContent =
        "VOICE NOT VERIFIED";

    assistantMessage.textContent =
        "Voice not recognized.";

    assistantSubMessage.textContent =
        "Command was not accepted.";

    listenText.textContent =
        "TRY AGAIN";

    authText.textContent =
        "Voice authentication failed";

    securityStatus.textContent =
        "BLOCKED";

}


/* =========================================================
   07. LISTEN BUTTON
========================================================= */

if (listenButton) {

    listenButton.addEventListener(
        "click",
        handleListenButton
    );

}


function handleListenButton() {

    if (isProcessing) {

        showToast(
            "JARVIS is currently processing."
        );

        return;
    }


    if (isListening) {

        stopListening();

        return;
    }


    startListening();

}


/* =========================================================
   08. VOICE RECOGNITION
========================================================= */

function initializeVoiceRecognition() {

    const SpeechRecognition =
        window.SpeechRecognition ||
        window.webkitSpeechRecognition;

    if (!SpeechRecognition) {

        recognitionAvailable = false;

        console.log(
            "Speech Recognition is not available in this browser."
        );

        return;
    }


    recognitionAvailable = true;

    recognition =
        new SpeechRecognition();


    recognition.continuous =
        false;

    recognition.interimResults =
        true;

    recognition.maxAlternatives =
        1;


    /*
       Phase 1 browser voice recognition.

       Android native voice engine will be connected
       in a later phase.
    */

    recognition.lang =
        "en-US";


    recognition.onstart = () => {

        isListening = true;

        setState(
            JARVIS_STATE.LISTENING
        );

        showToast(
            "JARVIS is listening..."
        );

    };


    recognition.onresult = (event) => {

        let finalText = "";

        let interimText = "";


        for (
            let i = event.resultIndex;
            i < event.results.length;
            i++
        ) {

            const transcript =
                event.results[i][0].transcript;

            if (event.results[i].isFinal) {

                finalText += transcript;

            } else {

                interimText += transcript;

            }

        }


        if (interimText) {

            commandText.textContent =
                interimText;

        }


        if (finalText) {

            commandText.textContent =
                finalText;

            processCommand(
                finalText.trim()
            );

        }

    };


    recognition.onerror = (event) => {

        console.warn(
            "Speech recognition error:",
            event.error
        );

        isListening = false;

        if (
            event.error === "not-allowed" ||
            event.error === "service-not-allowed"
        ) {

            showToast(
                "Microphone permission is required."
            );

        } else {

            showToast(
                "Voice recognition error."
            );

        }

        setState(
            JARVIS_STATE.IDLE
        );

    };


    recognition.onend = () => {

        isListening = false;

        if (
            currentState ===
            JARVIS_STATE.LISTENING
        ) {

            setState(
                JARVIS_STATE.IDLE
            );

        }

    };

}


/* =========================================================
   START LISTENING
========================================================= */

function startListening() {

    if (!recognitionAvailable) {

        runDemoListening();

        return;
    }


    try {

        recognition.start();

    } catch (error) {

        console.warn(error);

        runDemoListening();

    }

}


/* =========================================================
   STOP LISTENING
========================================================= */

function stopListening() {

    isListening = false;

    if (recognition) {

        try {

            recognition.stop();

        } catch (error) {

            console.warn(error);

        }

    }

    setState(
        JARVIS_STATE.IDLE
    );

}


/* =========================================================
   DEMO LISTENING
========================================================= */

function runDemoListening() {

    setState(
        JARVIS_STATE.LISTENING
    );

    showToast(
        "Demo voice mode active."
    );


    setTimeout(() => {

        if (
            currentState ===
            JARVIS_STATE.LISTENING
        ) {

            processCommand(
                "YouTube kholo"
            );

        }

    }, 2500);

}


/* =========================================================
   09. COMMAND PROCESSOR
========================================================= */

async function processCommand(command) {

    if (!command) {

        setState(
            JARVIS_STATE.IDLE
        );

        return;
    }


    isProcessing = true;

    commandText.textContent =
        command;


    setState(
        JARVIS_STATE.THINKING
    );


    await wait(1000);


    const normalized =
        normalizeCommand(command);


    const intent =
        detectIntent(normalized);


    if (intent) {

        await executeIntent(
            intent,
            command
        );

    } else {

        await answerGeneralQuestion(
            command
        );

    }


    isProcessing = false;

}


/* =========================================================
   NORMALIZE COMMAND
========================================================= */

function normalizeCommand(command) {

    return command
        .toLowerCase()
        .trim()
        .replace(/\s+/g, " ");

}


/* =========================================================
   10. INTENT DETECTION
========================================================= */

function detectIntent(command) {


    /* -------------------------------
       YOUTUBE
    -------------------------------- */

    if (
        command.includes("youtube") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_YOUTUBE"
        };

    }


    /* -------------------------------
       CHROME
    -------------------------------- */

    if (
        command.includes("chrome") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_CHROME"
        };

    }


    /* -------------------------------
       TIKTOK
    -------------------------------- */

    if (
        command.includes("tiktok") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_APP",
            app: "TikTok"
        };

    }


    /* -------------------------------
       INSTAGRAM
    -------------------------------- */

    if (
        command.includes("instagram") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_APP",
            app: "Instagram"
        };

    }


    /* -------------------------------
       FACEBOOK
    -------------------------------- */

    if (
        command.includes("facebook") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_APP",
            app: "Facebook"
        };

    }


    /* -------------------------------
       WHATSAPP
    -------------------------------- */

    if (
        command.includes("whatsapp") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_APP",
            app: "WhatsApp"
        };

    }


    /* -------------------------------
       TELEGRAM
    -------------------------------- */

    if (
        command.includes("telegram") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_APP",
            app: "Telegram"
        };

    }


    /* -------------------------------
       SPOTIFY
    -------------------------------- */

    if (
        command.includes("spotify") &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_APP",
            app: "Spotify"
        };

    }


    /* -------------------------------
       SETTINGS
    -------------------------------- */

    if (
        (
            command.includes("settings") ||
            command.includes("setting")
        ) &&
        (
            command.includes("open") ||
            command.includes("khol") ||
            command.includes("kholo")
        )
    ) {

        return {
            type: "OPEN_SETTINGS"
        };

    }


    /* -------------------------------
       FLASHLIGHT ON
    -------------------------------- */

    if (
        (
            command.includes("flashlight") ||
            command.includes("torch")
        ) &&
        (
            command.includes("on") ||
            command.includes("karo") ||
            command.includes("chalao")
        )
    ) {

        return {
            type: "FLASHLIGHT",
            value: "ON"
        };

    }


    /* -------------------------------
       FLASHLIGHT OFF
    -------------------------------- */

    if (
        (
            command.includes("flashlight") ||
            command.includes("torch")
        ) &&
        (
            command.includes("off") ||
            command.includes("band")
        )
    ) {

        return {
            type: "FLASHLIGHT",
            value: "OFF"
        };

    }


    /* -------------------------------
       HOME
    -------------------------------- */

    if (
        command.includes("home screen") ||
        command.includes("home par") ||
        command === "home"
    ) {

        return {
            type: "HOME"
        };

    }


    /* -------------------------------
       STOP
    -------------------------------- */

    if (
        command === "stop" ||
        command.includes("band ho") ||
        command.includes("cancel")
    ) {

        return {
            type: "STOP"
        };

    }


    /* -------------------------------
       TIME
    -------------------------------- */

    if (
        command.includes("what time") ||
        command.includes("time kya") ||
        command.includes("kitne baje") ||
        command === "time"
    ) {

        return {
            type: "TIME"
        };

    }


    /* -------------------------------
       GREETING
    -------------------------------- */

    if (
        command.includes("hello jarvis") ||
        command.includes("hi jarvis") ||
        command.includes("hey jarvis") ||
        command === "hello"
    ) {

        return {
            type: "GREETING"
        };

    }


    return null;

}


/* =========================================================
   11. COMMAND EXECUTION
========================================================= */

async function executeIntent(intent, originalCommand) {

    switch (intent.type) {


        /* =========================
           YOUTUBE
        ========================== */

        case "OPEN_YOUTUBE":

            setState(
                JARVIS_STATE.EXECUTING,
                {
                    action:
                        "Opening YouTube..."
                }
            );

            commandText.textContent =
                originalCommand;

            await wait(700);


            if (androidConnected) {

                const opened =
                    openAndroidApp(
                        "YouTube"
                    );

                if (opened) {

                    speakResponse(
                        "Opening YouTube."
                    );

                    break;

                }

            }


            speakResponse(
                "Opening YouTube."
            );

            openWebsite(
                "https://www.youtube.com/"
            );

            break;


        /* =========================
           CHROME
        ========================== */

        case "OPEN_CHROME":

            setState(
                JARVIS_STATE.EXECUTING,
                {
                    action:
                        "Opening browser..."
                }
            );

            await wait(700);


            if (androidConnected) {

                const opened =
                    openAndroidApp(
                        "Chrome"
                    );

                if (opened) {

                    speakResponse(
                        "Opening Chrome."
                    );

                    break;

                }

            }


            speakResponse(
                "Opening the browser."
            );

            openWebsite(
                "https://www.google.com/"
            );

            break;


        /* =========================
           OTHER ANDROID APPS
        ========================== */

        case "OPEN_APP":

            setState(
                JARVIS_STATE.EXECUTING,
                {
                    action:
                        `Opening ${intent.app}...`
                }
            );

            await wait(500);


            if (androidConnected) {

                const opened =
                    openAndroidApp(
                        intent.app
                    );

                if (opened) {

                    speakResponse(
                        `Opening ${intent.app}.`
                    );

                    break;

                }

            }


            speakResponse(
                `${intent.app} is not available as a native Android app action yet.`
            );

            break;


        /* =========================
           SETTINGS
        ========================== */

        case "OPEN_SETTINGS":

            setState(
                JARVIS_STATE.EXECUTING,
                {
                    action:
                        "Opening Android settings..."
                }
            );

            await wait(500);


            if (androidConnected) {

                const opened =
                    executeAndroidSystemAction(
                        "SETTINGS"
                    );

                if (opened) {

                    speakResponse(
                        "Opening settings."
                    );

                    break;

                }

            }


            speakResponse(
                "Android settings are available in the Android app."
            );

            break;


        /* =========================
           FLASHLIGHT
        ========================== */

        case "FLASHLIGHT":

            setState(
                JARVIS_STATE.EXECUTING,
                {
                    action:
                        `Flashlight ${intent.value}`
                }
            );

            await wait(600);


            if (androidConnected) {

                const action =
                    intent.value === "ON"
                        ? "FLASHLIGHT_ON"
                        : "FLASHLIGHT_OFF";

                const success =
                    executeAndroidSystemAction(
                        action
                    );

                if (success) {

                    if (
                        intent.value === "ON"
                    ) {

                        speakResponse(
                            "Flashlight is on."
                        );

                    } else {

                        speakResponse(
                            "Flashlight is off."
                        );

                    }

                    break;

                }

            }


            speakResponse(
                "Flashlight control is not available in browser mode."
            );

            break;


        /* =========================
           HOME
        ========================== */

        case "HOME":

            setState(
                JARVIS_STATE.EXECUTING,
                {
                    action:
                        "Going to home screen..."
                }
            );

            await wait(500);


            if (androidConnected) {

                const success =
                    executeAndroidSystemAction(
                        "HOME"
                    );

                if (success) {

                    speakResponse(
                        "Going to the home screen."
                    );

                    break;

                }

            }


            speakResponse(
                "Home screen control is available in the Android app."
            );

            break;


        /* =========================
           STOP
        ========================== */

        case "STOP":

            stopAllOperations();

            break;


        /* =========================
           TIME
        ========================== */

        case "TIME":

            const now =
                new Date();

            const timeString =
                now.toLocaleTimeString(
                    [],
                    {
                        hour: "numeric",
                        minute: "2-digit"
                    }
                );

            speakResponse(
                `The current time is ${timeString}.`
            );

            break;


        /* =========================
           GREETING
        ========================== */

        case "GREETING":

            speakResponse(
                "Hello. I'm JARVIS. How may I help you?"
            );

            break;


        default:

            setState(
                JARVIS_STATE.IDLE
            );

    }

}


/* =========================================================
   12. GENERAL AI RESPONSE
========================================================= */

async function answerGeneralQuestion(command) {

    /*
       Phase 1 fallback.

       AI API will be connected in a future phase.

       API keys should NOT be hardcoded into the
       final Android APK.
    */

    await wait(700);


    let response =
        getLocalResponse(command);


    if (!response) {

        response =
            "I understand your request. AI connection will be connected in the next phase.";

    }


    speakResponse(
        response
    );

}


/* =========================================================
   LOCAL RESPONSE ENGINE
========================================================= */

function getLocalResponse(command) {

    const text =
        command.toLowerCase();


    if (
        text.includes("how are you") ||
        text.includes("kaise ho") ||
        text.includes("kya haal")
    ) {

        return (
            "I'm operating normally. "
            + "All core systems are ready."
        );

    }


    if (
        text.includes("who are you") ||
        text.includes("tum kon ho")
    ) {

        return (
            "I'm JARVIS, your personal AI assistant."
        );

    }


    if (
        text.includes("what can you do") ||
        text.includes("kia kar sakte ho")
    ) {

        return (
            "I can understand commands, "
            + "answer questions, and control "
            + "supported phone functions."
        );

    }


    if (
        text.includes("thank you") ||
        text.includes("thanks") ||
        text.includes("shukriya")
    ) {

        return (
            "You're welcome."
        );

    }


    return null;

}


/* =========================================================
   13. TEXT TO SPEECH
========================================================= */

function speakResponse(text) {

    if (!text) return;


    if (thinkingOverlay) {

        thinkingOverlay.classList.remove(
            "active"
        );

        thinkingOverlay.setAttribute(
            "aria-hidden",
            "true"
        );

    }


    assistantMessage.textContent =
        text;

    assistantSubMessage.textContent =
        "JARVIS response";


    /* =====================================================
       ANDROID NATIVE TTS — PHASE 2
    ===================================================== */

    if (
        androidConnected &&
        window.JarvisAndroid &&
        typeof window.JarvisAndroid.speak ===
        "function"
    ) {

        try {

            const nativeSpeech =
                speakWithAndroid(text);

            if (nativeSpeech) {

                setState(
                    JARVIS_STATE.SPEAKING
                );

                clearTimeout(
                    speechTimer
                );

                speechTimer =
                    setTimeout(() => {

                        setState(
                            JARVIS_STATE.IDLE
                        );

                    }, Math.max(
                        1500,
                        text.length * 55
                    ));

                return;

            }

        } catch (error) {

            console.warn(
                "Native Android TTS failed:",
                error
            );

        }

    }


    /* =====================================================
       BROWSER TTS — PHASE 1 FALLBACK
    ===================================================== */

    if (
        "speechSynthesis" in window
    ) {

        try {

            window.speechSynthesis.cancel();

            const utterance =
                new SpeechSynthesisUtterance(
                    text
                );

            utterance.rate =
                0.92;

            utterance.pitch =
                0.9;

            utterance.volume =
                1;

            utterance.onstart = () => {

                setState(
                    JARVIS_STATE.SPEAKING
                );

                assistantMessage.textContent =
                    text;

            };


            utterance.onend = () => {

                setState(
                    JARVIS_STATE.IDLE
                );

            };


            utterance.onerror = () => {

                setState(
                    JARVIS_STATE.IDLE
                );

            };


            window.speechSynthesis.speak(
                utterance
            );

        } catch (error) {

            console.warn(
                "Speech synthesis error:",
                error
            );

            finishSpeaking();

        }

    } else {

        finishSpeaking();

    }

}


/* =========================================================
   FINISH SPEAKING
========================================================= */

function finishSpeaking() {

    clearTimeout(
        speechTimer
    );

    speechTimer =
        setTimeout(() => {

            setState(
                JARVIS_STATE.IDLE
            );

        }, 1200);

}


/* =========================================================
   14. OPEN WEBSITE
========================================================= */

function openWebsite(url) {

    try {

        window.open(
            url,
            "_blank"
        );

    } catch (error) {

        console.warn(
            "Unable to open URL:",
            error
        );

    }

}


/* =========================================================
   15. QUICK COMMANDS
========================================================= */

function initializeQuickCommands() {

    quickCommands.forEach(
        button => {

            button.addEventListener(
                "click",
                () => {

                    const command =
                        button.dataset.command;

                    if (!command) return;

                    commandText.textContent =
                        command;

                    processCommand(
                        command
                    );

                }
            );

        }
    );

}


/* =========================================================
   16. KEYBOARD CONTROLS
========================================================= */

function initializeKeyboardControls() {

    document.addEventListener(
        "keydown",
        event => {

            /*
               Space = Listen
            */

            if (
                event.code ===
                "Space"
            ) {

                if (
                    document.activeElement.tagName ===
                    "INPUT" ||
                    document.activeElement.tagName ===
                    "TEXTAREA"
                ) {
                    return;
                }

                event.preventDefault();

                handleListenButton();

            }


            /*
               Escape = Stop
            */

            if (
                event.key ===
                "Escape"
            ) {

                stopAllOperations();

            }

        }
    );

}


/* =========================================================
   17. STOP EVERYTHING
========================================================= */

function stopAllOperations() {

    isListening = false;

    isProcessing = false;


    if (recognition) {

        try {

            recognition.stop();

        } catch (error) {

            console.warn(error);

        }

    }


    if (
        "speechSynthesis" in window
    ) {

        try {

            window.speechSynthesis.cancel();

        } catch (error) {

            console.warn(error);

        }

    }


    /* Stop Android native TTS */

    stopAndroidSpeech();


    if (thinkingOverlay) {

        thinkingOverlay.classList.remove(
            "active"
        );

        thinkingOverlay.setAttribute(
            "aria-hidden",
            "true"
        );

    }


    setState(
        JARVIS_STATE.IDLE
    );


    assistantMessage.textContent =
        "Command stopped.";

    assistantSubMessage.textContent =
        "JARVIS is ready again.";

    showToast(
        "Operation stopped."
    );


    setTimeout(() => {

        if (
            currentState ===
            JARVIS_STATE.IDLE
        ) {

            assistantMessage.textContent =
                "I'm your personal AI assistant.";

            assistantSubMessage.textContent =
                "How may I help you?";

        }

    }, 1800);

}


/* =========================================================
   18. TOAST SYSTEM
========================================================= */

function showToast(message) {

    if (!toast) return;


    toast.textContent =
        message;

    toast.classList.add(
        "show"
    );


    clearTimeout(
        toastTimer
    );


    toastTimer =
        setTimeout(() => {

            toast.classList.remove(
                "show"
            );

        }, 2600);

}


/* =========================================================
   19. UTILITY — WAIT
========================================================= */

function wait(milliseconds) {

    return new Promise(
        resolve => {

            setTimeout(
                resolve,
                milliseconds
            );

        }
    );

}


/* =========================================================
   20. RESIZE HANDLER
========================================================= */

let resizeTimer = null;

window.addEventListener(
    "resize",
    () => {

        clearTimeout(
            resizeTimer
        );

        resizeTimer =
            setTimeout(() => {

                createParticles();

            }, 300);

    }
);


/* =========================================================
   21. VISIBILITY HANDLER
========================================================= */

document.addEventListener(
    "visibilitychange",
    () => {

        if (
            document.hidden
        ) {

            console.log(
                "JARVIS UI moved to background."
            );

        } else {

            console.log(
                "JARVIS UI returned to foreground."
            );

        }

    }
);


/* =========================================================
   22. ANDROID BRIDGE — PHASE 2
========================================================= */


/*
   AndroidBridge.kt exposes:

   window.JarvisAndroid.openApp(...)
   window.JarvisAndroid.systemAction(...)
   window.JarvisAndroid.speak(...)
   window.JarvisAndroid.stopSpeaking(...)
   window.JarvisAndroid.startJarvisService(...)
   window.JarvisAndroid.stopJarvisService(...)
   window.JarvisAndroid.isJarvisServiceRunning(...)
   window.JarvisAndroid.ping(...)
*/


function initializeAndroidBridge() {

    if (
        window.JarvisAndroid
    ) {

        androidConnected = true;

        console.log(
            "JARVIS Android bridge detected."
        );

        try {

            if (
                typeof window.JarvisAndroid.ping ===
                "function"
            ) {

                console.log(
                    window.JarvisAndroid.ping()
                );

            }

        } catch (error) {

            console.warn(
                "Android bridge ping failed:",
                error
            );

        }

    } else {

        androidConnected = false;

        console.log(
            "Android bridge not available. Browser mode active."
        );

    }

}


/* =========================================================
   ANDROID READY CALLBACK
   Called by MainActivity
========================================================= */

function handleAndroidReady() {

    androidConnected = true;

    console.log(
        "JARVIS Android Core Connected."
    );

    if (systemStatus) {

        systemStatus.textContent =
            "ANDROID READY";

    }

    if (backgroundBadge) {

        backgroundBadge.textContent =
            "ANDROID";

    }

    showToast(
        "JARVIS Android Core Connected."
    );

}


/* =========================================================
   ANDROID MICROPHONE PERMISSION GRANTED
========================================================= */

function handleAndroidPermissionGranted(
    permission
) {

    if (
        permission ===
        "MICROPHONE"
    ) {

        androidMicrophoneGranted =
            true;

        console.log(
            "Android microphone permission granted."
        );

        showToast(
            "Microphone permission granted."
        );

    }

}


/* =========================================================
   ANDROID MICROPHONE PERMISSION DENIED
========================================================= */

function handleAndroidPermissionDenied(
    permission
) {

    if (
        permission ===
        "MICROPHONE"
    ) {

        androidMicrophoneGranted =
            false;

        console.warn(
            "Android microphone permission denied."
        );

        showToast(
            "Microphone permission denied."
        );

    }

}


/* =========================================================
   NATIVE APP LAUNCHER
========================================================= */

function openAndroidApp(
    appName
) {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.openApp !==
        "function"
    ) {

        return false;
    }


    try {

        return Boolean(
            window.JarvisAndroid.openApp(
                appName
            )
        );

    } catch (error) {

        console.error(
            "Android app launch error:",
            error
        );

        return false;

    }

}


/* =========================================================
   NATIVE SYSTEM ACTION
========================================================= */

function executeAndroidSystemAction(
    action
) {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.systemAction !==
        "function"
    ) {

        return false;
    }


    try {

        return Boolean(
            window.JarvisAndroid.systemAction(
                action
            )
        );

    } catch (error) {

        console.error(
            "Android system action error:",
            error
        );

        return false;

    }

}


/* =========================================================
   NATIVE JARVIS SPEECH
========================================================= */

function speakWithAndroid(
    text
) {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.speak !==
        "function"
    ) {

        return false;
    }


    try {

        return Boolean(
            window.JarvisAndroid.speak(
                text
            )
        );

    } catch (error) {

        console.error(
            "Android TTS error:",
            error
        );

        return false;

    }

}


/* =========================================================
   STOP ANDROID SPEECH
========================================================= */

function stopAndroidSpeech() {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.stopSpeaking !==
        "function"
    ) {

        return;
    }


    try {

        window.JarvisAndroid.stopSpeaking();

    } catch (error) {

        console.warn(
            "Unable to stop Android speech:",
            error
        );

    }

}


/* =========================================================
   START JARVIS BACKGROUND SERVICE
========================================================= */

function startAndroidService() {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.startJarvisService !==
        "function"
    ) {

        return false;
    }


    try {

        const result =
            Boolean(
                window.JarvisAndroid.startJarvisService()
            );


        if (result) {

            if (backgroundBadge) {

                backgroundBadge.textContent =
                    "ACTIVE";

            }

            showToast(
                "JARVIS background service started."
            );

        }


        return result;

    } catch (error) {

        console.error(
            "JARVIS service start error:",
            error
        );

        return false;

    }

}


/* =========================================================
   STOP JARVIS BACKGROUND SERVICE
========================================================= */

function stopAndroidService() {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.stopJarvisService !==
        "function"
    ) {

        return false;
    }


    try {

        const result =
            Boolean(
                window.JarvisAndroid.stopJarvisService()
            );


        if (result) {

            if (backgroundBadge) {

                backgroundBadge.textContent =
                    "READY";

            }

            showToast(
                "JARVIS background service stopped."
            );

        }


        return result;

    } catch (error) {

        console.error(
            "JARVIS service stop error:",
            error
        );

        return false;

    }

}


/* =========================================================
   CHECK JARVIS SERVICE
========================================================= */

function isAndroidServiceRunning() {

    if (
        !window.JarvisAndroid ||
        typeof window.JarvisAndroid.isJarvisServiceRunning !==
        "function"
    ) {

        return false;
    }


    try {

        return Boolean(
            window.JarvisAndroid.isJarvisServiceRunning()
        );

    } catch (error) {

        console.warn(
            "Unable to check JARVIS service:",
            error
        );

        return false;

    }

}


/* =========================================================
   GENERIC ANDROID ACTION
========================================================= */

function androidAction(
    action,
    payload = null
) {

    if (
        !window.JarvisAndroid
    ) {

        return null;
    }


    if (
        typeof window.JarvisAndroid[action] !==
        "function"
    ) {

        return null;
    }


    try {

        if (
            payload === null ||
            typeof payload ===
            "undefined"
        ) {

            return window.JarvisAndroid[action]();

        }


        return window.JarvisAndroid[action](
            payload
        );

    } catch (error) {

        console.error(
            "Android bridge error:",
            error
        );

        return null;

    }

}


/* =========================================================
   ANDROID COMMAND ROUTER HOOK
========================================================= */

function sendToAndroidCommand(
    command,
    intent
) {

    console.log(
        "Android command:",
        command,
        intent
    );


    /*
       Full AI Command Router will be added
       in a future phase.
    */

    return null;

}


/* =========================================================
   ANDROID CALLBACKS EXPOSED TO MAIN ACTIVITY
========================================================= */


/*
   MainActivity calls:

   window.JARVIS.androidReady()

   window.JARVIS.androidPermissionGranted("MICROPHONE")

   window.JARVIS.androidPermissionDenied("MICROPHONE")
*/


/* =========================================================
   23. PUBLIC JARVIS API
========================================================= */

window.JARVIS = {

    getState() {

        return currentState;

    },


    listen() {

        startListening();

    },


    stop() {

        stopAllOperations();

    },


    command(command) {

        processCommand(
            command
        );

    },


    speak(text) {

        speakResponse(
            text
        );

    },


    setState(state) {

        setState(
            state
        );

    },


    showMessage(message) {

        assistantMessage.textContent =
            message;

    },


    showToast(message) {

        showToast(
            message
        );

    },


    /* =========================================
       ANDROID READY CALLBACK
    ========================================== */

    androidReady() {

        handleAndroidReady();

    },


    /* =========================================
       ANDROID PERMISSION CALLBACKS
    ========================================== */

    androidPermissionGranted(
        permission
    ) {

        handleAndroidPermissionGranted(
            permission
        );

    },


    androidPermissionDenied(
        permission
    ) {

        handleAndroidPermissionDenied(
            permission
        );

    },


    /* =========================================
       ANDROID APP CONTROL
    ========================================== */

    openApp(appName) {

        return openAndroidApp(
            appName
        );

    },


    systemAction(action) {

        return executeAndroidSystemAction(
            action
        );

    },


    /* =========================================
       ANDROID SPEECH
    ========================================== */

    androidSpeak(text) {

        return speakWithAndroid(
            text
        );

    },


    stopAndroidSpeech() {

        stopAndroidSpeech();

    },


    /* =========================================
       ANDROID SERVICE
    ========================================== */

    startBackgroundService() {

        return startAndroidService();

    },


    stopBackgroundService() {

        return stopAndroidService();

    },


    isBackgroundServiceRunning() {

        return isAndroidServiceRunning();

    },


    /* =========================================
       ANDROID STATUS
    ========================================== */

    isAndroidConnected() {

        return androidConnected;

    },


    isAndroidMicrophoneGranted() {

        return androidMicrophoneGranted;

    }

};


/* =========================================================
   24. DEVELOPMENT LOG
========================================================= */

console.log(
    "%c J A R V I S ",
    `
        color:#00eaff;
        font-size:24px;
        font-weight:bold;
        text-shadow:0 0 12px #00eaff;
    `
);

console.log(
    "%c Personal AI Assistant — Phase 1 + Phase 2",
    "color:#75dce8;font-size:12px;"
);

console.log(
    "%c Android native bridge integration enabled.",
    "color:#668b93;font-size:11px;"
);


/* =========================================================
   END OF JARVIS JAVASCRIPT ENGINE
========================================================= */
