package it.ipzs.cieidsdk.util

import android.app.Activity
import android.content.Context

class ActivityInfo(activity: Activity, activityType: ActivityType, context: Context) {
    var activity: Activity = activity
    var activityType: ActivityType = activityType
    var context: Context = context
}
