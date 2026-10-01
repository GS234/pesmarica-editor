package org.mpp.dbuild.music

enum class ToneSymbol{ C, D, E, F, G, A, H }
enum class ToneShape{ PLAIN, SHARP, FLAT }

enum class ChordType{ MINOR, MAJOR }

fun semiOffRel2C(symbol: ToneSymbol, shape: ToneShape): Int{
    return semiOffRel2C(Tone(symbol, shape))
}
fun semiOffRel2C(tone: Tone): Int{
    val off = when(tone.which){
        ToneSymbol.C -> 0
        ToneSymbol.D -> 2
        ToneSymbol.E -> 4
        ToneSymbol.F -> 5
        ToneSymbol.G -> 7
        ToneSymbol.A -> 9
        ToneSymbol.H -> 11
    }
    val off2 = when(tone.shape){
        ToneShape.FLAT -> -1
        ToneShape.PLAIN -> 0
        ToneShape.SHARP -> 1
    }
    return ((off + off2) + 12) % 12
}

//lookup tables
fun getScaleRelToC(chord: Chord): BasicChord{
    val stopnja = semiOffRel2C(chord.chord.tone)
    if(chord.chord.minorMajor == ChordType.MAJOR){
//        major scales
        return BasicChord(
            when(stopnja){
                0 -> Tone(ToneSymbol.C, ToneShape.PLAIN)
                1 -> Tone(ToneSymbol.D, ToneShape.FLAT)
                2 -> Tone(ToneSymbol.D, ToneShape.PLAIN)
                3 -> Tone(ToneSymbol.E, ToneShape.FLAT)
                4 -> Tone(ToneSymbol.E, ToneShape.PLAIN)
                5 -> Tone(ToneSymbol.F, ToneShape.PLAIN)
                6 -> Tone(ToneSymbol.F, ToneShape.SHARP)
                7 -> Tone(ToneSymbol.G, ToneShape.PLAIN)
                8 -> Tone(ToneSymbol.A, ToneShape.FLAT)
                9 -> Tone(ToneSymbol.A, ToneShape.PLAIN)
                10 -> Tone(ToneSymbol.H, ToneShape.FLAT)
                11 -> Tone(ToneSymbol.H, ToneShape.PLAIN)
                else -> Tone(ToneSymbol.C, ToneShape.PLAIN)
            },
            ChordType.MAJOR
        )
    }
    else{
//        minor scales
        return BasicChord(
            when(stopnja){
                0 -> Tone(ToneSymbol.C, ToneShape.PLAIN)
                1 -> Tone(ToneSymbol.D, ToneShape.FLAT)
                2 -> Tone(ToneSymbol.D, ToneShape.PLAIN)
                3 -> Tone(ToneSymbol.E, ToneShape.FLAT)
                4 -> Tone(ToneSymbol.E, ToneShape.PLAIN)
                5 -> Tone(ToneSymbol.F, ToneShape.PLAIN)
                6 -> Tone(ToneSymbol.F, ToneShape.SHARP)
                7 -> Tone(ToneSymbol.G, ToneShape.PLAIN)
                8 -> Tone(ToneSymbol.G, ToneShape.SHARP)
                9 -> Tone(ToneSymbol.A, ToneShape.PLAIN)
                10 -> Tone(ToneSymbol.H, ToneShape.FLAT)
                11 -> Tone(ToneSymbol.H, ToneShape.PLAIN)
                else -> Tone(ToneSymbol.A, ToneShape.PLAIN)
            },
            ChordType.MINOR
        )
    }
}

fun getBaseSymbol(which: ToneSymbol, shape: ToneShape, minorMajor: ChordType): String{
    val symbol = Tone(which, shape).toString()
    return when(minorMajor){
        ChordType.MAJOR -> symbol
        ChordType.MINOR -> symbol.lowercase()
    }
}

fun sharpen(tone: Tone): Tone = (tone - 1) + 1
fun flatten(tone: Tone): Tone = (tone + 1) - 1

fun flatSharpSwitch(tone: Tone): Tone{
    return when(tone.shape){
        ToneShape.PLAIN -> tone
        ToneShape.SHARP -> flatten(tone)
        ToneShape.FLAT -> sharpen(tone)
    }
}

fun noSigns(scale: BasicChord): Int{
    val nSigns = when(scale){
        BasicChord(Tone(ToneSymbol.C, ToneShape.PLAIN), ChordType.MAJOR) -> 0
        BasicChord(Tone(ToneSymbol.G, ToneShape.PLAIN), ChordType.MAJOR) -> 1
        BasicChord(Tone(ToneSymbol.D, ToneShape.PLAIN), ChordType.MAJOR) -> 2
        BasicChord(Tone(ToneSymbol.A, ToneShape.PLAIN), ChordType.MAJOR) -> 3
        BasicChord(Tone(ToneSymbol.E, ToneShape.PLAIN), ChordType.MAJOR) -> 4
        BasicChord(Tone(ToneSymbol.H, ToneShape.PLAIN), ChordType.MAJOR) -> 5
        BasicChord(Tone(ToneSymbol.F, ToneShape.SHARP), ChordType.MAJOR) -> 6
        BasicChord(Tone(ToneSymbol.C, ToneShape.SHARP), ChordType.MAJOR) -> 7
        BasicChord(Tone(ToneSymbol.F, ToneShape.PLAIN), ChordType.MAJOR) -> -1
        BasicChord(Tone(ToneSymbol.H, ToneShape.FLAT), ChordType.MAJOR) -> -2
        BasicChord(Tone(ToneSymbol.E, ToneShape.FLAT), ChordType.MAJOR) -> -3
        BasicChord(Tone(ToneSymbol.A, ToneShape.FLAT), ChordType.MAJOR) -> -4
        BasicChord(Tone(ToneSymbol.D, ToneShape.FLAT), ChordType.MAJOR) -> -5
        BasicChord(Tone(ToneSymbol.G, ToneShape.FLAT), ChordType.MAJOR) -> -6

        BasicChord(Tone(ToneSymbol.A, ToneShape.PLAIN), ChordType.MINOR) -> 0
        BasicChord(Tone(ToneSymbol.E, ToneShape.PLAIN), ChordType.MINOR) -> 1
        BasicChord(Tone(ToneSymbol.H, ToneShape.PLAIN), ChordType.MINOR) -> 2
        BasicChord(Tone(ToneSymbol.F, ToneShape.SHARP), ChordType.MINOR) -> 3
        BasicChord(Tone(ToneSymbol.C, ToneShape.SHARP), ChordType.MINOR) -> 4
        BasicChord(Tone(ToneSymbol.G, ToneShape.SHARP), ChordType.MINOR) -> 5
        BasicChord(Tone(ToneSymbol.D, ToneShape.SHARP), ChordType.MINOR) -> 6
        BasicChord(Tone(ToneSymbol.A, ToneShape.SHARP), ChordType.MINOR) -> 7
        BasicChord(Tone(ToneSymbol.D, ToneShape.PLAIN), ChordType.MINOR) -> -1
        BasicChord(Tone(ToneSymbol.G, ToneShape.PLAIN), ChordType.MINOR) -> -2
        BasicChord(Tone(ToneSymbol.C, ToneShape.PLAIN), ChordType.MINOR) -> -3
        BasicChord(Tone(ToneSymbol.F, ToneShape.PLAIN), ChordType.MINOR) -> -4
        BasicChord(Tone(ToneSymbol.H, ToneShape.FLAT), ChordType.MINOR) -> -5
        BasicChord(Tone(ToneSymbol.E, ToneShape.FLAT), ChordType.MINOR) -> -6
        else -> 0
    }
    return nSigns
}

//helper method to transpose, maybe add it in transpose (is not perfect)
fun applyScaleConstraints(tone: Tone, scale: BasicChord?): Tone{
//    scale constraints:
//    flatten sharps if in flat scale:
    var newTone = tone
    if(scale != null) {
//        val isFlatScale = (scale.tone.shape == ToneShape.FLAT || scale.tone == Tone(ToneSymbol.F, ToneShape.PLAIN))
        val isFlatScale = noSigns(scale) < 0
//        if (isFlatScale && tone.shape == ToneShape.SHARP) newTone = flatten(tone)
        if (isFlatScale) newTone = flatten(tone)
        else {
            if (tone.shape == ToneShape.FLAT) newTone = sharpen(tone)
        }
//    check if scale has 6 signs -> in this case, change F to Eis
        if (scale == BasicChord(Tone(ToneSymbol.F, ToneShape.SHARP), ChordType.MAJOR)) {
            if (tone == Tone(ToneSymbol.F, ToneShape.PLAIN)) newTone = Tone(ToneSymbol.E, ToneShape.SHARP)
        }
        if (scale == BasicChord(Tone(ToneSymbol.E, ToneShape.FLAT), ChordType.MINOR)) {
            if (tone == Tone(ToneSymbol.H, ToneShape.PLAIN)) newTone = Tone(ToneSymbol.C, ToneShape.FLAT)
        }
    }
    return newTone
}

// still has some problems (fism, esm (6 signs)) probably best to rewrite from scratch in the future
//fun transpose(tone: Tone, nSemi: Int, scale: BasicChord? = null): Tone{
fun transpose(tone: Tone, nSemi: Int): Tone{
    val m1 =  4
    val m2 =  11
    val nSemi2 = if (nSemi < 0) 12 - ((-nSemi) % 12) else nSemi%12
    var total = nSemi2

    val currentLevel = semiOffRel2C(tone)
    val newLevel = (currentLevel+nSemi2)
    val intRange = currentLevel..<newLevel

//    ce prestop meje (4, 11, dodaj se +1)
    if(m1 in intRange) total += 1
    if(m2 in intRange) total += 1
    if((m1+12) in intRange) total += 1
    if((m2+12) in intRange) total += 1

    val newTone = tone+total
    return newTone
}

data class Tone(
    val which: ToneSymbol,
    val shape: ToneShape
){
    override fun toString(): String {
        return when(which to shape){
            ToneSymbol.C to ToneShape.PLAIN -> "C"
            ToneSymbol.C to ToneShape.SHARP -> "Cis"
            ToneSymbol.C to ToneShape.FLAT -> "Ces"

            ToneSymbol.D to ToneShape.PLAIN -> "D"
            ToneSymbol.D to ToneShape.SHARP -> "Dis"
            ToneSymbol.D to ToneShape.FLAT -> "Des"

            ToneSymbol.E to ToneShape.PLAIN -> "E"
            ToneSymbol.E to ToneShape.SHARP -> "Eis"
            ToneSymbol.E to ToneShape.FLAT -> "Es"

            ToneSymbol.F to ToneShape.PLAIN -> "F"
            ToneSymbol.F to ToneShape.SHARP -> "Fis"
            ToneSymbol.F to ToneShape.FLAT -> "Fes"

            ToneSymbol.G to ToneShape.PLAIN -> "G"
            ToneSymbol.G to ToneShape.SHARP -> "Gis"
            ToneSymbol.G to ToneShape.FLAT -> "Ges"

            ToneSymbol.A to ToneShape.PLAIN -> "A"
            ToneSymbol.A to ToneShape.SHARP -> "Ais"
            ToneSymbol.A to ToneShape.FLAT -> "As"

            ToneSymbol.H to ToneShape.PLAIN -> "H"
            ToneSymbol.H to ToneShape.SHARP -> "His"
            ToneSymbol.H to ToneShape.FLAT -> "B"
            else -> {"?"}
        }
    }
    operator fun plus(increment: Int): Tone{
        if(increment < 0) return (this - (-increment))
        var wholeInc = increment/2
        val semiInc = increment%2
        var newShape = this.shape
        if(semiInc > 0){
            newShape = when(this.shape){
                ToneShape.FLAT -> ToneShape.PLAIN
                ToneShape.PLAIN -> ToneShape.SHARP
                ToneShape.SHARP -> {
                    wholeInc += 1
                    ToneShape.PLAIN
                }
            }
        }
        val newSymbol = ToneSymbol.entries[ (this.which.ordinal + wholeInc)%(ToneSymbol.entries.size)]
        return Tone(newSymbol, newShape)
    }
    operator fun minus(decrement: Int): Tone{
        if(decrement < 0) return this + (-decrement)
        var wholeDec = decrement/2
        val semiDec = decrement%2
        var newShape = this.shape
        if(semiDec > 0){
            newShape = when(this.shape){
                ToneShape.FLAT -> {
                    wholeDec += 1
                    ToneShape.PLAIN
                }
                ToneShape.PLAIN -> ToneShape.FLAT
                ToneShape.SHARP -> ToneShape.PLAIN
            }
        }
        val newSymbol = ToneSymbol.entries[ (this.which.ordinal + (ToneSymbol.entries.size - wholeDec))%(ToneSymbol.entries.size)]
        return Tone(newSymbol, newShape)
    }
}

data class BasicChord(
    val tone: Tone,
    val minorMajor: ChordType,
){
    override fun toString(): String {
        return getBaseSymbol(this.tone.which, this.tone.shape, this.minorMajor)
    }
    operator fun plus(increment: Int): BasicChord{
        return BasicChord((this.tone + increment), this.minorMajor)
    }
    operator fun minus(decrement: Int): BasicChord{
        return BasicChord((this.tone - decrement), this.minorMajor)
    }
}

data class Chord(
    val chord: BasicChord,
    val over: Tone? = null,
    val form: String = "",
    val encloseForm: Boolean = false,
    val encloseChord: Boolean = false,
){
    override fun toString(): String {
        val baseSymbol = getBaseSymbol(chord.tone.which, chord.tone.shape, chord.minorMajor)
        val form1 = if(encloseForm) "($form)" else form // enclose if needed
        val chordStr = if(over != null)
            "${baseSymbol}$form1\\$over"
        else
            "${baseSymbol}$form1"
        return if(encloseChord) "($chordStr)" else chordStr
    }

    fun transpose(nSemi: Int, mode: ToneShape = ToneShape.PLAIN, withinScale: Chord?=null):Chord{
        val newScale = if(withinScale != null) getScaleRelToC(withinScale.transpose(nSemi)) else null
//        println("new scale: $newScale")
        val newChordTone = applyScaleConstraints(transpose(chord.tone, nSemi), newScale)
        if(over != null) {
            val newOverTone = applyScaleConstraints(transpose(over, nSemi), newScale)
            return Chord(
                BasicChord(newChordTone, chord.minorMajor),
                newOverTone,
                form,
                encloseForm = encloseForm,
                encloseChord = encloseChord
            )
        }
        else{
            return Chord(
                BasicChord(newChordTone, chord.minorMajor),
                null,
                form,
                encloseForm = encloseForm,
                encloseChord = encloseChord
            )
        }
    }
}
