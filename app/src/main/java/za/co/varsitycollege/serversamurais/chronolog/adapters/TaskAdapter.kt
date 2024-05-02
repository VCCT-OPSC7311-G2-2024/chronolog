package za.co.varsitycollege.serversamurais.chronolog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task

class TaskAdapter(private val tasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTaskName: TextView = itemView.findViewById(R.id.durationTextView)
        var textViewDate: TextView = itemView.findViewById(R.id.dateTextView)
        var textViewDescription: TextView = itemView.findViewById(R.id.descriptionTextView)
        var textViewDuration: TextView = itemView.findViewById(R.id.timerTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task_view, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.textViewTaskName.text = task.name
        holder.textViewDate.text = task.date.toString()
        holder.textViewDescription.text = task.description
        holder.textViewDuration.text = task.duration.toString()
    }

    override fun getItemCount() = tasks.size


}