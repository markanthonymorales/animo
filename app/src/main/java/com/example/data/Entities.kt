package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class PreferenceEntry(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "verse_cache")
data class VerseEntry(
    @PrimaryKey val id: String, // e.g., "mood_language" or "YYYY-MM-DD"
    val text: String,
    val reference: String,
    val mood: String,
    val language: String
)

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "verse" or "prayer"
    val text: String,
    val referenceOrTitle: String,
    val mood: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)
