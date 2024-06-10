import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.Helpers.RecyclerAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Notification

class SharedViewModel : ViewModel() {
    val data: MutableLiveData<MutableList<Notification>> = MutableLiveData(mutableListOf())
    val adapter = MutableLiveData<RecyclerAdapter>()


    init {
        loadData()
    }

    private fun loadData() {
        // This is just a placeholder. Replace this with your actual data loading logic.
        val items = mutableListOf(
            Notification("Title 1", "Date 1"),
            Notification("Title 2", "Date 2"),
            // Add more items...
        )
        data.value = items
    }
}



