package org.mpp.dbuild.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(entities = [SongEntity::class, SongInfo::class, Playlist::class, PlaylistSong::class], version = 1, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}

// The Room compiler generates the 'actual' implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}


// fun getRoomDatabase(
//     builder: RoomDatabase.Builder<AppDatabase>
// ): AppDatabase {
//     return builder
//         .setDriver(BundledSQLiteDriver())
//         .setQueryCoroutineContext(Dispatchers.IO)
//         .build()
// }
