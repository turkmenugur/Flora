package com.trkmn.flora.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trkmn.flora.R
import com.trkmn.flora.data.PlantAnalysisEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    analysis: PlantAnalysisEntity,
    onNavigateBack: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(analysis.plantType) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = File(analysis.imagePath),
                    contentDescription = "Bitki Fotoğrafı",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Bitki Türü") },
                        supportingContent = { Text(analysis.plantType) },
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
                        supportingContent = { Text(analysis.generalInfo) },
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
                        supportingContent = { Text(analysis.disease ?: "Sağlıklı") },
                        leadingContent = {
                            Icon(
                                painter = painterResource(
                                    if (analysis.disease == null) R.drawable.ic_check else R.drawable.ic_warning
                                ),
                                contentDescription = null,
                                tint = if (analysis.disease == null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    )

                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text("Doğruluk Oranı") },
                        supportingContent = { Text("%${(analysis.confidence * 100).toInt()}") },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    analysis.locationName?.let {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Konum") },
                            supportingContent = { Text(it) },
                            leadingContent = {
                                Text("📍")
                            }
                        )
                    }

                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text("Analiz Tarihi") },
                        supportingContent = { Text(dateFormat.format(analysis.timestamp)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
} 