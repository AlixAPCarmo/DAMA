package pt.ipt.DAMA.ui.adapter

/**
 * Data class to represent an item in the list
 */
data class ItemList(
    val name: String,
    var imageResource: String,
    val isFromApi: Boolean,
    val id: Int,
    val r: Runnable
)
