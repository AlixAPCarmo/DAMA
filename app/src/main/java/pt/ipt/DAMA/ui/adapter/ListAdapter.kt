package pt.ipt.DAMA.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.DAMA.R

class ListAdapter(
    private val dataList: List<ItemList>,
    private val context: Context,
    private val onItemDelete: (ItemList) -> Unit
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val button: Button = view.findViewById(R.id.button)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.textView.text = item.name
        Glide.with(holder.itemView).load(item.imageResource).into(holder.imageView)
        holder.button.setOnClickListener { item.r.run() }

        // Show delete button only for items fetched from celestial API
        holder.deleteButton.visibility = if (item.isFromApi) View.VISIBLE else View.GONE
        holder.deleteButton.setOnClickListener { onItemDelete(item) }
    }

    override fun getItemCount() = dataList.size
}
