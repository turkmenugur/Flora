package com.trkmn.flora.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantAnalysisDao {
    @Query("SELECT * FROM plant_analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<PlantAnalysisEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: PlantAnalysisEntity): Long
    
    @Delete
    suspend fun deleteAnalysis(analysis: PlantAnalysisEntity)
} 