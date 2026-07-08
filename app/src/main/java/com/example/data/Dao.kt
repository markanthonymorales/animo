package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences WHERE `key` = :key")
    fun getPreferenceFlow(key: String): Flow<PreferenceEntry?>

    @Query("SELECT * FROM preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): PreferenceEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(entry: PreferenceEntry)
}

@Dao
interface VerseDao {
    @Query("SELECT * FROM verse_cache WHERE id = :id")
    suspend fun getCachedVerse(id: String): VerseEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(entry: VerseEntry)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteItem)

    @Query("DELETE FROM favorites WHERE text = :text AND language = :lang")
    suspend fun deleteFavoriteByContent(text: String, lang: String)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE text = :text AND language = :lang)")
    suspend fun isFavorite(text: String, lang: String): Boolean
}

@Dao
interface DownloadedDao {
    @Query("SELECT * FROM downloaded_resources ORDER BY timestamp DESC")
    fun getAllDownloadsFlow(): Flow<List<DownloadedResource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(resource: DownloadedResource)

    @Query("DELETE FROM downloaded_resources WHERE resourceId = :id")
    suspend fun deleteDownload(id: String)

    @Query("SELECT EXISTS(SELECT * FROM downloaded_resources WHERE resourceId = :id)")
    suspend fun isDownloaded(id: String): Boolean

    @Query("SELECT * FROM downloaded_resources WHERE resourceId = :id")
    suspend fun getDownloadById(id: String): DownloadedResource?
}
