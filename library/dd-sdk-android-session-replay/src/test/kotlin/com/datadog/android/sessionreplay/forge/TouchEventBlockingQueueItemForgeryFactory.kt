/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.forge

import com.datadog.android.sessionreplay.internal.async.TouchEventBlockingQueueItem
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeryFactory

internal class TouchEventBlockingQueueItemForgeryFactory : ForgeryFactory<TouchEventBlockingQueueItem> {
    override fun getForgery(forge: Forge): TouchEventBlockingQueueItem {
        val item = TouchEventBlockingQueueItem(
            timestamp = forge.aLong(),
            newRumContext = forge.getForgery(),
            prevRumContext = forge.getForgery()
        )

        item.touchData = listOf(forge.getForgery())
        return item
    }
}
