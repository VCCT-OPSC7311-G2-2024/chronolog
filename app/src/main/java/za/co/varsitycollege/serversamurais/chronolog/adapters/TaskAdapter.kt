package za.co.varsitycollege.serversamurais.chronolog.adapters

import android.animation.ObjectAnimator
import android.os.CountDownTimer
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.NonCancellable.start
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide

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
        val imageViewTaskPhoto: ImageView = itemView.findViewById(R.id.taskPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Inflate the layout for a single task item
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task_view, parent, false)

        // Create and return a new TaskViewHolder instance
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        tasks = tasks.sortedByDescending { task -> task.date }
        val task = tasks[position]  // Assume tasks are already sorted and list is updated outside this method.

        holder.textViewTaskName.text = task.name ?: "No Name"
        holder.textViewTaskDescription.text = task.description ?: "No Description"
        holder.textViewTaskDate.text = task.date?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No Date"
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)


        // expanded view
        holder.textViewExpandedTaskName.text = task.name ?: "No Name"
        holder.textViewExpandedTaskDescription.text = task.description ?: "No Description"
        holder.textViewExpandedTaskDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.date)
        holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)

        // Set up the image loading with Glide
        Glide.with(holder.itemView.context)
            .load(task.photoUrl)
            .placeholder(R.drawable.sun)  // Consider adding a placeholder
            .into(holder.imageViewTaskPhoto)

        // Set up toggle button actions
        setupToggleButton(holder, task)

        // Handle item view click to toggle expanded/collapsed state
        holder.itemView.setOnClickListener {
            toggleDetailsVisibility(holder)
        }
    }

    private fun setupToggleButton(holder: TaskViewHolder, task: Task) {
        val toggleAction = { isExpanded: Boolean ->
            if (task.isRunning) {
                stopTimer(holder, task)
            } else {
                startTimer(holder, task)
            }
            updateView(holder, task, isExpanded)
        }

        holder.toggleTimer.setOnClickListener { toggleAction(false) }
        holder.toggleTimerExpanded.setOnClickListener { toggleAction(true) }
    }

    private fun toggleDetailsVisibility(holder: TaskViewHolder) {
        val isVisible = holder.detailsLayout.visibility == View.VISIBLE
        holder.detailsLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
        holder.summaryLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateView(holder: TaskViewHolder, task: Task, isExpanded: Boolean) {
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)
        if (isExpanded) {
            holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)
        }
        val iconRes = if (task.isRunning) R.drawable.stop else R.drawable.play_button_shape
        holder.toggleTimer.setImageResource(iconRes)
        holder.toggleTimerExpanded.setImageResource(iconRes)
    }

    private fun formatTime(secondsTotal: Int): String {
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    override fun getItemCount(): Int = tasks.size

    fun addTask(newTask: Task) {
        tasks = tasks + newTask
        notifyDataSetChanged()
    }

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



    private fun stopTimer(holder: TaskViewHolder, task: Task) {
        timers[task.taskId]?.let {
            it.cancel()
            timers.remove(task.taskId)
            task.isRunning = false
            val finalDuration = (task.duration ?: 0) + 1  // Capture the final tick if needed
            task.duration = finalDuration
            holder.textViewTaskDuration.text = formatTime(finalDuration)
            holder.textViewExpandedTaskDuration.text = formatTime(finalDuration)
            // Update Firebase with the final duration
            updateTaskDurationInFirebase(task)
        }
    }

    private fun updateTaskDurationInFirebase(task: Task) {
        val updateDuration = hashMapOf<String, Int?>("duration" to task.duration)
        firebaseHelper.updateTaskDuration(task.taskId!!, firebaseHelper.getUserId(), updateDuration)
    }
    private fun updateView(holder: TaskViewHolder, task: Task) {
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)
        holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)

        holder.toggleTimer.setImageResource(if (task.isRunning) R.drawable.stop else R.drawable.play_button_shape)
        holder.toggleTimerExpanded.setImageResource(if (task.isRunning) R.drawable.stop else R.drawable.play_button_shape)
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

