

package middle.typedefs;
import middle.pieces.*;
import kotlinx.datetime.LocalDate;

// These two "value class"-es are solely to make the type-checker consider
// ID_String as type-wise incompatible with Tag_T, so that they can't be
// accidentally confused with eachother. 
// Conversion to string can be done by accessing the field, like in:
//		val id  : ID_String = ID_String("myid")
//		val str : String    = id.name

// An identifier for an item
@JvmInline
value class PID(val name : String);

// A tag
@JvmInline
value class Tag_T(val tag : String);

fun Tag_T.matchesString(str : String) : Boolean { 
	return this.tag.contains(str);
}

enum class stockLevelE { Low, High };

// ProductInfo contains all the information 
// that one might put in a report for it.
// Feel free to omit whatever members you want.
data class ProductInfo
	( var namePiece    : NamePiece
	, var pricePiece   : PricePiece
	, var tagSet       : Set<Tag_T>
	, var stockPiece   : StockPiece
	, var stockLevel   : stockLevelE

	// Is null when no products have been sold yet
	, var salesInfo   : SalesInfo? 
	) : IName  by namePiece
	  , IPrice by pricePiece
	  , IStock by stockPiece

data class NewProductInfo
	( var namePiece   : NamePiece
	, var pricePiece  : PricePiece

	// Optional values
	, var tagSet        : Set<Tag_T> = setOf()
	, var stockBoundary : Int = 0
	, var stockPiece    : StockPiece = StockPiece(0,0)
	, var dateSold      : LocalDate? = null
	) : IName  by namePiece 
	  , IPrice by pricePiece
	  , IStock by stockPiece

