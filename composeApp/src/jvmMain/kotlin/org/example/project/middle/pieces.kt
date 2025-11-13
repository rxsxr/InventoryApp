
package middle.pieces;
import middle.price.*;
import middle.typedefs.*;

import kotlinx.datetime.LocalDate;

// This includes various "pieces" of the other classes that 
// may be included in said classes or not. 
// It'll help with conversion functions.
// See the long comment at the end for details of /why/ I chose to do it this way.

interface ISalesInfo 
	{ var totalSold   : Int
	; var revenue     : Price // = sellPrice * soldAmount, total revenue from sales
	; var cost        : Price // = buyPrice  * stockAmount, total cost of the stock
	; var profit      : Price // = revenue - cost
	; var dateSold    : LocalDate?
	};
data class SalesInfo
	( override var totalSold   : Int 
	, override var revenue     : Price // = sellPrice * soldAmount, total revenue from sales
	, override var cost        : Price // = buyPrice  * stockAmount, total cost of the stock
	, override var profit      : Price // = revenue - cost
	, override var dateSold    : LocalDate? 
	) : ISalesInfo;

interface IName 
	{ var gName  : String
	; var idName : PID
	}
data class NamePiece
	( override var gName  : String
	, override var idName : PID
	) : IName;

interface IPrice { var buyPrice : Price; var sellPrice : Price }
data class PricePiece 
	( override var buyPrice  : Price
	, override var sellPrice : Price
	) : IPrice;
//

interface IStock 
	{ var totalStock : Int
	; var totalSold  : Int 
	}
data class StockPiece
	( override var totalStock : Int 
	, override var totalSold  : Int 
	) : IStock;
//




// LONG EXPLANATION 
//	Note, you probably won't need to understand /why/ I used this weird method, 
//	but I'm putting the explanation here in case you are curious. 
//
//** How it's used 
//
//	The way it's used is that a "piece" is included as 
//	a field member of a class, and then the interface part is 
//	inherited, and "deferred" to the corresponding member. For example: 
//		data class Derived
//			( var namePart  : NamePiece
//			, var pricePart : PricePiece
//			) : IName  by namePart
//			  , IPrice by pricePart
//
//	Then, I can use instances like so:
//		val test : Derived = 
//			BothPieces(
//				NamePiece("gname", "idName"), 
//				PricePiece(Price(150), Price(170))
//			)
//
//		// I can set the "gName" and "idName" properties, 
//		// as Derived inherits the interface properties in IName and IPrice
//		test.gName  = "newGName"
//		test.idName = "newIDName"
//
//		// I can also access them too
//		test.buyPrice = test.sellPrice + Price(100)
//
//	This is basically a kind of multiple-composition / multiple-inheritance.
//
//** Why I'd even do this
//	The reason is threefold: 
//		1. It allows for me to "bundle" properties together into "pieces" 
//		   that can be accessed as a whole, and 
//		2. It still allows for me to refer to the properties via the usual way
//		3. Doing this for any particular class requires as little extra effort
//		   as possible
//
//	Well, as well:
//		4. It also allows for easier conversions. 
//
//	But that's a consequence of the first reason. Suppose I had another class
//	that didn't specify prices, 
//
//		data class OtherDerived
//			( var namePart  : NamePiece
//			, var something : String
//			);
//
//	I want to convert from Derived to OtherDerived, given I also have the value of "something". 
//	I can do it more easily with these pieces:
//	
//		fun derivedToOther(der : Derived, something : String) : OtherDerived { 
//			return OtherDerived
//				( namePart  = der.namePart
//				, something = something
//				)
//		}
//
//	This may seem like a *LOT* of work for a simple conversion. But with the 
//	ProductEntry, ProductInfo, NewProductInfo, and ProductHandle classes, 
//	all needing various conversions, any way to reduce the size of those 
//	conversions helps with readability and maintenance.
//
//	I can't use an inheritance hierarchy because, well, there's no 
//	way in Kotlin to construct a subclass from a superclass by "adjoining" 
//	new fields. That is, given
//		data class Base(var x:Int);
//		data class Derived(var x:Int, var y:Int)   : Base(x);
//
//	I can't do something like:
//
//		val Bval    : Base    = Base(1);
//		val newBval : Derived = Derived( Bval, y = 4 ) 
//
//	Where "Derived( Bval, y = 4 )" instantiates "Derived" using fields already 
//	specified in "Bval". 
//
//	So, my complex method for which I have no name for seems to be the best 
//	way to accomplish this task.
