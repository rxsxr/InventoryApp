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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
//import androidx.compose.foundation.layout.weight
//import androidx.compose.ui.layout.weight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
//import inventoryapp.composeapp.generated.resources.Res
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
import java.time.LocalDate

import inventoryapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

import middle.typedefs.PID
import middle.ProductDB
import middle.typedefs.ProductInfo
import androidx.compose.ui.res.useResource
import java.io.File



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



@Composable
@Preview
fun InventoryPage(
    openUpdateInventoryPage: () -> Unit,
    registerInventoryRefresh: ((() -> Unit) -> Unit)? = null
    ) {

    var products by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }
    var filtered by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var tagFiltered by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }
    var lowStockFiltered by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }

    // refresh function
    fun refreshInventory() {
        val newProducts = ProductDB.itemMap.keys
            .map { pid -> ProductDB.getInfoFor(pid) }
            //.filter { info -> info.totalStock > 0 }
            .sortedBy { it.gName }

        products = newProducts
    }

    // expose refresh function
    LaunchedEffect(Unit) {
        registerInventoryRefresh?.invoke {refreshInventory() }
    }

    // load DB from dbfile.json
    LaunchedEffect(Unit) {
        try {
            loadDatabaseFile()
            refreshInventory()
        } catch (e: Exception) {
            loadError = e.message ?: "Unknown error loading database"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(products) { filtered = products }

    // combined filters
    fun applyCombinedFilters() {
        val tagActive = tagFiltered.isNotEmpty() && tagFiltered != products
        val lowActive = lowStockFiltered.isNotEmpty() && lowStockFiltered != products

        filtered =
            when {
                tagActive && lowActive ->
                    products.filter { it in tagFiltered && it in lowStockFiltered }

                tagActive -> tagFiltered
                lowActive -> lowStockFiltered

                else -> products
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

        // title
        Text (
            text = "INVENTORY",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )



        Row ( // all filters

        ) {
            Column ( // filter by tags
                modifier = Modifier.weight(1f)
            ) {
                Text (
                    text = "Filter by Tags",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 3.dp)
                )

                tagFilter(
                    allProducts = products,
                    resetSignal = false
                    ) { newFiltered ->
                    tagFiltered = newFiltered
                    applyCombinedFilters()
                }

            }

            Column ( // filter by low stock
                modifier = Modifier.weight(1f)
            ) {
                Text (
                    text = "Filter by Low Stock",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 3.dp)
                )

                lowStockFilter(products) { newFiltered ->
                    lowStockFiltered = newFiltered
                    applyCombinedFilters()
                }

            }
        }

        // cases for data loading
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
            modifier = Modifier
                .width(250.dp)
        ) {
            Text(
                "UPDATE INVENTORY",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }
    }
}


var refreshInventoryCallback: (() -> Unit)? = null



enum class UpdateMode {
    None,
    AddRemoveStock,
    AddNewProduct,
    RemoveProduct,
    EditProductInfo
}


@Composable
@Preview
fun UpdateInventoryPage(
    refreshInventory: (() -> Unit)? = null
) {

    var mode by remember { mutableStateOf(UpdateMode.None) }

    when (mode) {

        UpdateMode.None -> {
            UpdateInventoryMenu(
                onAddRemoveStock = { mode = UpdateMode.AddRemoveStock },
                onAddNewProduct = { mode = UpdateMode.AddNewProduct },
                onRemoveProduct = { mode = UpdateMode.RemoveProduct },
                onEditProductInfo = { mode = UpdateMode.EditProductInfo }
            )
        }

        UpdateMode.AddRemoveStock -> {
            AddRemoveStockUI(
                onBack = { mode = UpdateMode.None },
                refreshInventory = { refreshInventory?.invoke() }
                )
        }

        UpdateMode.AddNewProduct -> {
            AddNewProductUI(
                onBack = { mode = UpdateMode.None },
                refreshInventory = { refreshInventory?.invoke() }
            )
        }

        UpdateMode.RemoveProduct -> {
            RemoveProductUI(
                onBack = { mode = UpdateMode.None },
                refreshInventory = { refreshInventory?.invoke() }
            )
        }

        UpdateMode.EditProductInfo -> {
            EditProductUI(
                onBack = { mode = UpdateMode.None },
                refreshInventory = { refreshInventory?.invoke() }
            )
        }
    }
}

@Preview
@Composable
fun ReportPage() {

    var selectedProducts by remember { mutableStateOf<Set<PID>>(emptySet()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var reportMessage by remember { mutableStateOf<String?>(null) }


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
            text = "REPORT GENERATION",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(Modifier.height(40.dp))

        Text (
            text = "#1 - Select Products (by Name or Tags)",
            color = lightBlue,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.Start)
        )

        Text (
            text = "Select by Name",
            color = Color.White,
            fontFamily = monospace,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .align(Alignment.Start)
        )


        // product selection by name
        selectProductsUI(
            currentSelection = selectedProducts,
            onSelectionChanged = { newSet ->
                selectedProducts = newSet
            }
        )

        Spacer(Modifier.height(20.dp))


        Text (
            text = "#2 - Select Timeframe",
            color = lightBlue,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.Start)
        )


        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (modifier = Modifier.weight(1f)) {
                DateSelector(
                    label = "Start Date",
                    date = startDate,
                    onDateChange = { startDate = it }
                )
            }

            Column (modifier = Modifier.weight(1f)) {
                DateSelector(
                    label = "End Date",
                    date = endDate,
                    onDateChange = { endDate = it }
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        ElevatedButton( // generate report button
            onClick = {

                if (selectedProducts.isEmpty()) {
                    reportMessage = "Error: no product selected"
                    return@ElevatedButton
                }

                val result = generateReportFromSelections(
                    selectedProducts = selectedProducts,
                    startDate = startDate,
                    endDate = endDate
                )

                reportMessage = if (result != null)
                    "Report generated successfully! File name: $result"
                else
                    "Failed to generate report."

            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue,
            ),
            modifier = Modifier
                .width(250.dp)
        ){
            Text(
                "GENERATE REPORT",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }

        if (reportMessage != null) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = reportMessage!!,
                fontFamily = monospace,
                color = yellow
            )
        }



    }
}
