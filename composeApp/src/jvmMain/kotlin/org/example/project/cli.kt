

package cli;
import middle.*;
import middle.typedefs.*;


val myPath : String = 
	"/mnt/arch/home/tye/Documents/university work/y4s1/" + 
	"Programming_Languages/final/data_gen/"

fun cli() {
	val initFile : String = 
	myPath + "./output.json"

	val dbFile   : String = 
	myPath + "../dbFolder/dbfile.json"

	ProductDB.loadFromInit(initFile);
	ProductDB.saveToDB(dbFile);

	/*
	while(true) {
		print("> ");
		val cline = readln().trim();
		if (cline.length == 0) continue;

		when {
			cline.startsWith("pp") -> 
				// Print products
				for ((k,prod) in ProductDB.itemMap) {
					println(k.toString() + " => " + prod.toString());
				}

			cline.startsWith("exit") -> break;
		}

	}*/
	
	for ( (k,prod) in ProductDB.itemMap ) {
		val prodStr = 
			"%-20s ".format( prod.gName ) + 
			"%s\t%s\t".format( prod.buyPrice, prod.sellPrice ) + 
			"%s\t%s\t".format( prod.totalStock, prod.totalSold ) + 
			"%s".format( prod.getDateStr() )
		println("%-16s".format(k.name) + " => " + prodStr);

	}


}
