package org.mpp.dbuild.ui

import org.mpp.dbuild.data.sources.Song
import org.mpp.dbuild.music.LyricsChunk


enum class DialogType {
    NEW, OPEN, IMPORT, EXPORT, CLEAR, HELP
}

data class SongsUiState(
    val fileName: String? = null,
    val songs: List<Song> = listOf(),
    val selectedSong: Song? = null,
    val showDialog: DialogType? = null
){
    override fun toString(): String {
        return "SongsUiState:\nfileName: $fileName\n# of songs: ${songs.size}\nselected song: ${selectedSong?.title ?: "-"}"
    }
}

data class SongsUiFlags(
    val isFetchingSongs: Boolean = false,
    val isFetchingPreview: Boolean = false,
    val isWritingToFile: Boolean = false,
    val isAddingNew: Boolean = false,
    val isSavingNew: Boolean = false,
    val isDeleting: Boolean = false,
)

data class FetchedSongsState(
    val isFetchingSongs: Boolean = false,
    val songs: List<Song>? = null,
)

data class LyricsState(
    val isProcessing: Boolean = false,
    val chunks: List<LyricsChunk> = listOf()
)


