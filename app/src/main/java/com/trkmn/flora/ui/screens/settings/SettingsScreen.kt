package com.trkmn.flora.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var notifications by remember { mutableStateOf(true) }
    var locationServices by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
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
        ) {
            // Bildirim Ayarları
            SettingsSection(title = "Bildirim Ayarları") {
                SettingsSwitch(
                    title = "Bildirimler",
                    subtitle = "Analiz sonuçları için bildirim al",
                    icon = Icons.Default.Notifications,
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Konum Ayarları
            SettingsSection(title = "Konum Ayarları") {
                SettingsSwitch(
                    title = "Konum Servisleri",
                    subtitle = "Bitki fotoğraflarında konum bilgisini kaydet",
                    icon = Icons.Default.LocationOn,
                    checked = locationServices,
                    onCheckedChange = { locationServices = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Uygulama Hakkında
            SettingsSection(title = "Uygulama Hakkında") {
                SettingsItem(
                    title = "Versiyon",
                    subtitle = "1.0.0",
                    icon = Icons.Default.Info
                )
                
                SettingsItem(
                    title = "Gizlilik Politikası",
                    subtitle = "Gizlilik politikamızı görüntüle",
                    icon = Icons.Default.Info,
                    onClick = { /* Gizlilik politikası sayfasına git */ }
                )
                
                SettingsItem(
                    title = "Yardım ve Destek",
                    subtitle = "Sık sorulan sorular ve iletişim",
                    icon = Icons.Default.Info,
                    onClick = { /* Yardım sayfasına git */ }
                )
                
                SettingsItem(
                    title = "Geri Bildirim",
                    subtitle = "Hatalı analiz veya önerilerinizi bildirin",
                    icon = Icons.Default.Email,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("trkmn.ugur.12@gmail.com"))
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Flora - Geri Bildirim")
                        }
                        try {
                            context.startActivity(android.content.Intent.createChooser(intent, "E-posta gönder"))
                        } catch (e: Exception) {
                            // E-posta uygulaması bulunamadı
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Uğur Türkmen tarafından geliştirildi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { 
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { 
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable(enabled = onClick != null, onClick = onClick ?: {})
    )
} 