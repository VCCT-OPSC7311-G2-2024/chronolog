package za.co.varsitycollege.serversamurais.chronolog.adapters

import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private var tasks: List<Task>, private var firebaseHelper: FirebaseHelper) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private val timers = HashMap<String?, CountDownTimer>()
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTaskName: TextView = itemView.findViewById(R.id.recentTaskNameTextView)
        val textViewTaskDescription: TextView = itemView.findViewById(R.id.recentDescriptionTextView)
        val textViewTaskDate: TextView = itemView.findViewById(R.id.recentDateTextView)
        val textViewTaskDuration: TextView = itemView.findViewById(R.id.recentDurationTextView)
        val toggleTimer: ImageButton = itemView.findViewById(R.id.toggleTimer)
        val summaryLayout: LinearLayout = itemView.findViewById(R.id.summaryLayout)


        val detailsLayout: LinearLayout = itemView.findViewById(R.id.detailLayout)
        val textViewExpandedTaskName: TextView = itemView.findViewById(R.id.recentTaskNameExpandedTextView)
        val textViewExpandedTaskDescription: TextView = itemView.findViewById(R.id.recentDescriptionExpandedTextView)
        val textViewExpandedTaskDate: TextView = itemView.findViewById(R.id.recentDateExpandedTextView)
        val textViewExpandedTaskDuration: TextView = itemView.findViewById(R.id.recentDurationExpandedTextView)
        val toggleTimerExpanded: ImageButton = itemView.findViewById(R.id.toggleTimerExpanded)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task_view, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.textViewTaskName.text = task.name ?: "No Name"
        holder.textViewTaskDescription.text = task.description ?: "No Description"
        holder.textViewTaskDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.date)
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)

        // expanded view
        holder.textViewExpandedTaskName.text = task.name ?: "No Name"
        holder.textViewExpandedTaskDescription.text = task.description ?: "No Description"
        holder.textViewExpandedTaskDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.date)
        holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)

        holder.toggleTimer.setOnClickListener {
            if (task.isRunning) {
                stopTimer(task)
                task.duration = (task.duration ?: 0) + (holder.textViewTaskDuration.text.toString().toIntOrNull() ?: 0)
                val updateDuration = hashMapOf<String, Int?>(
                    "duration" to task.duration
                )
                firebaseHelper.updateTaskDuration(task.taskId!!, firebaseHelper.getUserId(), updateDuration)
                updateView(holder, task)
            } else {
                startTimer(holder, task)
                updateView(holder, task)
            }
        }

        holder.toggleTimerExpanded.setOnClickListener {
            if (task.isRunning) {
                stopTimer(task)
                task.duration = (task.duration ?: 0) + (holder.textViewTaskDuration.text.toString().toIntOrNull() ?: 0)
                val updateDuration = hashMapOf<String, Int?>(
                    "duration" to task.duration
                )
                firebaseHelper.updateTaskDuration(task.taskId!!, firebaseHelper.getUserId(), updateDuration)
                updateView(holder, task)
            } else {
                startTimer(holder, task)
                updateView(holder, task)
            }
        }



        holder.itemView.setOnClickListener {
            val visible = holder.detailsLayout.visibility == View.VISIBLE
            holder.detailsLayout.visibility = if (visible) View.GONE else View.VISIBLE
            holder.summaryLayout.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int = tasks.size

    private fun startTimer(holder: TaskViewHolder, task: Task) {

            val timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val newDuration = (task.duration ?: 0) + 1
                    task.duration = newDuration

                    holder.textViewTaskDuration.text = formatTime(newDuration)
                    holder.textViewExpandedTaskDuration.text = formatTime(newDuration)
                }

                override fun onFinish() {}
            }
            timer.start()
            timers[task.taskId] = timer
            task.isRunning = true

    }


    private fun stopTimer(task: Task) {
        timers[task.taskId]?.cancel()
        timers.remove(task.taskId)
        task.isRunning = false
    }

    private fun updateView(holder: TaskViewHolder, task: Task) {
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)
        holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)

        holder.toggleTimer.setImageResource(if (task.isRunning) R.drawable.stop else R.drawable.play_button_shape)
        holder.toggleTimerExpanded.setImageResource(if (task.isRunning) R.drawable.stop else R.drawable.play_button_shape)
    }

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return String.format("%02d:%02d", hours, remainingMinutes)
    }

    fun filterByDateRange(startDate: String, endDate: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)
            tasks = tasks.filter { task ->
                val taskDate = task.date
                taskDate != null && !taskDate.before(start) && !taskDate.after(end)
            }
            notifyDataSetChanged()
        } catch (e: ParseException) {
            Log.e("TaskAdapter", "Failed to parse date: ${e.message}")
        }
    }
}

