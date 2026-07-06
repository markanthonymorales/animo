package com.example.data

data class Scripture(
    val reference: String,
    val text: String,
    val mood: String,
    val language: String
)

data class Prayer(
    val id: String,
    val title: String,
    val content: String,
    val isMorning: Boolean,
    val language: String
)

data class MediaItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String, // "video" (verse video), "audio_worship" (worship song), "audio_prayer" (spoken prayer)
    val url: String,  // Mock or real audio URL
    val duration: String,
    val language: String,
    val lyricOrScripture: String
)

object SanctuaryData {
    val scriptures = listOf(
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

    val prayers = listOf(
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

    val mediaItems = listOf(
        // English
        MediaItem(
            id = "v1_en",
            title = "Philippians 4 Verse Meditation",
            subtitle = "Visual Verse & Voiceover",
            type = "video",
            url = "https://example.com/videos/phil4.mp3",
            duration = "1:30",
            language = "en",
            lyricOrScripture = "Do not be anxious about anything, but in everything by prayer and petition, present your requests to God..."
        ),
        MediaItem(
            id = "w1_en",
            title = "Abide in Me",
            subtitle = "Acoustic Worship Track",
            type = "audio_worship",
            url = "https://example.com/audio/abide.mp3",
            duration = "4:15",
            language = "en",
            lyricOrScripture = "Peace, be still. You are with me in the valley. You are with me in the storm. I will rest in Your love."
        ),
        MediaItem(
            id = "p1_en",
            title = "Quiet My Mind",
            subtitle = "Guided Audio Prayer",
            type = "audio_prayer",
            url = "https://example.com/audio/quiet_mind.mp3",
            duration = "3:00",
            language = "en",
            lyricOrScripture = "Take a deep breath in... and exhale. Father, we ask for Your stillness to wash over every racing thought..."
        ),

        // Spanish
        MediaItem(
            id = "v1_es",
            title = "Meditación Filipenses 4",
            subtitle = "Verso Visual y Voz en Off",
            type = "video",
            url = "https://example.com/videos/phil4_es.mp3",
            duration = "1:40",
            language = "es",
            lyricOrScripture = "Por nada estéis afanosos, sino sean conocidas vuestras peticiones delante de Dios en toda oración y ruego..."
        ),
        MediaItem(
            id = "w1_es",
            title = "Permanece en Mí",
            subtitle = "Pista Acústica de Adoración",
            type = "audio_worship",
            url = "https://example.com/audio/abide_es.mp3",
            duration = "4:10",
            language = "es",
            lyricOrScripture = "Paz, quédate quieto. Estás conmigo en el valle. Estás conmigo en la tormenta. Descansaré en tu amor."
        ),
        MediaItem(
            id = "p1_es",
            title = "Calma mi Mente",
            subtitle = "Oración Guiada en Audio",
            type = "audio_prayer",
            url = "https://example.com/audio/quiet_mind_es.mp3",
            duration = "3:15",
            language = "es",
            lyricOrScripture = "Inhala profundamente... y exhala. Padre, te pedimos que Tu quietud inunde cada pensamiento acelerado..."
        ),

        // Tagalog
        MediaItem(
            id = "v1_tl",
            title = "Meditasyon sa Filipas 4",
            subtitle = "Visual Verse at Voiceover",
            type = "video",
            url = "https://example.com/videos/phil4_tl.mp3",
            duration = "1:45",
            language = "tl",
            lyricOrScripture = "Huwag kayong mabalisa sa anumang bagay, kundi sa lahat ng bagay sa pamamagitan ng panalangin na may pasasalamat..."
        ),
        MediaItem(
            id = "w1_tl",
            title = "Manatili Ka sa Akin",
            subtitle = "Acoustic Worship Track",
            type = "audio_worship",
            url = "https://example.com/audio/abide_tl.mp3",
            duration = "4:20",
            language = "tl",
            lyricOrScripture = "Kapayapaan, manahimik. Kasama kita sa lambak. Kasama kita sa bagyo. Mamahinga ako sa Iyong pag-ibig."
        ),
        MediaItem(
            id = "p1_tl",
            title = "Patahimikin ang Aking Isip",
            subtitle = "Gabay na Panalangin sa Audio",
            type = "audio_prayer",
            url = "https://example.com/audio/quiet_mind_tl.mp3",
            duration = "3:20",
            language = "tl",
            lyricOrScripture = "Huminga nang malalim... at dahan-dahang i-exhale. Ama, hinihiling namin ang Iyong kapayapaan sa bawat isipang mabalisa..."
        )
    )
}
