package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Log
import java.util.Calendar

sealed class Screen {
    object Welcome : Screen()
    object Dashboard : Screen()
    object MediaPlayer : Screen()
    object Downloads : Screen()
    object Counsel : Screen()
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    DOWNLOADING, PAUSED, COMPLETED, FAILED_WIFI_REQUIRED, ERROR
}

data class ActiveDownload(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String,
    val content: String,
    val duration: String,
    val progress: Float, // 0.0f to 1.0f
    val status: DownloadStatus,
    val totalSizeMb: Float,
    val downloadedSizeMb: Float
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = SanctuaryRepository(db)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Selected Mood Filter
    private val _selectedMood = MutableStateFlow<String>("Anxious")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    // Global Language Preference
    val language: StateFlow<String> = repository.getLanguageFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    // Notifications Preferences
    val morningTime: StateFlow<String> = repository.getMorningNotificationTimeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00 AM")

    val nightTime: StateFlow<String> = repository.getNightNotificationTimeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "09:00 PM")

    // Bookmarked Favorites Flow
    val favorites: StateFlow<List<FavoriteItem>> = repository.getFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offline Downloads Flow
    val downloads: StateFlow<List<DownloadedResource>> = repository.getDownloadsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Scripture (The Bible Project Reflection) States
    private val _dailyScriptureText = MutableStateFlow<String>("")
    val dailyScriptureText: StateFlow<String> = _dailyScriptureText.asStateFlow()

    private val _dailyScriptureReference = MutableStateFlow<String>("")
    val dailyScriptureReference: StateFlow<String> = _dailyScriptureReference.asStateFlow()

    private val _dailyScriptureExplanation = MutableStateFlow<String>("")
    val dailyScriptureExplanation: StateFlow<String> = _dailyScriptureExplanation.asStateFlow()

    private val _isFetchingScripture = MutableStateFlow<Boolean>(false)
    val isFetchingScripture: StateFlow<Boolean> = _isFetchingScripture.asStateFlow()

    private val _selectedScriptureTheme = MutableStateFlow<String>("Covenant")
    val selectedScriptureTheme: StateFlow<String> = _selectedScriptureTheme.asStateFlow()

    // App-Wide Network State
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    val syncOverWifiOnly: StateFlow<Boolean> = repository.getSyncOverWifiOnlyFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleSyncOverWifiOnly() {
        viewModelScope.launch {
            repository.saveSyncOverWifiOnly(!syncOverWifiOnly.value)
        }
    }

    private val _simulateMobileData = MutableStateFlow(false)
    val simulateMobileData: StateFlow<Boolean> = _simulateMobileData.asStateFlow()

    fun toggleSimulateMobileData() {
        _simulateMobileData.value = !_simulateMobileData.value
    }

    private val _selectedSoundscape = MutableStateFlow("Forest Rainfall")
    val selectedSoundscape: StateFlow<String> = _selectedSoundscape.asStateFlow()

    private val _meditationAmbientVolume = MutableStateFlow(0.7f)
    val meditationAmbientVolume: StateFlow<Float> = _meditationAmbientVolume.asStateFlow()

    private val _meditationGuidanceVolume = MutableStateFlow(0.5f)
    val meditationGuidanceVolume: StateFlow<Float> = _meditationGuidanceVolume.asStateFlow()

    fun selectSoundscape(name: String) {
        _selectedSoundscape.value = name
        if (_isMeditationRunning.value) {
            SanctuaryAudioEngine.startPlaying(name)
        }
    }

    fun setMeditationAmbientVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _meditationAmbientVolume.value = clamped
        SanctuaryAudioEngine.ambientVolume = clamped
    }

    fun setMeditationGuidanceVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _meditationGuidanceVolume.value = clamped
        SanctuaryAudioEngine.guidanceVolume = clamped
    }

    // Active Downloads Map
    private val _activeDownloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val activeDownloads: StateFlow<List<ActiveDownload>> = _activeDownloads.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    // Meditation Timer States
    private val _meditationDuration = MutableStateFlow(5) // 5, 10, or 20 minutes
    val meditationDuration: StateFlow<Int> = _meditationDuration.asStateFlow()

    private val _meditationTimeRemaining = MutableStateFlow(300L) // countdown seconds
    val meditationTimeRemaining: StateFlow<Long> = _meditationTimeRemaining.asStateFlow()

    private val _isMeditationRunning = MutableStateFlow(false)
    val isMeditationRunning: StateFlow<Boolean> = _isMeditationRunning.asStateFlow()

    private var meditationTimerJob: Job? = null

    // Daily Affirmation States
    private val _dailyAffirmationText = MutableStateFlow<String>("")
    val dailyAffirmationText: StateFlow<String> = _dailyAffirmationText.asStateFlow()

    private val _isFetchingAffirmation = MutableStateFlow<Boolean>(false)
    val isFetchingAffirmation: StateFlow<Boolean> = _isFetchingAffirmation.asStateFlow()

    private val localAffirmationsEn = listOf(
        "You are worthy of peace, rest, and happiness. Take a deep breath and let go of what you cannot control.",
        "Your journey is unique, and you are growing at your own perfect pace.",
        "You have the strength to navigate today with grace and patience.",
        "Every breath is a fresh beginning. Let peace fill your heart.",
        "You are deeply valued, loved, and never alone in your struggles.",
        "Your mind deserves rest. Give yourself permission to slow down and breathe.",
        "There is a quiet strength within you that can weather any storm."
    )

    private val localAffirmationsEs = listOf(
        "Eres digno de paz, descanso y felicidad. Respira hondo y suelta lo que no puedes controlar.",
        "Tu camino es único y estás creciendo a tu propio ritmo perfecto.",
        "Tienes la fuerza para superar el día de hoy con gracia y paciencia.",
        "Cada respiración es un nuevo comienzo. Deja que la paz llene tu corazón.",
        "Eres profundamente valorado, amado y nunca estás solo en tus luchas.",
        "Tu mente merece descanso. Date permiso para ir más despacio y respirar.",
        "Hay una fuerza silenciosa dentro de ti que puede superar cualquier tormenta."
    )

    private val localAffirmationsTl = listOf(
        "Karapat-dapat ka sa kapayapaan, pahinga, at kaligayahan. Huminga nang malalim at isuko ang mga bagay na hindi mo kontrolado.",
        "Ang iyong lakbayin ay natatangi, at ikaw ay lumalago sa iyong sariling perpektong bilis.",
        "May sapat kang lakas upang harapin ang araw na ito nang may biyaya at tiyaga.",
        "Ang bawat hininga ay isang bagong simula. Hayaan mong punuin ng kapayapaan ang iyong puso.",
        "Ikaw ay lubos na pinahahalagahan, minamahal, at hindi nag-iisa sa iyong mga pinagdaraanan.",
        "Karapat-dapat sa pahinga ang iyong isipan. Bigyan ang iyong sarili ng pahintulot na magdahan-dahan.",
        "Mayroong tahimik na lakas sa loob mo na kayang lampasan ang anumang bagyo."
    )

    // Media Player States
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    val bibleProjectMediaItems = listOf(
        // English
        MediaItem(
            id = "v1_en",
            title = "The Bible Project: Covenant",
            subtitle = "Theme Video Series - Genesis to Revelation",
            type = "video",
            url = "https://bibleproject.com/explore/video/covenant/",
            duration = "5:40",
            language = "en",
            lyricOrScripture = "God enters a sacred agreement to restore relationship with humanity, culminating in Jesus the ultimate Covenant keeper."
        ),
        MediaItem(
            id = "v2_en",
            title = "The Bible Project: Tree of Life",
            subtitle = "Theme Video Series - Wisdom & Eternity",
            type = "video",
            url = "https://bibleproject.com/explore/video/tree-of-life/",
            duration = "4:15",
            language = "en",
            lyricOrScripture = "From Eden to the New Jerusalem, God's desire is to share His eternal life-giving presence with all who trust Him."
        ),
        MediaItem(
            id = "w1_en",
            title = "The Bible Project: Grace & Peace",
            subtitle = "Epistles Meditative Audio Track",
            type = "audio_worship",
            url = "https://bibleproject.com/podcast/shema-listen/",
            duration = "3:45",
            language = "en",
            lyricOrScripture = "Peace and Grace be multiplied to you in the knowledge of God and of Jesus our Lord. Cast your anxieties upon Him."
        ),
        MediaItem(
            id = "p1_en",
            title = "The Bible Project: Wisdom Literature",
            subtitle = "Guided Audio Prayer & Reflection",
            type = "audio_prayer",
            url = "https://bibleproject.com/explore/video/wisdom-proverbs/",
            duration = "6:10",
            language = "en",
            lyricOrScripture = "Fear of the Lord is the beginning of wisdom. Let us seek His peaceful paths and understand His righteous ways."
        ),

        // Spanish
        MediaItem(
            id = "v1_es",
            title = "The Bible Project: El Pacto",
            subtitle = "Serie de Videos de Temas Bíblicos",
            type = "video",
            url = "https://proyectobiblia.com/video/el-pacto/",
            duration = "6:05",
            language = "es",
            lyricOrScripture = "Dios entra en un acuerdo sagrado para restaurar la relación con la humanidad, culminando en Jesús."
        ),
        MediaItem(
            id = "v2_es",
            title = "The Bible Project: El Árbol de la Vida",
            subtitle = "Serie de Videos - Sabiduría de Dios",
            type = "video",
            url = "https://proyectobiblia.com/video/arbol-vida/",
            duration = "4:30",
            language = "es",
            lyricOrScripture = "Desde el Edén hasta la Nueva Jerusalén, el deseo de Dios es compartir Su presencia dadora de vida eterna."
        ),
        MediaItem(
            id = "w1_es",
            title = "The Bible Project: Gracia y Paz",
            subtitle = "Pista de Audio Meditativa de Epístolas",
            type = "audio_worship",
            url = "https://proyectobiblia.com/video/gracia/",
            duration = "4:00",
            language = "es",
            lyricOrScripture = "Que la gracia y la paz les sean multiplicadas en el conocimiento de Dios y de nuestro Señor Jesús."
        ),
        MediaItem(
            id = "p1_es",
            title = "The Bible Project: Literatura de Sabiduría",
            subtitle = "Reflexión y Oración de Sabios",
            type = "audio_prayer",
            url = "https://proyectobiblia.com/video/proverbios/",
            duration = "6:30",
            language = "es",
            lyricOrScripture = "El temor del Señor es el principio de la sabiduría. Busquemos Sus caminos pacíficos y rectos."
        ),

        // Tagalog
        MediaItem(
            id = "v1_tl",
            title = "The Bible Project: Ang Tipan",
            subtitle = "Serye ng Bidyo Tungkol sa Tipan",
            type = "video",
            url = "https://bibleproject.com/tagalog/",
            duration = "6:20",
            language = "tl",
            lyricOrScripture = "Pumasok ang Diyos sa isang sagradong kasunduan upang ipanumbalik ang ugnayan sa sangkatauhan sa pamamagitan ni Hesus."
        ),
        MediaItem(
            id = "v2_tl",
            title = "The Bible Project: Puno ng Buhay",
            subtitle = "Serye ng Bidyo - Karunungan ng Diyos",
            type = "video",
            url = "https://bibleproject.com/tagalog/",
            duration = "4:45",
            language = "tl",
            lyricOrScripture = "Mula sa Eden hanggang sa Bagong Jerusalem, nais ng Diyos na ibahagi ang Kanyang buhay sa lahat ng nagtitiwala."
        ),
        MediaItem(
            id = "w1_tl",
            title = "The Bible Project: Biyaya at Kapayapaan",
            subtitle = "Mapanalanging Audio Mula sa Sulat",
            type = "audio_worship",
            url = "https://bibleproject.com/tagalog/",
            duration = "4:10",
            language = "tl",
            lyricOrScripture = "Biyaya at kapayapaan ang sumagana sa inyo sa pagkakilala sa Diyos at kay Hesus na ating Panginoon."
        ),
        MediaItem(
            id = "p1_tl",
            title = "The Bible Project: Panitikan ng Karunungan",
            subtitle = "Gabay na Panalangin at Pagninilay",
            type = "audio_prayer",
            url = "https://bibleproject.com/tagalog/",
            duration = "6:40",
            language = "tl",
            lyricOrScripture = "Ang paggalang sa Panginoon ang simula ng karunungan. Hanapin natin ang Kanyang mapayapang landas."
        )
    )

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playProgress = MutableStateFlow(0f)
    val playProgress: StateFlow<Float> = _playProgress.asStateFlow()

    // Counsel AI Chat States
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _showSOSCrisisModal = MutableStateFlow(false)
    val showSOSCrisisModal: StateFlow<Boolean> = _showSOSCrisisModal.asStateFlow()

    private val _showSOSGeneralModal = MutableStateFlow(false)
    val showSOSGeneralModal: StateFlow<Boolean> = _showSOSGeneralModal.asStateFlow()

    // Track active favorite states dynamically for current items
    private val _isCurrentItemFavorited = MutableStateFlow(false)
    val isCurrentItemFavorited: StateFlow<Boolean> = _isCurrentItemFavorited.asStateFlow()

    init {
        // Observe network connectivity
        val connectivityManager = application.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val hasInternet = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isOffline.value = !hasInternet

            connectivityManager.registerDefaultNetworkCallback(object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    _isOffline.value = false
                }

                override fun onLost(network: android.net.Network) {
                    _isOffline.value = true
                }

                override fun onCapabilitiesChanged(
                    network: android.net.Network,
                    networkCapabilities: android.net.NetworkCapabilities
                ) {
                    val hasInternetCap = networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isOffline.value = !hasInternetCap
                }
            })
        } catch (e: Exception) {
            _isOffline.value = false
        }

        // Load last selected mood preference if any
        viewModelScope.launch {
            repository.getLastMoodFlow().collectLatest { lastMood ->
                if (lastMood != null) {
                    _selectedMood.value = lastMood
                }
            }
        }

        // Collect isPlaying to start/stop synthesized ambient audio engine
        viewModelScope.launch {
            _isPlaying.collect { playing ->
                if (playing) {
                    SanctuaryAudioEngine.startPlaying()
                } else {
                    SanctuaryAudioEngine.stopPlaying()
                }
            }
        }

        // Initialize default daily scripture based on Covenant theme
        fetchDailyScripture("Covenant")

        // Initialize Daily Affirmation
        fetchDailyAffirmation()

        // Auto-initialize first AI Welcome message
        resetChat()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setMood(mood: String) {
        _selectedMood.value = mood
        viewModelScope.launch {
            repository.saveLastMood(mood)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.saveLanguage(lang)
            // Refresh first message in selected language
            resetChat()
            // Fetch daily affirmation for new language
            fetchDailyAffirmation(forceRefresh = true)
        }
    }

    fun saveMorningTime(time: String) {
        viewModelScope.launch {
            repository.saveMorningNotificationTime(time)
        }
    }

    fun saveNightTime(time: String) {
        viewModelScope.launch {
            repository.saveNightNotificationTime(time)
        }
    }

    // Dynamic filtering of verse based on selected mood and language
    fun getDynamicDailyVerse(): Scripture {
        val currentMood = _selectedMood.value
        val currentLang = language.value
        return scripturesFallback.firstOrNull { 
            it.mood.equals(currentMood, ignoreCase = true) && it.language == currentLang 
        } ?: scripturesFallback.first { it.language == "en" }
    }

    // Dynamic filtering of prayer
    fun getDynamicPrayer(isMorning: Boolean): Prayer {
        val currentLang = language.value
        return prayersFallback.firstOrNull { 
            it.isMorning == isMorning && it.language == currentLang 
        } ?: prayersFallback.first { it.isMorning == isMorning && it.language == "en" }
    }

    // Favorites Interaction
    fun toggleFavorite(text: String, titleOrRef: String, type: String) {
        viewModelScope.launch {
            val lang = language.value
            val isFav = repository.isFavorite(text, lang)
            if (isFav) {
                repository.removeFavorite(text, lang)
            } else {
                repository.addFavorite(
                    FavoriteItem(
                        type = type,
                        text = text,
                        referenceOrTitle = titleOrRef,
                        mood = _selectedMood.value,
                        language = lang
                    )
                )
            }
            checkCurrentItemFavorited(text)
        }
    }

    fun checkCurrentItemFavorited(text: String) {
        viewModelScope.launch {
            _isCurrentItemFavorited.value = repository.isFavorite(text, language.value)
        }
    }

    // Media Player controls
    fun selectMedia(item: MediaItem) {
        stopMeditation()
        _currentMediaItem.value = item
        _isPlaying.value = true
        _playProgress.value = 0f
        checkCurrentItemFavorited(item.lyricOrScripture)
    }

    fun togglePlay() {
        if (!_isPlaying.value) {
            stopMeditation()
        }
        _isPlaying.value = !_isPlaying.value
    }

    // Meditation Timer Controls
    fun setMeditationDuration(minutes: Int) {
        _meditationDuration.value = minutes
        if (!_isMeditationRunning.value) {
            _meditationTimeRemaining.value = minutes * 60L
        }
    }

    fun startMeditation() {
        if (_isMeditationRunning.value) return
        _isMeditationRunning.value = true
        
        if (_isPlaying.value) {
            _isPlaying.value = false
        }
        
        SanctuaryAudioEngine.startPlaying(_selectedSoundscape.value)
        
        meditationTimerJob?.cancel()
        meditationTimerJob = viewModelScope.launch {
            while (_meditationTimeRemaining.value > 0 && _isMeditationRunning.value) {
                delay(1000)
                _meditationTimeRemaining.value = _meditationTimeRemaining.value - 1
            }
            if (_meditationTimeRemaining.value <= 0) {
                stopMeditation()
            }
        }
    }

    fun pauseMeditation() {
        _isMeditationRunning.value = false
        meditationTimerJob?.cancel()
        meditationTimerJob = null
        SanctuaryAudioEngine.stopPlaying()
    }

    fun stopMeditation() {
        _isMeditationRunning.value = false
        meditationTimerJob?.cancel()
        meditationTimerJob = null
        _meditationTimeRemaining.value = _meditationDuration.value * 60L
        SanctuaryAudioEngine.stopPlaying()
    }

    fun updateProgress(value: Float) {
        _playProgress.value = value
    }

    // Toggle General SOS Calm modal
    fun toggleSOSGeneral(show: Boolean) {
        _showSOSGeneralModal.value = show
    }

    fun dismissCrisisModal() {
        // Crisis modal is technically non-dismissible or can be closed once they acknowledge
        _showSOSCrisisModal.value = false
    }

    fun triggerCrisisExplicitly() {
        _showSOSCrisisModal.value = true
    }

    // Chat management
    fun resetChat() {
        val welcomeMsg = when (language.value) {
            "es" -> "Hola, soy Counsel, tu compañero de paz. ¿Qué pesa sobre tu corazón hoy? Estoy aquí para escucharte sin juzgar."
            "tl" -> "Kumusta, ako si Counsel, ang iyong katuwang sa kapayapaan. Ano ang bumabagabag sa iyong puso ngayon? Narito ako upang makinig nang buong puso."
            else -> "Hello, I am Counsel, your companion of peace. What weighs heavy on your heart today? I am here to listen without judgment."
        }
        _chatHistory.value = listOf(ChatMessage(welcomeMsg, false))
        _showSOSCrisisModal.value = false
    }

    // Strict safety check for self-harm keyword match
    private fun containsCrisisKeywords(text: String): Boolean {
        val keywords = listOf(
            "suicide", "kill myself", "want to die", "end my life", "self harm", "hurt myself", "cut myself",
            "suicidio", "matarme", "quiero morir", "quitarme la vida", "hacerme daño", "cortarme",
            "magpakamatay", "gusto ko nang mamatay", "tapusin ang buhay", "saktan ang sarili"
        )
        val normalized = text.lowercase()
        return keywords.any { normalized.contains(it) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user message
        val userMsg = ChatMessage(text, true)
        _chatHistory.value = _chatHistory.value + userMsg

        // 2. Immediate Client-Side Safety Check
        if (containsCrisisKeywords(text)) {
            _showSOSCrisisModal.value = true
            // Insert immediate local crisis hotlines notification
            val safetyMsg = when (language.value) {
                "es" -> "ALERTA DE SEGURIDAD: Por favor, comunícate con un profesional o una línea de ayuda de inmediato. Tu vida es preciosa."
                "tl" -> "ALERTA SA KALIGTASAN: Mangyaring makipag-ugnayan sa isang propesyonal o helpline ngayon din. Mahalaga ang iyong buhay."
                else -> "SAFETY SHIELD ACTIVATED: Please contact a professional counselor or crisis line immediately. Your life has infinite value."
            }
            _chatHistory.value = _chatHistory.value + ChatMessage(safetyMsg, false)
            return
        }

        // 3. Call Gemini API in background coroutine safely
        _isSending.value = true
        viewModelScope.launch {
            try {
                val response = callGeminiWithPrompt(text, _chatHistory.value)
                _chatHistory.value = _chatHistory.value + ChatMessage(response, false)
            } catch (e: Exception) {
                val errorMsg = when (language.value) {
                    "es" -> "Lo siento, tengo problemas para conectarme a mi santuario espiritual. Mantengamos la calma juntos."
                    "tl" -> "Paumanhin, nahihirapan akong kumonekta sa aking espiritwal na santuwaryo. Manatili tayong kalmado magkasama."
                    else -> "I apologize, I am having trouble connecting to the spiritual sanctuary right now. Let us remain in peaceful silence together."
                }
                _chatHistory.value = _chatHistory.value + ChatMessage(errorMsg, false)
            } finally {
                _isSending.value = false
            }
        }
    }

    private suspend fun callGeminiWithPrompt(newUserPrompt: String, history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Friendly local fallback if key is unconfigured
            return@withContext getLocalEmpatheticFallback(newUserPrompt)
        }

        // Map chat history (excluding the very last prompt since we add it manually, or just include it)
        // Only take the last 6 messages to keep context short, fast, and secure
        val recentHistory = history.takeLast(6)
        val contents = recentHistory.map { msg ->
            Content(
                role = if (msg.isUser) "user" else "model",
                parts = listOf(Part(text = msg.content))
            )
        }

        val systemInstruction = """
            You are a gentle, empathetic spiritual companion named Counsel from Animo: Your Spiritual Sanctuary. 
            Your role is to support individuals suffering from anxiety, depression, grief, and life stress.
            
            CRITICAL RULE - EMOTIONAL SENSITIVITY & ACTIVE LISTENING:
            1. NEVER 'verse-bomb' (never dump multiple random scriptures without listening first).
            2. Always use deep active listening. Validate the user's pain, grief, or stress with warmth, gentleness, and deep, non-judgmental empathy.
            3. Make the user feel heard, understood, and safe before anything else.
            4. ONLY after you have fully validated their emotions, seamlessly and gently integrate exactly ONE comfort-focused scripture/verse from the Holy Bible into your response as a source of strength, hope, or rest. Explain why this verse offers comfort in their specific situation.
            
            SAFETY RULE:
            If the user mentions suicide, self-harm, or severe crisis, express deep care and urge them to seek immediate human help.
            
            Keep your tone ultra-peaceful, meditative, gentle, warm, and low-stimulation. Keep your responses relatively concise (1-2 paragraphs) so they are easy to read for someone who is overwhelmed.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
            generationConfig = com.example.data.api.GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            )
        )

        // Hit gemini-3.5-flash as default based on system skill instruction for basic/general text tasks
        val result = RetrofitClient.service.generateContent(
            model = "gemini-3.5-flash",
            apiKey = apiKey,
            request = request
        )

        return@withContext result.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: getLocalEmpatheticFallback(newUserPrompt)
    }

    // Rich local fallback to ensure seamless offline or unconfigured API key experience
    private fun getLocalEmpatheticFallback(userPrompt: String): String {
        val lang = language.value
        val normalized = userPrompt.lowercase()

        val response = if (lang == "es") {
            when {
                normalized.contains("ansia") || normalized.contains("nervioso") || normalized.contains("asustado") -> {
                    "Te escucho. La ansiedad se siente como una tormenta impetuosa, pero no estás solo. Tu dolor es completamente válido. Permítete respirar profundamente. Recuerda que la paz de Dios sobrepasa todo entendimiento (Filipenses 4:7). ¿Te gustaría hablar más sobre lo que te preocupa?"
                }
                normalized.contains("triste") || normalized.contains("llorar") || normalized.contains("solo") -> {
                    "Lamento mucho que sientas esta profunda tristeza. Está bien llorar y sentir debilidad. El Señor está cerca de los que tienen el corazón roto (Salmo 34:18). Estoy aquí para acompañarte en este silencio."
                }
                else -> {
                    "Gracias por abrir tu corazón. Valoro mucho tu confianza. Sea lo que sea por lo que estés pasando, recuerda que hay una gracia tranquila esperándote. Dios promete fortalecerte y sostenerte (Isaías 41:10)."
                }
            }
        } else if (lang == "tl") {
            when {
                normalized.contains("kaba") || normalized.contains("natatakot") || normalized.contains("balisa") -> {
                    "Naririnig kita. Ang pagkabahala ay parang malakas na bagyo, ngunit hindi ka nag-iisa. Valid ang iyong nararamdaman. Subukang huminga nang malalim. Tandaan, ang kapayapaan ng Diyos ay nag-iingat sa ating mga puso (Filipas 4:7). Narito ako para sa iyo."
                }
                normalized.contains("malungkot") || normalized.contains("umiiyak") || normalized.contains("mag-isa") -> {
                    "Ikinalulungkot ko ang bigat na iyong nararanasan. Okay lang na malungkot at umiyak. Ang Panginoon ay malapit sa mga may pusong wasak (Awit 34:18). Narito ako upang samahan ka sa iyong lumbay."
                }
                else -> {
                    "Salamat sa pagbabahagi ng iyong puso. Pinahahalagahan ko ang iyong tiwala. Anuman ang iyong pinagdaraanan, tandaan na may kapayapaan at lakas na nakalaan para sa iyo. Ang Diyos ang ating kalasag at aalalay sa atin (Isaias 41:10)."
                }
            }
        } else {
            // English fallback
            when {
                normalized.contains("anxious") || normalized.contains("nervous") || normalized.contains("scared") || normalized.contains("overwhelm") -> {
                    "I hear you. Anxiety can feel like a raging storm, but please know you are safe here. Your feelings are completely valid. Take a deep breath with me. Remember, the peace of God which transcends all understanding is guarding your heart (Philippians 4:7). Would you like to share what feels most heavy?"
                }
                normalized.contains("sad") || normalized.contains("depress") || normalized.contains("lonely") || normalized.contains("cry") -> {
                    "I am so sorry you are holding this deep sorrow. It is completely okay to feel weary and low on energy. The Lord is close to the brokenhearted and saves those who are crushed in spirit (Psalm 34:18). I am here to stand in gentle solidarity with you."
                }
                else -> {
                    "Thank you for sharing that with me. Your pain and your story matter. Whatever stress or weight you are carrying, remember that you are deeply loved and not alone. 'Come to me, all you who are weary and burdened, and I will give you rest' (Matthew 11:28)."
                }
            }
        }
        return response
    }

    // Download / Offline Management
    private fun isWifiConnected(): Boolean {
        return try {
            val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            true // default to true in case of failure
        }
    }

    fun startOrResumeDownload(
        id: String,
        title: String,
        subtitle: String,
        type: String,
        content: String,
        duration: String
    ) {
        val existing = _activeDownloads.value.find { it.id == id }
        val initialProgress = existing?.progress ?: 0f
        val totalSizeMb = when (type) {
            "video" -> 45.2f
            "audio_worship", "audio_prayer" -> 8.4f
            else -> 0.05f
        }

        downloadJobs[id]?.cancel()

        val isWifi = isWifiConnected() && !_simulateMobileData.value
        val wifiOnly = syncOverWifiOnly.value

        if (wifiOnly && !isWifi) {
            val newTask = ActiveDownload(
                id = id,
                title = title,
                subtitle = subtitle,
                type = type,
                content = content,
                duration = duration,
                progress = initialProgress,
                status = DownloadStatus.FAILED_WIFI_REQUIRED,
                totalSizeMb = totalSizeMb,
                downloadedSizeMb = initialProgress * totalSizeMb
            )
            _activeDownloads.value = _activeDownloads.value.filter { it.id != id } + newTask
            return
        }

        val newTask = ActiveDownload(
            id = id,
            title = title,
            subtitle = subtitle,
            type = type,
            content = content,
            duration = duration,
            progress = initialProgress,
            status = DownloadStatus.DOWNLOADING,
            totalSizeMb = totalSizeMb,
            downloadedSizeMb = initialProgress * totalSizeMb
        )
        _activeDownloads.value = _activeDownloads.value.filter { it.id != id } + newTask

        val job = viewModelScope.launch {
            var currentProgress = initialProgress
            while (currentProgress < 1f) {
                delay(300) // fast progression for outstanding UX

                val currentWifi = isWifiConnected() && !_simulateMobileData.value
                val currentWifiOnly = syncOverWifiOnly.value
                if (currentWifiOnly && !currentWifi) {
                    _activeDownloads.value = _activeDownloads.value.map {
                        if (it.id == id) {
                            it.copy(
                                status = DownloadStatus.FAILED_WIFI_REQUIRED,
                                downloadedSizeMb = currentProgress * totalSizeMb
                            )
                        } else it
                    }
                    return@launch
                }

                currentProgress += 0.1f
                if (currentProgress >= 1f) {
                    currentProgress = 1f
                }

                _activeDownloads.value = _activeDownloads.value.map {
                    if (it.id == id) {
                        it.copy(
                            progress = currentProgress,
                            downloadedSizeMb = currentProgress * totalSizeMb,
                            status = if (currentProgress >= 1f) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING
                        )
                    } else it
                }
            }

            // Save completed download record into Room database!
            repository.addDownload(
                DownloadedResource(
                    resourceId = id,
                    title = title,
                    subtitle = subtitle,
                    type = type,
                    content = content,
                    duration = duration,
                    language = language.value
                )
            )
        }
        downloadJobs[id] = job
    }

    fun pauseDownload(id: String) {
        downloadJobs[id]?.cancel()
        downloadJobs.remove(id)
        _activeDownloads.value = _activeDownloads.value.map {
            if (it.id == id) {
                it.copy(status = DownloadStatus.PAUSED)
            } else it
        }
    }

    fun cancelOrDeleteDownload(id: String) {
        downloadJobs[id]?.cancel()
        downloadJobs.remove(id)
        _activeDownloads.value = _activeDownloads.value.filter { it.id != id }
        viewModelScope.launch {
            repository.removeDownload(id)
        }
    }

    fun toggleDownload(
        id: String,
        title: String,
        subtitle: String,
        type: String,
        content: String,
        duration: String
    ) {
        viewModelScope.launch {
            val isDown = repository.isDownloaded(id)
            if (isDown) {
                cancelOrDeleteDownload(id)
            } else {
                val active = _activeDownloads.value.find { it.id == id }
                if (active != null) {
                    if (active.status == DownloadStatus.DOWNLOADING) {
                        pauseDownload(id)
                    } else {
                        startOrResumeDownload(id, title, subtitle, type, content, duration)
                    }
                } else {
                    startOrResumeDownload(id, title, subtitle, type, content, duration)
                }
            }
        }
    }

    suspend fun isDownloaded(id: String): Boolean {
        return repository.isDownloaded(id)
    }

    // Daily Scripture (The Bible Project Theme Reflections)
    fun setScriptureTheme(theme: String) {
        _selectedScriptureTheme.value = theme
        fetchDailyScripture(theme)
    }

    fun fetchDailyScripture(theme: String) {
        viewModelScope.launch {
            _isFetchingScripture.value = true
            val lang = language.value
            val id = "scripture_${theme}_${lang}"

            // 1. ALWAYS TRY LOADING LOCALLY FIRST FROM ROOM
            val localRes = repository.getDownloadById(id)
            if (localRes != null) {
                val parts = localRes.content.split("|||")
                if (parts.size >= 2) {
                    _dailyScriptureText.value = parts[0]
                    _dailyScriptureExplanation.value = parts[1]
                } else {
                    _dailyScriptureText.value = localRes.content
                    _dailyScriptureExplanation.value = localRes.subtitle
                }
                _dailyScriptureReference.value = localRes.title
                _isFetchingScripture.value = false
                Log.d("SanctuarySync", "Successfully loaded $id locally from database first")
                return@launch
            }

            // 2. FALLBACK TO ON-DEMAND FETCHING
            val endpointUrl = when (lang) {
                "es" -> "https://proyectobiblia.com"
                "tl" -> "https://bibleproject.com/tagalog/"
                else -> "https://bibleproject.com"
            }
            Log.d("SanctuarySync", "On-demand fallback fetch from Bible Project endpoint: $endpointUrl")

            if (isOffline.value) {
                loadOfflineScriptureFallback(theme, lang)
                _isFetchingScripture.value = false
                return@launch
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "null") {
                loadOfflineScriptureFallback(theme, lang)
                _isFetchingScripture.value = false
                return@launch
            }

            try {
                val systemPrompt = """
                    You are an expert Bible teacher and scholar from the localized Bible Project endpoint ($endpointUrl). 
                    Generate exactly ONE inspirational scripture passage or verse based on the requested theme: '$theme'.
                    
                    CRITICAL FORMATTING RULE:
                    Your output must be in a clean JSON format. Do not use markdown wraps or extra explanations outside the JSON.
                    The JSON structure must be exactly:
                    {
                      "reference": "Book Chapter:Verse",
                      "text": "The actual text of the scripture...",
                      "explanation": "A beautiful 2-3 sentence reflection in ${if(lang == "es") "Spanish" else if(lang == "tl") "Tagalog" else "English"} explaining how this verse fits into $endpointUrl's theme of '$theme' and how it brings peace to a tired heart."
                    }
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Please generate scripture JSON for the theme: $theme via $endpointUrl")))),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = com.example.data.api.GenerationConfig(
                        temperature = 0.5f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(
                        model = "gemini-3.5-flash",
                        apiKey = apiKey,
                        request = request
                    )
                }

                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (textResponse != null) {
                    val cleanJson = textResponse.trim()
                        .removePrefix("```json")
                        .removeSuffix("```")
                        .trim()

                    val referenceRegex = "\"reference\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val textRegex = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val explanationRegex = "\"explanation\"\\s*:\\s*\"([^\"]+)\"".toRegex()

                    val ref = referenceRegex.find(cleanJson)?.groupValues?.get(1)
                    val txt = textRegex.find(cleanJson)?.groupValues?.get(1)
                    val exp = explanationRegex.find(cleanJson)?.groupValues?.get(1)

                    if (ref != null && txt != null && exp != null) {
                        _dailyScriptureReference.value = ref
                        _dailyScriptureText.value = txt
                        _dailyScriptureExplanation.value = exp
                    } else {
                        loadOfflineScriptureFallback(theme, lang)
                    }
                } else {
                    loadOfflineScriptureFallback(theme, lang)
                }
            } catch (e: Exception) {
                loadOfflineScriptureFallback(theme, lang)
            } finally {
                _isFetchingScripture.value = false
            }
        }
    }

    private fun loadOfflineScriptureFallback(theme: String, lang: String) {
        if (lang == "es") {
            when (theme) {
                "Covenant" -> {
                    _dailyScriptureReference.value = "Génesis 15:18"
                    _dailyScriptureText.value = "En aquel día hizo Jehová un pacto con Abram..."
                    _dailyScriptureExplanation.value = "El Pacto con Abraham demuestra el compromiso inquebrantable de Dios de bendecir y estar presente con Su creación, incluso en medio de nuestras debilidades y dudas."
                }
                "Tree of Life" -> {
                    _dailyScriptureReference.value = "Apocalipsis 22:2"
                    _dailyScriptureText.value = "En medio de la calle de la ciudad, y a uno y otro lado del río, estaba el árbol de la vida..."
                    _dailyScriptureExplanation.value = "La serie de videos sobre el Árbol de la Vida de The Bible Project ilustra cómo Dios anhela nutrir nuestras almas con Su eterno amor y sanar nuestras heridas."
                }
                "Grace & Peace" -> {
                    _dailyScriptureReference.value = "Efesios 2:8"
                    _dailyScriptureText.value = "Porque por gracia sois salvos por medio de la fe; y esto no de vosotros, pues es don de Dios."
                    _dailyScriptureExplanation.value = "La gracia de Dios es Su amor inmerecido que nos rescata y nos concede shalom (paz perfecta) en medio del estrés y la fatiga."
                }
                "Wisdom Literature" -> {
                    _dailyScriptureReference.value = "Proverbios 3:5-6"
                    _dailyScriptureText.value = "Fíate de Jehová de todo tu corazón, y no te apoyes en tu propia prudencia..."
                    _dailyScriptureExplanation.value = "La sabiduría bíblica de The Bible Project nos anima a descansar en el entendimiento de Dios, soltando el control de nuestras propias ansiedades."
                }
                else -> { // Justice
                    _dailyScriptureReference.value = "Miqueas 6:8"
                    _dailyScriptureText.value = "Oh hombre, él te ha declarado lo que es bueno... solamente hacer justicia, y amar misericordia..."
                    _dailyScriptureExplanation.value = "Hacer justicia y amar la misericordia refleja el carácter santo de Dios, unificando nuestra fe con acciones de amor compasivo."
                }
            }
        } else if (lang == "tl") {
            when (theme) {
                "Covenant" -> {
                    _dailyScriptureReference.value = "Genesis 15:18"
                    _dailyScriptureText.value = "Sa araw na iyon ay gumawa ang Panginoon ng pakikipagtipan kay Abram..."
                    _dailyScriptureExplanation.value = "Ang Tipan ng Diyos ay nagpapakita ng Kanyang tapat na pangako na kailanman ay hindi tayo iiwan o pababayaan, nagbibigay ng walang-hanggang katiyakan."
                }
                "Tree of Life" -> {
                    _dailyScriptureReference.value = "Pahayag 22:2"
                    _dailyScriptureText.value = "Sa gitna ng kalsada ng lungsod, at sa magkabilang panig ng ilog, nandoon ang puno ng buhay..."
                    _dailyScriptureExplanation.value = "Pinatutunayan ng bidyo tungkol sa Puno ng Buhay na nais ng Diyos na mapuno ng Kanyang buhay at kagalingan ang ating mga pusong nagluluksa."
                }
                "Grace & Peace" -> {
                    _dailyScriptureReference.value = "Efeso 2:8"
                    _dailyScriptureText.value = "Sapagkat sa biyaya kayo ay naligtas sa pamamagitan ng pananalig..."
                    _dailyScriptureExplanation.value = "Ang biyaya ay ang mapagmahal na regalo ng Diyos na nag-aalis ng ating takot at nagpapalaya sa atin mula sa bigat ng buhay."
                }
                "Wisdom Literature" -> {
                    _dailyScriptureReference.value = "Kawikaan 3:5-6"
                    _dailyScriptureText.value = "Magtiwala ka sa Panginoon nang buong puso mo, at huwag kang manalig sa iyong sariling karunungan..."
                    _dailyScriptureExplanation.value = "Ang karunungan ay nagtuturo sa atin na ibigay ang ating buong tiwala sa Diyos sa halip na mag-alala sa bukas."
                }
                else -> { // Justice
                    _dailyScriptureReference.value = "Mikas 6:8"
                    _dailyScriptureText.value = "Ipinakita niya sa iyo, Oh tao, kung ano ang mabuti... ang gumawa ng may katarungan, at ibigin ang kaawaan..."
                    _dailyScriptureExplanation.value = "Ang katarungan at awa ay magkaagapay sa puso ng Diyos upang lumikha ng isang payapa at matuwid na mundo."
                }
            }
        } else {
            // English default
            when (theme) {
                "Covenant" -> {
                    _dailyScriptureReference.value = "Genesis 15:18"
                    _dailyScriptureText.value = "On that day the Lord made a covenant with Abram..."
                    _dailyScriptureExplanation.value = "The Bible Project's Covenant series reminds us that God binds Himself to us in an unbreakable promise of loyalty, ensuring that we never walk through dark valleys alone."
                }
                "Tree of Life" -> {
                    _dailyScriptureReference.value = "Revelation 22:2"
                    _dailyScriptureText.value = "On each side of the river stood the tree of life, bearing twelve crops of fruit..."
                    _dailyScriptureExplanation.value = "The Tree of Life theme captures God's ultimate desire: to restore humanity to His healing, life-giving presence where anxiety and death are no more."
                }
                "Grace & Peace" -> {
                    _dailyScriptureReference.value = "Ephesians 2:8"
                    _dailyScriptureText.value = "For it is by grace you have been saved, through faith—and this is not from yourselves, it is the gift of God..."
                    _dailyScriptureExplanation.value = "Grace is the free, calming gift of God's unconditional favor that brings deep peace (Shalom) to our restless souls, removing the pressure to perform."
                }
                "Wisdom Literature" -> {
                    _dailyScriptureReference.value = "Proverbs 3:5-6"
                    _dailyScriptureText.value = "Trust in the Lord with all your heart and lean not on your own understanding..."
                    _dailyScriptureExplanation.value = "The Wisdom Literature series guides us to place our complete weight upon God's loving foresight rather than exhausting our minds with endless anxiety."
                }
                else -> { // Justice
                    _dailyScriptureReference.value = "Micah 6:8"
                    _dailyScriptureText.value = "He has shown you, O mortal, what is good. And what does the Lord require of you? To act justly and to love mercy..."
                    _dailyScriptureExplanation.value = "The Bible Project's Justice theme shows how God's righteousness works hand-in-hand with His profound mercy, healing our broken relationships and weary world."
                }
            }
        }
    }

    fun fetchDailyAffirmation(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isFetchingAffirmation.value = true
            val lang = language.value
            val lastTimestamp = repository.getDailyAffirmationTimestamp()
            val savedText = repository.getDailyAffirmationText()
            val currentTime = System.currentTimeMillis()

            if (!forceRefresh && !savedText.isNullOrEmpty() && (currentTime - lastTimestamp < 24 * 60 * 60 * 1000L)) {
                _dailyAffirmationText.value = savedText
                _isFetchingAffirmation.value = false
                return@launch
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "null" || apiKey == "MY_GEMINI_API_KEY") {
                loadLocalAffirmationFallback(lang)
                _isFetchingAffirmation.value = false
                return@launch
            }

            try {
                val promptLangName = if (lang == "es") "Spanish" else if (lang == "tl") "Tagalog" else "English"
                val systemPrompt = """
                    You are a gentle, comforting companion of peace. 
                    Generate exactly ONE positive thought or affirmation for a person seeking mental rest, healing, or peace.
                    Keep it beautiful, inspiring, and calming (about 1-2 sentences).
                    The language must be: $promptLangName.
                    CRITICAL: Do not use any markdown formatting, asterisks, quotation marks, or extra conversational filler; output ONLY the affirmation text itself.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Please generate an inspiring affirmation in $promptLangName")))),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = com.example.data.api.GenerationConfig(
                        temperature = 0.8f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(
                        model = "gemini-3.5-flash",
                        apiKey = apiKey,
                        request = request
                    )
                }

                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                if (!textResponse.isNullOrEmpty()) {
                    val cleanText = textResponse.removeSurrounding("\"").removeSurrounding("'").trim()
                    _dailyAffirmationText.value = cleanText
                    repository.saveDailyAffirmationText(cleanText)
                    repository.saveDailyAffirmationTimestamp(currentTime)
                } else {
                    loadLocalAffirmationFallback(lang)
                }
            } catch (e: Exception) {
                loadLocalAffirmationFallback(lang)
            } finally {
                _isFetchingAffirmation.value = false
            }
        }
    }

    private suspend fun loadLocalAffirmationFallback(lang: String) {
        val list = when (lang) {
            "es" -> localAffirmationsEs
            "tl" -> localAffirmationsTl
            else -> localAffirmationsEn
        }
        val index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % list.size
        val text = list[index]
        _dailyAffirmationText.value = text
        repository.saveDailyAffirmationText(text)
        repository.saveDailyAffirmationTimestamp(System.currentTimeMillis())
    }

    fun getBibleProjectUrl(): String {
        return when (language.value) {
            "es" -> "https://proyectobiblia.com/"
            "tl" -> "https://bibleproject.com/tagalog/"
            else -> "https://bibleproject.com/"
        }
    }

    private val scripturesFallback = listOf(
        // ANXIOUS
        Scripture(
            reference = "Philippians 4:6-7",
            text = "Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God. And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.",
            mood = "Anxious",
            language = "en"
        ),
        Scripture(
            reference = "Filipenses 4:6-7",
            text = "No se inquieten por nada; más bien, en toda ocasión, con oración y ruego, presenten sus peticiones a Dios y denle gracias. Y la paz de Dios, que sobrepasa todo entendimiento, cuidará sus corazones y sus pensamientos en Cristo Jesús.",
            mood = "Anxious",
            language = "es"
        ),
        Scripture(
            reference = "Filipas 4:6-7",
            text = "Huwag kayong mabalisa tungkol sa anumang bagay. Sa halip, sabihin ninyo sa Diyos ang inyong mga kailangan sa pamamagitan ng pananalangin na may pasasalamat. At ang kapayapaan ng Diyos na hindi kayang maunawaan ng tao ang siyang mag-iingat sa inyong puso at pag-iisip sa pamamagitan ni Cristo Jesus.",
            mood = "Anxious",
            language = "tl"
        ),

        // SAD
        Scripture(
            reference = "Psalm 34:18",
            text = "The Lord is close to the brokenhearted and saves those who are crushed in spirit.",
            mood = "Sad",
            language = "en"
        ),
        Scripture(
            reference = "Salmos 34:18",
            text = "Cercano está el Señor para salvar a los que tienen roto el corazón y el espíritu destrozado.",
            mood = "Sad",
            language = "es"
        ),
        Scripture(
            reference = "Awit 34:18",
            text = "Malapit si Yahweh sa mga may pusong wasak, at inililigtas ang mga may tapat ngunit may lumbay na espiritu.",
            mood = "Sad",
            language = "tl"
        ),

        // LONELY
        Scripture(
            reference = "Isaiah 41:10",
            text = "Fear not, for I am with you; be not dismayed, for I am your God; I will strengthen you, I will help you, I will uphold you with my righteous right hand.",
            mood = "Lonely",
            language = "en"
        ),
        Scripture(
            reference = "Isaías 41:10",
            text = "No temas, porque yo estoy contigo; no te desalientes, porque yo soy tu Dios. Te fortaleceré, ciertamente te ayudaré, sí, te sostendré con la diestra de mi justicia.",
            mood = "Lonely",
            language = "es"
        ),
        Scripture(
            reference = "Isaias 41:10",
            text = "Huwag kang matakot, sapagkat ako'y sumasaiyo; huwag kang mangamba, sapagkat ako ang iyong Diyos. Palalakasin kita, tutulungan kita, at aalalayan kita ng aking kanang kamay na matuwid.",
            mood = "Lonely",
            language = "tl"
        ),

        // GRATEFUL
        Scripture(
            reference = "Psalm 107:1",
            text = "Give thanks to the Lord, for he is good; his love endures forever.",
            mood = "Grateful",
            language = "en"
        ),
        Scripture(
            reference = "Salmos 107:1",
            text = "Alaben al Señor porque él es bueno; su gran amor perdura para siempre.",
            mood = "Grateful",
            language = "es"
        ),
        Scripture(
            reference = "Awit 107:1",
            text = "Magpasalamat kayo kay Yahweh, sapagkat siya'y mabuti, at ang kanyang pag-ibig ay walang hanggan.",
            mood = "Grateful",
            language = "tl"
        ),

        // STRUGGLING
        Scripture(
            reference = "Matthew 11:28",
            text = "Come to me, all you who are weary and burdened, and I will give you rest.",
            mood = "Struggling",
            language = "en"
        ),
        Scripture(
            reference = "Mateo 11:28",
            text = "Vengan a mí todos ustedes que están cansados y agobiados, y yo les daré descanso.",
            mood = "Struggling",
            language = "es"
        ),
        Scripture(
            reference = "Mateo 11:28",
            text = "Lumapit kayo sa akin, kayong lahat na napapagod at nabibigatan sa inyong pasanin, at bibigyan ko kayo ng kapahingahan.",
            mood = "Struggling",
            language = "tl"
        )
    )

    private val prayersFallback = listOf(
        // MORNING
        Prayer(
            id = "m1_en",
            title = "Morning Light & Peace",
            content = "Heavenly Father, as I greet this morning, I ask for Your strength. Give me the grace to face this day with a calm mind and a patient heart. I release the need to control. Guide my feet in the path of peace, and remind me that You are already in my tomorrow.",
            isMorning = true,
            language = "en"
        ),
        Prayer(
            id = "m1_es",
            title = "Luz y Paz de la Mañana",
            content = "Padre Celestial, al saludar esta mañana, te pido Tu fuerza. Dame la gracia de enfrentar este día con una mente tranquila y un corazón paciente. Libero la necesidad de controlar todo. Guía mis pies en el camino de la paz y recuérdame que Tú ya estás en mi mañana.",
            isMorning = true,
            language = "es"
        ),
        Prayer(
            id = "m1_tl",
            title = "Liwanag at Kapayapaan sa Umaga",
            content = "Ama sa Langit, sa aking pagtanggap sa umagang ito, humihingi ako ng Iyong lakas. Bigyan Mo ako ng biyaya na harapin ang araw na ito nang may kalmadong isip at pasensyosong puso. Isinusuko ko ang pagnanais na kontrolin ang lahat. Ipatnubay Mo ang aking mga paa sa landas ng kapayapaan, at ipaalala sa akin na nandoon Ka na sa aking kinabukasan.",
            isMorning = true,
            language = "tl"
        ),

        // NIGHT
        Prayer(
            id = "n1_en",
            title = "Night of Sleep & Trust",
            content = "Lord, the day is done, and I come to rest. I cast all my anxieties, stress, and heavy burdens onto You, for You care for me deeply. Wrap me in Your peaceful presence, quiet my racing thoughts, and grant me restorative, deep sleep. I trust my life in Your hands.",
            isMorning = false,
            language = "en"
        ),
        Prayer(
            id = "n1_es",
            title = "Noche de Sueño y Confianza",
            content = "Señor, el día ha terminado y vengo a descansar. Dejo todas mis ansiedades, estrés y cargas pesadas sobre Ti, porque te preocupas profundamente por mí. Envuélveme en Tu presencia pacífica, aquieta mis pensamientos acelerados y concédeme un sueño reparador y profundo. Confío mi vida en Tus manos.",
            isMorning = false,
            language = "es"
        ),
        Prayer(
            id = "n1_tl",
            title = "Gabi ng Tulog at Pagtitiwala",
            content = "Panginoon, tapos na ang araw at lumalapit ako upang mamahinga. Inihahagis ko ang lahat ng aking alalahanin, stress, at mabibigat na pasanin sa Iyo, sapagkat lubos Mo akong pinahahalagahan. Yakapin Mo ako ng Iyong mapayapang presensya, patahimikin ang aking nagmamadaling isipan, at pagkalooban ako ng mahimbing na tulog. Ipinagkakatiwala ko ang aking buhay sa Iyong mga kamay.",
            isMorning = false,
            language = "tl"
        )
    )

    override fun onCleared() {
        super.onCleared()
        SanctuaryAudioEngine.stopPlaying()
    }
}
