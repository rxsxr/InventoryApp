// vim: expandtab
package org.example.project

import cli.*;
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.*

fun main() = application {

    var showWelcome by remember { mutableStateOf(true) }
    var showInventoryPage by remember { mutableStateOf(false) }
    var showReportPage by remember { mutableStateOf(false) }
    var showUpdateInventoryPage by remember { mutableStateOf(false) }

    if (showWelcome) {
        // welcome page
        Window(
            onCloseRequest = ::exitApplication,
            title = "Inventory"
        ) {
            WelcomePage(
                openInventoryPage = {
                    showWelcome = false
                    showInventoryPage = true
                },
                openReportPage = {
                    showWelcome = false
                    showReportPage = true
                }
            )
        }
    }

    if (showInventoryPage) {
        Window(
            onCloseRequest = {
                showInventoryPage = false
                showWelcome = true
            },
            title = "Inventory"
        ) {
            InventoryPage(
                openUpdateInventoryPage = {
                    showUpdateInventoryPage = true
                }
            )
        }
    }

    if (showUpdateInventoryPage) {
        Window(
            title = "Update Inventory",
            onCloseRequest = { showUpdateInventoryPage = false }
        ){
            UpdateInventoryPage()
        }
    }

    if (showReportPage) {
        Window(
            onCloseRequest = {
                showReportPage = false
                showWelcome = true
            },
            title = "Generate Report"
        ) {
            ReportPage()
        }
    }


    /*
    Window(
        onCloseRequest = ::exitApplication,
        title = "InventoryApp",
    ) {
        App()
    } */
}
