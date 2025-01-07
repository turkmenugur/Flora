package com.trkmn.flora.di

import android.content.Context
import androidx.room.Room
import com.trkmn.flora.data.AppDatabase
import com.trkmn.flora.data.PlantAnalysisDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flora_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    @Singleton
    fun providePlantAnalysisDao(database: AppDatabase): PlantAnalysisDao {
        return database.plantAnalysisDao()
    }
} 