/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.recorder.base64

internal interface Cache<K : Any, V : Any> {

    fun put(value: V) {}
    fun put(element: K, value: V) {}
    fun get(element: K): V? = null
    fun size(): Int
    fun clear()

    companion object {
        internal const val DOES_NOT_IMPLEMENT_COMPONENTCALLBACKS =
            "Cache instance does not implement ComponentCallbacks2"
    }
}
