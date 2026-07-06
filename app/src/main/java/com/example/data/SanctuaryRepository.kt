package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SanctuaryRepository(private val db: AppDatabase) {
    private val prefDao = db.preferenceDao()
    private val verseDao = db.verseDao()
    private val favoriteDao = db.favoriteDao()

    // Preferences
    fun getLanguageFlow(): Flow<String> = prefDao.getPreferenceFlow("pref_language").map { it?.value ?: "en" }
    suspend fun getLanguage(): String = prefDao.getPreference("pref_language")?.value ?: "en"
    suspend fun saveLanguage(lang: String) = prefDao.insertPreference(PreferenceEntry("pref_language", lang))

    fun getMorningNotificationTimeFlow(): Flow<String> = prefDao.getPreferenceFlow("pref_notif_morning").map { it?.value ?: "07:00 AM" }
    suspend fun saveMorningNotificationTime(time: String) = prefDao.insertPreference(PreferenceEntry("pref_notif_morning", time))

    fun getNightNotificationTimeFlow(): Flow<String> = prefDao.getPreferenceFlow("pref_notif_night").map { it?.value ?: "09:00 PM" }
    suspend fun saveNightNotificationTime(time: String) = prefDao.insertPreference(PreferenceEntry("pref_notif_night", time))

    fun getLastMoodFlow(): Flow<String?> = prefDao.getPreferenceFlow("pref_last_mood").map { it?.value }
    suspend fun saveLastMood(mood: String) = prefDao.insertPreference(PreferenceEntry("pref_last_mood", mood))

    // Verse cache
    suspend fun getCachedVerse(mood: String, lang: String): VerseEntry? {
        val id = "${mood}_$lang"
        return verseDao.getCachedVerse(id)
    }
    suspend fun saveCachedVerse(verse: VerseEntry) {
        verseDao.insertVerse(verse)
    }

    // Favorites
    fun getFavoritesFlow(): Flow<List<FavoriteItem>> = favoriteDao.getAllFavoritesFlow()
    suspend fun addFavorite(item: FavoriteItem) = favoriteDao.insertFavorite(item)
    suspend fun removeFavorite(text: String, lang: String) = favoriteDao.deleteFavoriteByContent(text, lang)
    suspend fun isFavorite(text: String, lang: String): Boolean = favoriteDao.isFavorite(text, lang)
}
