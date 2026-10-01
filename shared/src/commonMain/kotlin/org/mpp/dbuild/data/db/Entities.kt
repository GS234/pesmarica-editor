package org.mpp.dbuild.data.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        // this is required: each added song tries to update lyrics,
        // if the song (with title) exists, then update lyrics, if it does not exist, then insert
        // (this triggers conflict on insert, which is then ignored by OnConflictStrategy.IGNORE)
        Index(value = ["title"], unique = true)
    ]
)
data class SongEntity(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "lyrics") val lyrics: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("song_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["song_id"], unique = true)
    ]
)
data class SongInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name="song_id") val songId: Long,
    @ColumnInfo(name = "authors") val authors: String,
    @ColumnInfo(name="categories") val category: String,
    @ColumnInfo(name = "tonality") val tonality: String,
)


@Entity(
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Playlist(
    @ColumnInfo(name = "name") val name: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

//many to many
@Entity(
    primaryKeys = ["song_id", "playlist_id"],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("song_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Playlist::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("playlist_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class PlaylistSong(
    @ColumnInfo(name = "song_id") val songId: Long,
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
)

