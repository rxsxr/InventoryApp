
package middle;
import middle.typedefs.*;
import middle.price.*;
import middle.pieces.*;
import middle.constants.*;
import middle.transaction.*;

import backend.*;
import backend.usf.*;

import kotlinx.serialization.json.*;
import kotlinx.datetime.LocalDate;


sealed class ProductDBErrors : Exception();

class ItemNotFound(id:PID)      : ProductDBErrors();
class ItemAlreadyExists(id:PID) : ProductDBErrors();

// Implementation is named ProductDB. 
// Only use functions present in the interface
interface ProductDB_I { 
	fun hasProductID(item : PID) : Boolean;

	// Call this to get a handle for a product item. 
	// Modifying that handle will not change the DB until its 
	// .commit() method is called. 
	fun getHandleFor(item : PID) : ProductHandle_I;

	// Will be updated when sales are recorded
	val transactionList : List<Transaction>;

	fun getTransactionsFor(item : PID) : List<Transaction>;

	// For if you want to manually add a transaction.
	// - updateEntries determines if the corresponding ProductEntry should 
	//   be updated as well, but feel free to ignore if desired. 
	// NOTE: ProductHandle does this automatically, so you shouldn't /have/ to
	//   call this. If you want it to behave like you got a handle then added 
	//   the transaction, set updateEntries = true.
	fun addNewTransaction(trans : Transaction, updateEntries : Boolean = false); 

	// fun addNewTransactionE(trans : Transaction, updateEntries : Boolean);

	// This was what I was going to add, but apparently I can't override it. 
	//fun addNewTransaction(trans : Transaction, updateEntries : Boolean = false);

	// Shortcut for doing the above and then getting the info property
	fun getInfoFor(item : PID) : ProductInfo;

	fun addNewProduct(npi : NewProductInfo);

	fun getTagsMatching(str : String) : Set<Tag_T>;

	// Bulk tag operations 
	fun addTagProducts(tag : Tag_T, products : Iterable<PID>);

	fun loadFromInit(fname : String);

	fun saveToDB(fname : String);
	fun loadFromDB(fname : String);
}


object ProductDB : ProductDB_I { 

	// NOTE: Do NOT set this map in the front-end. It's only non-private 
	// so that a product handle can modify it.
	var itemMap : MutableMap<PID, ProductEntry> = mutableMapOf()

	override
	var transactionList : MutableList<Transaction> = mutableListOf();

	// NOTE: Also, don't call this, it's used for running the unit tests, namely 
	//  to test if I can completely restore the DB after:
	//		1. Getting a USF_T representation of the DB, 
	//		2. Resetting it, 
	//		3. Then loading it back from the USF_T, 
	//		4. Then finally comparing the original USF to what this DB gives back.
	//		And finally, all that inbetween a bunch of changes. If I lost information 
	//		at any point, the tests will show that.
	fun reset() { 
		itemMap.clear();
		TagDB.reset();
	}

	override
	fun hasProductID(item : PID) : Boolean = item in itemMap;

	override 
	fun getHandleFor(item : PID) : ProductHandle {
		return (
			ProductHandle
				( pEntry  = itemMap[item]!!.copy()
				, itagSet = TagDB.tagsOf(item).toMutableSet()
				)
		);
	}

	// Shortcut for doing the above and then getting the info property
	override
	fun getInfoFor(item : PID) : ProductInfo {
		if (! hasProductID(item)) { 
			throw ItemNotFound(item);
		} else {
			return itemMap[item]!!.toInfo()
		}
	}

	override
	fun addNewProduct(npi : NewProductInfo) {
		if (hasProductID(npi.idName)) {
			throw ItemAlreadyExists(npi.idName);
		} else {
			itemMap[npi.idName] = ProductEntry.fromNewPInfo(npi);
		}
	}

	override
	fun getTagsMatching(str : String) : Set<Tag_T> {
		return TagDB.getTagSet().filter() { it.matchesString(str) }.toSet();
	}

	// Bulk tag operations 
	override
	fun addTagProducts(tag : Tag_T, products : Iterable<PID>) {
		for (prod in products) {
			TagDB.addTagProd(tag, prod);
		}
	}

	// 
	//== BEGIN Transactions 
	//

	override 
	fun addNewTransaction(trans : Transaction, updateEntries : Boolean) {
		if (updateEntries) { 
			// Just defer this to the ProductHandle, rather than trying to 
			// replicate what it would do.
			val handle : ProductHandle = getHandleFor(trans.idName);

			handle.newSales = trans.numSold;
			handle.sellDate = trans.dateStamp;
			handle.commit();

		} else { 
			transactionList.add(trans);
		}
	}

	/*
	override 
	fun addNewTransaction(trans : Transaction) {
		_addNewTransaction(trans, false);
	}

	override
	fun addNewTransaction(trans : Transaction, updateEntries : Boolean) {
		_addNewTransaction(trans, updateEntries);
	}
	*/

	override
	fun getTransactionsFor(item : PID) : List<Transaction> = 
		transactionList.filter() { it.idName == item };

	//
	//== END Transactions 
	//

	class InitParseError() : Exception();

	private val tagKey   : String = "tagDB";
	private val prodKey  : String = "productDB";
	private val transKey : String = "transList";

	override
	fun loadFromInit(fname : String) {// {{{
		val initUSF : USF_T = JsonUSF.readFromFile(File(fname));
		//println(initUSF.toString());
		val itemKey  : String = "items"
		val transKey : String = "trans"

		// Process items
		if (initUSF !is U_Map       ) throw InitParseError();
		if (itemKey !in initUSF.keys) throw InitParseError();
	
		//println( initUSF.map[itemKey] );
		val items : List<USF_T> = initUSF.getList(itemKey, InitParseError());

		for (item in items) {
			if ( item !is U_Map ) throw InitParseError();

			// Extract fields
			val gName    : String = item.getString("g_name", InitParseError());
			val idName   : PID    = PID(item.getString("id_name", InitParseError()));

			val tags : List<Tag_T> = item.getList("tags", InitParseError()).map() { Tag_T(UCast.toString(it)) }
			val buyPrice  : Price = Price.fromString(UCast.toString(item["buy_price"]!! , nonMatch=InitParseError()));
			val sellPrice : Price = Price.fromString(UCast.toString(item["sell_price"]!!, nonMatch=InitParseError()));

			val stockAmount : Int = item.getInt("stock_amount", InitParseError()); 
			val stockBound  : Int = UCast.toInt(item["low_bound"]!!, nonMatch=InitParseError());

			val unitString : String = item.getString("unitStr", InitParseError());
			val unitAmount : Float  = item.getFloat("unitAmt", InitParseError());

			val dateSold : LocalDate = dateFormat.parse( item.getString("dateSold", InitParseError()) );

			// Add tags first
			for (tag in tags)  {
				TagDB.addTagProd(tag, idName);
			}

			// Add new product entry
			ProductDB.addNewProduct(
				NewProductInfo(
					  namePiece  = NamePiece(gName, idName)
					, pricePiece = PricePiece(buyPrice, sellPrice)
					, tagSet = tags.toSet()
					, stockPiece = StockPiece(stockAmount, 0)
					, stockBoundary = stockBound
					, dateSold = dateSold
					, unitPiece = FullUnitPiece(unitString, unitAmount)
					)
			)
		}

		val transList : List<USF_T> = initUSF.getList(transKey, InitParseError());

		for (trans in transList) {
			transactionList.add( Transaction.fromUSF(trans) );
		}

	}// }}}

	private val json : Json = Json { ignoreUnknownKeys = false }

	sealed class DBLoadError : USF_Error();

	class TagLoadError()       : DBLoadError();
	class ProductLoadError()   : DBLoadError();
	class TransLoadError()     : DBLoadError();
	class BothLoadError()      : DBLoadError();

	fun toUSF() : USF_T {// {{{
		val itemMapPart : USF_T = 
			U_Map(
			buildMap() {
				for ( (k,v) in itemMap ) {
					put(k.name, v.toUSF())
				}
			})

		val transPart  : USF_T = 
			U_List( transactionList.map() { it.toUSF() } );

		val tagMapPart : USF_T = TagDB.toUSF();

		return U_Map(mapOf(
				tagKey    to tagMapPart,
				prodKey   to itemMapPart,
				transKey  to transPart
			))
	}// }}}

	fun fromUSF(usf : USF_T) {// {{{
		if (usf !is U_Map) throw BothLoadError();

		val tagUSF    : USF_T = usf.getOrElse(tagKey, TagLoadError());
		val prodUSF   : USF_T = usf.getOrElse(prodKey, ProductLoadError());
		val transUSF  : USF_T = usf.getOrElse(transKey, TransLoadError());

		// Success is not here yet, we still need to load 
		// the USFs 

		try { 
			TagDB.fromUSF(tagUSF);
		} catch (e:TagDB_USF) {
			println("Tag DB load error occured");
			throw TagLoadError();
		}

		if (prodUSF !is U_Map) throw ProductLoadError();
		for ( (uk,uv) in prodUSF.map ) {
			val k = PID( uk )
			val v = ProductEntry.c.fromUSF(uv);
			itemMap[k] = v;
		}

		if (transUSF !is U_List) throw TransLoadError()
		for ( t in transUSF ) {
			var tt:Transaction? = null;
			try {
				tt = Transaction.fromUSF(t);
			} catch (e:USF_Error) {
				println("USF value " + t.toString() + " failed to convert to Transaction");
				println("Error was " + e.toString() );
				throw TransLoadError();
			} 

			// Should never be non-null by now
			transactionList.add(tt!!);
		}
	}// }}}

	override
	fun saveToDB(fname : String) {
		val writeUSF : USF_T = this.toUSF();
		JsonUSF.writeToFile(File(fname), writeUSF);
	}

	override 
	fun loadFromDB(fname : String) {
		val usf : USF_T = JsonUSF.readFromFile(File(fname));
		this.fromUSF(usf);
	}
}
