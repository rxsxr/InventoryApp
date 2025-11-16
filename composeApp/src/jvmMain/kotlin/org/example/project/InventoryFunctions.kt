package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import middle.ProductDB
import middle.typedefs.ProductInfo
import backend.JsonUSF
import org.example.project.darkBlue


// load inventory function
@Composable
fun loadInventory(): Triple<List<ProductInfo>, String?, Boolean> {

    var products by remember { mutableStateOf<List<ProductInfo>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val dbText = useResource("files/dbfile.json") { it.readBytes().decodeToString() }

            val usf = JsonUSF.fromString(dbText)
            ProductDB.fromUSF(usf)

            products = ProductDB.itemMap.keys
                .map { ProductDB.getInfoFor(it) }
                .filter { it.totalStock > 0 }
                .sortedBy { it.gName }

        } catch (e: Exception) {
            loadError = e.message ?: "Unknown error loading database"
        } finally {
            isLoading = false
        }
    }

    return Triple(products, loadError, isLoading)
}


// header row
@Composable
fun tableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(medBlue)
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        headerCell("Name", Modifier.weight(2f))
        headerCell("Tags", Modifier.weight(2f))
        headerCell("Stock", Modifier.weight(1f))
        headerCell("Buy Price", Modifier.weight(1f))
        headerCell("Sale Price", Modifier.weight(1f))
        headerCell("Unit", Modifier.weight(1f))
    }
}

@Composable
private fun headerCell(text: String, mod: Modifier) {
    Text(
        text = text,
        modifier = mod,
        color = Color.White,
        fontFamily = monospace,
        fontWeight = FontWeight.Bold
    )
}

// table records
@Composable
fun inventoryTable(products: List<ProductInfo>) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = darkBlue)
    ) {
        items(products) { info ->

            val tagText = info.tagSet.joinToString(", ") { it.tag }
            val stockColor = if (info.stockLevel.name == "Low") Color.Red else Color.Green
            val unitText = if (info.unitPiece.unitStr == null || info.unitPiece.unitStr == "<<NIL>>") {
                "none"
            } else {
                val str = info.unitPiece.unitStr
                val amt = info.unitPiece.unitAmt

                if (amt == null || amt.toString() == "<<NIL>>") {
                    str!!
                } else {
                    "$amt / $str" // eg. 1 / Kilogram
                }
            }
                /* when (info.unitPiece.unitStr) {
                                "L", "Litre", "Litres" -> "Litre"
                                "kg", "KG", "Kilogram" -> "Kilogram"
                                null -> "None"
                                else -> info.unitPiece.unitStr!!
                            } */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = lightBlue.copy(alpha = 0.1f))
                    .padding(vertical = 6.dp, horizontal = 8.dp)
            ) {

                itemCell(info.gName, Modifier.weight(2f), Color.White)
                itemCell(tagText, Modifier.weight(2f), lightBlue)
                itemCell(info.totalStock.toString(), Modifier.weight(1f), stockColor)
                itemCell(info.buyPrice.toString(), Modifier.weight(1f), Color.White)
                itemCell(info.sellPrice.toString(), Modifier.weight(1f), Color.White)
                itemCell(unitText, Modifier.weight(1f), Color.White)
            }
        }
    }
}

@Composable
private fun itemCell(text: String, mod: Modifier, color: Color) {
    Text(
        text = text,
        modifier = mod,
        color = color,
        fontFamily = monospace,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}




// dropdown tag selector
@Composable
fun tagDropdown(tags: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = lightBlue)) {
            Text(if (selected.isBlank()) "All Tags" else selected)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All Tags") }, onClick = {
                expanded = false
                onSelect("")
            })

            tags.forEach { t ->
                DropdownMenuItem(text = { Text(t) }, onClick = {
                    expanded = false
                    onSelect(t)
                })
            }
        }
    }
}





// tag filter
@Composable
fun tagFilter(
    allProducts: List<ProductInfo>,
    onFiltered: (List<ProductInfo>) -> Unit
) {
    // Collect all unique tags from products
    val allTags = allProducts
        .flatMap { it.tagSet.map { tag -> tag.tag } }
        .distinct()
        .sorted()

    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text("Filter by Tags", color = Color.White, fontWeight = FontWeight.Bold, modifier=Modifier.padding(bottom = 3.dp))

        // Multi-select dropdown button
        Button(
            onClick = { menuExpanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
        ) {
            val label = if (selectedTags.isEmpty()) "None"
            else selectedTags.joinToString(", ")
            Text(label, color = darkBlue)
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            // Clear all
            DropdownMenuItem(
                text = { Text("All Tags") },
                onClick = {
                    selectedTags = emptySet()
                    menuExpanded = false
                    onFiltered(allProducts)
                }
            )

            // Each tag item
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(Modifier.width(6.dp))
                            Text(tag)
                        }
                    },
                    onClick = {
                        selectedTags = if (isSelected)
                            selectedTags - tag
                        else
                            selectedTags + tag

                        val filtered =
                            if (selectedTags.isEmpty()) allProducts
                            else allProducts.filter { p ->
                                p.tagSet.any { t -> t.tag in selectedTags }
                            }

                        onFiltered(filtered)
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}




