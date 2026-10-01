package org.mpp.dbuild.data.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Embedded
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Relation
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class IdTitle(
    val id: Long,
    val title: String
)

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val n: Int
)

data class SongData(
    @Embedded
    val song: SongEntity,


    @Relation(
        parentColumns = ["id"],
        entityColumns = ["song_id"]
    )
    val info: SongInfo?
)
val emptySongEntity = SongEntity("", "")
val emptySongData = SongData(emptySongEntity,null)

@Dao
interface SongDao{
    @Query("SELECT * FROM songentity")
    suspend fun getAll(): List<SongEntity>

    @Query("SELECT * FROM songentity")
    fun getAllFlow(): Flow<List<SongEntity>>

    @Query("SELECT id, title FROM songentity")
    suspend fun getAllTitles(): List<IdTitle>

    @Query("SELECT id, title FROM songentity ORDER BY title COLLATE NOCASE ASC")
    fun getAllTitlesFlow(): Flow<List<IdTitle>>

    @Query("SELECT lyrics FROM songentity WHERE id=:id")
    suspend fun getSongLyricsById(id: Long): String?

    @Query("SELECT * FROM SongEntity WHERE id = :id")
    suspend fun getSongDataById(id: Long): SongData?

    @Query("SELECT * FROM SongEntity")
    fun getSongsWithDataFlow(): Flow<List<SongData>>

//    find by lyrics:
    @Query("SELECT id, title FROM songentity WHERE lyrics LIKE :lyricsQuery")
    suspend fun findByLyrics(lyricsQuery: String): List<IdTitle>

    // insert songs (those daos are used in combination (one after the other):
    @Query("UPDATE SongEntity SET lyrics = :lyrics WHERE title = :title")
    suspend fun updateLyrics(title: String, lyrics: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: SongEntity): Long
    // ---

//    insert? delete?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongEntity>)

    // to use in upsertSong
    @Query("SELECT * FROM SongEntity WHERE title = :title LIMIT 1")
    suspend fun getSongByTitle(title: String): SongEntity?

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("SELECT * FROM SongInfo WHERE song_id = :songId LIMIT 1")
    suspend fun getSongInfo(songId: Long): SongInfo?

    @Insert
    suspend fun insertSongInfo(info: SongInfo): Long

    @Update
    suspend fun updateSongInfo(info: SongInfo)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Transaction
    suspend fun upsertSong(
        song: SongEntity,
        info: SongInfo?
    ) {
        val existingSong = getSongByTitle(song.title)

        val songId = if (existingSong != null) {
            updateSong(
                existingSong.copy(
                    lyrics = song.lyrics
                )
            )
            existingSong.id
        } else {
            insertSong(song)
        }

        if (info != null) {
            val existingInfo = getSongInfo(songId)

            if (existingInfo != null) {
                updateSongInfo(
                    existingInfo.copy(
                        authors = info.authors,
                        category = info.category
                    )
                )
            } else {
                insertSongInfo(
                    info.copy(
                        songId = songId,
                        id = 0
                    )
                )
            }
        }
    }

}

@Dao
interface PlaylistDao{
    @Query("SELECT * FROM playlist")
    suspend fun getPlaylists(): List<Playlist>

    @Query("SELECT s.id, s.title FROM PlaylistSong p, songentity s WHERE p.playlist_id=:id AND p.song_id = s.id")
    suspend fun getPlaylistSongs(id: Long): List<IdTitle>

    @Query("SELECT * FROM playlist")
    fun getPlaylistsFlow(): Flow<List<Playlist>>

    @Query("SELECT p.id, p.name, COUNT(sp.song_id) AS n FROM playlist p LEFT JOIN playlistsong sp ON p.id = sp.playlist_id GROUP BY p.id, p.name")
    fun getPlaylistsWithCountFlow(): Flow<List<PlaylistWithCount>>

    @Query("SELECT s.id, s.title FROM PlaylistSong p, songentity s WHERE p.playlist_id=:id AND p.song_id = s.id")
    fun getPlaylistSongsFlow(id: Long): Flow<List<IdTitle>>

    // create new playlist
    // @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Query("INSERT OR IGNORE INTO Playlist(name) VALUES (:name)")
    suspend fun addPlaylist(name: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlaylist2(playlist: Playlist): Long


    // insert new song to playlist
    // mogoce ne dela glih dobr - vrjetno dovoli duplikate
    // @Query("INSERT OR IGNORE INTO PlaylistSong(song_id, playlist_id) VALUES (:songId, :playlistId)")
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(song: PlaylistSong)

    // also to delete playlist / song from playlist
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongsToPlaylist(songs: List<PlaylistSong>)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylistSong(playlistSong: PlaylistSong)

}

