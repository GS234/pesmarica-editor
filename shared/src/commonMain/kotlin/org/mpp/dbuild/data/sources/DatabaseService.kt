package org.mpp.dbuild.data.sources

import androidx.room3.executeSQL
import androidx.room3.useWriterConnection
import org.mpp.dbuild.data.db.AppDatabase

class DatabaseService(
    private val database: AppDatabase,
) {
    suspend fun exportToDb(destinationPath: String) {
        try {
            database.useWriterConnection { connection ->
                val escapedPath = destinationPath.replace("'", "''")

                connection.executeSQL(
                    "VACUUM INTO '$escapedPath'"
                )
            }
        }
        catch (e: Exception){
            println("[error]")
        }
    }
}



