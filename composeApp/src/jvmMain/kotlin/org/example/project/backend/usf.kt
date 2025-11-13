
package backend.usf;
import middle.pieces.*;
import middle.typedefs.*;
import middle.price.*;

// Universal Serial Format
// An ADT that allows for the actual database format to change without
// changing the API
// It also allows me to write the DB save/load code in an API-independent way.

// As well, IDK how to use kotlix.serialization's actual serialization, which 
// means I gotta serialize everything the hard way. 

sealed interface USF_T;

data class U_String(var str : String) : USF_T;
data class U_Int   (var int : Int)    : USF_T;

data class U_List(var list : List<USF_T>) : USF_T, List<USF_T> by list;

data class U_Map (var map  : Map<String,USF_T>) : USF_T, Map<String,USF_T> by map;

// Base class for all USF related errors
open class USF_Error : Exception();

// Thrown if join would clobber another map's field
object JoinOverwrite : USF_Error();

fun join(vararg maps : U_Map) : U_Map =
	U_Map(buildMap() {
		for (map in maps) {
			for ( (k,v) in map ) {
				if ( k in this ) throw JoinOverwrite;
				put(k,v)
			}
		}
	})


// Type casts

class USFCastError(var type: String) : USF_Error();

object UCast { 
	fun toInt(usf:USF_T, nonMatch:Throwable = USFCastError("Int")) : Int = 
		when (usf) { 
			is U_Int -> usf.int
			else -> throw nonMatch
		}

	fun toString(usf:USF_T, nonMatch:Throwable = USFCastError("String")) : String = 
		when (usf) { 
			is U_String -> usf.str
			else  -> throw nonMatch 
		}

	fun toMap(usf:USF_T, nonMatch:Throwable = USFCastError("Map")) : Map<String,USF_T> = 
		when (usf) { 
			is U_Map -> usf.map
			else  -> throw nonMatch
		}

	fun toList(usf:USF_T, nonMatch:Throwable = USFCastError("List")) : List<USF_T> = 
		when (usf) { 
			is U_List -> usf.list
			else  -> throw nonMatch
		}


}

object UMapBadGet : USF_Error();

fun U_Map.getOrElse(key:String, throwable:Throwable=UMapBadGet) : USF_T =
	if (key !in this) (throw throwable) else this[key]!!;

fun U_Map.getString(key:String, throwable:Throwable=UMapBadGet) : String =
	UCast.toString(this.getOrElse(key, throwable), throwable);

fun U_Map.getInt(key:String, throwable:Throwable=UMapBadGet) : Int = 
	UCast.toInt(this.getOrElse(key,throwable), throwable);



// Piece converters

object PieceError : USF_Error();

fun namePieceToUSF(np : NamePiece) : U_Map =
	U_Map(mapOf(
		"gName" to U_String(np.gName),
		"idName" to U_String(np.idName.name)
	));

fun namePieceFromUSF(usf : USF_T) : NamePiece {
	if ( usf !is U_Map ) throw PieceError;
	return NamePiece(
		  gName  = usf.getString("gName", PieceError)
		, idName = PID(usf.getString("idName", PieceError))
		)
}

fun pricePieceToUSF(pp : PricePiece) : U_Map =
	U_Map(mapOf(
		"buyPrice"  to U_Int(pp.buyPrice.amount ),
		"sellPrice" to U_Int(pp.sellPrice.amount)
	));

fun pricePieceFromUSF(usf : USF_T) : PricePiece {
	if ( usf !is U_Map ) throw PieceError;
	return PricePiece (
		  buyPrice  = Price(usf.getInt("buyPrice", PieceError))
		, sellPrice = Price(usf.getInt("sellPrice", PieceError))
		)
}

fun stockPieceToUSF(sp : StockPiece) : U_Map =
	U_Map(mapOf(
		"totalStock" to U_Int(sp.totalStock)
		, "totalSold" to U_Int(sp.totalSold)
	))

fun stockPieceFromUSF(usf : USF_T) : StockPiece {
	if ( usf !is U_Map ) throw PieceError;
	return StockPiece(
			  totalStock  = usf.getInt("totalStock", PieceError)
			, totalSold   = usf.getInt("totalSold", PieceError)
		)
}


