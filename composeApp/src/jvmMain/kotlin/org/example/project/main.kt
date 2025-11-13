// vim: expandtab
package org.example.project

import cli.*;
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

val doCli : Boolean = true;


fun main() { 
    if (doCli) {
        cli()
    } else {
        return application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "InventoryApp",
            ) {
                App()
            }
        }
    }
}
