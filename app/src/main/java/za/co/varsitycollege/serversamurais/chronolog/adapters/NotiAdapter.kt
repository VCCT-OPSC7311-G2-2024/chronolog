package za.co.varsitycollege.serversamurais.chronolog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem

class NotiAdapter(private val notiList: ArrayList<NotificationItem>) : RecyclerView.Adapter<NotiAdapter.MyViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notification_list, parent, false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = notiList[position]
        holder.date.text = currentItem.date
        holder.title.text = currentItem.title
    }

    override fun getItemCount(): Int {
        return notiList.size
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        // Add your view holder code here
        val date: TextView = itemView.findViewById(R.id.dateTextView)
        val title: TextView = itemView.findViewById(R.id.projectNameView)


    }

}