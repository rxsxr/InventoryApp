package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import java.time.Month
import java.time.Year
import kotlinx.datetime.*
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.LocalDate as KtxDate

import inventoryapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

import backend.JsonUSF
import middle.price.Price
import middle.ProductDB
import middle.typedefs.PID
import middle.typedefs.ProductInfo
import middle.transaction.Transaction
import java.io.File

// Colors & fonts from App.kt
import org.example.project.darkBlue
import org.example.project.lightBlue
import org.example.project.yellow
import org.example.project.monospace



@Composable
fun selectProductsUI(
    modifier: Modifier = Modifier,
    currentSelection: Set<PID> = emptySet(),
    onSelectionChanged: (Set<PID>) -> Unit
) {

    var selectedByName by remember { mutableStateOf<Set<PID>>(emptySet()) }
    var selectedByTag by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }

    // load DB + products
    val (products, loadError, isLoading) = loadInventory()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        when {
            isLoading -> {
                Text(
                    text = "Loading products...",
                    color = Color.White,
                    fontFamily = monospace
                )
            }

            loadError != null -> {
                Text(
                    text = "Error: $loadError",
                    color = Color.Red,
                    fontFamily = monospace
                )
            }

            products.isEmpty() -> {
                Text(
                    text = "No products available.",
                    color = Color.White,
                    fontFamily = monospace
                )
            }

            else -> {

                // select products by name
                productSelectByName(
                    products = products,
                    selected = selectedByName,
                    onSelectionChange = { newSet ->
                        if (newSet.isNotEmpty()) {
                            selectedByTag = emptyList()
                        }

                        selectedByName = newSet

                        val finalSet =
                            if (selectedByName.isNotEmpty())
                                selectedByName
                            else
                                selectedByTag.map { it.idName }.toSet()

                        onSelectionChanged(finalSet)
                    }
                )

                Spacer(Modifier.height(16.dp))


                Text (
                    text = "Select by Tag",
                    color = Color.White,
                    fontFamily = monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // select products by tag
                tagFilter(products) { newFiltered ->
                    if (newFiltered.isNotEmpty()) {
                        selectedByName = emptySet()
                    }

                    selectedByTag = newFiltered

                    val finalSet =
                        if (selectedByTag.isNotEmpty())
                            selectedByTag.map { it.idName }.toSet()
                        else
                            selectedByName

                    onSelectionChanged(finalSet)
                }


                /*Text(
                    text = if (selectedProducts.isEmpty())
                        "No products selected."
                    else
                        "Selected: " + selectedProducts.joinToString(", ") { pid ->
                            products.find { it.idName == pid }?.gName ?: pid.name
                        },
                    color = Color.White,
                    fontFamily = monospace
                )*/
            }
        }
    }
}


// products multi-select
@Composable
fun productSelectByName(
    products: List<ProductInfo>,
    selected: Set<PID>,
    onSelectionChange: (Set<PID>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // filter by gName or idName.name
    val filtered = remember(products, searchText) {
        if (searchText.isBlank()) products
        else products.filter {
            it.gName.contains(searchText, ignoreCase = true) ||
                    it.idName.name.contains(searchText, ignoreCase = true)
        }
    }

    // button label
    val buttonText =
        if (selected.isEmpty()) "None"
        else selected.joinToString(", ") { pid ->
            products.find { it.idName == pid }?.gName ?: pid.name
        }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
        ) {
            Text(buttonText, color = darkBlue, fontFamily = monospace)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(260.dp)
        ) {
            // search bar
            DropdownMenuItem(
                text = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                onClick = {}
            )

            // clear selection
            DropdownMenuItem(
                text = { Text("Clear Selection") },
                onClick = { onSelectionChange(emptySet()) }
            )

            // scroll area
            Box(
                modifier = Modifier
                    .height(260.dp)
                    .width(260.dp)
                    .padding(horizontal = 8.dp)
            ) {
                LazyColumn {
                    items(filtered) { product ->
                        val isChecked = selected.contains(product.idName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = selected.toMutableSet()
                                    if (isChecked) newSet.remove(product.idName)
                                    else newSet.add(product.idName)
                                    onSelectionChange(newSet)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(product.gName)
                        }
                    }
                }
            }

        }

    }
}


@Composable
fun DateSelector(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    startYear: Int = 2000,
    endYear: Int = LocalDate.now().year
) {
    val selected = date ?: LocalDate.now()

    var showMonthMenu by remember { mutableStateOf(false) }
    var showDayMenu by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }

    val months = Month.values().toList()
    val daysInMonth = selected.month.length(selected.isLeapYear)

    Column(
        Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            fontFamily = monospace,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {

            // month dropdown
            Box {
                Button(
                    onClick = { showMonthMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
                ) {
                    Text(
                        selected.month.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = monospace,
                        color = darkBlue
                    )
                }
                DropdownMenu(
                    expanded = showMonthMenu,
                    onDismissRequest = { showMonthMenu = false }
                ) {
                    months.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onDateChange(LocalDate.of(selected.year, m, selected.dayOfMonth.coerceAtMost(m.length(selected.isLeapYear))))
                                showMonthMenu = false
                            }
                        )
                    }
                }
            }

            // day dropdown
            Box {
                Button(
                    onClick = { showDayMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = lightBlue),
                    modifier = Modifier
                        .padding(start=10.dp, end=10.dp)
                ) {
                    Text(
                        selected.dayOfMonth.toString(),
                        fontFamily = monospace,
                        color = darkBlue
                    )
                }
                DropdownMenu(
                    expanded = showDayMenu,
                    onDismissRequest = { showDayMenu = false }
                ) {
                    for (d in 1..daysInMonth) {
                        DropdownMenuItem(
                            text = { Text(d.toString()) },
                            onClick = {
                                onDateChange(LocalDate.of(selected.year, selected.month, d))
                                showDayMenu = false
                            }
                        )
                    }
                }
            }

            // year dropdown
            Box {
                Button(
                    onClick = { showYearMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
                    ) {
                    Text(
                        selected.year.toString(),
                        fontFamily = monospace,
                        color = darkBlue
                    )
                }
                DropdownMenu(
                    expanded = showYearMenu,
                    onDismissRequest = { showYearMenu = false }
                ) {
                    for (y in endYear downTo startYear) {
                        DropdownMenuItem(
                            text = { Text(y.toString()) },
                            onClick = {

                                val leap = Year.isLeap(y.toLong())
                                val maxDay = selected.month.length(leap)
                                val newDay = selected.dayOfMonth.coerceAtMost(maxDay)

                                onDateChange(LocalDate.of(y, selected.month, newDay))
                                showYearMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}


// load transactions from file
fun loadAllTransactions(): List<Transaction> {
    val file = File("files/transFile.json")
    if (!file.exists()) return emptyList()

    val jsonText = file.readText()
    val usf = JsonUSF.fromString(jsonText)

    if (usf !is backend.usf.U_List) return emptyList()

    return usf.map { Transaction.fromUSF(it) }
}


// convert java time (UI) to kotlinx time (backend)
fun java.time.LocalDate.toKtx(): KtxDate =
    KtxDate(this.year, this.monthValue, this.dayOfMonth)




// filter transactions
fun filterTransactions(
    allTransactions: List<Transaction>,
    selectedProductPIDs: Set<PID>,
    start: LocalDate?,
    end: LocalDate?
): List<Transaction> {


    // convert UI dates to kotlinx
    val startKtx = start?.let { kotlinx.datetime.LocalDate(it.year, it.monthValue, it.dayOfMonth) }
    val endKtx   = end?.let { kotlinx.datetime.LocalDate(it.year, it.monthValue, it.dayOfMonth) }

    return allTransactions.filter { t ->
        val txDate = t.dateStamp

        val matchesProduct =
            if (selectedProductPIDs.isNotEmpty())
                t.idName in selectedProductPIDs
            else true

        val matchesTime =
            (startKtx == null || txDate >= startKtx) &&
                    (endKtx == null || txDate <= endKtx)

        matchesProduct && matchesTime
    }
}


// generate HTML report
fun generateHTMLReport(
    transactions: List<Transaction>,
    title: String = "Sales Report",
    productNames: Set<String> = emptySet(),
    startDate: LocalDate? = null,
    endDate: LocalDate? = null
): String {

    val totalRevenueCents = transactions.sumOf { it.revenue.amount.toLong() }
    val totalProfitCents = transactions.sumOf { it.profit.amount.toLong() }

    val totalRevenue = Price(totalRevenueCents.toInt())
    val totalProfit = Price(totalProfitCents.toInt())

    val productNamesList =
        if (productNames.isEmpty()) "<i>No products selected</i>"
        else productNames.sorted().joinToString(", ")

    val timePeriod =
        when {
            startDate == null && endDate == null -> "All Time"
            startDate != null && endDate == null -> "From $startDate"
            startDate == null && endDate != null -> "Up to $endDate"
            else -> "$startDate to $endDate"
        }



    val rows = transactions.joinToString("\n") { t ->
        """
        <tr>
            <td>${t.dateStamp}</td>
            <td>${t.idName.name}</td>
            <td>${t.numSold}</td>
            <td>${t.sellPrice}</td>
            <td>${t.buyPrice}</td>
            <td>${t.revenue}</td>
            <td>${t.cost}</td>
            <td>${t.profit}</td>
        </tr>
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8"/>
        <title>$title</title>
        <style>
            body { font-family: Arial; margin: 40px; }
            h1 { margin-bottom: 10px; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ddd; padding: 8px; }
            th { background: #f0f0f0; }
            tfoot td { font-weight: bold; background: #fafafa; }
        </style>
    </head>
    <body>
        <h1>$title</h1>

        <h2>Summary</h2>
        <p><b>Selected Products:</b> $productNamesList </p>
        <p><b>Time Period:</b> $timePeriod </p>
        <p><b>Total Number of Transactions:</b> ${transactions.size}</p>
        <p><b>Total Revenue:</b> $totalRevenue</p>
        <p><b>Total Profit:</b> $totalProfit</p>

        <h2>Details</h2>
        <table>
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Product</th>
                    <th>Qty Sold</th>
                    <th>Sale Price</th>
                    <th>Buy Price</th>
                    <th>Revenue</th>
                    <th>Expenses</th>
                    <th>Profit</th>
                </tr>
            </thead>
            <tbody>
                $rows
            </tbody>
        </table>
    </body>
    </html>
    """.trimIndent()
}

// save report function
fun saveReportHTML(html: String): File {

    val timestamp = java.time.LocalDateTime.now()
        .toString()
        .replace(":", "-")

    val file = File("files/report-$timestamp.html")
    file.parentFile?.mkdirs()
    file.writeText(html)
    return file

}


// generates report when button is clicked
fun generateReportFromSelections(
    selectedProducts: Set<PID>,
    startDate: LocalDate?,
    endDate: LocalDate?
): File? {
    return try {
        // load all transactions
        val allTransactions = loadAllTransactions()

        // filter transactions
        val filtered = filterTransactions(
            allTransactions = allTransactions,
            selectedProductPIDs = selectedProducts,
            start = startDate,
            end = endDate
        )

        // get product names
        val productNames: Set<String> = filtered
            .map { t -> ProductDB.getHandleFor(t.idName).info.gName }
            .toSet()

        // generate HTML
        val html = generateHTMLReport(
            transactions = filtered,
            productNames = productNames,
            startDate = startDate,
            endDate = endDate
        )

        // save to file
        saveReportHTML(html)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

