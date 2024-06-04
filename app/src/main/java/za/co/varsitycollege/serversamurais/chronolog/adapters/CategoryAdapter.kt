package za.co.varsitycollege.serversamurais.chronolog.adapters


import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CategoryAdapter(private var tasks: List<Task>, private var startDate: Date?, private var endDate: Date? ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private val timers = HashMap<String?, CountDownTimer>()
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryName)
        val categoryDurationTextView: TextView = itemView.findViewById(R.id.categoryTotalDuration)
    }

    /**
 * This function is responsible for creating a new ViewHolder for the RecyclerView.
 * A ViewHolder represents an individual item in the RecyclerView, and in this case, it represents a category.
 *
 * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
 * @param viewType The view type of the new View. This parameter is not being used in this function.
 * @return Returns a new ViewHolder that holds a View of the given view type.
 */
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
    // Inflate the layout for a single category item from the XML layout resource file (R.layout.item_category_view)
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category_view, parent, false)

    // Create and return a new CategoryViewHolder instance, passing the inflated view to its constructor
    return CategoryViewHolder(itemView)
}

    /**
 * This function is responsible for binding the data to the ViewHolder.
 * It calculates the total durations by category and sets the category name and duration to the respective TextViews in the ViewHolder.
 *
 * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
 * @param position The position of the item within the adapter's data set.
 */
override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

    // Calculate the total durations by category
    val (categories, durations) = calculateTotalDurationsByCategory(tasks, startDate, endDate)

    // Check if the position is within the size of the categories and durations lists
    if (position < categories.size && position < durations.size) {
        // Set the category name to the categoryNameTextView in the ViewHolder
        holder.categoryNameTextView.text = categories[position]

        // Set the category duration to the categoryDurationTextView in the ViewHolder
        holder.categoryDurationTextView.text = "Duration:\n" + formatTime(durations[position])
    }
}

    /**
 * This function calculates the total durations of tasks by category within a specified date range.
 * If the start date or end date is null, it calculates the total durations of tasks by category without considering the date range.
 *
 * @param tasks The list of tasks to be processed.
 * @param startDate The start date of the date range. If it is null, the function does not consider the date range.
 * @param endDate The end date of the date range. If it is null, the function does not consider the date range.
 * @return Returns a pair of lists. The first list contains the categories and the second list contains the total durations of tasks by category.
 */
fun calculateTotalDurationsByCategory(tasks: List<Task>, startDate: Date?, endDate: Date?): Pair<List<String>, List<Int>> {

    // If the start date or end date is null, calculate the total durations of tasks by category without considering the date range.
    if(startDate == null || endDate == null){
        val categoryDurations = tasks
            .filter { it.category != null }
            .groupBy { it.category!! }
            .mapValues { (_, tasks) ->
                tasks.sumOf { it.duration }
            }
        return Pair(categoryDurations.keys.toList(), categoryDurations.values.toList())
    }

    // Format the start date and end date to match the task date format.
    val formatter = SimpleDateFormat("yyyyMMdd")
    val start = formatter.format(startDate).toInt()
    val end = formatter.format(endDate).toInt()

    // Filter the tasks by category and date, group them by category, and calculate the total duration for each category.
    val categoryDurations = tasks
        .filter { it.category != null && it.date != null }
        .filter{
            val taskDate = formatter.format(it.date).toInt()
            taskDate in start..end
        }
        .groupBy { it.category!! }
        .mapValues { (_, tasks) ->
            tasks.sumOf { it.duration }
        }

    // Separate the map into two lists: one for keys (categories) and one for values (durations).
    val categories = categoryDurations.keys.toList()
    val durations = categoryDurations.values.toList()

    return Pair(categories, durations)
}


    /**
 * This function formats the given total seconds into a string in the format of "HH:MM:SS".
 *
 * @param secondsTotal The total seconds to be formatted.
 * @return Returns a string representing the formatted time.
 */
private fun formatTime(secondsTotal: Int): String {
    val hours = secondsTotal / 3600
    val minutes = (secondsTotal % 3600) / 60
    val seconds = secondsTotal % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * This function returns the number of categories in the tasks list.
 * It calculates the total durations by category and returns the size of the categories list.
 *
 * @return Returns the number of categories in the tasks list.
 */
override fun getItemCount(): Int {
    val (categories, _) = calculateTotalDurationsByCategory(tasks, startDate, endDate)
    return categories.size
}

/**
 * This function filters the tasks by a specified date range and updates the tasks list.
 * It parses the start date and end date strings into Date objects, filters the tasks by the date range, and updates the tasks list.
 * If the date parsing fails, it logs an error message.
 *
 * @param startDate The start date of the date range in the format of "dd/MM/yyyy".
 * @param endDate The end date of the date range in the format of "dd/MM/yyyy".
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

