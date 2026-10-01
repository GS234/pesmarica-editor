package org.mpp.dbuild.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.mpp.dbuild.music.chunkGenerator2

// lyrics preview ViewModel
class LyricsViewModel(): ViewModel(){
    var lyricsState by mutableStateOf(LyricsState())
        private set

    private var generateChunksJob: Job? = null

    fun generateNewChunks(lyrics: String){
        generateChunksJob?.cancel()
        lyricsState = lyricsState.copy(isProcessing = true)
        generateChunksJob = viewModelScope.launch {
            try {
                val chunks = chunkGenerator2(lyrics)
                lyricsState = lyricsState.copy(isProcessing = false, chunks = chunks)
            }
            catch(e: Exception){
                println("[!] error while converting lyrics to chunks.")
                lyricsState = lyricsState.copy(isProcessing = false)
            }
        }
    }

    fun resetState(){
        lyricsState = LyricsState()
    }
}