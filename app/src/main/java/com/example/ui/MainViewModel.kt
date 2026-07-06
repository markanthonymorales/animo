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

sealed class Screen {
    object Welcome : Screen()
    object Dashboard : Screen()
    object MediaPlayer : Screen()
    object Counsel : Screen()
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
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

    // Media Player States
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

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
        // Load last selected mood preference if any
        viewModelScope.launch {
            repository.getLastMoodFlow().collectLatest { lastMood ->
                if (lastMood != null) {
                    _selectedMood.value = lastMood
                }
            }
        }

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
        return SanctuaryData.scriptures.firstOrNull { 
            it.mood.equals(currentMood, ignoreCase = true) && it.language == currentLang 
        } ?: SanctuaryData.scriptures.first { it.language == "en" }
    }

    // Dynamic filtering of prayer
    fun getDynamicPrayer(isMorning: Boolean): Prayer {
        val currentLang = language.value
        return SanctuaryData.prayers.firstOrNull { 
            it.isMorning == isMorning && it.language == currentLang 
        } ?: SanctuaryData.prayers.first { it.isMorning == isMorning && it.language == "en" }
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
        _currentMediaItem.value = item
        _isPlaying.value = true
        _playProgress.value = 0f
        checkCurrentItemFavorited(item.lyricOrScripture)
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
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
}
