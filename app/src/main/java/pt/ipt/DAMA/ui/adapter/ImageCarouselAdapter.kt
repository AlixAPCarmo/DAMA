package pt.ipt.DAMA.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.DAMA.R

class ImageCarouselAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder>() {

    /**
     * ViewHolder class contains all the UI components that will be reused.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    /**
     * This method creates a new ViewHolder instance and inflates the item_image_carousel layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_carousel, parent, false)
        return ViewHolder(view)
    }

    /**
     * This method binds the data to the ViewHolder instance.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)
    }

    /**
     * This method returns the number of items in the list.
     */
    override fun getItemCount() = imageUrls.size
}
