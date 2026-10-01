package org.mpp.dbuild

// import androidx.compose.animation.AnimatedVisibility
// import androidx.compose.animation.fadeIn
// import androidx.compose.animation.fadeOut
// import androidx.compose.foundation.background
// import androidx.compose.foundation.gestures.awaitEachGesture
// import androidx.compose.foundation.gestures.awaitFirstDown
// import androidx.compose.foundation.gestures.calculateZoom
// import androidx.compose.foundation.gestures.waitForUpOrCancellation
// import androidx.compose.foundation.layout.Arrangement
// import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.Row
// import androidx.compose.foundation.layout.WindowInsets
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.safeDrawing
// import androidx.compose.foundation.layout.size
// import androidx.compose.foundation.layout.width
// import androidx.compose.foundation.layout.wrapContentHeight
// import androidx.compose.foundation.pager.HorizontalPager
// import androidx.compose.foundation.pager.rememberPagerState
// import androidx.compose.foundation.shape.CircleShape
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.MoreVert
// import androidx.compose.material3.Card
// import androidx.compose.material3.CenterAlignedTopAppBar
// import androidx.compose.material3.DropdownMenu
// import androidx.compose.material3.DropdownMenuItem
// import androidx.compose.material3.ExperimentalMaterial3Api
// import androidx.compose.material3.Icon
// import androidx.compose.material3.IconButton
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Scaffold
// import androidx.compose.material3.Slider
// import androidx.compose.material3.Text
// import androidx.compose.material3.TopAppBarDefaults
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect
// import androidx.compose.runtime.derivedStateOf
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableFloatStateOf
// import androidx.compose.runtime.mutableIntStateOf
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.saveable.rememberSaveable
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip
// import androidx.compose.ui.hapticfeedback.HapticFeedbackType
// import androidx.compose.ui.input.pointer.pointerInput
//
// import androidx.compose.ui.platform.LocalHapticFeedback
// import androidx.compose.ui.text.style.TextOverflow
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.window.Dialog
//
// import org.mpp.dbuild.NewLyrics
// import kotlin.math.roundToInt
//
//
// sealed class SongQuickSettingsAction{
//     object ToggleChordsAction: SongQuickSettingsAction()
//     object TransposeAction: SongQuickSettingsAction()
//     object AddToPlaylistAction: SongQuickSettingsAction()
// }
//
//
// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun SongPage(
//     onBack: () -> Unit
// ){
//     var displayChords by rememberSaveable { mutableStateOf(true) } // rememberSaveable -> so it does not change on screen rotation
//     var transposeNsemi by rememberSaveable { mutableIntStateOf(0) }
//     var openTransposeDialog by remember { mutableStateOf(false) }
//
// //    scroll, resize logika:
//     var scale by rememberSaveable { mutableFloatStateOf(1f) }
//
//     Scaffold(
//         contentWindowInsets = WindowInsets.safeDrawing,
//         topBar = {
//             CenterAlignedTopAppBar(
//                 title = {
//                     Text(
//                         modifier=Modifier.fillMaxWidth(),
//                         text=songData.song.title,
//                         overflow = TextOverflow.Ellipsis,
//                         softWrap = false,
//                         color = MaterialTheme.colorScheme.onBackground
//                     )},
//                 colors = TopAppBarDefaults.topAppBarColors(
//                     containerColor =  MaterialTheme.colorScheme.background
//                 ),
//             )
//         },
//     ) { innerPadding ->
//         Column(
//             modifier = Modifier
//                 .fillMaxSize()
//                 .padding(innerPadding)
//                 // .padding(horizontal = 20.dp)
//                 .pointerInput(Unit) {
//                     awaitEachGesture {
//                         do {
//                             val event = awaitPointerEvent()
//                             val pressed = event.changes.count { it.pressed }
//                             if (pressed >= 2) {
//                                 val zoom = event.calculateZoom()
//                                 if (zoom != 1f) {
//                                     scale = (scale * zoom).coerceIn(1f, 5f)
//                                     event.changes.forEach { it.consume() }
//                                 }
//                             }
//                         } while (event.changes.any { it.pressed })
//                     }
//                 }
//                 .pointerInput(Unit) {
//                     val viewConfig = viewConfiguration
//                     awaitEachGesture {
//                         val firstDown = awaitFirstDown()
//                         val firstUp = waitForUpOrCancellation() ?: return@awaitEachGesture
//                         val secondDown = awaitFirstDown()
//                         val isDoubleTap =
//                             (secondDown.uptimeMillis - firstUp.uptimeMillis) <= viewConfig.doubleTapTimeoutMillis
//
//                         if (!isDoubleTap) return@awaitEachGesture
//                         var previousY = secondDown.position.y
//
//                         while (true) {
//                             val event = awaitPointerEvent()
//                             val change = event.changes.firstOrNull() ?: break
//                             if (!change.pressed) break
//                             val dy = change.position.y - previousY
//                             previousY = change.position.y
//                             val zoomChange = 1f + (-dy / 300f)
//                             scale = (scale * zoomChange).coerceIn(1f, 5f)
//                             change.consume()
//                         }
//                     }
//                 }
//                 // .verticalScroll(scrollState)
//         ) {
//             when {
//                 openTransposeDialog -> {
//                     TransposeDialog(
//                         onDismissRequest = { openTransposeDialog = false },
//                         onConfirmation = { openTransposeDialog = false },
//                         initialValue = transposeNsemi,
//                         onValueUpdate = { transposeNsemi = it }
//                     )
//                 }
//                 // openAddToPlaylistDialog -> {
//                 //     AddToPlaylistDialog() { }
//                 // }
//             }
//             val info = if(songData.info != null && songData.info.authors.isNotBlank()) songData.info.authors else "-"
//             NewLyrics(
//                 lyrics = songData.song.lyrics,
//                 info = info,
//                 displayChords = displayChords,
//                 transposeNsemi = transposeNsemi,
//                 scale = scale
//             )
//         }
//     }
// }
//
//
//
