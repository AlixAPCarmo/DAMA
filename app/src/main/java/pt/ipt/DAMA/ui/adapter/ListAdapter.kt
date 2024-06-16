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

/**
 * Adapter class for RecyclerView to handle a list of items
 */
class ListAdapter(
    private val dataList: List<ItemList>,
    private val context: Context,
    private val onItemDelete: (ItemList) -> Unit
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    /**
     * ViewHolder class to hold references to the views in each item of the list
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val button: Button = view.findViewById(R.id.button)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
    }

    /**
     * Creates new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the custom layout for items
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from dataset at this position and replace the contents of the view with that element
        val item = dataList[position]
        holder.textView.text = item.name
        Glide.with(holder.itemView).load(item.imageResource).into(holder.imageView)
        holder.button.setOnClickListener { item.r.run() }

        // Show delete button only for items fetched from celestial API
        holder.deleteButton.visibility = if (item.isFromApi) View.VISIBLE else View.GONE
        holder.deleteButton.setOnClickListener { onItemDelete(item) }
    }

    /**
     * Return the size of dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataList.size
}
