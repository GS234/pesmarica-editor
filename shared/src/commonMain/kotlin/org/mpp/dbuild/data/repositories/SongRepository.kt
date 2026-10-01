package org.mpp.dbuild.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mpp.dbuild.data.db.SongEntity
import org.mpp.dbuild.data.db.SongInfo
import org.mpp.dbuild.data.sources.DatabaseInitService
import org.mpp.dbuild.data.sources.DatabaseResetService
import org.mpp.dbuild.data.sources.Song
import org.mpp.dbuild.data.sources.SongsFileLocalDataSource
import org.mpp.dbuild.data.sources.SongsLocalDataSource

class SongsRepository(
    private val songsLocalDataSource: SongsLocalDataSource,
    private val songsFileLocalDataSource: SongsFileLocalDataSource,
    private val databaseResetService: DatabaseResetService,
) {
    suspend fun getSongs(): List<Song> = songsLocalDataSource.getSongs()
    fun getSongsFlow(): Flow<List<Song>> {
        return songsLocalDataSource.getSongsWithDataFlow().map {entityList ->
            entityList.map { entity ->
                Song(
                    id = entity.song.id,
                    title = entity.song.title,
                    authors = entity.info?.authors ?: "-",
                    lyrics = entity.song.lyrics,
                )
            }
        }
    }

    private fun song2songData(song: Song): Pair<SongEntity, SongInfo>{
        return SongEntity(
            title = song.title,
            lyrics = song.lyrics
        ) to SongInfo(
            songId = -1L, // song is found by title and then matched by its real id (from db), so this could be anything because it does not matter
            authors = song.authors,
            category = "",
            tonality = ""
        )
    }

    suspend fun importSongs(songs: List<Song>) {
        val newSongs = songs.map { song2songData(it) }
        newSongs.forEach { song ->
            songsLocalDataSource.insertSongWithData(song.first, song.second)
        }
    }
    suspend fun importSongsFromFile(absolutePath: String) {
        val songs = readSongsFromFile(absolutePath)
        importSongs(songs)
    }
    suspend fun readSongsFromFile(absolutePath: String): List<Song> = withContext(Dispatchers.Default) {
        songsFileLocalDataSource.readSongsFromFile(absolutePath)
    }

    suspend fun exportSongsToText(path: String, songs: List<Song>){
        songsFileLocalDataSource.writeSongsToFile(path, songs)
    }

    suspend fun saveSong(song: Song){
        val songDataPair = song2songData(song)
        songsLocalDataSource.insertSongWithData(songDataPair.first, songDataPair.second)
    }

    suspend fun deleteSong(song: Song){
        songsLocalDataSource.deleteSong(SongEntity(title = song.title, lyrics = song.lyrics, id = song.id))
    }

    suspend fun resetDatabase(){
        databaseResetService.clearAllData()
    }
}