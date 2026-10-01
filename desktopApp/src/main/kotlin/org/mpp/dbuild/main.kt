package org.mpp.dbuild

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() {
    FileKit.init(appId = "PesmaricaEditor")
    application {
        var title by remember {mutableStateOf("Pesmarica Editor")}
        Window(
            onCloseRequest = ::exitApplication,
            title = title,
        ) {
            App(
                onNameChange = {title = it}
            )
        }
    }
}