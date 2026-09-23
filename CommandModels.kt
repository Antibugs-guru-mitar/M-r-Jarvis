package com.jarvis.ai

/**
 * =========================================================
 * JARVIS — PERSONAL AI ASSISTANT
 * PHASE 3 — COMMAND MODELS
 * =========================================================
 *
 * Ye file JARVIS ke command-related data structures
 * define karti hai.
 *
 * Is file ka kaam ACTION execute karna nahi hai.
 * Sirf data ko organized aur predictable format
 * mein represent karna hai.
 *
 * Flow:
 *
 * AI
 *  ↓
 * AIResponseParser
 *  ↓
 * CommandModels
 *  ↓
 * CommandRouter
 *  ↓
 * Android
 * =========================================================
 */


/* =========================================================
   COMMAND TYPE
========================================================= */

enum class CommandType {

    /*
     * Normal conversation.
     */
    CONVERSATION,

    /*
     * Android app open karna.
     */
    OPEN_APP,

    /*
     * Flashlight control.
     */
    FLASHLIGHT,

    /*
     * Home screen.
     */
    HOME,

    /*
     * Android settings.
     */
    OPEN_SETTINGS,

    /*
     * Website open karna.
     */
    OPEN_URL,

    /*
     * Current operation stop karna.
     */
    STOP,

    /*
     * Text ko voice mein bolna.
     */
    SPEAK,

    /*
     * Future commands ke liye.
     */
    UNKNOWN

}


/* =========================================================
   COMMAND PRIORITY
========================================================= */

enum class CommandPriority {

    LOW,

    NORMAL,

    HIGH,

    CRITICAL

}


/* =========================================================
   JARVIS COMMAND
========================================================= */

data class JarvisCommand(

    /*
     * Command ka type.
     */
    val type: CommandType,

    /*
     * Original user request.
     *
     * Example:
     *
     * "YouTube kholo"
     */
    val originalText: String = "",

    /*
     * Optional command action.
     *
     * Example:
     *
     * OPEN_APP
     */
    val action: String = "",

    /*
     * Command parameters.
     *
     * Example:
     *
     * app = YouTube
     *
     * value = ON
     */
    val parameters: Map<String, String> =
        emptyMap(),

    /*
     * Priority.
     */
    val priority: CommandPriority =
        CommandPriority.NORMAL,

    /*
     * Whether the command needs
     * additional confirmation.
     *
     * Sensitive actions can use this
     * in future.
     */
    val requiresConfirmation: Boolean =
        false,

    /*
     * Timestamp when command was created.
     */
    val timestamp: Long =
        System.currentTimeMillis()

)


/* =========================================================
   COMMAND EXECUTION STATUS
========================================================= */

enum class CommandExecutionStatus {

    /*
     * Command created but not executed yet.
     */
    PENDING,

    /*
     * Command is currently executing.
     */
    EXECUTING,

    /*
     * Command completed successfully.
     */
    SUCCESS,

    /*
     * Command failed.
     */
    FAILED,

    /*
     * User cancelled the command.
     */
    CANCELLED,

    /*
     * Command rejected by security layer.
     */
    DENIED

}


/* =========================================================
   COMMAND EXECUTION RESULT
========================================================= */

data class CommandExecutionResult(

    /*
     * Final execution status.
     */
    val status: CommandExecutionStatus,

    /*
     * Human-readable message.
     */
    val message: String = "",

    /*
     * Command that produced this result.
     */
    val command: JarvisCommand? = null,

    /*
     * Optional error message.
     */
    val error: String? = null,

    /*
     * Execution timestamp.
     */
    val timestamp: Long =
        System.currentTimeMillis()

)


/* =========================================================
   CONVERSATION MESSAGE ROLE
========================================================= */

enum class MessageRole {

    /*
     * User's message.
     */
    USER,

    /*
     * JARVIS / AI response.
     */
    ASSISTANT,

    /*
     * System-level instruction.
     */
    SYSTEM
}


/* =========================================================
   CONVERSATION MESSAGE
========================================================= */

data class ConversationMessage(

    /*
     * Who sent the message.
     */
    val role: MessageRole,

    /*
     * Message content.
     */
    val content: String,

    /*
     * Message timestamp.
     */
    val timestamp: Long =
        System.currentTimeMillis()
)


/* =========================================================
   AI REQUEST
========================================================= */

data class AIRequest(

    /*
     * User's message.
     */
    val message: String,

    /*
     * Previous conversation context.
     */
    val conversationHistory:
        List<ConversationMessage> =
        emptyList(),

    /*
     * Optional device context.
     *
     * Example:
     *
     * Android version
     * battery level
     * current app
     *
     * Future use.
     */
    val deviceContext:
        Map<String, String> =
        emptyMap()
)


/* =========================================================
   AI RESPONSE
========================================================= */

data class JarvisAIResponse(

    /*
     * AI's normal text response.
     */
    val text: String = "",

    /*
     * Optional command.
     */
    val command: JarvisCommand? = null,

    /*
     * Whether AI wants an Android action.
     */
    val containsCommand: Boolean =
        false,

    /*
     * Raw AI response for debugging/logging.
     */
    val rawResponse: String = "",

    /*
     * Whether response parsing succeeded.
     */
    val isValid: Boolean =
        true
)


/* =========================================================
   SECURITY LEVEL
========================================================= */

enum class SecurityLevel {

    /*
     * Completely normal conversation.
     */
    NORMAL,

    /*
     * Device action but low risk.
     */
    DEVICE_ACTION,

    /*
     * Requires additional permission.
     */
    PROTECTED,

    /*
     * Requires owner confirmation.
     */
    SENSITIVE,

    /*
     * Highest security level.
     */
    CRITICAL

}


/* =========================================================
   SECURE COMMAND
========================================================= */

data class SecureCommand(

    /*
     * Original command.
     */
    val command: JarvisCommand,

    /*
     * Security classification.
     */
    val securityLevel:
        SecurityLevel =
        SecurityLevel.NORMAL,

    /*
     * Whether owner verification
     * has been completed.
     */
    val ownerVerified: Boolean =
        false,

    /*
     * Whether biometric/PIN confirmation
     * is required.
     */
    val requiresUserConfirmation:
        Boolean =
        false
)
