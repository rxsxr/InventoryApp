
package middle.price;

sealed class PriceError : Exception()  {
// "$" wasn't found at the start or end 
object NoCurrencyMarker : PriceError();

// User entered an empty amount
object EmptyAmount      : PriceError();

object BadPriceString   : PriceError();
}

// Price represents an exact decimal. 

//   See, in Ada I could declare an exact decimal as 
//   "type Price is delta 0.01 digits 16" and be done with it. 
//   But here, I have to implement fixed-point arithmetic manually, which is 
//   tedious. And no, floats aren't good enough; floating point arithmetic is 
//   *never* exact.
data class Price(var amount : Int) {

	constructor(dollar : Int, decimal : Int) : this(100*dollar + decimal);

	operator fun plus(other : Price) : Price  = Price(amount + other.amount);

	operator fun minus(other : Price) : Price = Price(amount + other.amount);

	operator fun unaryPlus()  : Price = this ;
	operator fun unaryMinus() : Price = Price(-this.amount);

	fun toFloat() : Double = amount / 100.0;

	fun getDollarDecimal() : Pair<Int,Int> = Pair( amount / 100, amount % 100 );

	override
	fun toString() : String {
		return "$%d.%02d".format(amount / 100, amount % 100)
	}

	companion object fromString {
		operator fun invoke(inStr : String) : Price {  
			var retPrice : Price = Price(0);
			var isNegative : Boolean = false;
			var priceStr = inStr.trim();

			if (priceStr.length == 0) throw PriceError.EmptyAmount;

			// Check negative first
			if (priceStr[0] == '-') {
				isNegative = true

				// Get rid of negative sign
				priceStr = priceStr.drop(1)
			}

			priceStr = priceStr.trim()

			if (priceStr[0] != '$' && priceStr.last() != '$') {
				throw PriceError.NoCurrencyMarker;
			}

			priceStr = priceStr.trim('$')

			var parts : List<String> = priceStr.split('.')

			when ( parts.size ) {
				0 -> throw PriceError.EmptyAmount;

				1 -> // Presume the user meant to add ".00" rather than error out
				parts = listOf(parts[0], "00");

				2 -> null; // NO-OP

				else -> 
					// TODO: Make this more specific
					throw PriceError.BadPriceString;
			}

			// Now parts.size == 2
			assert(parts.size == 2);

			var dolStr = parts[0].trim();
			var decStr = parts[1].trim();

			// Check dolStr and decStr are valid

			fun badDstr(str : String) : Boolean =
			str.any() { it !in '0' .. '9' }

			if ( badDstr(dolStr) || badDstr(decStr) ) {
				throw PriceError.BadPriceString;
			}

			// Use a value of "0" if empty
			if ( dolStr.length == 0 ) dolStr = "0"
			if ( decStr.length == 0 ) decStr = "0"

			// Truncate decStr to 2 decimal places
			if (decStr.length >= 2) decStr = decStr.substring(0..1)

			// Now parse the results

			try {
				val decPart : Int = decStr.toInt();
				val dolPart : Int = dolStr.toInt();

				// It should be truncatedby now, so we shouldn't get 
				// anything more than 2 digits
				assert(0 <= decPart)
				assert(decPart < 100)

				retPrice = Price(100*dolPart + decPart)
			} catch (e: NumberFormatException) {
				// TODO: Make this more specific
				throw PriceError.BadPriceString;
			}

			// Account for negative sign if necessary
			if ( isNegative ) {
				retPrice = -retPrice;
			}

			return retPrice;
		}
	}
}

fun fromString(str : String) : Price = Price.fromString(str);

operator fun Int.times(price : Price) : Price = Price(this * price.amount);

val Zero : Price = Price(0);

// I can't use a secondary constructor without calling the primary 
// one because reasons, so I have to do this.
/*
// XXX: Should I make this use only one integer amount?
data class Price(var dollar : Int, var decimal : Int) {

	fun overflow() : Boolean {
		var ret = ( this.decimal > 100 ); // Whether we did an overflow
		this.dollar  += this.decimal / 100;
		this.decimal  = this.decimal % 100;

		return ret;
	}

	operator fun plus(other : Price) {
		applyMod( { x:Int, y:Int -> x+y }, this , other  )
		this.overflow();
	}

}

// I can't use a secondary constructor without calling the primary 
// one because reasons, so I have to do this.
fun fromString( inStr : String ) : Price {
	var retPrice : Price = Price(-1,-1);
	var priceStr = inStr.trim();

	if (priceStr[0] != '$' || priceStr.last() != '$') {
		throw PriceError.NoCurrencyMarker();
	}

	priceStr = priceStr.trim('$')

	var parts : List<String> = priceStr.split('.')

	when ( parts.size ) {
		0 -> throw PriceError.EmptyAmount();

		1 -> // Presume the user meant to add ".00" rather than error out
		parts = listOf(parts[0], "00");

		2 -> null; // NO-OP

		else -> 
			// TODO: Make this more specific
			throw PriceError.BadPriceString();
	}

	// Now parts.size == 2
	assert(parts.size == 2);

	var dolStr = parts[0];
	var decStr = parts[1];

	// Truncate decStr to 2 decimal places
	decStr = decStr.substring(0..1)

	// Now parse the results

	try {
		retPrice.dollar  = dolStr.toInt();
		retPrice.decimal = decStr.toInt();
	} catch (e: NumberFormatException) {
		// TODO: Make this more specific
		throw PriceError.BadPriceString();
	}

	return retPrice;
}

fun apply( fnc : (Int,Int) -> Int, a : Price, b : Price ) : Price {
	var newPrice : Price = Price(0,0)
	newPrice.dollar  = fnc(a.dollar , b.dollar);
	newPrice.decimal = fnc(a.decimal, b.decimal);
	return newPrice;
}

fun applyMod( fnc : (Int,Int) -> Int, a : Price, b : Price )  {
	a.dollar  = fnc(a.dollar , b.dollar);
	a.decimal = fnc(a.decimal, b.decimal);
}
*/
