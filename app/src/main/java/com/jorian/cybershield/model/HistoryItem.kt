package com.jorian.cybershield.model

data class HistoryItem(
    val url: String,
    val status: ScanStatus,
    val score: Int,
    val dateTime: String
)