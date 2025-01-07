package com.trkmn.flora.ui.screens.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.trkmn.flora.R
import com.trkmn.flora.data.PlantAnalysisEntity
import com.trkmn.flora.repository.PlantAnalysisRepository
import com.trkmn.flora.ui.components.CameraView
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAnalysis: (Uri) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (PlantAnalysisEntity) -> Unit,
    repository: PlantAnalysisRepository
) {
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }
    var analyses by remember { mutableStateOf<List<PlantAnalysisEntity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        repository.getAnalyses().collect { records ->
            analyses = records
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onNavigateToAnalysis(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            showCamera = true
        }
    }

    fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            showCamera = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    if (showCamera) {
        CameraView(
            onImageCaptured = { uri ->
                showCamera = false
                onNavigateToAnalysis(uri)
            },
            onError = { /* Hata durumunu göster */ },
            onClose = { showCamera = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Flora") },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                painter = painterResource(R.drawable.ic_plant),
                                contentDescription = "Profil"
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = "Ayarlar"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column {
                    FloatingActionButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_gallery),
                            contentDescription = "Galeriden Seç"
                        )
                    }
                    FloatingActionButton(
                        onClick = { checkAndRequestPermissions() },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = "Fotoğraf Çek"
                        )
                    }
                }
            }
        ) { paddingValues ->
            if (analyses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz analiz yapılmamış.\nKamera veya galeriden bir bitki fotoğrafı seçin.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(analyses) { analysis ->
                        AnalysisHistoryCard(analysis, onNavigateToDetail, repository)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AnalysisHistoryCard(
    analysis: PlantAnalysisEntity,
    onNavigateToDetail: (PlantAnalysisEntity) -> Unit,
    repository: PlantAnalysisRepository
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr")) }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Analizi Sil") },
            text = { Text("Bu analizi silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            repository.deleteAnalysis(analysis)
                        }
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { onNavigateToDetail(analysis) },
                onLongClick = { showMenu = true }
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = File(analysis.imagePath),
                    contentDescription = "Bitki Fotoğrafı",
                    modifier = Modifier
                        .size(80.dp),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = analysis.plantType,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = analysis.generalInfo.take(50) + if (analysis.generalInfo.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    analysis.locationName?.let {
                        Text(
                            text = "📍 $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = dateFormat.format(analysis.timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Paylaş") },
                onClick = {
                    showMenu = false
                    val shareText = """
                        Bitki Türü: ${analysis.plantType}
                        Genel Bilgiler: ${analysis.generalInfo}
                        ${analysis.disease?.let { "Hastalık: $it" } ?: "Sağlık Durumu: Sağlıklı"}
                        Doğruluk Oranı: %${(analysis.confidence * 100).toInt()}
                        ${analysis.locationName?.let { "Konum: $it" } ?: ""}
                        Tarih: ${dateFormat.format(analysis.timestamp)}
                    """.trimIndent()
                    
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Paylaş"))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null
                    )
                }
            )
            
            DropdownMenuItem(
                text = { Text("Sil") },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
} 