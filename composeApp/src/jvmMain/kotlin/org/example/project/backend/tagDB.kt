


package backend;
import middle.typedefs.*;
import backend.usf.*;

/* 
 * This manages the association between products an tags, which is complex enough 
 * to warrant it's own module
 * 
 * All it really does is manage two distinct mappings: 
 *	- From tags to sets of product ids, and 
 *	- From product ids to sets of tags
 * I /would/ express this by having TagDB_I inherit 
 * AbstractMap<Tag_T, PID> and 
 * AbstractMap<PID, Tag_T>, but IDK how to 
 * implement that "entries" property, so this'll have to do
 *
 */

class TagDB_USF : Exception();

interface TagDB_I { 
	fun reset(); // Clears the tag DB, for testing.

	fun getTagSet() : Set<Tag_T>;
	fun getTags()   : List<Tag_T>;

	fun delTag(tag : Tag_T);
	fun tagExists(tag : Tag_T) : Boolean;
	fun addTag(tag : Tag_T);

	fun addTagProd(tag : Tag_T, prod : PID);

	fun productHasTag(prod : PID, tag : Tag_T) : Boolean;

	fun productsOf(tag : Tag_T) : Set<PID>;
	fun tagsOf(prod : PID) : Set<Tag_T>;
	fun setTagsOf(prod : PID, newTagSet : Set<Tag_T>);

	fun fromUSF(usf : USF_T);
	fun toUSF  () : USF_T;

	// For testing purposes
	fun addTag(tag : String) = addTag(Tag_T(tag));
	fun delTag(tag : String) = delTag(Tag_T(tag));
	fun tagsOf(prod : String) = tagsOf(PID(prod));

}


object TagDB : TagDB_I { 
	val tagMap : MutableMap<Tag_T, MutableSet<PID>> = mutableMapOf();

	override 
	fun reset() { 
		tagMap.clear()
	}

	override
	fun getTags() : List<Tag_T> = tagMap.keys.toList();

	override 
	fun getTagSet() : Set<Tag_T> = tagMap.keys;

	override
	fun delTag(tag : Tag_T) {
		tagMap.remove(tag);
	}

	override
	fun tagExists(tag : Tag_T) : Boolean { 
		return tagMap.get(tag) != null;
	}

	override
	fun addTag(tag : Tag_T) { 
		// Don't replace tagMap.get(tag) if we shouldn't
		if (tag !in tagMap.keys) {
			tagMap.put(tag, mutableSetOf()) 
		}
	}

	override 
	fun addTagProd(tag : Tag_T, prod : PID) { 
		this.addTag(tag);
		tagMap.get(tag)!!.add(prod);
	}

	override
	fun productHasTag(prod : PID, tag : Tag_T) : Boolean {
		if ( tag !in tagMap.keys ) {
			return false;
		} else {
			return tagMap.get(tag)!!.contains(prod);
		}
	}

	override
	fun productsOf(tag : Tag_T) : Set<PID> = 
		tagMap.get(tag)?.toSet() ?: setOf();

	override
	fun tagsOf(prod : PID) : Set<Tag_T> = 
		buildSet() {
			for ((tag, pset) in tagMap) { 
				if (pset.contains(prod)) {
					add(tag)
				}
			}
		}

	override
	fun setTagsOf(prod : PID, newTagSet : Set<Tag_T>) {
		// Clear tags of product
		for ( (tag,ps) in tagMap ) {
			ps.remove(prod)
		}

		// Now add prod back to each tag in newTagSet
		for ( tag in newTagSet ) {
			this.addTag(tag);
			// We know it'll be in the tagmap now
			tagMap.get(tag)!!.add(prod)
		}
	}

	override
	fun fromUSF(usf : USF_T) {
		if (usf !is U_Map) { 
			throw TagDB_USF();
		}

		// TODO: Make this more robust
		for ((tname, u_value) in usf.map) {
			if (u_value !is U_List) { 
				throw TagDB_USF(); 
			} 

			val prodList : List<PID> = 
				buildList() {
					for (prod_item in u_value) {
						if ( prod_item !is U_String ) {
							throw TagDB_USF()
						}

						add(PID(prod_item.str))
					}
				}

			tagMap.put(Tag_T(tname), prodList.toMutableSet())
		}
	} 

	override
	fun toUSF  () : USF_T {
		val usfMap : Map<String, USF_T> = 
			buildMap() {
				for ((tv, pl) in tagMap) {
					put(tv.tag, U_List( pl.map() { U_String(it.name) } ))
				}
			}

		return U_Map(usfMap);
	}

}

