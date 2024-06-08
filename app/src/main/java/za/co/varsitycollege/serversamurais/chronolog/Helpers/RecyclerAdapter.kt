import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem

/**
 * Adapter for displaying a list of NotificationItems in a RecyclerView.
 * @param context The context in which the RecyclerView is being displayed.
 * @param data The list of NotificationItems to be displayed.
 */
class RecyclerAdapter(private val context: Context, private var data: List<NotificationItem>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_list, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Adds a new item to the data set and notifies the adapter that the data set has changed.
     * @param item The NotificationItem to be added to the data set.
     */
    fun addItem(item: NotificationItem) {
        data = data + item
        notifyDataSetChanged()
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * @param itemView The view that represents the data.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        /**
         * Binds the data to the views in the ViewHolder.
         * @param item The NotificationItem to be displayed.
         */
        fun bind(item: NotificationItem) {
            itemView.findViewById<TextView>(R.id.projectNameView).text = item.title
            itemView.findViewById<TextView>(R.id.hoursTextView).text = "${item.duration} minutes"
        }

        /**
         * Called when a view has been clicked.
         * @param view The view that was clicked.
         */
        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                Toast.makeText(context, "Clicked on item $position", Toast.LENGTH_SHORT).show()
            }
        }
    }
}