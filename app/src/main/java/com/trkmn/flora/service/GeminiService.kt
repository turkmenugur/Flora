package com.trkmn.flora.service

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.trkmn.flora.config.RemoteConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager
) {
    private var generativeModel: GenerativeModel? = null
    
    private suspend fun initializeModel() {
        if (generativeModel == null) {
            val apiKey = remoteConfigManager.getGeminiApiKey()
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 2048
                }
            )
        }
    }

    suspend fun analyzePlantImage(bitmap: Bitmap): PlantAnalysisResult = withContext(Dispatchers.IO) {
        try {
            initializeModel()
            
            val prompt = """
                Sen bir bitki uzmanısın. Bu görüntüyü analiz et ve bana şu bilgileri ver:
                1. Bu bir bitki mi? Eğer bitki değilse "NOT_A_PLANT" yanıtını ver.
                2. Eğer bitkiyse:
                   - Bitkinin Türkçe ve bilimsel adı nedir?
                   - Bitkinin genel özellikleri nelerdir? (Yetişme koşulları, bakım ihtiyaçları, kullanım alanları)
                   - Bitkinin sağlık durumu nasıl? Herhangi bir hastalık veya zararlı belirtisi var mı?
                   - Eğer hastalık varsa, tedavi önerileri nelerdir?
                   - Bu analizin doğruluk oranı nedir? (0-100 arası)
                
                Yanıtını tam olarak bu formatta ver ve her bölümü yeni bir satırda başlat:
                YES
                [Bitkinin Türkçe Adı (Bilimsel Adı)]
                [Genel Özellikler ve Bakım Bilgileri]
                [Sağlık Durumu ve Varsa Tedavi Önerileri]
                [Doğruluk Oranı]

                Örnek yanıt:
                YES
                Kırmızı Gül (Rosa damascena)
                Güneşli ortamları sever, düzenli sulama gerektirir. Bahçe ve süs bitkisi olarak kullanılır.
                Yapraklarda küf belirtisi var. Havalandırmayı artırın ve mantar ilacı uygulayın.
                85
            """.trimIndent()

            val response = generativeModel?.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            ) ?: throw Exception("Model başlatılamadı")

            val result = response.text?.trim() ?: throw Exception("API'den yanıt alınamadı")
            val lines = result.split("\n").filter { it.isNotBlank() }

            if (lines.isEmpty()) {
                throw Exception("API yanıtı boş")
            }

            if (lines[0].trim().uppercase() == "NOT_A_PLANT") {
                return@withContext PlantAnalysisResult(
                    isPlant = false,
                    plantType = "",
                    generalInfo = "",
                    disease = null,
                    confidence = 0f
                )
            }

            if (lines.size < 5) {
                throw Exception("API yanıtı eksik bilgi içeriyor")
            }

            PlantAnalysisResult(
                isPlant = true,
                plantType = lines[1].trim(),
                generalInfo = lines[2].trim(),
                disease = lines[3].trim().let { disease ->
                    when {
                        disease.contains("sağlıklı", ignoreCase = true) -> null
                        disease.contains("hastalık tespit edilemedi", ignoreCase = true) -> null
                        disease.contains("hastalık belirtisi yok", ignoreCase = true) -> null
                        disease.contains("hastalık bulunmamaktadır", ignoreCase = true) -> null
                        disease.isBlank() -> null
                        else -> disease
                    }
                },
                confidence = try {
                    lines[4].replace(Regex("[^0-9.]"), "").toFloat() / 100
                } catch (e: Exception) {
                    0.7f // Varsayılan güven değeri
                }
            )
        } catch (e: Exception) {
            throw PlantAnalysisException("Görüntü analizi sırasında bir hata oluştu: ${e.message ?: "Bilinmeyen hata"}")
        }
    }
}

data class PlantAnalysisResult(
    val isPlant: Boolean,
    val plantType: String,
    val generalInfo: String,
    val disease: String?,
    val confidence: Float,
    val locationName: String? = null
)

class PlantAnalysisException(message: String) : Exception(message) 