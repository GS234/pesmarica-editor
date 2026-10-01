package org.mpp.dbuild.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.mpp.dbuild.data.sources.Song
import org.mpp.dbuild.ui.theme.MonoFF
import org.mpp.dbuild.ui.theme.getMonoFontFamily

@Composable
private fun MainScreenRoot(
    songsViewModel: SongsViewModel = viewModel()
){

}

// @Preview(
//     name = "Desktop Shell Simulator",
//     // 🖥️ This forces Android Studio to render a 1024x768 desktop landscape canvas
//     device = "spec:width=1024dp,height=768dp,dpi=160",
//     showBackground = true,
//     backgroundColor = 0xFF121212 // Optional: Forces a dark gray background if your theme is dark
// )
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
    val mainScreenFlags = SongsUiFlags()
    // Text("abc")
    MainScreen(
        mainScreenState.fileName ?: "new file",
        mainScreenState.songs,
        selectedSong = songs[0],
        isFetchingSongs = mainScreenFlags.isFetchingSongs
    )
}

@Preview(
    name = "Desktop Shell Simulator",
    // 🖥️ This forces Android Studio to render a 1024x768 desktop landscape canvas
    device = "spec:width=1024dp,height=768dp,dpi=160",
    showBackground = true,
    backgroundColor = 0xFF121212 // Optional: Forces a dark gray background if your theme is dark
)
@Composable
private fun ActionWindowContainerRoot(){
    // OpenFileWindow()
    // ImportFileWindow()
    // ExportFileWindow()
    // ConfirmDeleteWindow()
    // HelpWindow()
    // ActionWindowContainer(
    //     "Action name",
    //     actions = {
    //         Button({}){Text("click")}
    //         Button({}){Text("click")}
    //         Button({}){Text("click")}
    //     }
    // )
}

sealed class RowAction{
    class OpenDB: RowAction()
    class NewDB: RowAction()
    class ExportDB: RowAction()
    class ClearDB: RowAction()
    class AppHelp: RowAction()
    data class ImportFromFile(val path: String): RowAction()
}

@Composable
private fun MainScreen(
    fileName: String,
    songs: List<Song>,
    selectedSong: Song? = null,
    isFetchingSongs: Boolean,
    onSongSelect: (Song) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onRowAction: (RowAction) -> Unit = {}

){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ,
    ){
        // action row
        val rowHeight = 30.dp
        Row(
            modifier = Modifier.fillMaxWidth().height(rowHeight)
        ){
            ActionRowButton("Odpri", onClick = { onRowAction(RowAction.OpenDB()) })
            ActionRowButton("Uvozi", onClick = { onRowAction(RowAction.ImportFromFile("/home/gasper/Ostalo/mpp/pesmarica_data/fili/source/other.txt")) })
            ActionRowButton("Izvozi", onClick = { onRowAction(RowAction.ExportDB()) })
            ActionRowButton("Novo", onClick = { onRowAction(RowAction.NewDB()) })
            ActionRowButton("Izbriši", onClick = { onRowAction(RowAction.ClearDB()) })
            ActionRowButton("Pomoč", onClick = { onRowAction(RowAction.AppHelp()) })
        }

        // content row
        Row(
            modifier = Modifier.fillMaxSize().padding(5.dp)
        ){
            // column proportions
            val songListWidth = 0.2f // % of remaining (first 20 %)
            val editorWidth = 0.5f // 50% of 80% = 40%
            val previewWidth = 1.0f // 100% of 40%
            ContentColumn(songListWidth){
                if(isFetchingSongs){
                    CircularProgressIndicator()
                }
                else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ){
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.95f)
                        ) {
                            items(
                                items = songs,
                                key = {it.id}
                            ) { item ->
                                SongButton(item.title, onClick = { onSongSelect(item) })
                                // Text(
                                //     text = "$i ${item.title}",
                                //     color = MaterialTheme.colorScheme.onBackground
                                // )
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            onClick = {}
                        ){
                            Text("dodaj +") //should be iconButton
                        }
                    }
                }
            }
            var lyricsTextFieldState = rememberTextFieldState(initialText = "")
            ContentColumn(editorWidth) {
                Text("Urejevalnik pesmi")
                // AnimatedVisibility
                if(selectedSong == null){
                    Text("Izberi pesem s seznama")
                }
                else{
                    key(selectedSong.id){
                        val naslovTextFieldState = rememberTextFieldState(initialText = selectedSong.title)
                        val avtorTextFieldState = rememberTextFieldState(initialText = selectedSong.authors)
                        lyricsTextFieldState = rememberTextFieldState(initialText = selectedSong.lyrics)
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            state = naslovTextFieldState,
                            label = { Text("Naslov") }
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            state = avtorTextFieldState,
                            label = { Text("Avtor") }
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ){
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.90f).padding(0.dp, 10.dp, 0.dp, 0.dp),
                                state = lyricsTextFieldState,
                                textStyle = LocalTextStyle.current.copy(fontFamily = getMonoFontFamily(MonoFF.RobotoMono))
                            )
                            // Text(
                            //     text = text.replace("_",""),
                            //     overflow = TextOverflow.Visible,
                            //     letterSpacing = TextParams.spacing,
                            //     lineHeight = TextParams.singleLineHeight*scale,
                            //     fontSize = TextParams.fontSize*scale,
                            //     fontFamily = TextParams.monoFont
                            // )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {}){
                                    Text("Shrani")
                                }
                            }
                        }
                    }
                }
            }
            ContentColumn(previewWidth){
                // AnimatedVisibility
                Text("Predogled")
                if(selectedSong == null){
                    Text("-")
                }
                else{
                    key(selectedSong.id){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(0.dp, 2.dp, 0.dp, 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground
                                        ), shape = RoundedCornerShape(5.dp)
                                    )
                            ) {
                                // NewLyrics(
                                //     selectedSong.lyrics
                                // )
                            }
                        }
                    }
                    // // ne bo vredu textField, ker to ni text (ampak chunk list)
                    // // treba bo narest pregledovalnik predogleda
                    // // (oz. wrapper, za prikaz pesmi loh kr direkt iz pesmarica projekta kopiram)
                    // key(selectedSong.id){
                    //     // val lyricsPreviewFieldState = rememberTextFieldState(initialText = selectedSong.lyrics)
                    //     OutlinedTextField(
                    //         modifier = Modifier.fillMaxSize().padding(0.dp, 8.dp, 0.dp, 0.dp),
                    //         readOnly = true,
                    //         state = lyricsTextFieldState,
                    //     )
                    // }
                }
            }
        }
    }
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
            Text(name)
            IconButton(
                modifier = Modifier.size(20.dp),
                onClick = onRemove
            ){
                Text("-")
            }
        }
    }
}

@Composable
private fun MainScreenOl(
    fileName: String,
    songs: List<Song>,
    selectedSong: Song? = null,
    isFetchingSongs: Boolean,
    onNameChange: (String) -> Unit = {},
    generateFile: (List<Song>) -> Unit = {},
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ,
    ){
        // action row
        val rowHeight = 30.dp
        Row(
            modifier = Modifier.fillMaxWidth().height(rowHeight)
        ){
            ActionRowButton("Datoteka")
        }

        // content row
        Row(
            modifier = Modifier.fillMaxSize().padding(5.dp)
        ){
            // column proportions
            val songListWidth = 0.2f // % of remaining (first 20 %)
            val editorWidth = 0.5f // 50% of 80% = 40%
            val previewWidth = 1.0f // 100% of 40%
            ContentColumn(songListWidth){
                if(isFetchingSongs){
                    CircularProgressIndicator()
                }
                else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ){
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.95f)
                        ) {
                            itemsIndexed(songs) { i, item ->
                                SongButton(item.title, {})
                            // Text(
                                //     text = "$i ${item.title}",
                                //     color = MaterialTheme.colorScheme.onBackground
                                // )
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            onClick = {}
                        ){
                            Text("dodaj +") //should be iconButton
                        }
                    }
                }
            }
            ContentColumn(editorWidth) {
                Text("Urejevalnik pesmi")
                // AnimatedVisibility
                if(selectedSong == null){
                    Text("Izberi pesem s seznama")
                }
                else{
                    val naslovTextFieldState = rememberTextFieldState(initialText = selectedSong.title)
                    val avtorTextFieldState = rememberTextFieldState(initialText = selectedSong.authors)
                    val lyricsTextFieldState = rememberTextFieldState(initialText = selectedSong.lyrics)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        state = naslovTextFieldState,
                        label = { Text("Naslov") }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        state = avtorTextFieldState,
                        label = { Text("Avtor") }
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ){
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.90f).padding(0.dp, 10.dp, 0.dp, 0.dp),
                            state = lyricsTextFieldState,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = {}){
                                Text("Shrani")
                            }
                        }
                    }
                }
            }
            ContentColumn(previewWidth){
                // AnimatedVisibility
                Text("Predogled")
                if(selectedSong == null){
                    Text("-")
                }
                else{
                    // ne bo vredu textField, ker to ni text (ampak chunk list)
                    // treba bo narest pregledovalnik predogleda
                    // (oz. wrapper, za prikaz pesmi loh kr direkt iz pesmarica projekta kopiram)
                    val lyricsPreviewFieldState = rememberTextFieldState(initialText = selectedSong.lyrics)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxSize().padding(0.dp, 8.dp, 0.dp, 0.dp),
                        readOnly = true,
                        state = lyricsPreviewFieldState,
                    )
                }
            }
        }
    }
}