package za.co.varsitycollege.serversamurais.chronolog.Helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

fun openIntent(activity: Activity,
               activityToOpen: Class<*>) {
    val intent = Intent(activity, activityToOpen)

    activity.startActivity(intent)
}