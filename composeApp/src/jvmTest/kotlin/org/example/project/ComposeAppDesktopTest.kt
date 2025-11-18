package org.example.project

import middle.*;
import middle.typedefs.*;
import middle.price.*;
import middle.pieces.*;

import backend.*;
import backend.usf.*;

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith;
import kotlin.test.*;

import kotlinx.datetime.*;

class ComposeAppDesktopTest {

	@Test
	fun example() {
		assertEquals(3, 1 + 2)
	}

	@BeforeTest
	fun resetTagDB() {
		ProductDB.reset();
		TagDB.reset();
	}

	// These test restoring to/from the DB

	fun testTagDBRestore() { 
		val origUSF : USF_T = TagDB.toUSF();
		TagDB.reset();
		TagDB.fromUSF(origUSF);

		assertEquals( TagDB.toUSF(), origUSF );
	}


	fun testFullRestore() { 
		/*
		val origUSF : USF_T = ProductDB.toUSF();
		ProductDB.reset();
		ProductDB.fromUSF(origUSF);

		assertEquals( ProductDB.toUSF(), origUSF );
		*/
	}


	fun assertTagSet(expSet : Set<String>) {
		val tagSet : Set<Tag_T> = expSet.map() { Tag_T(it) }.toSet();

		assertEquals(TagDB.getTagSet(), tagSet);

		for ( tag in tagSet ) {
			assert( TagDB.tagExists(tag) );
		}
	}

	@Test 
	fun tagDB_AddDel() {
		assertTagSet( setOf() );

		TagDB.addTag(Tag_T("tag1"));
		assertTagSet( setOf("tag1") );
		assert( TagDB.tagExists(Tag_T("tag1")) );

		testTagDBRestore();

		TagDB.delTag(Tag_T("tag1"));
		assertTagSet( setOf() );
		testTagDBRestore();

		TagDB.addTag(Tag_T("tag1")); testTagDBRestore();
		TagDB.addTag(Tag_T("tag4")); testTagDBRestore();
		TagDB.addTag(Tag_T("tag5")); testTagDBRestore();
		assertTagSet( setOf("tag1", "tag4", "tag5") );
		testTagDBRestore();
	}

	fun tsetOf(vararg strs : String) : Set<Tag_T> = 
		buildSet() {
			for (str in strs) add(Tag_T(str))
		}

	@Test 
	fun tagDB_ProdTag() {
		assertEquals(TagDB.tagsOf("p1"), tsetOf());
		TagDB.addTagProd(Tag_T("tag"), PID("p1"));
		assertEquals(TagDB.tagsOf("p1"), tsetOf("tag"));

		TagDB.addTagProd(Tag_T("tag2"), PID("p1"));
		assertEquals(TagDB.tagsOf("p1"), tsetOf("tag", "tag2"));

		TagDB.delTag("tag2");
	}

	inline fun <reified T:Throwable> parsingFails(with : List<String>) { 
		var p : Price = Price(0)
		for(str in with) {
			try {
				assertFailsWith<T>() { 
					p = fromString(str)
				}
			} catch (e:PriceError) {
				println("Price string \"${str}\" caused exception ${e} to occur");
				throw e;
			}
		}
	}

	@Test 
	fun Price_parseTest() {

		val goodList : List<String> = 
			listOf("$12.12", "$0.15", "$0.", "$0.0"
			, "$500.121", "-$0.0", "-$5.111")

		for (str in goodList) {
			try {
				val p : Price = fromString(str)
			} catch (e:PriceError) {
				println("Price string \"${str}\" caused exception ${e} to occur");
				throw e;
			}
		}

		parsingFails<PriceError.NoCurrencyMarker>(
			listOf(
				"1$.00", "1.5"
				, "1.7", "12", "1$2", "1$4$4"
			)
		)

		parsingFails<PriceError.EmptyAmount>(
			listOf( "", "  ", "   " )
		)

		parsingFails<PriceError.BadPriceString>(
			listOf( "\$abc.124", "\$.a", "\$0.-", "\$12..4", "\$1.1.1" )
		)
	}

	@Test 
	fun USF_Map_getters() { 
		val usf : U_Map = 
			U_Map(mapOf(
				"a" to U_Int(1),
				"b" to U_String("bstr"),
				"c" to U_Int(4)
			))

		assertEquals(usf.getInt("a"), 1)
		assertEquals(usf.getOrElse("a"), U_Int(1))
		assertEquals(usf.getOrElse("b"), U_String("bstr"));
	}

	@Test 
	fun Price_simpleTest() {
		var p1 : Price = Price(1200)
		var p2 : Price = 2 * p1;
		assertEquals(p2.amount, 2400)

		p1 = fromString("$12.99")
		assertEquals(p1, Price(1299))
		p1 = fromString("$6.45");
		assertEquals(p1, Price(645))
		p1 = fromString("$4")
		assertEquals(p1, Price(400))
		p1 = fromString("$0.21")
		assertEquals(p1, Price(21))

		// Truncation of extra decimal points
		p1 = fromString("$5.110")
		assertEquals(p1, Price(511))
		p1 = fromString("$5.115")
		assertEquals(p1, Price(511))
		p1 = fromString("$5.001")
		assertEquals(p1, Price(500))

		p1 = fromString("-$5.12")
		assertEquals(p1, Price(-512))
	}

	@Test 
	fun serialTests() {
		var pEntry : ProductEntry = 
			ProductEntry(
				namePiece   = NamePiece("test1",PID("test2")),
				pricePiece  = PricePiece(Price(50),Price(1200)),
				stockPiece  = StockPiece(120, 120),
				unitPiece   = UnitPiece("L", 1.0f),
				100,
				null
			);
		fun trySerial() = assertEquals(pEntry, ProductEntry.c.fromUSF(pEntry.toUSF()));

		trySerial();

		pEntry.dateSold = LocalDate.parse("2025-05-01");

		trySerial();

		pEntry.unitPiece = UnitPiece(null,null);

		trySerial();
	}

	@Test 
	fun jsonConvTest() {
		fun testUSFStr(s:String) { 
			val tmp1 = U_String(s);
			val tmp2 = USFToJson(tmp1); 
			val tmp3 = jsonToUSF(tmp2);
			assertEquals(tmp1, tmp3);
		}

		testUSFStr("abcd\\1\"")
		testUSFStr("a\\\\\\\\b\ncd\\1\"")

	}

	/*
	val dbPath : String = 
		"/mnt/arch/home/tye/Documents/university work/y4s1/Programming_Languages/final/" +
		"InventoryApp/composeApp/src/jvmMain/resources/files/"

	@Test
	fun productDBSerial() {
		val initFile : String = 
			dbPath + "./output.json"

		val dbFile   : String = 
			dbPath + "./dbfile.json"

		val transFile : String =
			dbPath + "./transFile.json"

		ProductDB.loadFromInit(initFile);

		ProductDB.saveProductsTo(dbFile);
		ProductDB.saveTransactionsTo(transFile);

	}
	*/
}
