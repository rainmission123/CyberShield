package com.jorian.cybershield.network

data class VirusTotalSubmitResponse(
    val data: VirusTotalSubmitData?
)

data class VirusTotalSubmitData(
    val id: String?
)

data class VirusTotalAnalysisResponse(
    val data: VirusTotalAnalysisData?
)

data class VirusTotalAnalysisData(
    val attributes: VirusTotalAttributes?
)

data class VirusTotalAttributes(
    val stats: VirusTotalStats?
)

data class VirusTotalStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val harmless: Int = 0,
    val undetected: Int = 0,
    val timeout: Int = 0
)