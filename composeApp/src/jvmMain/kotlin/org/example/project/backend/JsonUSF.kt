
package backend;
import backend.usf.*;

import kotlinx.serialization.json.*;
import java.io.File;
typealias File = java.io.File;

fun escapeString(s:String) = 
	buildString() {
		for (char in s) {
			when (char) {
				'\"' -> append("\\\"")
				'\\' -> append("\\\\")
				else -> append(char)
			}
		}
	}

fun descapeString(s:String) =
	buildString() {
		var escape : Boolean = false;
		for (char in s) { 
			if (escape) { 
				append(char);
				escape = false;
			} else {
				when (char) {
					'\\' -> escape = true;
					else -> append(char)
				}
			}
		}
	}

fun jsonToUSF(json : JsonElement) : USF_T {
	when (json) {
		is JsonPrimitive -> 
			if (json.isString) { 
				return U_String(descapeString(json.content));
			} else {
				return U_Int(json.content.toInt());
			}


		is JsonObject ->
			return U_Map( 
				buildMap() {
					for ( (key,value) in json ) {
						put(key, jsonToUSF(value))
					}
				}
			)

		is JsonArray -> return U_List( json.map() { jsonToUSF(it) } )
	}
}

fun USFToJson(usf : USF_T) : JsonElement =(
	when (usf) {
		is U_String -> JsonPrimitive(escapeString(usf.str))
		is U_Int    -> JsonPrimitive(usf.int)
		is U_List   -> JsonArray( usf.list.map() { USFToJson(it) } )
		is U_Map    -> 
			JsonObject( 
				buildMap() {
					for ( (key,value) in usf.map ) {
						put(key, USFToJson(value))
					}
				}
			)
	})

object JsonUSF { 

	private val json : Json = Json { ignoreUnknownKeys = false }

	fun fromString(str : String) : USF_T { 
		val jsonObject : JsonElement = json.parseToJsonElement(str);
		return jsonToUSF(jsonObject);
	}

	fun toString(usf : USF_T) : String { 
		val outJson : JsonElement = USFToJson(usf);
		return json.encodeToString(outJson);
	}

	fun readFromFile(file : File) : USF_T {
		// This JSON library is crap, I can't read the json directly from 
		// a file. I *HAVE* to read it from a string. I'm guessing that's because Kotlin 
		// doesn't have an backend-independent "File" type/class, so the library can't 
		// use that type/class.
		// So, if the file is, say, over 500MiB, I have to read in ALL 500MiB just 
		// to get the JSON object from it. No streaming allowed apparently.
		val str : String = file.readText();

		return fromString(str);
	}

	fun writeToFile(file : File, usf : USF_T) {
		val outStr  : String = toString(usf);
		file.writeText(outStr);
	}
}

