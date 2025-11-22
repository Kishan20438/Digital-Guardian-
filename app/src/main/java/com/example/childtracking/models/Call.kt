package com.example.childtracking.models

data class Call(
    val id: Long,
    val recording: String,
    val callType: String,
    val contactName: String,
    val phoneNumber: String,
    val duration: String,
    val dateTime: String,
    val location: String,
    val latitude: String,
    val longitude: String,
    val accuracy: String
) 