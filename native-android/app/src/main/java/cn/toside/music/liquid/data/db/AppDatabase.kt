package cn.toside.music.liquid.data.db

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isBuiltIn: Boolean = false,
    val source: String? = null,
    val sourceListId: String? = null,
    val coverUrl: String? = null,
    val position: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index("playlistId"), Index("sortOrder")],
)
data class PlaylistTrackEntity(
    val playlistId: String,
    val trackId: String,
    val musicJson: String,
    val sortOrder: Long,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "user_api_scripts")
data class UserApiScriptEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val version: String = "",
    val author: String = "",
    val homepage: String = "",
    val script: String,
    val enabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val keyword: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "dislike_rules")
data class DislikeRuleEntity(
    @PrimaryKey val rule: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "kv_cache")
data class KvCacheEntity(
    @PrimaryKey val cacheKey: String,
    val value: String,
    @ColumnInfo(index = true)
    val expiresAt: Long,
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY isBuiltIn DESC, position ASC, updatedAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY isBuiltIn DESC, position ASC, updatedAt DESC")
    suspend fun getAllPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylist(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaylist(entity: PlaylistEntity)

    @Query("UPDATE playlists SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renamePlaylist(id: String, name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET coverUrl = :coverUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCover(id: String, coverUrl: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM playlists WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomPlaylist(id: String)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun observeTracks(playlistId: String): Flow<List<PlaylistTrackEntity>>

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    suspend fun getTracks(playlistId: String): List<PlaylistTrackEntity>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    fun observeTrackCount(playlistId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackCount(playlistId: String): Int

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxSortOrder(playlistId: String): Long

    @Query("SELECT COALESCE(MIN(sortOrder), 0) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMinSortOrder(playlistId: String): Long

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId)")
    fun observeContainsTrack(playlistId: String, trackId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun containsTrack(playlistId: String, trackId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<PlaylistTrackEntity>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId IN (:trackIds)")
    suspend fun deleteTracks(playlistId: String, trackIds: List<String>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: String)
}

@Dao
interface UserApiDao {
    @Query("SELECT * FROM user_api_scripts ORDER BY createdAt DESC")
    fun observeScripts(): Flow<List<UserApiScriptEntity>>

    @Query("SELECT * FROM user_api_scripts ORDER BY createdAt DESC")
    suspend fun getAllScripts(): List<UserApiScriptEntity>

    @Query("SELECT * FROM user_api_scripts WHERE id = :id LIMIT 1")
    suspend fun getScript(id: String): UserApiScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(script: UserApiScriptEntity)

    @Query("DELETE FROM user_api_scripts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT keyword FROM search_history ORDER BY updatedAt DESC LIMIT 30")
    fun observeHistory(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE keyword = :keyword")
    suspend fun delete(keyword: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface DislikeDao {
    @Query("SELECT rule FROM dislike_rules ORDER BY createdAt DESC")
    fun observeRules(): Flow<List<String>>

    @Query("SELECT rule FROM dislike_rules")
    suspend fun getAllRules(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rules: List<DislikeRuleEntity>)

    @Query("DELETE FROM dislike_rules WHERE rule = :rule")
    suspend fun delete(rule: String)

    @Query("DELETE FROM dislike_rules")
    suspend fun clearAll()
}

@Dao
interface KvCacheDao {
    @Query("SELECT * FROM kv_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): KvCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: KvCacheEntity)

    @Query("DELETE FROM kv_cache WHERE cacheKey LIKE :prefix || '%'")
    suspend fun deleteByPrefix(prefix: String)

    @Query("DELETE FROM kv_cache WHERE expiresAt < :now")
    suspend fun pruneExpired(now: Long)

    @Query("DELETE FROM kv_cache")
    suspend fun clearAll()
}

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        UserApiScriptEntity::class,
        SearchHistoryEntity::class,
        DislikeRuleEntity::class,
        KvCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun userApiDao(): UserApiDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun dislikeDao(): DislikeDao
    abstract fun kvCacheDao(): KvCacheDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lx_liquid.db",
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
