package com.trkmn.flora.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trkmn.flora.R
import com.trkmn.flora.data.PlantAnalysisEntity
import com.trkmn.flora.repository.PlantAnalysisRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    repository: PlantAnalysisRepository,
    onNavigateToDetail: (PlantAnalysisEntity) -> Unit
) {
    var analyses by remember { mutableStateOf<List<PlantAnalysisEntity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        repository.getAnalyses().collect { records ->
            analyses = records
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bitkilerim") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "İstatistikler",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatisticItem(
                                icon = R.drawable.ic_plant,
                                value = analyses.size.toString(),
                                label = "Toplam Analiz"
                            )
                            
                            StatisticItem(
                                icon = R.drawable.ic_warning,
                                value = analyses.count { it.disease != null && it.disease.isNotBlank() }.toString(),
                                label = "Hastalıklı"
                            )
                            
                            StatisticItem(
                                icon = R.drawable.ic_check,
                                value = analyses.count { it.disease == null || it.disease.isBlank() }.toString(),
                                label = "Sağlıklı"
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Son Analizler",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(analyses.take(5)) { analysis ->
                AnalysisHistoryItem(analysis, onNavigateToDetail)
            }

            if (analyses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz analiz yapılmamış",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: Int,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalysisHistoryItem(
    analysis: PlantAnalysisEntity,
    onNavigateToDetail: (PlantAnalysisEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { onNavigateToDetail(analysis) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(60.dp)
            ) {
                AsyncImage(
                    model = File(analysis.imagePath),
                    contentDescription = "Bitki Fotoğrafı",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = analysis.plantType,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = analysis.generalInfo.take(30) + if (analysis.generalInfo.length > 30) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
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
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Detaylar",
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 