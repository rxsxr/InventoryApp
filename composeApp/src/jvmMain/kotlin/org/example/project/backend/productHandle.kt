

package backend;

import middle.*;
import middle.price.*;
import middle.typedefs.*;
import middle.pieces.*;
import kotlinx.datetime.*;

data class ProductHandle
	( var pEntry : ProductEntry 
	, var itagSet : MutableSet<Tag_T> 
	) : ProductHandle_I 
{ 
	override 
	var newSales : Int = 0;

	override
	var sellDate : LocalDate? = null;

	override 
	fun addTag(tag : Tag_T) { itagSet.add(tag) }

	override
	fun delTag(tag : Tag_T) { itagSet.remove(tag) }

	override 
	var tagSet : Set<Tag_T> 
		get() = itagSet.toSet()
		set(value) { itagSet = value.toMutableSet() }

	override
	val currentProblems : Set<HandleError_E>
		get() =
		buildSet() { 
			if (totalStock < newSales) add(HandleError_E.ETooManySales)
		}

	override
	val info : ProductInfo 
		get() =
			(ProductInfo
				( namePiece  = pEntry.namePiece
				, pricePiece = pEntry.pricePiece
				, tagSet     = pEntry.tagSet
				, stockPiece = pEntry.stockPiece
				, stockLevel = pEntry.stockLevel
				, salesInfo  = pEntry.salesInfo
				)
			)

	override
	var sellPrice : Price
		get() = pEntry.sellPrice
		set(value) { pEntry.sellPrice = value }

	override
	var buyPrice  : Price
		get() = pEntry.buyPrice
		set(value) { pEntry.buyPrice = value }

	override
	var totalStock : Int 
		get() = pEntry.totalStock
		set(value) { pEntry.totalStock = value }

	override
	var totalSold : Int
		get() = pEntry.totalSold
		set(value) { pEntry.totalSold = value }

	override
	var stockBoundary : Int
		get() = pEntry.stockBoundary 
		set(value) { pEntry.stockBoundary = value }

	override
	fun noProblems() : Boolean = this.currentProblems.isEmpty();

	override
	fun commit() {
		ProductDB.itemMap[pEntry.idName] = pEntry;
		TagDB.setTagsOf(pEntry.idName, itagSet);
	}
}
