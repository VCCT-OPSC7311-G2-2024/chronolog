package za.co.varsitycollege.serversamurais.chronolog.Helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import za.co.varsitycollege.serversamurais.chronolog.R

fun openIntent(activity: Activity,
               activityToOpen: Class<*>) {
    val intent = Intent(activity, activityToOpen)

    activity.startActivity(intent)
}

fun replaceFragment(activity: AppCompatActivity, fragment: Fragment) {
    activity.supportFragmentManager.beginTransaction()
        .replace(R.id.frameLayout, fragment) // replace 'frameLayout' with the id of your FrameLayout or other container
        .commit()
}