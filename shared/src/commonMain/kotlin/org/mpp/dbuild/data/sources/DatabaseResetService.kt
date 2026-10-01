package org.mpp.dbuild.data.sources

import org.mpp.dbuild.data.db.AppDatabase

class DatabaseResetService(
    private val database: AppDatabase,
) {
    suspend fun clearAllData() {
        database.clearAllTables()
    }
}
