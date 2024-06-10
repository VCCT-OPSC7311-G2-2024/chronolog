package za.co.varsitycollege.serversamurais.chronolog.Helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Notification

class RecyclerAdapter(private val context: Context, private var notifications: MutableList<Notification>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.dateTextView.text = notification.dateTime
        holder.projectNameView.text = notification.taskName
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    fun clearAllItems() {
        notifications.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val projectNameView: TextView = view.findViewById(R.id.projectNameView)
    }
}
