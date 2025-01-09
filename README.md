# 🌱 Flora - Yapay Zeka ile Bitki Türü ve Hastalığı Tanımlama Uygulaması

Flora, Generative AI ve modern mobil teknolojileri bir araya getirerek bitki hastalıklarını analiz etmenize ve bitkilerinizin durumunu takip etmenize yardımcı olan yenilikçi bir mobil uygulamadır. 

**Flora** sayesinde bitkilerinizin sağlığını koruyun ve daha bilinçli bir şekilde bakım yapın.

---

## 🚀 Özellikler

### 🖼️ ** AI ile Görsel Analiz**
Kameranızı kullanarak bitkilerinizin fotoğrafını çekin ve bitkinizin sağlığını anında analiz edin.
Ya da bir yerde dolaşırken hiç bilmediğiniz  bir çiçek, ağaç veya herhangi bir bitki mi gördünüz? Hemen Flora'yı açın kameranızla bitkinin görüntüsünü çekin. Flora bitkinin ne olduğunu ve hakkındaki bir çok bilgiyi size söyleyecek.

---

### 📂 **Yerel Tabanlı Veri Depolama**
Analiz sonuçlarınızı cihazınızda saklayabilirsiniz.  
Bitkinizle ilgili Flora'nın size sunmuş olduğu analizi fotoğrafını çektiğiniz konum ile birlikte kaydedip dilediğiniz zaman tekrar inceleyebilirsiniz.

---

## 📂 Proje Yapısı

```plaintext
FLORA
├── src/main/java/com/trkmn/flora
│   ├── config
│   │   └── RemoteConfigManager.kt
│   ├── data
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt
│   │   ├── PlantAnalysisDao.kt
│   │   └── PlantAnalysisEntity.kt
│   ├── di
│   │   └── DatabaseModule.kt
│   ├── navigation
│   │   └── AppNavigation.kt
│   ├── repository
│   │   └── PlantAnalysisRepository.kt
│   ├── service
│   │   └── GeminiService.kt
│   └── ui
│       ├── components
│       │   └── CameraView.kt
│       └── screens
│           ├── analysis
│           │   ├── AnalysisResult.kt
│           │   └── AnalysisScreen.kt
│           ├── detail
│           │   └── DetailScreen.kt
│           ├── home
│           │   └── HomeScreen.kt
│           └── profile
│           │   └── ProfileScreen.kt
│           └── settings
│               └── SettingsScreen.kt
```
🛠️ Kullanılan Teknolojiler
Programlama Dili: Kotlin
Arayüz: Jetpack Compose
Veri Tabanı: Room
Bağlantı Yönetimi: RemoteConfig
Bağımlılık Yönetimi: Hilt
Gen AI: Gemini 

