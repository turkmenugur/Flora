package com.trkmn.flora.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.trkmn.flora.data.PlantAnalysisDao
import com.trkmn.flora.data.PlantAnalysisEntity
import com.trkmn.flora.service.PlantAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantAnalysisRepository @Inject constructor(
    private val plantAnalysisDao: PlantAnalysisDao
) {
    suspend fun saveAnalysis(
        context: Context,
        imageUri: Uri,
        result: PlantAnalysisResult
    ): Long = withContext(Dispatchers.IO) {
        // Resmi dahili depolamaya kaydet
        val imageFile = saveImageToInternalStorage(context, imageUri)
        
        // Konum bilgisini al
        var latitude: Double? = null
        var longitude: Double? = null
        var locationName: String? = null

        // Konum izinlerini kontrol et
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    
                    // Konum adını al
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        locationName = addresses[0].let { address ->
                            buildString {
                                address.subLocality?.let { append(it).append(", ") }
                                address.locality?.let { append(it).append(", ") }
                                address.adminArea?.let { append(it) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Room'a kaydet
        val entity = PlantAnalysisEntity(
            plantType = result.plantType,
            generalInfo = result.generalInfo,
            disease = result.disease,
            confidence = result.confidence,
            imagePath = imageFile.absolutePath,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName
        )

        plantAnalysisDao.insertAnalysis(entity)
    }

    fun getAnalyses(): Flow<List<PlantAnalysisEntity>> = plantAnalysisDao.getAllAnalyses()

    private suspend fun saveImageToInternalStorage(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        val fileName = "PLANT_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
        
        file
    }

    suspend fun deleteAnalysis(analysis: PlantAnalysisEntity) {
        plantAnalysisDao.deleteAnalysis(analysis)
        // Resmi de sil
        try {
            File(analysis.imagePath).delete()
        } catch (e: Exception) {
            // Dosya silme hatası
        }
    }
} 