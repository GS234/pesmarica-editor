package org.mpp.dbuild.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.mpp.dbuild.NewTextWithChords
import org.mpp.dbuild.music.LyricsChunk
import org.mpp.dbuild.music.chunkGenerator2noSuspend

@Preview(
    name = "Desktop Shell Simulator",
    device = "spec:width=1024dp,height=768dp,dpi=160",
    showBackground = true,
    backgroundColor = 0xFF121212 // Optional: Forces a dark gray background if your theme is dark
)
@Composable
fun TextDisplay(){
    val neki = chunkGenerator2noSuspend(lyrics)
    var scale by remember { mutableFloatStateOf(1f) }
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    {
        Text(text = "${neki.size}")
        neki.forEach {
            when(it) {
                is LyricsChunk.LyricsChordsChunk -> {
                    it.lyrics.forEachIndexed { i, _ ->
                        NewTextWithChords(
                            it.lyrics[i],
                            it.chords[i],
                            scale
                        )
                    }
                }
                else -> {Text("-")}
            }
        }
        ScaleSlider { scale = it }
    }
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


const val lyrics = "\$D\n" +
        "   D                         h A G$\n" +
        "Gospod je moja luč in moja reš-i-tev.\n" +
        "   D                           h A  G$\n" +
        "Gospod trdnjava je v mojem življ-enju.\n" +
        "     e           G          e                  G$\n" +
        "Koga bi se moral bati, pred kom trepetati.\n" +
        "   D                         h A G$\n" +
        "Gospod je moja luč in moja reš-i-tev.\n" +
        "\n" +
        "          D$\n" +
        "Upaj v Gospoda, bodi močan.\n" +
        "        e              G            D$\n" +
        "Tvoje srce opogumi naj se, upaj v Boga. (2x)\n" +
        "D h A G$\n" +
        "(2x)\n"