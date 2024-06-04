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

/**
 * Adapter for the RecyclerView that displays tasks.
 * @property tasks List of tasks to be displayed.
 * @property firebaseHelper Helper for Firebase operations.
 */
class TaskAdapter(private var tasks: List<Task>, private var firebaseHelper: FirebaseHelper) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private val timers = HashMap<String?, CountDownTimer>()

    /**
     * ViewHolder for a single task item.
     * @property itemView The root view of the task item.
     */
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

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task_view, parent, false)
        return TaskViewHolder(itemView)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        tasks = tasks.sortedByDescending { task -> task.date }
        val task = tasks[position]

        holder.textViewTaskName.text = task.name ?: "No Name"
        holder.textViewTaskDescription.text = task.description ?: "No Description"
        holder.textViewTaskDate.text = task.date?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No Date"
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)

        holder.textViewExpandedTaskName.text = task.name ?: "No Name"
        holder.textViewExpandedTaskDescription.text = task.description ?: "No Description"
        holder.textViewExpandedTaskDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.date)
        holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)

        Glide.with(holder.itemView.context)
            .load(task.photoUrl)
            .placeholder(R.drawable.sun)
            .into(holder.imageViewTaskPhoto)

        setupToggleButton(holder, task)

        holder.itemView.setOnClickListener {
            toggleDetailsVisibility(holder)
        }
    }

    /**
     * Sets up the toggle button for the task timer.
     * @param holder The ViewHolder for the task.
     * @param task The task.
     */
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

    /**
     * Toggles the visibility of the task details.
     * @param holder The ViewHolder for the task.
     */
    private fun toggleDetailsVisibility(holder: TaskViewHolder) {
        val isVisible = holder.detailsLayout.visibility == View.VISIBLE
        holder.detailsLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
        holder.summaryLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Updates the view of the task.
     * @param holder The ViewHolder for the task.
     * @param task The task.
     * @param isExpanded Whether the task details are expanded.
     */
    private fun updateView(holder: TaskViewHolder, task: Task, isExpanded: Boolean) {
        holder.textViewTaskDuration.text = formatTime(task.duration ?: 0)
        if (isExpanded) {
            holder.textViewExpandedTaskDuration.text = formatTime(task.duration ?: 0)
        }
        val iconRes = if (task.isRunning) R.drawable.stop else R.drawable.play_button_shape
        holder.toggleTimer.setImageResource(iconRes)
        holder.toggleTimerExpanded.setImageResource(iconRes)
    }

    /**
     * Formats the given time in seconds to a string in the format "HH:mm:ss".
     * @param secondsTotal The time in seconds.
     * @return The formatted time string.
     */
    private fun formatTime(secondsTotal: Int): String {
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = tasks.size

    /**
     * Adds a new task to the list and notifies the adapter of the data set change.
     * @param newTask The new task to be added.
     */
    fun addTask(newTask: Task) {
        tasks = tasks + newTask
        notifyDataSetChanged()
    }

    /**
     * Starts the timer for the task.
     * @param holder The ViewHolder for the task.
     * @param task The task.
     */
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

    /**
     * Stops the timer for the task.
     * @param holder The ViewHolder for the task.
     * @param task The task.
     */
    private fun stopTimer(holder: TaskViewHolder, task: Task) {
        timers[task.taskId]?.let {
            it.cancel()
            timers.remove(task.taskId)
            task.isRunning = false
            val finalDuration = (task.duration ?: 0) + 1
            task.duration = finalDuration
            holder.textViewTaskDuration.text = formatTime(finalDuration)
            holder.textViewExpandedTaskDuration.text = formatTime(finalDuration)
            updateTaskDurationInFirebase(task)
        }
    }

    /**
     * Updates the duration of the task in Firebase.
     * @param task The task.
     */
    private fun updateTaskDurationInFirebase(task: Task) {
        val updateDuration = hashMapOf<String, Int?>("duration" to task.duration)
        firebaseHelper.updateTaskDuration(task.taskId!!, firebaseHelper.getUserId(), updateDuration)
    }

    /**
     * Filters the tasks by a date range and notifies the adapter of the data set change.
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     */
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