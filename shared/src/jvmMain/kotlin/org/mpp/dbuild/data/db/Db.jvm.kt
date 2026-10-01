package org.mpp.dbuild.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
//     val dbLocation = "/home/gasper/Documents/songdb/"
//     // val dbFile = File(System.getProperty("java.io.tmpdir"), "pesmi")
//     val dbFile = File(dbLocation, "pesmi")
//     return Room.databaseBuilder<AppDatabase>(
//         name = dbFile.absolutePath,
//     )
// }
