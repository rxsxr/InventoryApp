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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import middle.ProductDB
import middle.typedefs.ProductInfo
import backend.JsonUSF
import org.example.project.darkBlue

val greyBox = Color(0xFFD3D3D3)


// UPDATE MENU UI
@Composable
fun UpdateInventoryMenu(
    onAddRemoveStock: () -> Unit,
    onAddNewProduct: () -> Unit,
    onRemoveProduct: () -> Unit,
    onEditProductInfo: () -> Unit
    ) {

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = darkBlue)
            .safeContentPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // title
        Text(
            text = "UPDATE INVENTORY",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ElevatedButton( // add/remove stock button
            onClick = onAddRemoveStock,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue,
            ),
            modifier = Modifier
                .width(250.dp),
            //.padding(top = 20.dp),
        ) {
            Text(
                "Add/Remove Stock",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }

        ElevatedButton( // add new product button
            onClick = onAddNewProduct,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue,
            ),
            modifier = Modifier
                .width(250.dp),
            //.padding(top = 20.dp),
        ) {
            Text(
                "Add New Product",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }

        ElevatedButton( // add/remove product button
            onClick = onRemoveProduct,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue,
            ),
            modifier = Modifier
                .width(250.dp),
            //.padding(top = 20.dp),
        ) {
            Text(
                "Remove Product",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }

        ElevatedButton( // edit product info button
            onClick = onEditProductInfo,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue,
            ),
            modifier = Modifier
                .width(250.dp),
            //.padding(top = 20.dp),
        ) {
            Text(
                "Edit Product Info",
                fontFamily = monospace,
                fontSize = 16.sp,
            )
        }
    }
}


// INPUT FIELD FUNCTIONS
@Composable
fun inputLabel(t: String) =
    Text(t, color = lightBlue, fontFamily = monospace, fontSize = 16.sp)

@Composable
fun field(v: String, set: (String) -> Unit, width: Float = 0.7f) =
    TextField(
        value = v, onValueChange = set, singleLine = true,
        modifier = Modifier.fillMaxWidth(width),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = greyBox,
            focusedContainerColor = greyBox
        )
    )


// ADD/REMOVE STOCK UI
@Composable
fun AddRemoveStockUI(
    onBack: () -> Unit,
    refreshInventory: () -> Unit
) {

    var scrollState = rememberScrollState()
    var message by remember { mutableStateOf("") }

    var productName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("ADD") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // back button
        ElevatedButton(
            onClick = onBack,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue
            ),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back")
        }

        Spacer(Modifier.height(20.dp))

        // Title
        Text(
            text = "ADD / REMOVE STOCK",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )

        Spacer(Modifier.height(30.dp))

        // product name input
        inputLabel("Product Name")
        field(productName, { productName = it }, width = 0.5f)
        Spacer(Modifier.height(25.dp))

        // add or remove selection
        Text("Operation", color = lightBlue, fontFamily = monospace, fontSize = 16.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {

            RadioButton(
                selected = operation == "ADD",
                onClick = { operation = "ADD" }
            )
            Text("Add", color = Color.White)

            Spacer(Modifier.width(30.dp))

            RadioButton(
                selected = operation == "REMOVE",
                onClick = { operation = "REMOVE" }
            )
            Text("Remove", color = Color.White)
        }

        Spacer(Modifier.height(25.dp))

        // amount input
        inputLabel("Amount")
        field(amountText, { amountText = it }, width = 0.5f)

        Spacer(Modifier.height(40.dp))

        // update button
        ElevatedButton(
            onClick = {
                // check for blank fields
                if (productName.isBlank() || amountText.isBlank()) {
                    message = "Error: Please fill out all fields."
                    return@ElevatedButton
                }
                // check for negative input
                val amount = amountText.toIntOrNull()
                if (amount == null || amount <= 0) {
                    message = "Error: Amount must be a positive number."
                    return@ElevatedButton
                }

                // display update message & update inventory
                val result = updateStock(
                    productName = productName,
                    amount = amount,
                    isAdd = (operation == "ADD")
                )

                refreshInventory()
                message = result

            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue
            ),
            modifier = Modifier.width(200.dp)
        ) {
            Text("UPDATE", fontFamily = monospace)
        }

        Spacer(Modifier.height(20.dp))

        // status/error message format
        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = yellow,
                fontFamily = monospace,
                fontSize = 16.sp
            )
        }
    }
}




// ADD NEW PRODUCT UI
@Composable
fun AddNewProductUI(
    onBack: () -> Unit,
    refreshInventory: () -> Unit
) {

    var scrollState = rememberScrollState()
    var message by remember { mutableStateOf("") }

    var idName by remember { mutableStateOf("") }
    var gName by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var stockAmount by remember { mutableStateOf("") }
    var lowBound by remember { mutableStateOf("") }
    var dateSold by remember { mutableStateOf("") }
    var unitStr by remember { mutableStateOf("") }
    var unitAmt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // back button
        ElevatedButton(
            onClick = onBack,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue
            ),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back")
        }

        Spacer(Modifier.height(20.dp))

        // Title
        Text(
            text = "ADD NEW PRODUCT",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )

        Spacer(Modifier.height(25.dp))

        // input fields

        inputLabel("ID Name (lowercase, no spaces)")
        field(
            v = idName,
            set = { idName = it }
        )
        Spacer(Modifier.height(20.dp))

        inputLabel("Display Name")
        field(gName, { gName = it })
        Spacer(Modifier.height(20.dp))

        inputLabel("Tags (comma separated)")
        field(tags, { tags = it })
        Spacer(Modifier.height(20.dp))

        inputLabel("Buy Price (e.g. \$2.50)")
        field(
            v = buyPrice,
            set = { buyPrice = it },
            width = 0.5f
        )
        Spacer(Modifier.height(20.dp))

        inputLabel("Sell Price (e.g. \$3.99)")
        field(sellPrice, { sellPrice = it }, width = 0.5f)
        Spacer(Modifier.height(20.dp))

        inputLabel("Stock Amount")
        field(stockAmount, { stockAmount = it }, width = 0.4f)
        Spacer(Modifier.height(20.dp))

        inputLabel("Low Bound")
        field(lowBound, { lowBound = it }, width = 0.4f)
        Spacer(Modifier.height(20.dp))

        inputLabel("Date Sold (YYYY-MM-DD)")
        field(
            v = dateSold,
            set = { dateSold = it },
            width = 0.5f
        )
        Spacer(Modifier.height(20.dp))

        inputLabel("Unit String (Kg or L)")
        field(unitStr, { unitStr = it }, width = 0.4f)
        Spacer(Modifier.height(20.dp))

        inputLabel("Unit Amount")
        field(
            v = unitAmt,
            set = { unitAmt = it },
            width = 0.4f
        )
        Spacer(Modifier.height(30.dp))


        // add new product button
        ElevatedButton(
            onClick = {

                val result = addNewProduct(
                    idName, gName, tags, buyPrice, sellPrice,
                    stockAmount, lowBound, dateSold, unitStr, unitAmt
                )

                message = result
                if (!result.startsWith("Error")) {
                    refreshInventory()
                }

            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue
            ),
            modifier = Modifier.width(200.dp)
        ) {
            Text("ADD PRODUCT", fontFamily = monospace)
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(message, color = yellow, fontFamily = monospace)
        }
    }
}


// REMOVE PRODUCT UI
@Composable
fun RemoveProductUI(
    onBack: () -> Unit,
    refreshInventory: () -> Unit
) {

    var scrollState = rememberScrollState()
    var message by remember { mutableStateOf("") }

    var productName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // back button
        ElevatedButton(
            onClick = onBack,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue
            ),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back")
        }

        Spacer(Modifier.height(20.dp))

        // Title
        Text(
            text = "REMOVE PRODUCT",
            color = yellow,
            fontFamily = monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
        Spacer(Modifier.height(30.dp))

        // product name input
        inputLabel("Product Name")
        field(productName, { productName = it }, width = 0.5f)
        Spacer(Modifier.height(25.dp))

        // update button
        ElevatedButton(
            onClick = {
                val result = removeProduct(productName = productName)
                message = result
                if (!result.startsWith("Error")) {
                    refreshInventory()
                }
            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = lightBlue,
                contentColor = darkBlue
            ),
            modifier = Modifier.width(200.dp)
        ) {
            Text("REMOVE PRODUCT", fontFamily = monospace)
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(message, color = yellow, fontFamily = monospace)
        }
    }
}


// EDIT PRODUCT UI
/*
@Composable
fun EditProductUI(
    onBack: () -> Unit,
    refreshInventory: () -> Unit
) {

    var scrollState = rememberScrollState()
    var message by remember { mutableStateOf("") }

    var productName by remember { mutableStateOf("") }


}*/
