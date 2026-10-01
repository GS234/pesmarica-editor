package org.mpp.dbuild.data.sources

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.mpp.dbuild.data.db.SongDao
import org.mpp.dbuild.data.db.SongData
import org.mpp.dbuild.data.db.SongEntity
import org.mpp.dbuild.data.db.SongInfo

class SongsLocalDataSource(
    private val songDao: SongDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getSongs(): List<Song> = withContext(ioDispatcher){
        songDao.getAll().map { song -> Song(song.id, song.title, "", song.lyrics) }
    }
    fun getSongsFlow(): Flow<List<SongEntity>> {
        return songDao.getAllFlow()
    }

    fun getSongsWithDataFlow(): Flow<List<SongData>> {
        return songDao.getSongsWithDataFlow()
    }

    suspend fun insertSongWithData(song: SongEntity, info: SongInfo){
        withContext(ioDispatcher){
            songDao.upsertSong(song, info)
        }
    }
    suspend fun insertSong(song: SongEntity){
        withContext(ioDispatcher){
            songDao.insertSong(song)
        }
    }

    suspend fun deleteSong(song: SongEntity){
        withContext(ioDispatcher){
            songDao.deleteSong(song)
        }
    }
    // authors, ...
    suspend fun insertSongInfo(songInfo: SongInfo){
        withContext(ioDispatcher){
            songDao.insertSongInfo(songInfo)
        }
    }
}

interface SongsApi{
    fun getSongs(): List<Song>
}

class ActualSongsApi(): SongsApi{
    override fun getSongs(): List<Song> {
        val songs = listOf(
            Song(1, "Pesem 1", "js", "to je besedilo"),
            Song(2, "Pesem 2", "js", "to je besedilo"),
            Song(3, "Pesem 3", "js", "to je besedilo"),
            Song(4, "Pesem 4", "js", "to je besedilo"),
            Song(5, "Pesem 5", "js", "to je besedilo"),
            Song(6, "Pesem 6", "js", "to je besedilo"),

            )
        return songs
    }
}



data class Song(
    val id: Long,
    val title: String,
    val authors: String,
    val lyrics: String,
    val originalTitle: String = ""
)
