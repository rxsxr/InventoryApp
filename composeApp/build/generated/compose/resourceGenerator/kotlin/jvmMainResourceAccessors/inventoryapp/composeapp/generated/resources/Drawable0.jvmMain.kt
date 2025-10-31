@file:OptIn(InternalResourceApi::class)

package inventoryapp.composeapp.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/inventoryapp.composeapp.generated.resources/"

internal val Res.drawable.compose_multiplatform: DrawableResource by lazy {
      DrawableResource("drawable:compose_multiplatform", setOf(
        ResourceItem(setOf(), "${MD}drawable/compose-multiplatform.xml", -1, -1),
      ))
    }

internal val Res.drawable.myiconpack: DrawableResource by lazy {
      DrawableResource("drawable:myiconpack", setOf(
        ResourceItem(setOf(), "${MD}drawable/myiconpack", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectJvmMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("compose_multiplatform", Res.drawable.compose_multiplatform)
  map.put("myiconpack", Res.drawable.myiconpack)
}
