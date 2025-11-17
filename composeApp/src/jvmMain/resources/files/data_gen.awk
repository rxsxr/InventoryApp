
function randab(a, b) {
	return ( rand() * (b-a) + a )
}

function irand(n) {
	return int(n*rand());
}

function irandab(a,b) {
	return a + irand(1+b-a);
}

function round(x,n) {
	return int(n*x) / n;
}

function jsonstart() {
	if (FST_ITEM == 1) {
		FST_ITEM=0
	} else {
		printf ",\n"
	}

	printf ( INDS[I_IT] "{\n")
}

function jsonfield(fname, fform, fvalue, endofobj) {
	if (endofobj != "") {
		l_suffix = "\n"
	} else { 
		l_suffix = ",\n"
	}

	if (fform == "%s") {
		l_form="%s\"%s\":\"" fform "\"" l_suffix
	} else {
		l_form="%s\"%s\":" fform l_suffix
	}
	printf l_form, INDS[I_FD], fname, fvalue
}

function jsonend() {
	printf (INDS[I_IT] "}")
}


function inclvl() {
	IND_LVL++
}
function declvl() {
	IND_LVL--
}

function CIND() {
	return INDS[IND_LVL]
}

BEGIN {
	FS="\t"

	ISTR="  "

	IND_LVL=0

	FST_ITEM=1

	# Price Constants
	PRICE_MIN=1.0
	BPRICE_MAX=100.0
	PRICE_MAX=200.0
	MARKUP=1.2

	# "low" boundary 
	BOUNDS[0]=5
	BOUNDS[1]=400

	OUT_KIND="json"

	for (i=0; i<=4; i++) {
		if (i == 0) {
			INDS[0] = ""
		} else {
			INDS[i] = ISTR (INDS[i-1])
		}
	}

	I_IT=1 # Index for Item
	I_FD=2 # Index for Field
	I_TG=3 # Index for Tag

	if (OUT_KIND == "json") {
		printf "{\"items\":[\n"
	}
	inclvl()

	srand()
}

END {
	if (OUT_KIND == "json") {
		print "\n]}"
	}
	declvl()
}

{
	if ($1 == "") next;

	gname=$1
	id_name=$2
	tags=$3
	unitStr=$4
	if ( $5 == "" ) {
		unitAmt=randab(0.1, 6.0)
	} else {
		unitAmt=$5
	}

	buy_price=randab(PRICE_MIN, BPRICE_MAX)
	sell_price=(MARKUP + randab(-0.05, 0.05))*buy_price
	low_bnd=irandab(BOUNDS[0], BOUNDS[1]);
	stock_amount=irandab(10, 100)

	split(tags, tag_array, ",")


	switch(OUT_KIND) { 
		case "ini":
			printf "\n[%s]\n", id_name
			printf "name=\"%s\"\n", gname
			printf "tags=\"%s\"\n", tags
			printf "buy_price=%.02f\n", buy_price
			printf "sell_price=%.02f\n", sell_price
			printf "low_bound=%i\n", low_bnd
			break

		case "json":
			jsonstart()

			jsonfield("id_name", "%s", id_name)
			jsonfield("g_name", "%s", gname)

			printf INDS[I_FD] "\"tags\":\n"
			l_at_start=1
			for (i in tag_array) {
				if (l_at_start == 1) {
					printf INDS[I_TG] "["
					l_at_start=0
				} else {
					printf INDS[I_TG] ","
				}
				printf " \"%s\"\n", tag_array[i]
			}
			printf INDS[I_TG] "],\n"

			#jsonfield("buy_price", "%.02f", buy_price)
			#jsonfield("sell_price", "%.02f", sell_price)

			# 2025-11-11 11:52 
			#	The Kotlin side will use a custom Price class, so instead format 
			#	the floats into a JSON string type to be read in by that 
			#	price class
			jsonfield("buy_price", "\"$%.02f\"", buy_price)
			jsonfield("sell_price", "\"$%.02f\"", sell_price)
			jsonfield("stock_amount", "%i", stock_amount)
			jsonfield("low_bound", "%i", low_bnd)

			# 2025-11-13 15:03 Adding dateSold now
			# Generate a random date
			rngm = irandab(7, 10)

			# I don't feel like handling different month lengths, 
			# so just assume the absolute worst
			rngd = irandab(1, 29) 

			dateStr = sprintf("2025-%02d-%02d", rngm, rngd)
			jsonfield("dateSold", "%s", dateStr)

			jsonfield("unitStr", "%s", unitStr);
			jsonfield("unitAmt", "%f", unitAmt, 1);

			jsonend()
			break
	}
}




