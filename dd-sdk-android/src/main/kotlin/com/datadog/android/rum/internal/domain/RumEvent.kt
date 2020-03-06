/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-2020 Datadog, Inc.
 */

package com.datadog.android.rum.internal.domain

internal data class RumEvent(
    val context: RumContext,
    val timestamp: Long,
    val eventData: RumEventData,
    val attributes: Map<String, Any?>
)