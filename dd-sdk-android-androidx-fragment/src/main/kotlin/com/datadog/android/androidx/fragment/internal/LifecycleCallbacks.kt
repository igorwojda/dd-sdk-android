package com.datadog.android.androidx.fragment.internal

import android.app.Activity

internal interface LifecycleCallbacks<T : Activity> {

    // region Lifecycle

    fun register(activity: T)

    fun unregister(activity: T)

    // end region
}