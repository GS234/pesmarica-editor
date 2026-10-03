package org.mpp.dbuild.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.mpp.dbuild.TextParams
import org.mpp.dbuild.chordStyler
import org.mpp.dbuild.lyricsStyler
import org.mpp.dbuild.music.ChordLine
import org.mpp.dbuild.music.LyricsChunk
import org.mpp.dbuild.music.PositionedChord
import org.mpp.dbuild.music.chunkGenerator2
import org.mpp.dbuild.music.chunkGenerator2noSuspend
import org.mpp.dbuild.normalizeChordLyricsLine
import org.mpp.dbuild.styleText
import kotlin.collections.component1
import kotlin.collections.component2

const val text = "Li Europan lingues  es membres del sam familie. Lor separat existentie es un myth. Por scientie, musica, sport etc, litot Europa usa li sam vocabular. Li lingues differe solmen in li grammatica, li pronunciation e li plu commun vocabules. Omnicos"

@Composable
fun DoubleLine2(
    chordLines: List<ChordLine>,
    textLines: List<String>,
    showL1: Boolean = true,
    modifier: Modifier = Modifier,
    transposeNsemi: Int,
    scale: Float = 1f
){
    if(showL1){
        Box(modifier = modifier){
            chordLines.forEachIndexed { i, _ ->
                TextWithChords(textLines[i], chordLines[i].transpose(nSemi = transposeNsemi))
            }
        }
    }
    else{
        val l2 = textLines.joinToString("\n").replace("_","")
        Box(modifier = modifier){
            Text(
                text = l2,
                overflow = TextOverflow.Visible,
                letterSpacing = TextParams.spacing,
                lineHeight = TextParams.singleLineHeight*scale,
                fontSize = TextParams.fontSize*scale,
                fontFamily = TextParams.monoFont
            )
        }
    }
}


@Composable
fun TextWithChords(textLine: String, chordLine: ChordLine){
    var locations by remember { mutableStateOf(emptyMap<PositionedChord, Offset>()) }
    val density = LocalDensity.current
    val lineHeight = 55.sp
    val yOff = 6f
    Box {
        Text(
            text = textLine,
            lineHeight = lineHeight,
            onTextLayout = { layoutResult ->
                val newLocations = mutableMapOf<PositionedChord, Offset>()
                chordLine.positionedChords.forEach {
                    val rect = layoutResult.getBoundingBox(it.position)
                    val zacetekX = rect.left
                    val vrhY = rect.top - yOff
                    newLocations[it] = Offset(zacetekX, vrhY)
                }
                locations = newLocations
            },
        )

        locations.forEach { (positionedChord, offset) ->
            val xDp = with(density) { offset.x.toDp() }
            val yDp = with(density) { offset.y.toDp() }
            Box(
                modifier = Modifier
                    .offset(
                        x = xDp,
                        y = yDp
                    )
                    .background(Color.Black, shape = RoundedCornerShape(4.dp))
                    .padding(4.dp)
            ) {
                Text(positionedChord.chord.toString(), color = Color.White, fontSize = 12.sp)
            }
        }
    }
}


@Preview(
    name = "Desktop Shell Simulator",
    device = "spec:width=1024dp,height=768dp,dpi=160",
    showBackground = true,
    backgroundColor = 0xFF121212 // Optional: Forces a dark gray background if your theme is dark
)
@Composable
fun TextDisplay(){
    // var neki by remember { mutableStateOf<List<LyricsChunk>>(emptyList()) }
    // LaunchedEffect(Unit){
    //     val chunks = chunkGenerator2(lyrics)
    //     neki = chunks
    // }
    val neki = chunkGenerator2noSuspend(lyrics)
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
                        TextWithChords(it.lyrics[i], it.chords[i])
                    }
                }
                else -> {Text("-")}
            }
        }
        // OblacniText(text = text, oblacki = oblacki)
    }
}


const val lyrics = "\$D\n" +
        "   D                         h A G$\n" +
        "Gospod je moja luč in moja reš-i-tev.\n" +
        "   D                           h A  G$\n" +
        "Gospod trdnjava je v mojem življ-enju.\n" +
        "     e           G          e         G$\n" +
        "Koga bi se moral bati, pred kom trepetati.\n" +
        "   D                         h A G$\n" +
        "Gospod je moja luč in moja reš-i-tev.\n" +
        "\n" +
        "          D$\n" +
        "Upaj v Gospoda, bodi močan.\n" +
        "        e              G            D$\n" +
        "Tvoje srce opogumi naj se, upaj v Boga. (2x)\n" +
        "\n" +
        "   D                           h A G$\n" +
        "Gospoda prosim le, to skušam dos-e-či,\n" +
        "      D                               h A G$\n" +
        "da bi vse življenje dni bil v hiši Gospodovi.\n" +
        "     e           G          e         G$\n" +
        "Koga bi se moral bati, pred kom trepetati.\n" +
        "   D                           h A G$\n" +
        "Gospoda prosim le, ga skušam dos-e-či.\n" +
        "\n" +
        "          D$\n" +
        "Upaj v Gospoda, bodi močan.\n" +
        "        e              G            D$\n" +
        "Tvoje srce opogumi naj se, upaj v Boga. (2x)\n" +
        "\n" +
        "   D                     h A  G$\n" +
        "Gospod nauči me, uči me svojo pot.\n" +
        "   D                           h   A G$\n" +
        "Gospod, ti vodi me in varuj sovražni-kov.\n" +
        "     e           G          e         G$\n" +
        "Koga bi se moral bati, pred kom trepetati.\n" +
        "   D                     h A  G$\n" +
        "Gospod nauči me, uči me svojo pot.\n" +
        "\n" +
        "          D$\n" +
        "Upaj v Gospoda, bodi močan.\n" +
        "        e              G            D$\n" +
        "Tvoje srce opogumi naj se, upaj v Boga. (2x)\n"