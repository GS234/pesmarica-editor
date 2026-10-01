package org.mpp.dbuild.music

import org.mpp.dbuild.data.sources.Song

fun text2Songs(text: String): List<Song>{
    val lines = text.split('\n')
    val songs = mutableListOf<Song>()
    var i = 0
    var songCount = 0 // song count
    while(i < lines.size && lines[i] != "=") i++ // skip possible leading blanks

    var title = ""
    var originalTitle = ""
    var authors = ""
    var lyrics = mutableListOf<String>()
    while(i < lines.size){
        if(lines[i] == "="){
            i++ //increase i (skip =, we are now on title (hopefully))
            while(i < lines.size && lines[i] == "") i++ // skip possible leading blanks
            if(i >= lines.size) break // if we go over list, stop immediately (invalid song)
            if(songCount == 0){ //start new song, do not create yet
                title = lines[i]
            }
            else{ //create previous song, start new
                val song = createSong(songCount.toLong(), title, originalTitle, authors, lyrics.joinToString("\n"))
                if(song != null) songs.add(song)

//                start new song
                title = lines[i]
                originalTitle = ""
                authors = ""
                lyrics = mutableListOf()
            }
            songCount++
        }
        else{
            val line = lines[i]
            when{
                line.startsWith("%t") -> originalTitle = line.substringAfter("%t").trim()
                line.startsWith("%a") -> authors = line.substringAfter("%a").trim()
                else -> lyrics.add(line)
            }
        }
        i++
    }
    val finalSong = createSong(songCount.toLong(), title, originalTitle, authors, lyrics.joinToString("\n"))
    if(finalSong != null) songs.add(finalSong)
    return songs.toList()
}

fun createSong(id: Long, title: String, originalTitle: String, authors: String, lyrics: String): Song?{
    return if(lyrics.isNotEmpty() && title != ""){
        Song(
            id = id,
            title = title,
            originalTitle = originalTitle,
            authors = authors,
            lyrics = lyrics
        )
    }
    else null
}

// parser for songs:
fun songs2Text(songs: List<Song>): String{
    val resultBuilder = StringBuilder("")
    songs.forEach {
        val header = createHeader(
            title = it.title,
            originalTitle = it.originalTitle,
            authors = it.authors
        )
        resultBuilder.append(header)
        resultBuilder.append(it.lyrics.trim())
        resultBuilder.append('\n')
        resultBuilder.append('\n')
    }
    return resultBuilder.toString()
}

fun createHeader(title: String, originalTitle: String = "", authors: String = ""): String{
    return "=\n$title\n" +
            (if(originalTitle.isNotEmpty()) "%t $originalTitle\n" else "") +
            (if(authors.isNotEmpty()) "%a $authors\n" else "")
}