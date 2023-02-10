/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.forge

import com.datadog.android.sessionreplay.internal.recorder.ViewGlobalBounds
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeryFactory

internal class ViewGlobalBoundsForgeryFactory : ForgeryFactory<ViewGlobalBounds> {
    override fun getForgery(forge: Forge): ViewGlobalBounds {
        return ViewGlobalBounds(
            x = forge.aLong(),
            y = forge.aLong(),
            width = forge.aPositiveLong(),
            height = forge.aPositiveLong()
        )
    }
}
