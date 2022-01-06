/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.log.model

import com.datadog.android.utils.forge.Configurator
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings
@ForgeConfiguration(Configurator::class)
internal class LogEventTest {

    @RepeatedTest(8)
    fun `M serialize deserialized event W toJson()+fromJson()`(
        @Forgery event: LogEvent
    ) {
        // Given
        val json = event.toJson().toString()

        // When
        val result = LogEvent.fromJson(json)

        // Then
        assertThat(result).isEqualTo(result)
    }
}
