package com.jorian.cybershield.model

data class ScanResult(
    val url: String,
    val status: ScanStatus,
    val message: String,
    val reasons: List<String> = emptyList(),
    val score: Int = 0
)

enum class ScanStatus {
    SAFE,
    SUSPICIOUS,
    DANGEROUS
}