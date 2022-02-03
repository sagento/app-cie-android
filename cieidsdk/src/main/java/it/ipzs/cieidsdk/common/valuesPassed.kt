package it.ipzs.cieidsdk.common

import android.app.Activity
import android.content.Context

class valuesPassed(
    activityParam: Activity?,
    contextParam: Context?,
    callbackParam: Callback?
) {
    @JvmName("getActivity1")
    fun getActivity(): Activity? {
        if (activity != null)
            return activity

        return null
    }

    @JvmName("getCallback1")
    fun getCallback(): Callback? {
        return callback
    }

    @JvmName("getContext1")
    fun getContext(): Context? {
        if (context != null)
            return context

        if (activity != null)
            return (activity ?: return null).baseContext

        return null
    }

    private var activity = activityParam
    private var context = contextParam
    private var callback = callbackParam
}