package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.foundation.layout.weight
//import androidx.compose.ui.layout.weight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import inventoryapp.composeapp.generated.resources.Res
import inventoryapp.composeapp.generated.resources.compose_multiplatform
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow

import middle.ProductDB
import middle.typedefs.ProductInfo
import androidx.compose.ui.res.useResource
import backend.JsonUSF



val darkBlue = Color(0xFF023047)
val medBlue = Color(0xFF219EBC)
val lightBlue = Color(0xFF8ECAE6)
val yellow = Color(0xFFFFB703)
val orange = Color(0xFFFB8500)


val monospace = FontFamily.Monospace

@Composable
@Preview
fun WelcomePage(openInventoryPage: () -> Unit, openReportPage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = darkBlue)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.SpaceAround,
    ) { // full page
        Column(
            modifier = Modifier
                .weight(3f)
                .fillMaxWidth(),
                //.background(color = medBlue), // remove background color
            horizontalAlignment = Alignment.CenterHorizontally
        ) { // title
            Text(
                "GROCERY STORE",
                color = yellow,
                fontFamily = monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier.padding(top = 100.dp)
            )
            Text(
                "INVENTORY",
                color = yellow,
                fontFamily = monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                //.background(color = orange) // remove background color
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.SpaceEvenly,
        ) { // actions
            ElevatedButton( // view inventory button
                onClick = openInventoryPage,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = lightBlue,
                    contentColor = darkBlue,
                ),
                //elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(250.dp),
                    //.padding(top = 20.dp),
            ) {
                Text(
                    "VIEW INVENTORY",
                    fontFamily = monospace,
                    fontSize = 16.sp,
                )
            }

            ElevatedButton( // generate report button
                onClick = openReportPage,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = lightBlue,
                    contentColor = darkBlue
                ),
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 30.dp),
            ) {
                Text(
                    "GENERATE REPORT",
                    fontFamily = monospace,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/*
@Composable
@Preview
fun InventoryPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            //.background(color = darkBlue)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("This is the inventory page!")
    }
}
 */

@Composable
@Preview
fun InventoryPage(openUpdateInventoryPage: () -> Unit) {

    var products by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // load DB from dbfile.json
    LaunchedEffect(Unit) {
        try {
            // file path
            val dbText =  useResource("files/dbfile.json") { it.readBytes().decodeToString() }

            val usf = JsonUSF.fromString(dbText)
            ProductDB.fromUSF(usf)

            val infos = ProductDB.itemMap.keys
                .map { pid -> ProductDB.getInfoFor(pid) }
                .filter { info -> info.totalStock > 0 } // only products in stock
                .sortedBy { it.gName }

            products = infos
        } catch (e: Exception) {
            loadError = e.message ?: "Unknown error loading database"
        } finally {
            isLoading = false
        }
    }


    // format page
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = darkBlue)
            .safeContentPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // title:
        Text (
            text = "INVENTORY",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // cases for data loading:
        when {
            isLoading -> { // when products are loading
                Text(
                    text = "Loading inventory...",
                    color = lightBlue,
                    fontFamily = monospace
                )
            }

            loadError != null -> { // when products fail to load
                Text(
                    text = "Error: $loadError",
                    color = Color.Red,
                    fontFamily = monospace
                )
            }

            products.isEmpty() -> { // when no products in stock
                Text(
                    text = "No products in stock.",
                    color = lightBlue,
                    fontFamily = monospace
                )
            }

            else -> { // products load successfully
                var filtered by remember { mutableStateOf(products) }

                tagFilter(products) { newFiltered ->
                    filtered = newFiltered
                }

                // display table in scrollable box
                Box (
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    Column {
                        tableHeaderRow()
                        inventoryTable(filtered)
                    }
                }


            }
        }





        ElevatedButton( // update inventory button
            onClick = openUpdateInventoryPage,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue,
            ),
            //elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .width(250.dp),
            //.padding(top = 20.dp),
        ) {
            Text(
                "UPDATE INVENTORY",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
@Preview
fun UpdateInventoryPage() {
    Text("This is the update inventory page!")
}

@Composable
@Preview
fun ReportPage() {
    Text("This is the report page!")
}
