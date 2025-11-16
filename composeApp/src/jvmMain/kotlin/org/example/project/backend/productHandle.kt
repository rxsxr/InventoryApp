

package backend;

import kotlin.math.*;

import kotlin.enums.*;

import middle.*;
import middle.price.*;
import middle.typedefs.*;
import middle.pieces.*;
import kotlinx.datetime.*;
import kotlin.time.Clock;

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

			if (pEntry.unitStr == null && pEntry.unitAmt != null) {
				add(HandleError_E.EMissingUnitStr)
			}

			if (pEntry.unitStr != null && pEntry.unitAmt == null) {
				add(HandleError_E.EMissingUnitAmt)
			}
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
				, unitPiece  = pEntry.unitPiece
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
	val hasUnit      : Boolean  
		get() = (pEntry.unitStr != null && pEntry.unitAmt != null);

	override
	val buyPerUnit   : Price 
		get() {
			if (hasUnit) { 
				return this.buyPrice
			} else {
				val ppu:Float = this.buyPrice.amount / this.unitAmount;
				return Price(ppu.roundToInt());
			}
		}

	override
	val sellPerUnit  : Price
		get() { 
			if (hasUnit) {
				return this.sellPrice;
			} else {
				return Price((this.sellPrice.amount / this.unitAmount).roundToInt());
			}
		}

	// Shortened unit string. 
	override
	var unitString   : String?
		get() = pEntry.unitStr ?: "item"
		set(value) { pEntry.unitStr = value }

	// The quantity of the unit for each item. 
	override
	var unitAmount   : Float
		get() = pEntry.unitAmt ?: 1.0f;
		set(value) { pEntry.unitAmt = value }

	override
	fun setUnits(str : String, amount : Float) { 
		pEntry.unitPiece = UnitPiece(str, amount);
	}

	override 
	fun clearUnitAmt() { pEntry.unitAmt = null }
	override 
	fun clearUnitStr() { pEntry.unitStr = null }

	override
	fun clearUnits() { 
		clearUnitAmt();
		clearUnitStr();
	}

	override
	var totalStock : Int 
		get() = pEntry.totalStock - newSales
		set(value) { pEntry.totalStock = value }

	override
	var totalSold : Int
		get() = pEntry.totalSold + newSales
		set(value) { 
			// For checking my calculations
			val origSum  = this.totalSold + this.totalStock;

			val origSold = pEntry.totalSold;
			pEntry.totalSold = value 

			// How many did we sell? 
			val numSold = (pEntry.totalSold - origSold);
			this.totalStock = this.totalStock - numSold;

			val newSum = this.totalSold + this.totalStock;
			assert(origSum == newSum);
		}

	override
	var stockBoundary : Int
		get() = pEntry.stockBoundary 
		set(value) { pEntry.stockBoundary = value }

	override
	fun noProblems() : Boolean = this.currentProblems.isEmpty();

	override
	fun commit() {
		// Check problems
		for ( prob in enumEntries<HandleError_E>() ) {
			if ( prob in this.currentProblems ) {
				throw makeHandleError(prob)
			}
		}

		// Obtain sellDate if not set
		if (this.sellDate == null) { 
			pEntry.dateSold = Clock.System.todayIn(TimeZone.currentSystemDefault());
		}

		// Update the stock amounts
		pEntry.totalStock -= newSales;
		pEntry.totalSold  += newSales;

		ProductDB.itemMap[pEntry.idName] = pEntry;
		TagDB.setTagsOf(pEntry.idName, itagSet);
	}
}
