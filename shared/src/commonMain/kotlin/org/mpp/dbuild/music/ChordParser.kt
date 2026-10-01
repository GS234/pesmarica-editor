package org.mpp.dbuild.music

// import java.io.File
import org.mpp.dbuild.utils.formatString
import org.mpp.dbuild.utils.readFile
import kotlin.sequences.forEach

const val chordRegexString: String = "\\(?([HCDFGhcdfg](is|es)?|[AaEe](is|s)?|[Bb])(\\()?(maj|sus|add)?(([24679]|11)(-3)?)?\\)?(\\/([HCDFGhcdfg](is|es)?|[AaEe](is|s)?|[Bb])?)?\\)?" //v3
//groups: 0-all, 1-symbol, 2-is, 3-es, 4-oklepaj, 5-maj, 6-7-3, 7-stevilka, 8--3, 9-overTone, 10-overSymbol, 11-is, 12-es
fun isLowerCase(c: Char) = c > 'H'
fun getChordTypeFromSymbol(s: String): ChordType {
    if(s.isNotEmpty()){
//        println("is symbol ${s[0]} a lowercase? :${isLowerCase(s[0])}")
        return when(isLowerCase(s[0])){
            true -> ChordType.MINOR
            false -> ChordType.MAJOR
        }
    }
    return ChordType.MAJOR // mogoce ni najbols tole (mogoce bi blo dobr met empty? (chordtype option): todo
}

fun getChordSymbolFromGroupValues(groupValues: List<String>): BasicChord {
    val symbol = groupValues[1]
    val isB = groupValues[1][0] == 'b' || groupValues[1][0] == 'B'

    val toneShape =
        when{
            isB -> ToneShape.FLAT // b
            groupValues[2].isEmpty() && groupValues[3].isEmpty() -> ToneShape.PLAIN // no sign
            groupValues[2].isEmpty() -> if(groupValues[3] == "s") ToneShape.FLAT else ToneShape.SHARP //chords: EeAa
            else -> if(groupValues[2] == "es") ToneShape.FLAT else ToneShape.SHARP // other chords (normal ones)
        }

    val toneSymbol = getToneSymbolFromSymbol(symbol)
    val minorMajor = getChordTypeFromSymbol(symbol)
    return BasicChord(Tone(toneSymbol, toneShape), minorMajor)
}

fun getToneSymbolFromSymbol(s: String): ToneSymbol {
    return when(s.lowercase()[0]){
        'c' -> ToneSymbol.C
        'd' -> ToneSymbol.D
        'e' -> ToneSymbol.E
        'f' -> ToneSymbol.F
        'g' -> ToneSymbol.G
        'a' -> ToneSymbol.A
        'h' -> ToneSymbol.H
        'b' -> ToneSymbol.H
        else -> {
            ToneSymbol.C}
    }
}

fun getChordFromGroupValues(groupValues: List<String>): Chord {
    val symbol = getChordSymbolFromGroupValues(groupValues)
    val encloseChord = groupValues[0][0] == '('
    val encloseForm = !groupValues[4].isEmpty()

//    println(groupValues)
    val chordForm = "${groupValues[5]}${groupValues[6]}"
//    get possible over:
    var overTone: Tone? = null
    if(groupValues[9].isNotEmpty()){
        overTone = getChordSymbolFromGroupValues(groupValues.takeLast(4)).tone
    }
    return Chord(
        symbol,
        overTone,
        chordForm,
        encloseForm = encloseForm,
        encloseChord = encloseChord
    )
}

//generate chord line
data class ChordLine(
    val annotatedChords: List<Pair<Int?, Chord>>,
    val format: String,
    val withinScale: Chord? = null
){
    private fun addToFormatString(nSemi: Int): String{
        val stringSubstitutions = Array<String>(annotatedChords.size * 2) { "" }
        annotatedChords.forEachIndexed { i, el ->
//            println(withinScale)
            val chord = if(nSemi == 0) el.second.toString() else el.second.transpose(nSemi, withinScale = withinScale).toString()
            val clip = (el.first ?: chord.length) - chord.length
//            zakaj +1? ker ce je 0, pol so akordi cist skupi, treba je dt vsaj en space vmes
            val offsetStr = when{
                clip < 0 -> "".padStart((-clip)+1, '<')
                clip == 0 && el.first != null -> "<" // implicitno +1
                else -> ""
            }
            stringSubstitutions[2*i] = chord
            stringSubstitutions[2*i+1] = offsetStr
        }
        return formatString(format, stringSubstitutions) // common expect: is platform-specific (jvm: String.format(...))
        // return String.format(format, *stringSubstitutions)
    }

    override fun toString(): String {
        return addToFormatString(0)
    }
    fun transpose(nSemi: Int): String{
        return addToFormatString(nSemi)
    }
}
//returns: (position, chord)
fun getChordsFromLine(line: String): ArrayList<Pair<Int, Chord>>{
    val chordRegex = chordRegexString.toRegex()
    val matchResults = chordRegex.findAll(line)
    val result = ArrayList<Pair<Int, Chord>>()
    matchResults.forEach {
            el ->
//        println(el.groupValues)
        val chord = getChordFromGroupValues(el.groupValues)
        result.add(el.range.first to chord)
    }
    return result
}

fun generateChordLine(chordLine: String, withinScale: Chord?=null): ChordLine{
    val r = getChordsFromLine(chordLine)
    val formatString = StringBuilder()
    val offsets = mutableListOf<Int?>()
    for (i in 0..<r.size){
        val p = r[i]
        val toNext = if(i+1 < r.size) r[i+1].first - p.first else null
        offsets.add(toNext)
        if(i == 0){
            formatString.append("".padStart(p.first))
        }
        val formatStringVal = if(toNext != null && toNext > 0) "%-${toNext}s%s" else "%s%s" // %0s throws error, but %-0s and %s are same
        formatString.append(formatStringVal)
    }
    val formatStr = formatString.toString()
    val mapped = r.mapIndexed { index, el -> offsets[index] to el.second }
    return ChordLine(mapped, formatStr, withinScale = withinScale)
}

//fun main() {
////    val chordLine = "D/Fis F/C Bmaj7 b/d d7 Es F$\n"
//    val chordLine = "(dis(maj7-3)/Fis) (des(maj7-3)/Fis) (Es(maj7-3)/B) (Eis(maj7-3)/B) (b(maj7-3)/Es) F/C Bmaj7 b/d d7 Es F$\n" +
//            "\n" +
//            "B/D b/d Cis fis Fis F/E Gmaj7 Gmaj9 G7 G6 D/Fis A4 Fis7/Ais E7/D H7/Dis cis7 c/Es g7/D D2/E dis7/Gis a6 Cmaj7 Asmaj7 C2/B Gsus4 (D)$\n" +
//            "E4-3 e7 G9 Hsus Hsus2 Hsus4 C(maj7) Gmaj9 Desmaj7 g11 (E(maj11))$"
//
//    val chordLine2 = generateChordLine(chordLine)
//    println(chordLine2)
//}


// inject chords into lines:
fun generateLinesWithChords(filename: String): String{
    val lineList = mutableListOf<String>()
    val newLineList = mutableListOf<String>()
    val chordRegex = chordRegexString.toRegex()
    readFile(filename).forEach { lineList.add(it) }
    // File(filename).useLines { lines -> lines.forEach { lineList.add(it) } } // read lines into lineList

    val delimiters = mutableListOf<Int>()
    val chords = mutableListOf<Chord>()
    var haveChords = false

    lineList.forEach {
        val strLen = it.length
        var newLine = ""
        if(it.isNotEmpty() && it[strLen-1] == '$'){ // have chord line
            if(haveChords){
//                previous line was chords, add line with chords one space apart
                chords.forEach { c -> newLine = "$newLine{$c} " } // mogoce stringbuilder bols?
                newLineList.add(newLine)
                chords.clear()
                delimiters.clear()
            }

// actual matching code:
            val matchResults = chordRegex.findAll(it)
            matchResults.forEach {
                    el ->
                delimiters.add(el.range.first)
                val chord = getChordFromGroupValues(el.groupValues)
                chords.add(chord)
            }
            haveChords = true
        }
        else{
            if(haveChords){
//                segment string
                val segments = mutableListOf<String>()
                for (i in 0..<delimiters.size){
                    val d = delimiters[i]

                    if(i == delimiters.size-1){
                        if(d < it.length) segments.add(it.substring(d))
                    }
                    else{
                        val dNext = delimiters[i+1]
                        val segment = when{
                            d < it.length && dNext < it.length -> it.substring(d..<dNext)
                            d < it.length && dNext >= it.length -> it.substring(d)
                            else -> " "
                        }
                        segments.add(segment)
                    }
                }
//                add chords
                for (i in 0..<chords.size){
                    newLine = if(i < segments.size){
                        "$newLine{${chords[i]}}${segments[i]}"
                    }
                    else "$newLine{${chords[i]}} "
                }
                newLineList.add(newLine)
                chords.clear()
                delimiters.clear()
                haveChords = false
            }
            else{
//                save current line as is (without chords)
                newLineList.add(it)
            }
        }
    }
    return newLineList.joinToString(separator = "\n")
}