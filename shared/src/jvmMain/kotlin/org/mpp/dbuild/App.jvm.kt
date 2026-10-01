package org.mpp.dbuild

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mpp.dbuild.data.datastore.rememberDataStore
import org.mpp.dbuild.data.db.AppDatabase
import org.mpp.dbuild.data.repositories.SettingsRepository
import org.mpp.dbuild.data.repositories.SongsRepository
import org.mpp.dbuild.data.sources.DatabaseInitService
import org.mpp.dbuild.data.sources.DatabaseResetService
import org.mpp.dbuild.data.sources.DatabaseService
import org.mpp.dbuild.data.sources.FileContentReaderWriter
import org.mpp.dbuild.data.sources.FileCopyInterface
import org.mpp.dbuild.data.sources.SongsFileLocalDataSource
import org.mpp.dbuild.data.sources.SongsLocalDataSource
import org.mpp.dbuild.ui.LyricsViewModel
import org.mpp.dbuild.ui.MainScreenRoot
import org.mpp.dbuild.ui.NewFileWindow
import org.mpp.dbuild.ui.OpenFileWindow
import org.mpp.dbuild.ui.SongsViewModel
import java.io.File

@Composable
fun App(
    onNameChange: (String) -> Unit = {}
) {
    val fileContentReader = remember { JvmFileContentReaderWriter() }
    val fileCopyInterface = remember { JvmFileCopyInterface() }
    val databaseInitService = remember { DatabaseInitService(fileCopyInterface = fileCopyInterface) }
    val songsFileLocalDataSource = remember { SongsFileLocalDataSource(fileContentReader) } // fileDataSource
    val databaseFactory = remember { JvmDatabaseFactory() }
    val scope = rememberCoroutineScope()
    val containerFactory = remember {
        AppContainerFactory(
            databaseFactory = databaseFactory,
            songsFileLocalDataSource = songsFileLocalDataSource
        )
    }
    val datastore = rememberDataStore()
    val settingsRepository = remember { SettingsRepository(datastore) } //this thing does not change

    // -- database-dependent
    // var databasePath by remember { mutableStateOf("") }
    val databasePath by settingsRepository.dbPath.collectAsState(initial = "")
    val containerResult = remember(databasePath) {
        containerFactory.create(
            databasePath = databasePath,
        )
    }
    val container: AppContainer? = containerResult.getOrNull()
    val containerError: Throwable? = containerResult.exceptionOrNull()
    DisposableEffect(container) {
        onDispose {
            container?.close()
        }
    }
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (container != null) {
                val songsViewModel: SongsViewModel = viewModel(key = databasePath) {
                    val svm = SongsViewModel(
                        repository = container.songsRepository,
                        settingsRepository = settingsRepository,
                        databaseService = container.databaseService,
                    )
                    svm.setFileName(extractFilename(databasePath))
                    svm
                }
                LaunchedEffect(container.songsRepository) {
                    songsViewModel.switchRepositoryAndDbService(
                        container.songsRepository,
                        container.databaseService
                    )
                    val filename = extractFilename(databasePath)
                    songsViewModel.setFileName(filename)
                    onNameChange(filename)
                }
                val lyricsViewModel = LyricsViewModel()
                MainScreenRoot(
                    songsViewModel = songsViewModel,
                    lyricsViewModel = lyricsViewModel,
                    onNewDb = {
                        scope.launch {
                            databaseInitService.initDb(it)
                            settingsRepository.setDbPath(it)
                        }
                    }
                )
            }
            else{
                var openDbDialog by remember { mutableStateOf(0) }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Datoteka")
                    when (openDbDialog) {
                        1 -> {
                            OpenFileWindow(
                                onCancel = {openDbDialog = 0},
                                onOpen = {
                                    scope.launch {
                                        settingsRepository.setDbPath(it)
                                    }
                                }
                            )
                        }
                        2 -> {
                            NewFileWindow(
                                onNew = {
                                    scope.launch {
                                        databaseInitService.initDb(it)
                                        settingsRepository.setDbPath(it)
                                    }
                                },
                                onCancel = {openDbDialog = 0}
                            )
                        }
                        else -> {
                            Button(onClick = {
                                openDbDialog = 1
                            }){
                                Text("Odpri")
                            }
                            Button(onClick = {
                                openDbDialog = 2
                            }){
                                Text("Novo")
                            }
                        }
                    }
                }
            }
        }
    }
}

class JvmFileContentReaderWriter: FileContentReaderWriter {
    override fun readText(path: String): String {
        val file = File(path)
        return if (file.exists() && file.isFile) {
            file.readText()
        } else {
            ""
        }
    }
    override fun readLines(path: String): List<String> {
        val text = this.readText(path)
        return if(text != ""){
            text.split('\n')
        } else{
            listOf<String>()
        }
    }
    override fun writeText(path: String, text: String) {
        val file = File(path)
        file.writeText(text)
    }
}

// class JvmFileContentWriter: FileContentReader {
//     override fun readText(path: String): String {
//         val file = File(path)
//         return if (file.exists() && file.isFile) {
//             file.readText()
//         } else {
//             ""
//         }
//     }
//     override fun readLines(path: String): List<String> {
//         val text = this.readText(path)
//         return if(text != ""){
//             text.split('\n')
//         } else{
//             listOf<String>()
//         }
//     }
// }

// AI generated:
class AppContainer(
    val database: AppDatabase,
    val databaseService: DatabaseService,
    val songsRepository: SongsRepository,
){
    fun close(){
        database.close()
    }
}

class AppContainerFactory(
    private val databaseFactory: DatabaseFactory,
    private val songsFileLocalDataSource: SongsFileLocalDataSource
) {
    fun create(databasePath: String): Result<AppContainer> {
        val databaseResult = databaseFactory.create(databasePath)

        return databaseResult.map { database ->
            val songDao = database.songDao()

            val songsLocalDataSource = SongsLocalDataSource(
                songDao = songDao,
                ioDispatcher = Dispatchers.IO
            )

            val databaseResetService = DatabaseResetService(database)
            val databaseService = DatabaseService(
                database = database,
            )

            val repository = SongsRepository(
                songsLocalDataSource = songsLocalDataSource,
                songsFileLocalDataSource = songsFileLocalDataSource,
                databaseResetService = databaseResetService
            )

            AppContainer(
                database = database,
                databaseService = databaseService,
                songsRepository = repository,
            )
        }
    }
}

interface DatabaseFactory {
    fun create(path: String): Result<AppDatabase>
}

class JvmDatabaseFactory : DatabaseFactory {
    override fun create(path: String): Result<AppDatabase> {
        return runCatching {
            if(path.isBlank()) throw IllegalArgumentException("wrong file name")
            val file = File(path)
            if(!(file.exists() && file.isFile)) throw IllegalArgumentException("wrong file name")
            val database = Room.databaseBuilder<AppDatabase>(
                name = path
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            // should also verify if database is valid
            database
        }


    }
}

fun extractFilename(filePath: String): String = filePath.substringAfterLast(if (filePath.contains("/")) "/" else "\\")

class JvmFileCopyInterface: FileCopyInterface{
    override suspend fun copyTo(sourcePath: String, destinationPath: String) {
        withContext(Dispatchers.IO) {
            val sourceFile = File(sourcePath)
            val targetFile = File(destinationPath)
            try {
                sourceFile.copyTo(targetFile)
            } catch (e: Exception) {
                println("[error] ${e.message}")
            }
        }
    }

    override suspend fun saveTo(byteArray: ByteArray, destinationPath: String) {
        withContext(Dispatchers.IO) {
            val targetFile = File(destinationPath)
            try {
                targetFile.writeBytes(byteArray)
            } catch (e: Exception) {
                println("[error] ${e.message}")
            }
        }
    }

}
