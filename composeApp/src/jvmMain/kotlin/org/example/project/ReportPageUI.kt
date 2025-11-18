package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp

import inventoryapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

import backend.JsonUSF
import middle.ProductDB
import middle.typedefs.PID
import middle.typedefs.ProductInfo
import java.io.File

// Colors & fonts from App.kt
import org.example.project.darkBlue
import org.example.project.lightBlue
import org.example.project.yellow
import org.example.project.monospace

/**
 * Main composable for selecting products on the Report page.
 *
 * You can call this from your ReportPage() in App.kt:
 *
 *   @Composable
 *   fun ReportPage() {
 *       Column( ... ) {
 *           // title etc...
 *           selectProductsUI()
 *       }
 *   }
 */
@Composable
fun selectProductsUI(
    modifier: Modifier = Modifier,
    onSelectionChanged: (Set<PID>) -> Unit = {}
) {
    var products by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedProducts by remember { mutableStateOf<Set<PID>>(emptySet()) }

    // Load DB + products (same as InventoryPage)
    LaunchedEffect(Unit) {
        try {
            val runtimeFile = File("files/dbfile.json")

            // If runtime DB doesn't exist, copy from resources
            if (!runtimeFile.exists()) {
                runtimeFile.parentFile?.mkdirs()


                @OptIn(ExperimentalResourceApi::class)
                val bytes = Res.readBytes("files/dbfile.json")

                //val bytes = useResource("files/dbfile.json") { it.readBytes() }
                runtimeFile.writeBytes(bytes)
            }

            val dbText = runtimeFile.readText()
            val usf = JsonUSF.fromString(dbText)
            ProductDB.prodFromUSF(usf)

            products = ProductDB.itemMap.keys
                .map { pid -> ProductDB.getInfoFor(pid) }
                .sortedBy { it.gName }

        } catch (e: Exception) {
            loadError = e.message ?: "Unknown error loading database"
        } finally {
            isLoading = false
        }
    }

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
                // The actual multiselect control
                ProductMultiSelect(
                    products = products,
                    selected = selectedProducts,
                    onSelectionChange = { newSelection ->
                        selectedProducts = newSelection
                        onSelectionChanged(newSelection)
                    }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (selectedProducts.isEmpty())
                        "No products selected."
                    else
                        "Selected: " + selectedProducts.joinToString(", ") { pid ->
                            products.find { it.idName == pid }?.gName ?: pid.name
                        },
                    color = Color.White,
                    fontFamily = monospace
                )
            }
        }
    }
}

/**
 * Multi-select dropdown with search for ProductInfo / PID.
 */
@Composable
fun ProductMultiSelect(
    products: List<ProductInfo>,
    selected: Set<PID>,
    onSelectionChange: (Set<PID>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Filter by gName or idName.name
    val filtered = remember(products, searchText) {
        if (searchText.isBlank()) products
        else products.filter {
            it.gName.contains(searchText, ignoreCase = true) ||
                    it.idName.name.contains(searchText, ignoreCase = true)
        }
    }

    // Button label
    val buttonText =
        if (selected.isEmpty()) "Select Products"
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
            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Clear selection
            DropdownMenuItem(
                text = { Text("Clear Selection") },
                onClick = { onSelectionChange(emptySet()) }
            )

            // Scrollable list of products
            LazyColumn(
                modifier = Modifier.heightIn(max = 260.dp)
            ) {
                items(filtered, key = { it.idName.name }) { product ->
                    val isChecked = selected.contains(product.idName)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(product.gName)
                            }
                        },
                        onClick = {
                            val newSet = selected.toMutableSet()
                            if (isChecked) newSet.remove(product.idName)
                            else newSet.add(product.idName)
                            onSelectionChange(newSet)
                        }
                    )
                }
            }
        }
    }
}
