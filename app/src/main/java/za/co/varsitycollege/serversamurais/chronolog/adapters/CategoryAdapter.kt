/*
package za.co.varsitycollege.serversamurais.chronolog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.TimeSheet
import za.co.varsitycollege.serversamurais.chronolog.model.Category

class CategoryAdapter(private val categories: ArrayList<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewCategoryName: TextView = itemView.findViewById(R.id.textViewCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewCategoryName.text = categories[position].categoryName

        holder.textViewCategoryName.setOnClickListener {
            // Handle the button click here

        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}


*/
