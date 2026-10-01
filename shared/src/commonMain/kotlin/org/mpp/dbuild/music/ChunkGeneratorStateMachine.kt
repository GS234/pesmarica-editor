package org.mpp.dbuild.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mpp.dbuild.NormalizedChordLyrics

//classi za stanje, input
//sealed: da prevajalnik zagotovi, da smo obdelal vse
sealed class State{
    object S0: State()
    object S1: State()
    object S2: State()
    object S3: State()
    object S4: State()
    object S5: State()
}

sealed class Input{
    object ChordLine: Input()
    object TextLine: Input()
    object EmptyLine: Input()
}

sealed class LyricsChunk{
    data class LyricsOnlyChunk(val lyrics: List<String>): LyricsChunk()
    data class ChordsOnlyChunk(val chords: List<ChordLine>): LyricsChunk()
    data class LyricsChordsChunk(
        val lyrics: List<String>,
        val chords: List<ChordLine>,
        val normalizedCL: NormalizedChordLyrics? = null // possible optimization
    ): LyricsChunk()
}

object ChunkGeneratorStateMachine{
    val textLines = mutableListOf<String>()  // buffer for text
    //    val chordLines = mutableListOf<String>() // buffer for chords
    val chordLines = mutableListOf<ChordLine>() // buffer for chords
    //    val textChunks = mutableListOf<LyricsChunk>() // chunks: this is returned
    val textChunks = mutableListOf<LyricsChunk>() // chunks: this is returned

    var inputLine: String = "" // line that is read (is set inside onInput)
    var currentState: State = State.S0
        private set

    var scale: Chord? = null // scale - annotation: $

    fun onInput(line: String){
//        check for possible scale change:
        if((line.firstOrNull() ?: ' ') == '$'){
            val chords = getChordsFromLine(line)
            if(chords.isNotEmpty()){
                scale = chords[0].second
//                println("[scale change] scale is now $scale")
            }
            return // skip this line, because it is no longer needed
        }

        val input = when (line.lastOrNull()){
            '$' -> Input.ChordLine
            null -> Input.EmptyLine
            else -> Input.TextLine
        }
        inputLine = if (input == Input.ChordLine) line.substring(0,line.length-1) else line

        currentState = when(currentState){
            is State.S0 -> handleS0(input)
            is State.S1 -> handleS1(input)
            is State.S2 -> handleS2(input)
            is State.S3 -> handleS3(input)
            is State.S4 -> handleS4(input)
            is State.S5 -> handleS5(input)
        }
    }

    private fun handleS0(input: Input): State = when(input){
        Input.EmptyLine -> {State.S0}
        Input.ChordLine -> {
            chordLines.add(generateChordLine(inputLine, withinScale = scale))
            State.S2
        }
        Input.TextLine -> {
            textLines.add(inputLine)
            State.S1
        }
    }
    private fun handleS1(input: Input): State = when(input){
        Input.EmptyLine -> {
            addLyricsChunk()
            State.S0
        }
        Input.ChordLine -> {
            addLyricsChunk()
            chordLines.add(generateChordLine(inputLine, withinScale = scale))
            State.S2
        }
        Input.TextLine -> {
            textLines.add(inputLine)
            State.S1
        }
    }
    private fun handleS2(input: Input): State = when(input){
        Input.EmptyLine -> {
            addChordChunk()
            State.S0
        }
        Input.ChordLine -> {
            chordLines.add(generateChordLine(inputLine, withinScale = scale))
            State.S5
        }
        Input.TextLine -> {
            textLines.add(inputLine)
            State.S3
        }
    }
    private fun handleS3(input: Input): State = when(input){
        Input.EmptyLine -> {
            addChordLyricsChunk()
            State.S0
        }
        Input.ChordLine -> {
            chordLines.add(generateChordLine(inputLine, withinScale = scale))
            State.S4
        }
        Input.TextLine -> {
            addChordLyricsChunk()
            textLines.add(inputLine)
            State.S1
        }
    }
    private fun handleS4(input: Input): State = when(input){
        Input.EmptyLine -> {
            addChordLyricsChunk()
            State.S0
        }
        Input.ChordLine -> {
            addChordLyricsChunk()
            chordLines.add(generateChordLine(inputLine, withinScale = scale))
            State.S2
        }
        Input.TextLine -> {
            textLines.add(inputLine)
            State.S3
        }
    }
    private fun handleS5(input: Input): State = when(input){
        Input.EmptyLine -> {
            addChordChunk()
            clearBuffers()
            State.S0
        }
        Input.ChordLine -> {
            chordLines.add(generateChordLine(inputLine, withinScale = scale))
            State.S5
        }
        Input.TextLine -> {

            val previousChordLine = chordLines.removeAt(chordLines.size-1)
            addChordChunk()
            chordLines.add(previousChordLine)
            textLines.add(inputLine)
            State.S3
        }
    }

    //    pomozne funkcije
    private fun addChordChunk() {
        // textChunks.add(LyricsChunk2(null, chordLines.toList()))
        textChunks.add(LyricsChunk.ChordsOnlyChunk( chordLines.toList()))
        clearBuffers()
    }
    private fun addLyricsChunk() {
        // textChunks.add(LyricsChunk2(textLines.toList(), null))
        textChunks.add(LyricsChunk.LyricsOnlyChunk(textLines.toList()))
        clearBuffers()
    }
    private fun addChordLyricsChunk(){
        textChunks.add(
            // LyricsChunk2(
            //     textLines.toList(),
            //     chordLines.toList()
            // )
            LyricsChunk.LyricsChordsChunk(
                textLines.toList(),
                chordLines.toList()
            )
        )
        clearBuffers()
    }
    fun getChunks(): List<LyricsChunk> = textChunks.toList()

    private fun clearBuffers(){
        textLines.clear()
        chordLines.clear()
    }
    fun resetMachine(){
        clearBuffers()
        textChunks.clear()
        inputLine = ""
        currentState = State.S0
        scale = null
    }
}

//this is not tested against all cases! (might be bugged)
// this function might be slow - heavy computation
suspend fun chunkGenerator2(lyricsLines: String): List<LyricsChunk> = withContext(Dispatchers.Default){
    val lines = "$lyricsLines\n".split("\n")
//    init generator
    ChunkGeneratorStateMachine.resetMachine()
    lines.forEach { string ->
        ChunkGeneratorStateMachine.onInput(string)
    }
    ChunkGeneratorStateMachine.getChunks()
}