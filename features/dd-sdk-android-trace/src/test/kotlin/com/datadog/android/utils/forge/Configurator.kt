/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.utils.forge

import com.datadog.android.tests.elmyr.useCoreFactories
import com.datadog.tools.unit.forge.BaseConfigurator
import fr.xgouchet.elmyr.Forge

internal class Configurator : BaseConfigurator() {

    override fun configure(forge: Forge) {
        super.configure(forge)

        // Core
        forge.useCoreFactories()

        // APM
        forge.addFactory(SpanForgeryFactory())
        forge.addFactory(SpanEventForgeryFactory())
        forge.addFactory(TraceConfigurationForgeryFactory())
        forge.addFactory(CoreDDSpanForgeryFactory())
        forge.addFactory(AgentSpanLinkForgeryFactory())

        // MISC
        forge.addFactory(BigIntegerFactory())
    }
}
