package org.mpp.dbuild.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.mpp.dbuild.data.repositories.SettingsRepository
import org.mpp.dbuild.data.repositories.SongsRepository
import org.mpp.dbuild.data.sources.DatabaseInitService
import org.mpp.dbuild.data.sources.DatabaseService
import org.mpp.dbuild.data.sources.Song

class SongsViewModel(
    private var repository: SongsRepository,
    private val settingsRepository: SettingsRepository,
    private var databaseService: DatabaseService,
    // private val databaseInitService: DatabaseInitService,
) : ViewModel() {
    // main screen state
    var uiState by mutableStateOf(SongsUiState())
        private set

    fun exportDatabase(destinationPath: String) {
        viewModelScope.launch {
            databaseService.exportToDb(destinationPath)
            closeDialog()
        }
    }

    fun exportDatabaseAsText(destinationPath: String){
        viewModelScope.launch {
            uiFlags = uiFlags.copy(isWritingToFile = true)
            repository.exportSongsToText(destinationPath, uiState.songs)
            uiFlags = uiFlags.copy(isWritingToFile = false)
            closeDialog()
        }
    }

    // ??
    // fun initNewDatabase(destinationPath: String){
    //     viewModelScope.launch {
    //         databaseInitService.initDb(destinationPath)
    //         settingsRepository.setDbPath(destinationPath)
    //     }
    // }

    fun setDbPath(path: String){
        viewModelScope.launch {
            settingsRepository.setDbPath(path)
        }
    }

    fun switchRepositoryAndDbService(newRepository: SongsRepository, newDbService: DatabaseService) {
        repository = newRepository
        databaseService = newDbService

        // Reset state that belongs to the old database.
        uiState = uiState.copy(
            songs = emptyList(),
            selectedSong = null,
            showDialog = null
        )
        fetchSongs()
    }

    // var auxUiState by mutableStateOf()
    var uiFlags by mutableStateOf(SongsUiFlags())
        private set

    var fetchedSongsState by mutableStateOf(FetchedSongsState())
        private set

    private val emptySong = Song(
        id = -1L,
        title = "",
        authors = "",
        lyrics = "",
        originalTitle = ""
    )

    private var fetchJob: Job? = null

    fun fetchSongs() {
        fetchJob?.cancel()
        uiFlags = uiFlags.copy(isFetchingSongs = true)

        fetchJob = viewModelScope.launch {
            repository.getSongsFlow()
                .catch { e ->
                    // catch any database error
                    uiFlags = uiFlags.copy(isFetchingSongs = true)
                    println(e.stackTraceToString())
                }
                .collect { newSongs ->
                    // when db gets updated, new list is returned
                    uiState = uiState.copy(
                        songs = newSongs.sortedWith { song, song1 ->
                            song.title.compareTo(song1.title)
                        },
                    )
                    uiFlags = uiFlags.copy(isFetchingSongs = false)
                }
        }
    }

    fun editNew(){
        uiState = uiState.copy(selectedSong = emptySong)
        uiFlags = uiFlags.copy(isAddingNew = true)
    }

    fun testFlags(flags: SongsUiFlags){
        uiFlags = flags
    }

    fun saveSong(song: Song){
        if(uiFlags.isSavingNew) return // do not allow new save while current is still ongoing
        if(song.title.isNotEmpty() && song.lyrics.isNotEmpty()){
            uiFlags = uiFlags.copy(isSavingNew = true)
            viewModelScope.launch {
                repository.saveSong(song)
                uiFlags = uiFlags.copy(isSavingNew = false)
            }
        }
    }

    fun deleteSong(song: Song){
        uiFlags = uiFlags.copy(isDeleting = true)
        viewModelScope.launch {
            repository.deleteSong(song)
            uiFlags = uiFlags.copy(isDeleting = false)
        }
    }

    fun importSongs(filePath: String) {
        viewModelScope.launch {
            try {
                // Nastavimo ime datoteke v UI stanje, da uporabnik vidi, kaj uvaža
                // val shortFileName = filePath.substringAfterLast(if (filePath.contains("/")) "/" else "\\")
                uiFlags = uiFlags.copy(isFetchingSongs = true)
                // Repozitorij prebere datoteko in jo shrani v Room
                repository.importSongsFromFile(filePath) // flow poskrbi, da so podatki aktualni
                uiFlags = uiFlags.copy(isFetchingSongs = false)
            } catch (e: Exception) {
                uiFlags = uiFlags.copy(isFetchingSongs = false)
                println("Napaka pri uvozu: ${e.stackTraceToString()}")
            }
        }
    }

    fun importFetchedSongs() {
        if(!fetchedSongsState.isFetchingSongs && fetchedSongsState.songs != null) {
            val songs = fetchedSongsState.songs ?: listOf()
            viewModelScope.launch {
                try {
                    uiFlags = uiFlags.copy(isFetchingSongs = true)
                    repository.importSongs(songs) // flow poskrbi, da so podatki aktualni
                    uiFlags = uiFlags.copy(isFetchingSongs = false)
                    uiState = uiState.copy(showDialog = null)
                    resetDialogStates()
                } catch (e: Exception) {
                    uiFlags = uiFlags.copy(isFetchingSongs = false)
                    println("Napaka pri uvozu: ${e.stackTraceToString()}")
                }
            }
        }
        else println("no songs to add")
    }
    fun previewSongs(filePath: String){
        viewModelScope.launch {
            try {
                fetchedSongsState = fetchedSongsState.copy(songs = null, isFetchingSongs = true)
                val songs = repository.readSongsFromFile(filePath)
                fetchedSongsState = fetchedSongsState.copy(songs = songs, isFetchingSongs = false)
            }
            catch (e: Exception){
                fetchedSongsState = fetchedSongsState.copy(isFetchingSongs = false)
                println("Napaka pri uvozu: ${e.stackTraceToString()}")
            }
        }
    }
    fun selectSong(song: Song){
        uiState = uiState.copy(selectedSong = song)
        uiFlags = uiFlags.copy(isAddingNew = false)
    }
    fun resetDatabase(){
        viewModelScope.launch {
            try {
                // uiState = uiState.copy(isFetchingSongs = true)
                uiFlags = uiFlags.copy(isFetchingSongs = true)

                repository.resetDatabase()
                // uiState = uiState.copy(isFetchingSongs = false)
                uiFlags = uiFlags.copy(isFetchingSongs = false)
            } catch (e: Exception) {
                // uiState = uiState.copy(isFetchingSongs = false)
                uiFlags = uiFlags.copy(isFetchingSongs = false)
                println("Napaka pri brisanju: ${e.stackTraceToString()}")
            }
        }
    }
    fun setFileName(fileName: String){
        uiState = uiState.copy(fileName = fileName)
    }

    fun openDialog(which: DialogType){
        uiState = uiState.copy(showDialog = which)
    }
    fun closeDialog(){
        uiState = uiState.copy(showDialog = null)
        resetDialogStates()
    }
    fun resetDialogStates(){
        fetchedSongsState = FetchedSongsState()
    }
}