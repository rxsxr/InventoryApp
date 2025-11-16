
package backend.usf;
import backend.*;
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

data class U_String(var str : String) : USF_T
data class U_Int   (var int : Int   ) : USF_T

data class U_List(var list : List<USF_T>) : USF_T, List<USF_T> by list;

// Base class for all USF related errors
open class USF_Error : Exception();

//object UMapBadGet : USF_Error();
class UMapBadGet(var key:String) : USF_Error() 
{ override fun toString() : String = "UMapBadGet(key=" + key + ")" }

data class U_Map (var map  : Map<String,USF_T>) : USF_T, Map<String,USF_T> by map {

	fun getOrElse(key:String, throwable:Throwable=UMapBadGet(key)) : USF_T {
		if (key !in this.map.keys) {
			return (throw throwable)
		} else {
			return this.map[key]!!;
		}
	}

	fun getString(key:String, throwable:Throwable=UMapBadGet(key)) : String =
		UCast.toString(this.getOrElse(key, throwable), throwable);

	fun getInt(key:String, throwable:Throwable=UMapBadGet(key)) : Int = 
		UCast.toInt(this.getOrElse(key,throwable), throwable);

	fun getList(key:String, throwable:Throwable=UMapBadGet(key)) : List<USF_T> = 
		UCast.toList(this.getOrElse(key,throwable), throwable);

	fun getFloat(key:String, throwable:Throwable=UMapBadGet(key)) : Float = 
		UCast.toFloat(this.getOrElse(key,throwable), throwable);

	fun getMap(key:String, throwable:Throwable=UMapBadGet(key)) : U_Map = 
		U_Map(UCast.toMap(this.getOrElse(key,throwable), throwable));

}

fun USFtoString(usf : USF_T) : String =
	buildString() {

		fun toStringRec(i_tmp : Int, cur : USF_T) {
			var indent : Int = i_tmp;
			fun indStr() : String = "  ".repeat(indent) 
			fun valPut(v : String) { append(indStr()); append(v) }
			append(indStr());

			when (cur) {
				is U_String -> valPut("U_String(\"" + cur.str + "\")\n")
				is U_Int    -> valPut("U_Int(" + cur.int.toString() + ")\n")
				is U_List   -> {
						valPut("U_List(\n");
						for ( cld in cur.list ) {
							toStringRec(indent + 1, cld)
						}
						valPut(")\n");
					}

				is U_Map    -> {
						valPut("U_Map(\n");
						indent += 1;
						for ( (k,v) in cur.map ) {
							valPut("\"" + k + "\"" + " -> \n");
							toStringRec(indent, v)
						}
						indent -= 1;
						valPut(")\n");
					}
			}
		}

		toStringRec(0, usf);
	}


// Thrown if join would clobber another map's field
class JoinOverwrite() : USF_Error();

fun join(vararg maps : U_Map) : U_Map =
	U_Map(buildMap() {
		for (map in maps) {
			for ( (k,v) in map ) {
				if ( k in this ) throw JoinOverwrite();
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

	fun toList(usf:USF_T, nonMatch:Throwable = USFCastError("List")) : List<USF_T> {
		return when (usf) { 
			is U_List -> usf.list
			else  -> throw nonMatch
		}
	}

	fun toFloat(usf:USF_T, nonMatch:Throwable = USFCastError("List")) : Float {
		return when (usf) {
			is U_Int    -> usf.int.toFloat()
			is U_String -> usf.str.toFloatOrNull() ?: throw nonMatch
			else -> throw nonMatch
		}
	}
}

fun <T> nullWrap(x:T?, body : T.() -> USF_T) : USF_T = 
	if ( x == null ) 
		UNIL
	else 
		body(x)


// Piece converters

open class PieceError(var name : String = "") : USF_Error();

class UnitPieceError() : PieceError("Unit");

fun namePieceToUSF(np : NamePiece) : U_Map =
	U_Map(mapOf(
		"gName" to U_String(np.gName),
		"idName" to U_String(np.idName.name)
	));

fun namePieceFromUSF(usf : USF_T) : NamePiece {
	if ( usf !is U_Map ) throw PieceError();

	val gName  : String = usf.getString("gName", PieceError());
	val idName : String = usf.getString("idName", PieceError());

	return NamePiece(
		  gName  = gName
		, idName = PID(idName)
		)
}

fun pricePieceToUSF(pp : PricePiece) : U_Map =
	U_Map(mapOf(
		"buyPrice"  to U_Int(pp.buyPrice.amount ),
		"sellPrice" to U_Int(pp.sellPrice.amount)
	));

fun pricePieceFromUSF(usf : USF_T) : PricePiece {
	if ( usf !is U_Map ) throw PieceError();
	return PricePiece (
		  buyPrice  = Price(usf.getInt("buyPrice", PieceError()))
		, sellPrice = Price(usf.getInt("sellPrice", PieceError()))
		)
}

fun stockPieceToUSF(sp : StockPiece) : U_Map =
	U_Map(mapOf(
		  "totalStock" to U_Int(sp.totalStock)
		, "totalSold" to U_Int(sp.totalSold)
	))

fun stockPieceFromUSF(usf : USF_T) : StockPiece {
	if ( usf !is U_Map ) throw PieceError();
	return StockPiece(
			  totalStock  = usf.getInt("totalStock", PieceError())
			, totalSold   = usf.getInt("totalSold", PieceError())
		)
}

fun unitPieceToUSF(up : UnitPiece) : U_Map = 
	U_Map(mapOf( 
		"unitPiece" to 
		U_Map(mapOf(
			  "unitStr" to nullWrap(up.unitStr) { U_String(this) }
			, "unitAmt" to nullWrap(up.unitAmt) { U_String(this.toString()) }
		))
	));

fun unitPieceFromUSF(usf:USF_T) : UnitPiece {
	if ( usf !is U_Map ) throw UnitPieceError();
	val upUsf : USF_T = usf.getOrElse("unitPiece", UnitPieceError());
	if ( upUsf !is U_Map ) throw UnitPieceError();

	println(USFtoString(usf));
	val U_unitStr : String = upUsf.getString("unitStr");
	val U_unitAmt : String = upUsf.getString("unitAmt");

	val unitStr : String? = if (U_unitStr==NILs) null else U_unitStr;
	val unitAmt : Float?  = if (U_unitAmt==NILs) null else U_unitAmt.toFloat();

	return UnitPiece(unitStr, unitAmt);
}
