package com.messenger

data class Message(
    val id: String = "",
    val text: String = "",
    val receiverText: String = "",
    val senderPhoneNumber: String? = "",
    val receiverPhoneNumber: String = "",
    val senderName: String? = "",
    val receiverName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)



