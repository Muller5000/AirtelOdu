package com.example.airteldatamonitor.data.model

data class RouterSmsMessage(
    val id: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val dateStr: String,
    val isRead: Boolean,
    val isSelf: Boolean = false
) {
    /**
     * Attempts to extract a 4-8 digit verification code or OTP if present in the message.
     */
    val extractedOtp: String?
        get() {
            // Match patterns like "code is 123456", "OTP: 1234", "123456 is your code", etc.
            val otpPattern = Regex("""(?i)(?:code|otp|pin|token|verification|password)[:\s]+([0-9]{4,8})\b|\b([0-9]{4,8})\s+(?:is your|is the)""")
            val match = otpPattern.find(content)
            if (match != null) {
                return match.groupValues[1].ifEmpty { match.groupValues[2] }
            }
            return null
        }
}

data class SmsCenterState(
    val messages: List<RouterSmsMessage> = emptyList(),
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val maxCapacity: Int = 500,
    val smscNumber: String = ""
)

data class UssdResponse(
    val success: Boolean,
    val message: String,
    val needReply: Boolean = false,
    val rawCode: String = ""
)
