/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.rum.internal

import com.datadog.android.rum.RumSessionListener

internal class NoOpRumSessionListener : RumSessionListener {
    override fun onSessionStarted(sessionId: String, isDiscarded: Boolean) {
        // no-op
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is NoOpRumSessionListener
    }

    override fun hashCode(): Int {
        return 0
    }
}
