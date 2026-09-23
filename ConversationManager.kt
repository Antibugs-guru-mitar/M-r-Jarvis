package com.jarvis.ai

import android.content.Context

/**
 * =========================================================
 * JARVIS — PERSONAL AI ASSISTANT
 * PHASE 3 — CONVERSATION MANAGER
 * =========================================================
 *
 * Responsibilities:
 *
 * 1. User/AI conversation history maintain karna.
 * 2. Recent messages AI ko provide karna.
 * 3. Conversation size ko control karna.
 * 4. New conversation start karna.
 * 5. Simple session memory provide karna.
 *
 * IMPORTANT:
 *
 * Ye permanent personal memory system nahi hai.
 * Abhi ye current conversation/session ko manage karta hai.
 *
 * Permanent memory future phase mein database ke through
 * add ki ja sakti hai.
 * =========================================================
 */

class ConversationManager(
    private val context: Context
) {

    companion object {

        /*
         * Maximum messages jo current session mein
         * memory mein rakhe jayenge.
         */
        private const val MAX_MESSAGES = 30

        /*
         * AI ko ek request mein maximum messages
         * bhejne ki limit.
         */
        private const val MAX_CONTEXT_MESSAGES = 20

        /*
         * Har individual message ki maximum length.
         */
        private const val MAX_MESSAGE_LENGTH = 8_000
    }


    /*
     * Current conversation.
     */
    private val messages =
        mutableListOf<ConversationMessage>()


    /* =====================================================
       ADD USER MESSAGE
    ===================================================== */

    fun addUserMessage(
        message: String
    ) {

        addMessage(
            ConversationMessage(
                role = MessageRole.USER,
                content =
                    sanitizeMessage(message)
            )
        )

    }


    /* =====================================================
       ADD ASSISTANT MESSAGE
    ===================================================== */

    fun addAssistantMessage(
        message: String
    ) {

        addMessage(
            ConversationMessage(
                role = MessageRole.ASSISTANT,
                content =
                    sanitizeMessage(message)
            )
        )

    }


    /* =====================================================
       ADD SYSTEM MESSAGE
    ===================================================== */

    fun addSystemMessage(
        message: String
    ) {

        addMessage(
            ConversationMessage(
                role = MessageRole.SYSTEM,
                content =
                    sanitizeMessage(message)
            )
        )

    }


    /* =====================================================
       ADD MESSAGE
    ===================================================== */

    private fun addMessage(
        message: ConversationMessage
    ) {

        if (
            message.content.isBlank()
        ) {
            return
        }


        messages.add(
            message
        )


        trimConversation()

    }


    /* =====================================================
       GET ALL MESSAGES
    ===================================================== */

    fun getMessages():
        List<ConversationMessage> {

        return messages.toList()

    }


    /* =====================================================
       GET RECENT CONTEXT
    ===================================================== */

    fun getRecentContext():
        List<ConversationMessage> {

        if (
            messages.size <=
            MAX_CONTEXT_MESSAGES
        ) {

            return messages.toList()

        }


        return messages
            .takeLast(
                MAX_CONTEXT_MESSAGES
            )

    }


    /* =====================================================
       BUILD AI REQUEST
    ===================================================== */

    fun buildAIRequest(
        userMessage: String,
        deviceContext:
            Map<String, String> =
            emptyMap()
    ): AIRequest {

        return AIRequest(
            message =
                sanitizeMessage(
                    userMessage
                ),
            conversationHistory =
                getRecentContext(),
            deviceContext =
                deviceContext
        )

    }


    /* =====================================================
       PROCESS USER MESSAGE
    ===================================================== */

    fun prepareUserMessage(
        message: String
    ): AIRequest {

        addUserMessage(
            message
        )


        return buildAIRequest(
            userMessage = message
        )

    }


    /* =====================================================
       PROCESS AI RESPONSE
    ===================================================== */

    fun saveAIResponse(
        response: String
    ) {

        addAssistantMessage(
            response
        )

    }


    /* =====================================================
       SAVE COMPLETE AI RESPONSE
    ===================================================== */

    fun saveAIResponse(
        response:
            JarvisAIResponse
    ) {

        if (
            response.text.isNotBlank()
        ) {

            addAssistantMessage(
                response.text
            )

        }

    }


    /* =====================================================
       LAST USER MESSAGE
    ===================================================== */

    fun getLastUserMessage():
        String? {

        return messages
            .asReversed()
            .firstOrNull {
                it.role ==
                    MessageRole.USER
            }
            ?.content

    }


    /* =====================================================
       LAST ASSISTANT MESSAGE
    ===================================================== */

    fun getLastAssistantMessage():
        String? {

        return messages
            .asReversed()
            .firstOrNull {
                it.role ==
                    MessageRole.ASSISTANT
            }
            ?.content

    }


    /* =====================================================
       LAST MESSAGE
    ===================================================== */

    fun getLastMessage():
        ConversationMessage? {

        return messages.lastOrNull()

    }


    /* =====================================================
       MESSAGE COUNT
    ===================================================== */

    fun getMessageCount():
        Int {

        return messages.size

    }


    /* =====================================================
       CHECK EMPTY
    ===================================================== */

    fun isEmpty():
        Boolean {

        return messages.isEmpty()

    }


    /* =====================================================
       CLEAR CONVERSATION
    ===================================================== */

    fun clearConversation() {

        messages.clear()

    }


    /* =====================================================
       START NEW SESSION
    ===================================================== */

    fun startNewSession() {

        messages.clear()

    }


    /* =====================================================
       TRIM CONVERSATION
    ===================================================== */

    private fun trimConversation() {

        while (
            messages.size >
            MAX_MESSAGES
        ) {

            messages.removeAt(
                0
            )

        }

    }


    /* =====================================================
       SANITIZE MESSAGE
    ===================================================== */

    private fun sanitizeMessage(
        message: String
    ): String {

        return message
            .trim()
            .take(
                MAX_MESSAGE_LENGTH
            )

    }


    /* =====================================================
       CONVERSATION SUMMARY
    ===================================================== */

    fun getConversationSummary():
        String {

        if (
            messages.isEmpty()
        ) {

            return "No conversation yet."

        }


        val userMessages =
            messages.count {
                it.role ==
                    MessageRole.USER
            }


        val assistantMessages =
            messages.count {
                it.role ==
                    MessageRole.ASSISTANT
            }


        return buildString {

            append(
                "Messages: ${messages.size}"
            )

            append(
                " | User: $userMessages"
            )

            append(
                " | JARVIS: $assistantMessages"
            )

        }

    }


    /* =====================================================
       EXPORT CONTEXT
    ===================================================== */

    fun exportContext():
        List<Map<String, String>> {

        return getRecentContext()
            .map {

                mapOf(
                    "role" to
                        it.role.name.lowercase(),

                    "content" to
                        it.content,

                    "timestamp" to
                        it.timestamp.toString()
                )

            }

    }


    /* =====================================================
       RESTORE CONTEXT
    ===================================================== */

    fun restoreContext(
        contextMessages:
            List<ConversationMessage>
    ) {

        messages.clear()


        contextMessages
            .takeLast(
                MAX_MESSAGES
            )
            .forEach {

                if (
                    it.content.isNotBlank()
                ) {

                    messages.add(
                        it
                    )

                }

            }

    }


    /* =====================================================
       CONTEXT OBJECT
    ===================================================== */

    fun getContextObject():
        ConversationContext {

        return ConversationContext(
            messages =
                getRecentContext(),
            messageCount =
                messages.size,
            lastUserMessage =
                getLastUserMessage(),
            lastAssistantMessage =
                getLastAssistantMessage()
        )

    }

}


/* =========================================================
   CONVERSATION CONTEXT
========================================================= */

data class ConversationContext(

    val messages:
        List<ConversationMessage>,

    val messageCount:
        Int,

    val lastUserMessage:
        String?,

    val lastAssistantMessage:
        String?
)
