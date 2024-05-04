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
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide

class CategoryAdapter(private var tasks: List<Task>, private var startDate: Date?, private var endDate: Date? ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private val timers = HashMap<String?, CountDownTimer>()
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryName)
        val categoryDurationTextView: TextView = itemView.findViewById(R.id.categoryTotalDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Inflate the layout for a single task item
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category_view, parent, false)

        // Create and return a new TaskViewHolder instance
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        val (categories, durations) = calculateTotalDurationsByCategory(tasks, startDate, endDate)
        if (position < categories.size && position < durations.size) {
            holder.categoryNameTextView.text = categories[position]
            holder.categoryDurationTextView.text = "Duration:\n" + formatTime(durations[position])
        }


    }

    fun calculateTotalDurationsByCategory(tasks: List<Task>, startDate: Date?, endDate: Date?): Pair<List<String>, List<Int>> {

        if(startDate == null || endDate == null){
            val categoryDurations = tasks
                .filter { it.category != null }
                .groupBy { it.category!! }
                .mapValues { (_, tasks) ->
                    tasks.sumOf { it.duration }
                }
            return Pair(categoryDurations.keys.toList(), categoryDurations.values.toList())
        }

        val formatter = SimpleDateFormat("yyyyMMdd")  // Ensure the date format matches your task date format
        val start = formatter.format(startDate).toInt()
        val end = formatter.format(endDate).toInt()


        val categoryDurations = tasks
            .filter { it.category != null && it.date != null }
            .filter{
                val taskDate = formatter.format(it.date).toInt()
                taskDate in start..end
            }// Ensure the category is not null
            .groupBy { it.category!! } // Group tasks by category
            .mapValues { (_, tasks) ->
                tasks.sumOf { it.duration } // Calculate the total duration for each category
            }

        // Separate the map into two lists: one for keys (categories) and one for values (durations)
        val categories = categoryDurations.keys.toList()
        val durations = categoryDurations.values.toList()

        return Pair(categories, durations)
    }


    private fun formatTime(secondsTotal: Int): String {
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    override fun getItemCount(): Int {
        val (categories, _) = calculateTotalDurationsByCategory(tasks, startDate, endDate)
        return categories.size
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

