package com.trkmn.flora.ui.screens.analysis

data class AnalysisResult(
    val plantType: String,
    val disease: String?,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
) 