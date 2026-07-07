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
    val url: String,  // Mock or real audio/web URL
    val duration: String,
    val language: String,
    val lyricOrScripture: String
)
