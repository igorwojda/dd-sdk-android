/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.net

import com.datadog.android.v2.api.Request
import com.datadog.android.v2.api.RequestFactory
import com.datadog.android.v2.api.context.DatadogContext

internal class SessionReplayRequestFactory(
        private val sessionReplayOkHttpUploader: SessionReplayOkHttpUploader,
        private val batchToSegmentsMapper: BatchToSegmentsMapper=BatchToSegmentsMapper()) :
        RequestFactory {
    override fun create(context: DatadogContext,
                        batchData: List<ByteArray>,
                        batchMetadata: ByteArray?): Request {

        batchToSegmentsMapper.map(batchData).forEach {
            sessionReplayOkHttpUploader.upload(it.first, (it.second.toString()+"\n").toByteArray())
        }
        return Request("","","", emptyMap(),ByteArray(0))
    }
}