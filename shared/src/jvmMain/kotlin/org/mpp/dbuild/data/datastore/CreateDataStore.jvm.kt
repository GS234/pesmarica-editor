package org.mpp.dbuild.data.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File

// AI generated:
@Composable
actual fun rememberDataStore(): PrefsDataStore {
    return remember {
        createDataStore {
            // 1. Identify the operating system and user home path
            val os = System.getProperty("os.name").lowercase()
            val userHome = System.getProperty("user.home")

            // 2. Select a standard, hidden folder layout depending on the OS
            val appName = "Pesmarica Editor"
            val appDir = when {
                os.contains("win") -> File(System.getenv("APPDATA"), appName)
                os.contains("mac") -> File(userHome, "Library/Application Support/$appName")
                else -> File(userHome, ".config/$appName") // Linux fallback
            }

            // 3. Ensure the folder structure exists before creating the file path
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            // 4. Return the full absolute path pointing to your settings file
            File(appDir, dataStoreFileName).absolutePath
        }
    }
}