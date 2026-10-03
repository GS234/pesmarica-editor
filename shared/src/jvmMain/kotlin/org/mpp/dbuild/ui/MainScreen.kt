package org.mpp.dbuild.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path
import org.mpp.dbuild.NewLyrics2
import org.mpp.dbuild.data.sources.Song
import org.mpp.dbuild.music.LyricsChunk
import org.mpp.dbuild.ui.theme.MonoFF
import org.mpp.dbuild.ui.theme.getMonoFontFamily
import kotlin.math.roundToInt

@Composable
fun MainScreenRoot(
    songsViewModel: SongsViewModel = viewModel(),
    lyricsViewModel: LyricsViewModel = viewModel(),
    onNewDb: (String) -> Unit = {} // new empty database
){
    val uiState = songsViewModel.uiState
    val uiFlags = songsViewModel.uiFlags
    val fetchedSongsState = songsViewModel.fetchedSongsState
    val lyricsState = lyricsViewModel.lyricsState
    Box(modifier = Modifier.fillMaxSize()){
        MainScreen(
            uiState.songs,
            selectedSong = uiState.selectedSong,
            selectedSongLyricsChunks = lyricsState.chunks,
            isFetchingSongs = uiFlags.isFetchingSongs,
            uiFlags = uiFlags,
            onDbChange = {
                songsViewModel.setDbPath(it)
            },
            onSongSelect = {
                songsViewModel.selectSong(it)
                lyricsViewModel.generateNewChunks(it.lyrics)
            },
            onRowAction = {
                when(it){
                    is RowAction.OpenDB -> songsViewModel.openDialog(DialogType.OPEN)
                    is RowAction.ExportDB -> songsViewModel.openDialog(DialogType.EXPORT)
                    is RowAction.ImportFromFile -> songsViewModel.openDialog(DialogType.IMPORT)
                    is RowAction.NewDB -> songsViewModel.openDialog(DialogType.NEW)
                    is RowAction.ClearDB -> songsViewModel.openDialog(DialogType.CLEAR)
                    is RowAction.AppHelp -> songsViewModel.openDialog(DialogType.HELP)
                }
            },
            onTextUpdate = {
                lyricsViewModel.generateNewChunks(it)
            },
            onSave = {
                songsViewModel.saveSong(it)
            },
            onAddNewSong = {
                songsViewModel.editNew()
            },
            onDelete = {
                songsViewModel.deleteSong(it)
            }
        )
        if(uiState.showDialog != null){
            Dialog(
                onDismissRequest = songsViewModel::closeDialog
            ){
                when(uiState.showDialog){
                    DialogType.IMPORT ->
                        ImportFileWindow(
                            fetchedSongsState = fetchedSongsState,
                            onPreview = { path ->
                                if(path != null) songsViewModel.previewSongs(path)
                                else println("napaka: neznana pot (null)")
                            },
                            onImport = {
                                songsViewModel.importFetchedSongs()
                            },
                            onCancel = {
                                songsViewModel.closeDialog()
                            }
                        )
                    DialogType.NEW -> {
                        NewFileWindow(
                            onNew = {
                                onNewDb(it)
                                songsViewModel.closeDialog()
                            },
                            onCancel = {
                                songsViewModel.closeDialog()
                            }
                        )
                    }
                    DialogType.OPEN -> {
                        OpenFileWindow(
                            onOpen = {
                                // onDbChange(it)
                                if(it.isNotEmpty()) songsViewModel.setDbPath(it)
                                else songsViewModel.closeDialog()
                                // songsViewModel.fetchSongs()
                                // songsViewModel.closeDialog()
                            },
                            onCancel = {
                                songsViewModel.closeDialog()
                            }
                        )
                    }
                    DialogType.EXPORT -> {
                        ExportFileWindow(
                            onExport = { path, type ->
                                when(type){
                                    ExportType.DB -> {
                                        songsViewModel.exportDatabase(path)
                                    }
                                    ExportType.TEXT -> {
                                        songsViewModel.exportDatabaseAsText(path)
                                    }
                                }
                            },
                            onCancel = {
                                songsViewModel.closeDialog()
                            }
                        )
                    }
                    DialogType.CLEAR -> ConfirmDeleteWindow(
                        onCancel = songsViewModel::closeDialog,
                        onConfirm = {
                            songsViewModel.resetDatabase()
                            songsViewModel.closeDialog()
                        }
                    )
                    DialogType.HELP -> {
                        HelpWindow(
                            onClose = songsViewModel::closeDialog
                        )
                    }
                }

            }
        }
    }

}

@Composable
private fun MainScreenPreview(){
    val songs = listOf(
        Song(1, "Pesem 1", "js", "to je besedilo"),
        Song(2, "Pesem 2", "js", "to je besedilo"),
        Song(3, "Pesem 3", "js", "to je besedilo"),
        Song(4, "Pesem 4", "js", "to je besedilo"),
        Song(5, "Pesem 5", "js", "to je besedilo"),
        Song(6, "Pesem 6", "js", "to je besedilo"),

        )
    val mainScreenState = SongsUiState(
        songs = songs,
        fileName = null
    )
    val mainScreenFlags = SongsUiFlags(
        isFetchingSongs = false,
        isFetchingPreview = false,
    )
    // Text("abc")
    MainScreen(
        mainScreenState.songs,
        selectedSong = songs[0],
        isFetchingSongs = mainScreenFlags.isFetchingSongs,
        uiFlags = mainScreenFlags
    )
}

@Composable
private fun ActionRowButton(
    title: String,
    onClick: () -> Unit = {},
){
    // should be hoverable
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(5.dp)
            .clickable(onClick = onClick)
        // .background(MaterialTheme.colorScheme.background)
        ,
        contentAlignment = Alignment.Center
    )
    {
        Text(title)
    }
}

@Composable
private fun ContentColumn(
    widthFraction: Float,
    padding: Dp = 5.dp,
    content: @Composable () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .fillMaxHeight()
            .padding(padding)
    ){
        content()
    }
}

@Composable
private fun SongButton(
    name: String,
    onClick: () -> Unit = {},
    onRemove: () -> Unit = {}
){
    Box{
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {onClick()}
                .padding(5.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                modifier = Modifier.weight(1f),
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                modifier = Modifier.size(20.dp),
                onClick = onRemove
            ){
                Text("x")
            }
        }
    }
}

sealed class RowAction{
    class OpenDB: RowAction()
    class NewDB: RowAction()
    class ExportDB: RowAction()
    class ClearDB: RowAction()
    class AppHelp: RowAction()
    class ImportFromFile: RowAction()
}

@Composable
private fun MainScreen(
    songs: List<Song>,
    selectedSong: Song? = null,
    selectedSongLyricsChunks: List<LyricsChunk> = listOf(),
    isFetchingSongs: Boolean,
    uiFlags: SongsUiFlags,
    showDialog: Boolean = false,
    onSongSelect: (Song) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onDbChange: (String) -> Unit = {},
    onRowAction: (RowAction) -> Unit = {},
    onTextUpdate: (String) -> Unit = {},
    onAddNewSong: ()->Unit = {},
    onSave: (Song) -> Unit = {},
    onDelete: (Song) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // action row
        val rowHeight = 30.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            ActionRowButton("Novo", onClick = { onRowAction(RowAction.NewDB()) })
            ActionRowButton("Odpri", onClick = { onRowAction(RowAction.OpenDB()) })
            ActionRowButton("Uvozi", onClick = { onRowAction(RowAction.ImportFromFile()) })
            ActionRowButton("Izvozi", onClick = { onRowAction(RowAction.ExportDB()) })
            ActionRowButton("Izbriši", onClick = { onRowAction(RowAction.ClearDB()) })
            // ActionRowButton("Pomoč", onClick = { onRowAction(RowAction.AppHelp()) })
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

        // content row
        Row(
            modifier = Modifier.fillMaxSize().padding(5.dp)
        ) {
            // column proportions
            val songListWidth = 0.2f // % of remaining (first 20 %)
            val editorWidth = 0.5f // 50% of 80% = 40%
            val previewWidth = 1.0f // 100% of 40%
            ContentColumn(songListWidth) {
                Text("Seznam pesmi")
                if (isFetchingSongs) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        LazyColumn(
                            // modifier = Modifier.fillMaxHeight(0.95f)
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = songs,
                                key = { it.id }
                            ) { item ->
                                SongButton(
                                    item.title,
                                    onClick = { onSongSelect(item) },
                                    onRemove = { onDelete(item) }
                                )
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            onClick = onAddNewSong
                        ) {
                            Text("Dodaj") //should be iconButton
                        }
                    }
                }
            }
            var lyricsTextFieldState = rememberTextFieldState(initialText = "")
            ContentColumn(editorWidth) {
                Text("Urejevalnik pesmi")
                // AnimatedVisibility
                if (selectedSong == null) {
                    Text(
                        modifier = Modifier.alpha(0.6f),
                        text = "Izberi pesem s seznama ali dodaj novo",
                    )
                } else {
                    key(selectedSong.id) {
                        val naslovTextFieldState =
                            rememberTextFieldState(initialText = selectedSong.title)
                        val avtorTextFieldState =
                            rememberTextFieldState(initialText = selectedSong.authors)
                        lyricsTextFieldState =
                            rememberTextFieldState(initialText = selectedSong.lyrics)
                        LaunchedEffect(lyricsTextFieldState.text){
                            // println("text changed!")
                            onTextUpdate(lyricsTextFieldState.text.toString())
                        }
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            state = naslovTextFieldState,
                            label = { Text("Naslov") }
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            state = avtorTextFieldState,
                            label = { Text("Avtor") }
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // update preview
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().weight(1f)
                                    .padding(0.dp, 10.dp, 0.dp, 0.dp),
                                state = lyricsTextFieldState,
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = getMonoFontFamily(
                                        MonoFF.RobotoMono
                                    )
                                ),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {
                                    onSave(
                                        Song(
                                            id = -1L,
                                            title = naslovTextFieldState.text.toString(),
                                            authors = avtorTextFieldState.text.toString(),
                                            lyrics = lyricsTextFieldState.text.toString(),
                                            originalTitle = "",
                                        )
                                    )
                                },
                                    enabled = !(uiFlags.isSavingNew || naslovTextFieldState.text.isEmpty() || lyricsTextFieldState.text.isEmpty())) {
                                    Text("Shrani")
                                }
                            }
                        }
                    }
                }
            }
            ContentColumn(previewWidth) {
                // AnimatedVisibility
                var scale by remember {mutableFloatStateOf(1f)}
                Text("Predogled besedila")
                if (selectedSong == null) {
                    Text("-")
                } else {
                    var transposeNsemi by remember {mutableIntStateOf(0)}
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                    ) {
                        key(selectedSong.id, selectedSongLyricsChunks) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline
                                            ), shape = RoundedCornerShape(5.dp)
                                        )
                                ) {
                                    NewLyrics2(
                                        chunks = selectedSongLyricsChunks,
                                        transposeNsemi = transposeNsemi,
                                        scale = scale
                                    )
                                }
                            }
                        }
                        TransposeSlider { transposeNsemi = it }
                        Text("Povečava: $scale")
                        ScaleSlider { scale = it }
                    }
                }
            }
        }
    }
}

//action windows:
@Composable
private fun ActionWindowContainer(
    actionName: String = "",
    width: Dp = 600.dp,
    height: Dp = 400.dp,
    actions: @Composable () -> Unit = {}, //row scope
    content: @Composable () -> Unit = {}
){
    val paddingDp = 10.dp
    val titleRowHeight = 20.dp
    val actionRowHeight = 50.dp
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(paddingDp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(titleRowHeight),
            ){
                Text(actionName)
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(height - paddingDp*2 - titleRowHeight - actionRowHeight)
            ){
                content()
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(actionRowHeight),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End)
            ){
                actions()
            }

        }
    }

}


@Composable
fun OpenFileWindow(
    onOpen: (String) -> Unit = {},
    onCancel: () -> Unit = {}
){
    val filenameDefaultValue = ""
    val launcher = rememberFilePickerLauncher(
        onError = {
            println(it.message)
            onCancel()
        },
        onResult = { file ->
            onOpen(file?.path ?: filenameDefaultValue)
        },
    )
    launcher.launch()

    ActionWindowContainer(
        actionName = "Odpri",
        height = 150.dp,
        width = 400.dp,
        actions = {
            Button(
                onClick = onCancel
            ){
                Text("Prekliči")
            }
        }
    ) {
        CircularProgressIndicator()
    }
}
@Composable
private fun ImportFileWindow(
    fetchedSongsState: FetchedSongsState,
    onPreview: (String?) -> Unit = {},
    onImport: () -> Unit = {},
    onCancel: () -> Unit = {}
){
    ActionWindowContainer(
        width = 250.dp,
        actionName = "Uvozi datoteko",
        actions = {
            Button(onClick = onCancel){
                Text("Prekliči")
            }
            Button(onClick = onImport){
                Text("Uvozi")
            }
        }
    ) {
        val textFieldWidth = 120.dp
        val filenameDefaultValue = "-"
        var filename by remember { mutableStateOf(filenameDefaultValue) }
        // file picker
        val launcher = rememberFilePickerLauncher(
            onError = { println(it.message)},
            onResult = { file ->
                filename = file?.path ?: filenameDefaultValue
                onPreview(filename)
                // println(file?.path)
            }
        )
        Row(
            modifier = Modifier.fillMaxSize()
        ){
            Column(
                modifier = Modifier.fillMaxSize()
            ){
                Text("Datoteka: $filename")
                Button(onClick= {launcher.launch()}){Text("Izberi")}
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    if(!fetchedSongsState.songs.isNullOrEmpty())
                        "Zaznane pesmi (${fetchedSongsState.songs.size}):"
                    else "Zaznane pesmi:"
                )
                if(fetchedSongsState.isFetchingSongs){
                    CircularProgressIndicator()
                }
                else{
                    if(fetchedSongsState.songs != null){
                        LazyColumn {
                            itemsIndexed(fetchedSongsState.songs){ index, song ->
                                Text("${index + 1}. ${song.title}")
                            }
                        }
                    }
                    else{
                        Text("ni pesmi")
                    }
                }
            }
        }
    }
}

// todo: finish export logic (as txt, as db)
enum class ExportType{
    DB, TEXT
}
@Composable
private fun ExportFileWindow(
    onExport: (String, ExportType) -> Unit = {path, asText -> },
    onCancel: () -> Unit = {}
){
    val launcher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = { failure ->
            // A valid file-saving operation could not be completed
            println("canceled")
            onCancel()
        },
        onResult = { file ->
            if (file == null) {
                // The user canceled the saver
                onCancel()
            } else {
                if(file.path.endsWith(".txt")) onExport(file.path, ExportType.TEXT)
                else onExport(file.path, ExportType.DB)
            }
        },
    )
    launcher.launch(
        suggestedName = "pesmi",
        defaultExtension = "db",
        allowedExtensions = setOf("db", "txt")
    )
    ActionWindowContainer(
        actionName = "Izvozi datoteko pesmi",
        height = 240.dp,
        width = 295.dp,
        actions = {
            Button(onClick = onCancel){
                Text("Prekliči")
            }
        }
    ) {
        CircularProgressIndicator()
    }
}
@Composable
fun NewFileWindow(
    onNew: (String) -> Unit = {},
    onCancel: () -> Unit = {},
){
    val launcher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = { failure ->
            // A valid file-saving operation could not be completed
            println("canceled")
            onCancel()
        },
        onResult = { file ->
            if (file == null) {
                // The user canceled the saver
                onCancel()
            } else {
                onNew(file.path)
                // filePath = file.path
            }
        },
    )
    launcher.launch(
        suggestedName = "novo",
        defaultExtension = "db"
    )
    ActionWindowContainer(
        actionName = "Nova datoteka",
        height = 240.dp,
        width = 295.dp,
        actions = {
            Button(onClick = onCancel){
                Text("Prekliči")
            }
        }
    ) {
        CircularProgressIndicator()
    }
}
@Composable
private fun ConfirmDeleteWindow(
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {}
){
    ActionWindowContainer(
        actionName = "Potrdi izbris",
        height = 180.dp,
        width = 300.dp,
        actions = {
            Button(onClick = onCancel){
                Text("ne")
            }
            Button(onClick = onConfirm){
                Text("da")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Text("Ali res želite izbrisati vse pesmi iz datoteke? Dejanja ni mogoče razveljaviti.")
        }
    }
}
// help window: todo - content
@Composable
private fun HelpWindow(
    onClose: () -> Unit = {}
){
    ActionWindowContainer(
        actionName = "Pomoč",
        actions = {
            Button(onClick = onClose){
                Text("zapri")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Text("Pomoč pri uporabi aplikacije")
        }
    }
}


@Composable
fun TransposeSlider(
    initialValue: Int = 0,
    onValueUpdate: (v:Int) -> Unit,
) {
    var sliderPosition by remember { mutableFloatStateOf(initialValue.toFloat()) }
    var previous by remember { mutableIntStateOf(initialValue) }
    Text(text = "Transponiraj: ${
        when {
            sliderPosition.roundToInt() > 0 -> "+"
            sliderPosition.roundToInt() < 0 -> ""
            else -> " "
        }
    }${(sliderPosition.roundToInt()).toString()}")
    Slider(
        value = sliderPosition,
        onValueChange = { value ->
            sliderPosition = value
            val newValue = value.roundToInt()
            onValueUpdate(value.roundToInt())
            previous = newValue
        },
        valueRange = -5f..6f,
        steps = 10
    )
}

@Composable
fun ScaleSlider(
    initialValue: Float = 1f,
    onValueUpdate: (v:Float) -> Unit,
) {
    var sliderPosition by remember { mutableFloatStateOf(initialValue) }
    Slider(
        value = sliderPosition,
        onValueChange = { value ->
            sliderPosition = value
            onValueUpdate(value)
        },
        valueRange = 0.8f..3f,
    )
}
