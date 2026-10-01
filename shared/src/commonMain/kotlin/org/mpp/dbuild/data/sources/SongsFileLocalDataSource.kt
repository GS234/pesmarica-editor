package org.mpp.dbuild.data.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mpp.dbuild.music.songs2Text
import org.mpp.dbuild.music.text2Songs

class SongsFileLocalDataSource(
    private val contentReaderWriter: FileContentReaderWriter
) {

    // Funkcija prebere datoteko in vrne seznam vrstic (ali surovo besedilo)
    fun readSongsFromFile(filePath: String): List<Song> { // suspend?
        val text = contentReaderWriter.readText(filePath)
        return text2Songs(text) // possibly long computation time
        // val rawSongs = lines.split("=")
        // val songs = mutableListOf<Song>()
        // rawSongs.forEachIndexed { index, rawSong ->
        //     val song = Song(index.toLong(), "pesem X", "", rawSong)
        //     songs.add(song)
        // }
    }

    suspend fun writeSongsToFile(filePath: String, songs: List<Song>) = withContext(Dispatchers.IO) {
        val text = songs2Text(songs)
        contentReaderWriter.writeText(filePath, text)
    }

}

interface FileContentReaderWriter {
    fun readLines(path: String): List<String>
    fun readText(path: String): String
    fun writeText(path: String, text: String)
}