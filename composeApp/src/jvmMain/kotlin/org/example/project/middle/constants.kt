

package middle.constants;

import  kotlinx.datetime.LocalDate;
import  kotlinx.datetime.format.*;

// The date format used, which is "yyyy-mm-dd".
val dateFormat = 
	LocalDate.Format {
		year()          // yyyy
		char('-')       // -
		monthNumber()   // mm
		char('-')       // -
		day()           // dd
	}

