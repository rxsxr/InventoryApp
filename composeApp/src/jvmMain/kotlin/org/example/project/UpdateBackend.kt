package org.example.project

import inventoryapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

import backend.JsonUSF
import backend.TagDB
import middle.ProductDB
import middle.typedefs.*
import middle.pieces.*
import middle.price.*
import java.io.File

/*

// load dbfile
@OptIn(ExperimentalResourceApi::class)
fun loadDatabaseFile() {

    val runtimeFile = File("files/dbfile.json")

    if (!runtimeFile.exists()) {
        runtimeFile.parentFile?.mkdirs()
        val bytes = Res.readBytes("files/dbfile.json")
        runtimeFile.writeBytes(bytes)
    }

    val dbText = runtimeFile.readText()
    val usf = JsonUSF.fromString(dbText)
    ProductDB.prodFromUSF(usf)
}
*/


fun updateStock(productName: String, amount: Int, isAdd: Boolean):String {

    val cleaned = productName.trim().lowercase()

    // find product PID by name
    val pid = ProductDB.itemMap.entries
        .firstOrNull { (_, info) ->
            info.gName.trim().lowercase() == cleaned
        }
        ?.key
        ?: return "Error: Product '$productName' not found."

    // get mutable handle
    val handle = ProductDB.getHandleFor(pid)

    val oldStock = handle.totalStock
    val newStock = if (isAdd) oldStock + amount else oldStock - amount

    // prevent negative stock
    if (newStock < 0) {
        return "Error: Cannot remove $amount, stock would be negative"
    }

    // update value on handle
    handle.totalStock = newStock

    // commit handle into ProductDB
    try {
        handle.commit()
    } catch (e: Exception) {
        return "Commit failed: ${e.message}"
    }

    // save DB back to JSON
    saveDBToFile()

    return "Updated '$productName': $oldStock → $newStock"
}


fun saveDBToFile() {
    val file = File("files/dbfile.json")
    file.parentFile?.mkdirs()
    ProductDB.saveProductsTo("files/dbfile.json");
}



fun addNewProduct(
    idName: String,
    gName: String,
    tags: String,
    buyPrice: String,
    sellPrice: String,
    stockAmount: String,
    lowBound: String,
    dateSold: String,
    unitStr: String,
    unitAmt: String
): String {

    // validation
    if (idName.isBlank() || gName.isBlank())
        return "Error: ID name and display name are required."

    val pid = PID(idName.trim())

    if (ProductDB.hasProductID(pid))
        return "Error: A product with ID '$idName' already exists."

    val stock = stockAmount.toIntOrNull()
        ?: return "Error: Stock must be a number."

    val low = lowBound.toIntOrNull()
        ?: return "Error: Low Bound must be a number."

    val unitAmtFloat = unitAmt.toFloatOrNull()
        ?: return "Error: Unit amount must be a number."

    // prices
    val buy: Price
    val sell: Price
    try {
        buy = Price.fromString(buyPrice)
        sell = Price.fromString(sellPrice)
    } catch (e: Exception) {
        return "Error: Invalid price format. Use like $5.99"
    }

    // dateSold
    val soldDate = try {
        if (dateSold.isBlank()) null
        else kotlinx.datetime.LocalDate.parse(dateSold)
    } catch (e: Exception) {
        return "Error: Invalid date format. Use YYYY-MM-DD"
    }

    // Tags → Tag_T objects
    val tagList = tags.split(",", ";")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { Tag_T(it) }
        .toSet()


    // build NewProductInfo
    val npi = NewProductInfo(
        namePiece = NamePiece(
            gName = gName.trim(),
            idName = pid
        ),
        pricePiece = PricePiece(
            buyPrice = buy,
            sellPrice = sell
        ),
        tagSet = tagList,
        stockBoundary = low,
        stockPiece = StockPiece(
            totalStock = stock,
            totalSold = 0
        ),
        unitPiece = FullUnitPiece(
            unitString = unitStr.trim(),
            unitAmount = unitAmtFloat
        ),
        dateSold = soldDate
    )

    // insert into ProductDB & dbfile.json
    return try {
        ProductDB.addNewProduct(npi)
        ProductDB.saveProductsTo("files/dbfile.json")
        "Product '$gName' added successfully."
    } catch (e: Exception) {
        "Error: Failed to add product. ${e.message}"
    }

}



fun removeProduct(productName: String):String {

    val cleaned = productName.trim().lowercase()

    // find product PID by name
    val pid = ProductDB.itemMap.entries
        .firstOrNull { (_, info) ->
            info.gName.trim().lowercase() == cleaned
        }
        ?.key
        ?: return "Error: Product '$productName' not found."

    // removal
    return try {
        TagDB.setTagsOf(pid, emptySet()) // remove tags first
        ProductDB.itemMap.remove(pid)
        ProductDB.saveProductsTo("files/dbfile.json")
        "Product '$productName' removed successfully."
    } catch (e: Exception) {
        "Error: Failed to remove product. ${e.message}"
    }

}

fun editProduct(
    gName: String,
    tags: String,
    buyPrice: String,
    sellPrice: String,
    lowBound: String,
    unitStr: String,
    unitAmt: String
):String {

    // validate product name
    if (gName.isBlank())
        return "Error: product name is required."

    val cleaned = gName.trim().lowercase()
    val pid = ProductDB.itemMap.entries
        .firstOrNull { (_, info) ->
            info.gName.trim().lowercase() == cleaned
        }
        ?.key
        ?: return "Error: product '$gName' not found."

    // get product handle
    val handle = ProductDB.getHandleFor(pid)


    if (buyPrice.isNotBlank()) {
        try {
            handle.buyPrice = Price.fromString(buyPrice)
        } catch (e: Exception) {
            return "Error: Invalid Buy Price '$buyPrice'"
        }
    }

    if (sellPrice.isNotBlank()) {
        try {
            handle.sellPrice = Price.fromString(sellPrice)
        } catch (e: Exception) {
            return "Error: Invalid Sell Price '$sellPrice'"
        }
    }

    if (lowBound.isNotBlank()) {
        val low = lowBound.toIntOrNull()
            ?: return "Error: Low Bound must be a number."
        handle.stockBoundary = low
    }


    val changeUnitStr = unitStr.isNotBlank()
    val changeUnitAmt = unitAmt.isNotBlank()

    if (changeUnitStr || changeUnitAmt) {
        val finalUnitStr = if (!changeUnitStr) handle.unitString ?: ""
                            else unitStr.trim()

        val finalUnitAmt = if (!changeUnitAmt) handle.unitAmount
                            else unitAmt.toFloatOrNull()
                            ?: return "Error: Unit amount must be numeric."

        handle.setUnits(finalUnitStr, finalUnitAmt)

    }


    if (tags.isNotBlank()) {

        // parse new tags
        val newTags = tags.split(",", ";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { Tag_T(it) }
            .toSet()

        // remove old tags
        TagDB.setTagsOf(pid, emptySet())
        // add new tags
        TagDB.setTagsOf(pid, newTags)

        handle.tagSet = newTags
    }


    // commit changes
    return try {
        if (!handle.noProblems()) {
            val problems = handle.currentProblems.joinToString { it.toString() }
            return "Error: Cannot commit product edits. Problems: $problems"
        }

        handle.commit()
        ProductDB.saveProductsTo("files/dbfile.json")
        "Product '$gName' info updated successfully."
    } catch (e: Exception) {
        "Error: Failed to update product info. ${e.message}"
    }

}
