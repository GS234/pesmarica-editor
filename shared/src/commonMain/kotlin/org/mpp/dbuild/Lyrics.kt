package org.mpp.dbuild

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mpp.dbuild.music.Chord
import org.mpp.dbuild.music.ChordLine
import org.mpp.dbuild.music.LyricsChunk
import org.mpp.dbuild.music.PositionedChord
import org.mpp.dbuild.music.chunkGenerator2
import org.mpp.dbuild.ui.theme.MonoFF
import org.mpp.dbuild.ui.theme.getMonoFontFamily
import kotlin.collections.component1
import kotlin.collections.component2

object TextParams{
    val singleLineHeight: TextUnit = 20.sp
    val lineHeight: TextUnit = 40.sp
    val padding: Dp = 18.dp
    val spacing: TextUnit = (-1).sp
    val fontSize: TextUnit = 15.sp
    val monoFont: FontFamily @Composable get() = getMonoFontFamily(MonoFF.RobotoMono)
}

//notes to self:
// namest space-ov: _
// namest /: \\

//style text:
fun styleText(
    text: String,
    styler: (Char) -> SpanStyle,
): AnnotatedString{
    return buildAnnotatedString {
        text.forEach { ch ->
            withStyle(
                styler(ch)
            ){
                append(ch)
            }
        }
    }
}

//stylers used in styleText for chord lines and lyrics lines
fun chordStyler(mainColor: Color): (Char) -> SpanStyle =
    { ch -> SpanStyle(
        color =
            if(ch == '_') mainColor.copy(0f)
            else mainColor,
        fontWeight = FontWeight.Bold,
    )}
fun lyricsStyler(mainColor: Color): (Char) -> SpanStyle =
    { ch -> SpanStyle(
        color =
            when(ch){
                '_' -> mainColor.copy(alpha = 0f)
                '-' -> mainColor.copy(alpha = 0.3f)
                else -> mainColor
            },
    )}
//-----------

fun concatChordLines(chordLines: List<ChordLine>, transposeNsemi: Int): String{
    val l1Sb = StringBuilder()
    chordLines.forEach {
        l1Sb.append(it.transpose(transposeNsemi))
        l1Sb.append('\n')
    }
    return l1Sb.toString()
}

//TODO: move to chunkGenerator

// heavy computation
//todo: maybe convert do dfa (like chunkGenerator)?
data class NormalizedChordLyrics(
    val normalizedChordLine: String,
    val normalizedLyricsLine: String,
)
suspend fun normalizeChordLyricsLine(chordLine:String, lyricLine: String): NormalizedChordLyrics =
    withContext(Dispatchers.Default){
    //    merge lines
        val newChordLine = StringBuilder()
        val newLyricLine = StringBuilder()

        var lyricLineIndex = 0
        var chordLineIndex = 0
        while(lyricLineIndex < lyricLine.length || chordLineIndex < chordLine.length){
            if(lyricLineIndex < lyricLine.length && chordLineIndex < chordLine.length){
                /* normal mode: */
                val lyricChar = lyricLine[lyricLineIndex]
                val chordChar = chordLine[chordLineIndex]
                when{
                    lyricChar != ' ' && chordChar != ' ' -> {
                        if(chordChar == '<'){
    //                        skip all <, insert one ' ', for every < in chordLine, add '-' to lyric line
                            var j = 0
                            do{
                                newLyricLine[newLyricLine.lastIndex-j] = '-'
                                lyricLineIndex -= 1
                                chordLineIndex += 1
                                j += 1
                            }
                            while(chordLineIndex < chordLine.length && chordLine[chordLineIndex] == '<')
                            chordLineIndex -= 1
                            newLyricLine.append(lyricLine[lyricLineIndex])
                            newChordLine.append(if(lyricLine[lyricLineIndex] == ' ') ' ' else '_') // if previous char was ' ', no need for '_'
                        }
                        else{
                            newChordLine.append(chordChar)
                            newLyricLine.append(lyricChar)
                        }
                    }
                    lyricChar != ' ' && chordChar == ' ' -> {
                        newLyricLine.append(lyricChar)
                        newChordLine.append('_')
                    }
    //                tole mogoce se problematicno
                    lyricChar == ' ' && chordChar != ' ' -> {
    //                    this is where fun begins
                        if(chordChar == '<'){
    //                        skip all <, insert one ' ', for every < in chordLine, add '-' to lyric line
                            var j = 0
                            do{
                                newLyricLine[newLyricLine.lastIndex-j] = '-'
                                lyricLineIndex -= 1
                                chordLineIndex += 1
                                j += 1
                            }
                            while(chordLineIndex < chordLine.length && chordLine[chordLineIndex] == '<')
                            chordLineIndex -= 1
                            newLyricLine.append(lyricLine[lyricLineIndex])
                            newChordLine.append(if(lyricLine[lyricLineIndex] == ' ') ' ' else ' ') // if previous char was ' ', no need for '_'
                        }
                        else{
                            newChordLine.append(chordChar)
                            newLyricLine.append('_')
                        }
                    }
                    lyricChar == ' ' && chordChar == ' ' -> {
                        newLyricLine.append(lyricChar)
                        newChordLine.append(chordChar)
                    }
                }
                lyricLineIndex += 1
                chordLineIndex += 1
            }
            else if(lyricLineIndex >= lyricLine.length){
                val chordChar = chordLine[chordLineIndex]
                when{
                    chordChar == '<' -> {
                        while(chordLineIndex < chordLine.length && chordLine[chordLineIndex] == '<') chordLineIndex += 1
                        chordLineIndex -= 1
                        newChordLine.append(' ')
                    }
                    else -> newChordLine.append(chordChar)
                }
                chordLineIndex += 1
            }
            else{
                val lyricChar = lyricLine[lyricLineIndex]
                newLyricLine.append(lyricChar)
                newChordLine.append(if(lyricChar == ' ') ' ' else '_')
                lyricLineIndex += 1
            }
        }
        NormalizedChordLyrics(
            newChordLine.toString(),
            newLyricLine.toString()

        )
        // newChordLine.toString() to newLyricLine.toString() // return
    }


@Composable
fun DoubleLine(
    chordLines: List<ChordLine>,
    textLines: List<String>,
    showL1: Boolean = true,
    modifier: Modifier = Modifier,
    transposeNsemi: Int,
    scale: Float = 1f
){
    if(showL1){
        Column(modifier = modifier){
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
                    var lastOffset: Offset? = null
                    if(it.position < textLine.length) {
                        val rect = layoutResult.getBoundingBox(it.position)
                        // val zacetekX = (rect.left + rect.right) / 2f
                        val zacetekX = rect.left
                        val vrhY = rect.top - yOff
                        lastOffset = Offset(zacetekX, vrhY)
                        newLocations[it] = lastOffset
                    }
                    else{
                        newLocations[it] = lastOffset ?: Offset(0f, 0f)
                    }
                }
                locations = newLocations
            },
        )

        locations.forEach { (positionedChord, offset) ->
            val bubblePadding = 2.dp
            val xDp = with(density) { offset.x.toDp()-bubblePadding }
            val yDp = with(density) { offset.y.toDp() }
            Box(
                modifier = Modifier
                    .offset(
                        x = xDp,
                        y = yDp
                    )

            ) {
                ChordBubble(
                    chord = positionedChord.chord,
                    padding = bubblePadding
                )
            }
        }
    }
}

@Composable
fun ChordBubble(
    chord: Chord,
    padding: Dp = 2.dp
){
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = padding)
    ){
        Text(chord.toString(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
    }
}




// todo: optimize
@Composable
fun OldDoubleLine(
    chordLines: List<ChordLine>,
    textLines: List<String>,
    showL1: Boolean = true,
    transposeNsemi: Int,
    modifier: Modifier = Modifier,
    scale: Float = 1f
){
    if(showL1){
        var l1 by remember {mutableStateOf("")}
        var l2 by remember {mutableStateOf("")}

        LaunchedEffect(transposeNsemi) {
            val chordLinesFinal = mutableListOf<String>()
            val textLinesFinal = mutableListOf<String>()
            for (i in 0..<chordLines.size){
                if(i < textLines.size){
                    val normalizedChordLyricsLines = normalizeChordLyricsLine(chordLines[i].getOldChordLine().transpose(transposeNsemi), textLines[i])
                    chordLinesFinal.add(normalizedChordLyricsLines.normalizedChordLine)
                    textLinesFinal.add(normalizedChordLyricsLines.normalizedLyricsLine)
                }
            }
            l1 = chordLinesFinal.joinToString("\n")
            l2 = textLinesFinal.joinToString("\n")
        }

        // val l1 = chordLinesFinal.joinToString("\n")
        // val l2 = textLinesFinal.joinToString("\n")


        Box(modifier = modifier){
    //        akordi
            Text(
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, TextParams.padding*scale),
                text = styleText(
                    l1,
                    chordStyler(MaterialTheme.colorScheme.primary)
                ),
                overflow = TextOverflow.Visible,
                letterSpacing = TextParams.spacing,
                lineHeight = TextParams.lineHeight*scale,
                fontSize = TextParams.fontSize*scale,
                fontFamily = TextParams.monoFont

            )
    //        besedilo
            Text(
                modifier = Modifier.padding(0.dp, TextParams.padding*scale, 0.dp, 0.dp),
                text = styleText(
                    l2,
                    lyricsStyler(MaterialTheme.colorScheme.onBackground)
                ),
                overflow = TextOverflow.Visible,
                letterSpacing = TextParams.spacing,
                lineHeight = TextParams.lineHeight*scale,
                fontSize = TextParams.fontSize*scale,
                fontFamily = TextParams.monoFont
            )
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
fun BasicLyrics(
    text: String,
    modifier: Modifier = Modifier,
    scale: Float = 1f
){
    Box(modifier = modifier){
        Text(
            text = text.replace("_",""),
            overflow = TextOverflow.Visible,
            letterSpacing = TextParams.spacing,
            lineHeight = TextParams.singleLineHeight*scale,
            fontSize = TextParams.fontSize*scale,
            fontFamily = TextParams.monoFont
        )
    }
}
@Composable
fun ChordText(
    chords: List<ChordLine>,
    modifier: Modifier = Modifier,
    transposeNsemi: Int,
    showLine: Boolean = true,
    scale: Float = 1f
){
    if(showLine){
        Column {
            chords.forEach { chordLine ->
                val transposedChordLine = chordLine.transpose(transposeNsemi)
                Row {
                    transposedChordLine.positionedChords.forEach { chord ->
                        ChordBubble(chord.chord)
                        Spacer(modifier = Modifier.size(2.dp))
                    }
                }
            }
        }
        // val text = styleText(
        //     concatChordLines(chords, transposeNsemi).replace("<+".toRegex(), " "),
        //     chordStyler(MaterialTheme.colorScheme.primary)
        // )
        // Box(modifier = modifier){
        //     Text(
        //         text = text,
        //         overflow = TextOverflow.Visible,
        //         letterSpacing = TextParams.spacing,
        //         lineHeight = TextParams.singleLineHeight*scale,
        //         fontSize = TextParams.fontSize*scale,
        //         fontFamily = TextParams.monoFont
        //     )
        // }
    }
}

@Composable
fun NewLyrics2(
    chunks: List<LyricsChunk> = listOf(),
    info: String = "",
    displayChords: Boolean = true,
    transposeNsemi: Int = 0,
    scale: Float = 1f
){
    val chunkSpacing = 10.dp
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 0.dp)
    ) {
        item {
            Text(
                modifier = Modifier.alpha(0.6f),
                text = info,
            )
            Spacer(modifier = Modifier.height(chunkSpacing))
        }
        itemsIndexed(chunks){index, chunk ->
            when(chunk){
                is LyricsChunk.LyricsChordsChunk -> DoubleLine(
                    chunk.chords,
                    chunk.lyrics,
                    showL1 = displayChords,
                    transposeNsemi = transposeNsemi,
                    modifier = Modifier.padding(0.dp,0.dp,0.dp,10.dp),
                    scale=scale
                )
                is LyricsChunk.LyricsOnlyChunk -> BasicLyrics(
                    text = (chunk.lyrics).joinToString("\n"),
                    modifier = Modifier.padding(0.dp,0.dp,0.dp,10.dp),
                    scale=scale
                )
                is LyricsChunk.ChordsOnlyChunk -> ChordText(
                    chords = chunk.chords,
                    transposeNsemi = transposeNsemi,
                    showLine = displayChords,
                    modifier = Modifier.padding(0.dp,0.dp,0.dp,10.dp),
                    scale=scale
                )
            }
            Spacer(modifier = Modifier.height(chunkSpacing))
        }
    }
}



@Composable
fun NewLyrics(
    lyrics: String = "",
    info: String = "",
    displayChords: Boolean = true,
    transposeNsemi: Int = 0,
    scale: Float = 1f
){
//    todo: optimize
//    var scaleFactor by remember { mutableFloatStateOf(1f)}
    var chunks by remember {
        mutableStateOf(listOf<LyricsChunk>())
    }
    val chunkSpacing = 10.dp
    LaunchedEffect(lyrics){
        chunks = chunkGenerator2(lyrics) // ubistvu bi loh ze kle transponiru
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 0.dp)
    ) {
        item {
            Text(
                modifier = Modifier.alpha(0.6f),
                text = info,
            )
            Spacer(modifier = Modifier.height(chunkSpacing))
        }
        itemsIndexed(chunks){index, chunk ->
            when(chunk){
                is LyricsChunk.LyricsChordsChunk -> DoubleLine(
                    chunk.chords,
                    chunk.lyrics,
                    showL1 = displayChords,
                    transposeNsemi = transposeNsemi,
                    modifier = Modifier.padding(0.dp,0.dp,0.dp,10.dp),
                    scale=scale
                )
                is LyricsChunk.LyricsOnlyChunk -> BasicLyrics(
                    text = (chunk.lyrics).joinToString("\n"),
                    modifier = Modifier.padding(0.dp,0.dp,0.dp,10.dp),
                    scale=scale
                )
                is LyricsChunk.ChordsOnlyChunk -> ChordText(
                    chords = chunk.chords,
                    transposeNsemi = transposeNsemi,
                    showLine = displayChords,
                    modifier = Modifier.padding(0.dp,0.dp,0.dp,10.dp),
                    scale=scale
                )
            }
            Spacer(modifier = Modifier.height(chunkSpacing))
        }
    }
}






