package com.trkmn.flora.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "plant_analyses")
data class PlantAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantType: String,
    val generalInfo: String,
    val disease: String?,
    val confidence: Float,
    val imagePath: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val timestamp: Date = Date()
) 