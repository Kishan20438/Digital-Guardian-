package com.example.childtracking.models

data class AppUsageData(
    val appName: String,
    val packageName: String,
    val version: String,
    val size: String,
    val installDate: String,
    val status: String
) 