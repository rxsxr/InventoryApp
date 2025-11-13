

package middle;
import middle.typedefs.*;
import middle.price.*;
import kotlinx.datetime.*;

enum class HandleError_E 
	{ ETooManySales // A call to recordSales implies we sold more than we have in stock 
	};
//


sealed class HandleError : Exception();

object TooManySales : HandleError();


// Changing anything in an instance of ProductHandle will NOT 
// propagate those changes to the database. 
// Also note that these are not exactly attributes. Some may be wrapped in 
// function calls to get/set other values.
// That only happens after calling commit()
interface ProductHandle_I { 

	val currentProblems : Set<HandleError_E>;

	val info        : ProductInfo;
	var tagSet      : Set<Tag_T>;

	fun addTag(tag : Tag_T);
	fun delTag(tag : Tag_T);

	var sellPrice   : Price;
	var buyPrice    : Price;

	// NOTE: Do not change totalStock to account for sales. 
	// That is done automatically.
	// It should only be changed when accounting for new stock being bought
	var totalStock  : Int;
	val totalSold   : Int;

	// Set sellDate = null to record the current date
	// DO NOT change totalStock after this. 
	// It will be automatically changed.
	var newSales : Int;
	var sellDate : LocalDate?; // Default is null  

	// This determines at which point an item is "low" or "high" on stock
	var stockBoundary : Int;

	// This will save the changes to the DB if there aren't any problems.
	// Make sure to check that currentProblems is empty, or that 
	// noProblems() returns true!
	// If problems are found, then the call to commit() will throw 
	// a subclass of HandleError and not modify the DB. 
	fun commit();

	fun noProblems() : Boolean;
}



