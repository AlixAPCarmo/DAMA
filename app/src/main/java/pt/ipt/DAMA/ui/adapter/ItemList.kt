package pt.ipt.DAMA.ui.adapter

data class ItemList(
    val name: String,
    var imageResource: String,
    val isFromApi: Boolean,
    val id: Int,
    val r: Runnable
)
