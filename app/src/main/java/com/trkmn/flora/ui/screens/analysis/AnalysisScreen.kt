package com.trkmn.flora.ui.screens.analysis

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trkmn.flora.R
import com.trkmn.flora.service.GeminiService
import com.trkmn.flora.service.PlantAnalysisException
import com.trkmn.flora.service.PlantAnalysisResult
import com.trkmn.flora.repository.PlantAnalysisRepository
import kotlinx.coroutines.launch
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    imageUri: Uri?,
    onNavigateBack: () -> Unit,
    onSaveAnalysis: () -> Unit,
    geminiService: GeminiService,
    repository: PlantAnalysisRepository
) {
    val context = LocalContext.current
    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<PlantAnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAnalysisMarkedAsWrong by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var manualLocation by remember { mutableStateOf("") }
    var useDeviceLocation by remember { mutableStateOf(false) }
    var deviceLocation by remember { mutableStateOf<Location?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var locationName by remember { mutableStateOf<String?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    suspend fun getLocationDetails() {
        try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                
                val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
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
            errorMessage = "Konum bilgisi alınamadı: ${e.message}"
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                coroutineScope.launch {
                    getLocationDetails()
                }
            }
        }
    }

    fun checkAndRequestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                coroutineScope.launch {
                    getLocationDetails()
                }
            }
        } else {
            locationPermissionLauncher.launch(permissions)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    fun performAnalysis(bitmap: Bitmap) {
        isAnalyzing = true
        errorMessage = null
        analysisResult = null
        
        coroutineScope.launch {
            try {
                analysisResult = geminiService.analyzePlantImage(bitmap)
                if (!analysisResult!!.isPlant) {
                    errorMessage = "Bu görüntü bir bitki değil"
                }
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is PlantAnalysisException -> e.message
                    else -> "Görüntü analizi sırasında bir hata oluştu: ${e.message}"
                }
            } finally {
                isAnalyzing = false
            }
        }
    }

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }
                performAnalysis(bitmap)
            } catch (e: Exception) {
                errorMessage = "Görüntü yüklenirken bir hata oluştu: ${e.message}"
                isAnalyzing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bitki Analizi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Bitki Fotoğrafı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Fotoğraf yüklenemedi")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (isAnalyzing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "AI",
                            modifier = Modifier
                                .size(64.dp)
                                .scale(scale),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Flora Bitkiyi Analiz Ediyor...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .width(200.dp)
                                .padding(16.dp)
                        )
                    }
                } else if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_warning),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else if (analysisResult != null && analysisResult!!.isPlant) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                ListItem(
                                    headlineContent = { Text("Bitki Türü") },
                                    supportingContent = { 
                                        Text(
                                            analysisResult?.plantType ?: "",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_plant),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )

                                HorizontalDivider()
                                
                                ListItem(
                                    headlineContent = { Text("Genel Bilgiler") },
                                    supportingContent = { 
                                        Text(
                                            analysisResult?.generalInfo ?: "",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_info),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )

                                HorizontalDivider()
                                
                                ListItem(
                                    headlineContent = { Text("Sağlık Durumu") },
                                    supportingContent = { 
                                        Text(
                                            analysisResult?.disease ?: "Sağlıklı",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(
                                                if (analysisResult?.disease == null) 
                                                    R.drawable.ic_check 
                                                else 
                                                    R.drawable.ic_warning
                                            ),
                                            contentDescription = null,
                                            tint = if (analysisResult?.disease == null)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.error
                                        )
                                    }
                                )

                                HorizontalDivider()
                                
                                ListItem(
                                    headlineContent = { Text("Doğruluk Oranı") },
                                    supportingContent = { 
                                        Text(
                                            "${(analysisResult?.confidence?.times(100))?.toInt()}%",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        //Paylaş Butonu
                                        OutlinedButton(
                                            onClick = {
                                                val shareText = buildString {
                                                    appendLine("🌿 Bitki Analiz Sonucu")
                                                    appendLine("------------------------")
                                                    appendLine("📝 Tür: ${analysisResult?.plantType}")
                                                    appendLine("ℹ️ Genel Bilgiler:")
                                                    appendLine(analysisResult?.generalInfo)
                                                    appendLine("🏥 Sağlık Durumu:")
                                                    appendLine(analysisResult?.disease ?: "Sağlıklı")
                                                    appendLine("📊 Doğruluk: ${(analysisResult?.confidence?.times(100))?.toInt()}%")
                                                    appendLine("\n📱 Flora uygulaması ile analiz edildi")
                                                }

                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                }

                                                val shareIntent = Intent.createChooser(sendIntent, "Analiz Sonucunu Paylaş")
                                                context.startActivity(shareIntent)
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_share),
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Paylaş")
                                        }
                                        //Yeniden Analiz Et Butonu
                                        OutlinedButton(
                                            onClick = {
                                                imageUri?.let { uri ->
                                                    try {
                                                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                                                        } else {
                                                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                                        }
                                                        performAnalysis(bitmap)
                                                    } catch (e: Exception) {
                                                        errorMessage = "Görüntü yüklenirken bir hata oluştu: ${e.message}"
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_refresh),
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Yeniden")
                                        }
                                    }
                                    //Analiizi Kaydet Butonu
                                    FilledTonalButton (
                                        onClick = { showLocationDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_save),
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Analizi Kaydet")
                                    }

                                    if (!isAnalysisMarkedAsWrong) {
                                        TextButton(
                                            onClick = { isAnalysisMarkedAsWrong = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Analiz Hatalı mı?")
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "message/rfc822"
                                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("trkmn.ugur.12@gmail.com"))
                                                    putExtra(Intent.EXTRA_SUBJECT, "Flora - Hatalı Analiz Bildirimi")
                                                    putExtra(Intent.EXTRA_TEXT, """
                                                Analiz Sonucu:
                                                Bitki Türü: ${analysisResult?.plantType}
                                                Genel Bilgiler: ${analysisResult?.generalInfo}
                                                Sağlık Durumu: ${analysisResult?.disease ?: "Sağlıklı"}
                                                Doğruluk Oranı: %${(analysisResult?.confidence?.times(100))?.toInt()}
                                                
                                                Cihaz Bilgileri:
                                                Model: ${Build.MODEL}
                                                Android Sürümü: ${Build.VERSION.RELEASE}
                                                
//                                                Konum Bilgisi:
//                                                ${if (useDeviceLocation) "Cihaz Konumu: ${deviceLocation?.latitude}, ${deviceLocation?.longitude}" else "Manuel Konum: $manualLocation"}
//                                            """.trimIndent())
                                                }
                                                try {
                                                    context.startActivity(Intent.createChooser(intent, "E-posta gönder"))
                                                } catch (e: Exception) {
                                                    errorMessage = "E-posta uygulaması bulunamadı"
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Geliştiriciyi Bilgilendir")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Konum Ekle") },
            text = {
                Column {
                    TextField(
                        value = manualLocation,
                        onValueChange = { manualLocation = it },
                        label = { Text("Konum Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useDeviceLocation,
                            onCheckedChange = { checked ->
                                useDeviceLocation = checked
                                if (checked) {
                                    checkAndRequestLocationPermission()
                                }
                            }
                        )
                        Text("Cihaz konumunu kullan")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationDialog = false
                        coroutineScope.launch {
                            try {
                                isSaving = true
                                imageUri?.let { uri ->
                                    analysisResult?.let { result ->
                                        repository.saveAnalysis(
                                            context = context,
                                            imageUri = uri,
                                            result = result.copy(
                                                locationName = if (useDeviceLocation) locationName else manualLocation.takeIf { it.isNotBlank() }
                                            )
                                        )
                                        onSaveAnalysis()
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Kaydetme sırasında bir hata oluştu: ${e.message}"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Kaydet")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
} 