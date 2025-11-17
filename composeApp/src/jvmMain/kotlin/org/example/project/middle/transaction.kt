
package middle.transaction;
import kotlinx.datetime.*;

import backend.usf.*;

import middle.price.*;
import middle.pieces.*;
import middle.typedefs.*;

class USF_TransError() : USF_Error();

// NOTE: All values are obtained from when the transaction 
// was made.
data class Transaction
	( var dateStamp   : LocalDate
	, var idName      : PID          // The product's id when the transaction was made
	, var numSold     : Int          // The value of newSales when commit() was called
	, var pricePiece  : PricePiece   // The price it was when the transaction was made
	) : IPrice by pricePiece 
{ 

	val day   : Int get() = dateStamp.day;
	val month : Int get() = dateStamp.month.number;
	val year  : Int get() = dateStamp.year;

	val revenue : Price 
		get() = numSold * pricePiece.sellPrice;

	val cost : Price 
		get() = numSold * pricePiece.buyPrice;

	val profit : Price = revenue - cost;

// Private functions, not needed.

	fun toUSF() : USF_T = 
		join(
			U_Map(mapOf(
				"dateStamp" to dateToUSF(dateStamp),
				"numSold"   to U_Int(numSold),
				"idName"    to U_String(idName.name)
			)),
			pricePieceToUSF(pricePiece)
		)

	companion object c { 
		fun fromUSF(usf : USF_T) : Transaction {
			if ( usf !is U_Map ) throw USF_TransError();

			val dateStamp = usfToDate(usf.getOrElse("dateStamp", USF_TransError()))
			val numSold   = usf.getInt("numSold", USF_TransError())

			val pricePiece = pricePieceFromUSF(usf)
			val idName     = PID(usf.getString("idName", USF_TransError()))
			// val namePiece  = namePieceFromUSF(usf)

			return Transaction(
				dateStamp   = dateStamp,
				numSold     = numSold,
				idName      = idName,
				// namePiece   = namePiece,
				pricePiece  = pricePiece
			)
		}
	}
}
