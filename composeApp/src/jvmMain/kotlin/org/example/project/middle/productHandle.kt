

package middle;
import middle.typedefs.*;
import middle.price.*;
import kotlinx.datetime.*;

enum class HandleError_E 
	{ ETooManySales   // The newSales property implies we sold more than we have in stock 
	, EMissingUnitStr // unitAmount is set, but unitString is not
	, EMissingUnitAmt // unitString is set, but unitAmount is not
	};
//

// NOTE: These are only thrown when trying to call commit() 
// with existing problems
sealed class HandleError : Exception();

class TooManySales()     : HandleError();
class MissingUnitStr()   : HandleError();
class MissingUnitAmt()   : HandleError();

// This defines the association of HandleError_E -> HandleError() instance
fun makeHandleError(prob : HandleError_E) : HandleError =
	when (prob) {
		HandleError_E.ETooManySales    -> TooManySales()
		HandleError_E.EMissingUnitStr  -> MissingUnitStr()
		HandleError_E.EMissingUnitAmt  -> MissingUnitAmt()
	}

// Changing anything in an instance of ProductHandle will NOT 
// propagate those changes to the database. 
// Also note that these are not exactly attributes. Some may be wrapped in 
// function calls to get/set other values.
// That only happens after calling commit()
interface ProductHandle_I { 

	// Collects all obvious problems into a set
	// This is calculated from the current state of the handle.
	val currentProblems : Set<HandleError_E>;

	val info        : ProductInfo;
	var tagSet      : Set<Tag_T>;

	fun addTag(tag : Tag_T);
	fun delTag(tag : Tag_T);

	// These are buy/sell prices per item, not per unit. 
	// I'm keeping it like this because this is used to compute profits
	// from sales.
	var sellPrice   : Price;
	var buyPrice    : Price;

	//	Notes about units:
	//		Some products won't have a unit attached to them. Roughly, if 
	//		this product doesn't have a unit assigned to it, it'll default 
	//		to using stock counts, item buy/sell prices, and "items" as it's 
	//		unit string.
	//	If not hasUnit: 
	//		sellPerUnit  == sellPrice
	//		buyPerUnit   == buyPrice
	//		unitAmount   == variable (can be set)
	//		unitString   == variable (can be set) 
	//	Else: 
	//		sellPerUnit == sellPrice / unitAmount
	//		buyPerUnit  == buyPrice  / unitAmount 
	//		unitAmount  == variable (can be set)
	//		unitString  == variable (can be set)
	//	For example, if milk was sold in 2L cartons at $6.00 per carton, and bought at 
	//	$5.00, then 
	//	hasUnit would be true, and 
	//		unitAmount   == 2
	//		unitString   == "L"
	//		sellPerUnit  == $6.00 / 2 = $3.00 / L
	//		buyPerUnit   == $5.00 / 2 = $2.50 / L
	//	But, if not hasUnit, then 
	//		unitAmount   == null
	//		unitString   == null
	//		sellPerUnit  == sellPrice = $6.00 
	//		buyPerUnit   == buyPrice  = $5.00
	//	So "perUnit", roughly, presumes "unitAmount" is exactly 1.
	//	Also note that hasUnit is true if and only if both 
	//	unitAmount AND unitString are set.

	// Will be true if and only if this product has a unit assigned to it.
	// That is, if and only if both unitString and unitAmount have been assigned
	val hasUnit      : Boolean;

	// These are the prices-per-unit. 
	// They are calculated from the item buy/sell price and unit amount, 
	// hence they are immutable
	val buyPerUnit   : Price;
	val sellPerUnit  : Price;

	// Shortened unit string. Default is "items" 
	var unitString   : String?;
	fun clearUnitStr(); // Unsets unitString

	// The quantity of the unit for each item. 
	var unitAmount   : Float;
	fun clearUnitAmt(); // Unsets unitAmount 

	// Useful for setting both unitString and unitAmount at once.
	fun setUnits(str : String, amount : Float);

	// Unsets any units this product has
	fun clearUnits();

	//	Notes about stock and sales: 
	//	- If sellDate is assigned a value, no matter what happens, that 
	//	  sellDate will become the new sellDate.
	//	- New sales should be "entered" into the "newSales" variable. 
	//	- To record the date of the sales, set the "sellDate" variable,
	//	  if newSales > 0, the current date is presumed to be the sell date and recorded.

	// NOTE: Do not change totalStock to account for sales. 
	// That is done automatically.
	// It should only be changed when accounting for new stock being bought
	var totalStock  : Int;
	val totalSold   : Int;

	// Set sellDate = null to record the current date
	// DO NOT change totalStock after this. 
	// It will automatically be changed.
	var newSales : Int;        // Default = true
	var sellDate : LocalDate?; // Default = null  

	// This determines at which point an item is "low" or "high" on stock
	var stockBoundary : Int;   // Default = original

	// Set this to false if you don't want a transaction being recorded 
	// for this if newSales > 0. 
	var addNewTransaction : Boolean; // Default = true

	// This will save the changes to the DB if there aren't any problems.
	// Make sure to check that currentProblems is empty, or that 
	// noProblems() returns true!
	// If problems are found, then the call to commit() will throw 
	// a subclass of HandleError and not modify the DB. 
	// If sellDate is null, the current date is obtained, and sellDate is set to that.
	// You may call this multiple times without any problems.
	fun commit();


	fun noProblems() : Boolean;
}
