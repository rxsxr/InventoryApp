
package backend;

import  kotlinx.datetime.LocalDate;
import  kotlinx.datetime.format.*;

import backend.usf.*;

import middle.constants.dateFormat;
import middle.typedefs.*;
import middle.pieces.*;
import middle.price.*;

data class ProductEntry
	( var namePiece      : NamePiece
	, var pricePiece     : PricePiece
	, var stockPiece     : StockPiece
	, var stockBoundary  : Int
	, var dateSold       : LocalDate?
	) : IName by namePiece 
	  , IPrice by pricePiece
	  , IStock by stockPiece
{
	val stockLevel : stockLevelE
		get() =
			if (this.totalStock > stockBoundary) 
				stockLevelE.High
			else
				stockLevelE.Low
	//

	var tagSet : Set<Tag_T>
		get() = TagDB.tagsOf(idName)
		set(value) {
			TagDB.setTagsOf(idName, value)
		}

	val salesInfo : SalesInfo
		get() = _getSalesInfo();

	// NOTE: This needs an underscore, since apparently the "get()" for the above "salesInfo" property
	//	is apparently compiled to "getSalesInfo()", which would clash with the below if the underscore 
	//	were removed
	fun _getSalesInfo() : SalesInfo {
		val revenue   : Price      = totalSold  * sellPrice;
		val cost      : Price      = totalStock * buyPrice;
		val profit    : Price      = revenue - cost;
		val dateSold  : LocalDate? = dateSold

		return (
			SalesInfo
				( revenue   = revenue
				, cost      = cost
				, profit    = profit
				, totalSold = totalSold
				, dateSold  = dateSold
				))
	}

	fun toInfo() : ProductInfo = 
		(ProductInfo
			( namePiece   = this.namePiece
			, pricePiece  = this.pricePiece
			, tagSet      = this.tagSet
			, stockPiece  = this.stockPiece
			, stockLevel  = this.stockLevel
			, salesInfo   = this.salesInfo
			)
		)

	fun getDateStr() : String { 
		if (dateSold != null) { 
			return dateFormat.format(dateSold!!);
		} else { 
			return NILs;
		}
	}

	// Okay, so my hack to get a companion function won't work, as 
	// apparently only one companion object is allowed per class, because 
	// the kotlin designers love arbitrary restrictions. 
	//	I shudder to consider what other breakages would occur if 
	//	you added inheritance to this. 
	
	//	Actually, I tested that, and it's worse than I thought. Sub-classes 
	//	just clobber the super-class's companion object. It's as if the name 
	//	of the companion object is entirely meaningless, and all that matters is 
	//	that it exists. I still don't understand why they even have names for it, 
	//	then. Giving it a name is just misleading design; it makes you treat it 
	//	like an identifier, when it never was in the first place.

	// Anyways, to make it as painless as possible, make each companion object 
	// just have the identifier "c".
	companion object c {
		object FromUSFError : Exception();
		fun fromNewPInfo(npi : NewProductInfo) : ProductEntry { 
			// Setup TagDB
			TagDB.setTagsOf(npi.idName, npi.tagSet)

			return ( ProductEntry
				( namePiece = npi.namePiece
				, pricePiece = npi.pricePiece
				, stockPiece = 
					(StockPiece 
						( totalStock = npi.totalStock
						, totalSold  = npi.totalSold
						)
					)
				, stockBoundary = npi.stockBoundary
				, dateSold   = npi.dateSold
				))
		}

		fun fromUSF(usf : USF_T) : ProductEntry {
			if ( usf !is U_Map ) throw FromUSFError;

			val namePiece  = namePieceFromUSF(usf);
			val pricePiece = pricePieceFromUSF(usf);
			val stockPiece = stockPieceFromUSF(usf);

			val stockBound  = usf.getInt("stockBound", FromUSFError);

			val dateSoldStr = usf.getString("dateSold", FromUSFError);
			val dateSold : LocalDate?  = 
				if (dateSoldStr == NILs) 
					null
				else 
					dateFormat.parse(dateSoldStr);

			return ProductEntry(
					  namePiece     = namePiece
					, pricePiece    = pricePiece
					, stockPiece    = stockPiece
					, stockBoundary = stockBound
					, dateSold      = dateSold
					)
		}
	}

	fun toUSF() : USF_T =
		join( 
			namePieceToUSF(namePiece),
			pricePieceToUSF(pricePiece),
			stockPieceToUSF(stockPiece),
			U_Map(mapOf(
				"stockBound" to U_Int(stockBoundary),
				"dateSold"   to U_String(this.getDateStr() )
			))
		)

}
