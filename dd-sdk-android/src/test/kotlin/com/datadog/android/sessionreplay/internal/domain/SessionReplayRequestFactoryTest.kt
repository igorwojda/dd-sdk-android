/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.domain

import com.datadog.android.log.Logger
import com.datadog.android.rum.RumAttributes
import com.datadog.android.sessionreplay.model.MobileSegment
import com.datadog.android.sessionreplay.net.BatchesToSegmentsMapper
import com.datadog.android.utils.forge.Configurator
import com.datadog.android.v2.api.RequestFactory
import com.datadog.android.v2.api.context.DatadogContext
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.io.IOException
import java.util.stream.Stream

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(Configurator::class)
internal class SessionReplayRequestFactoryTest {

    lateinit var testedRequestFactory: SessionReplayRequestFactory

    @Mock
    lateinit var mockBatchesToSegmentsMapper: BatchesToSegmentsMapper

    @Mock
    lateinit var mockRequestBodyFactory: RequestBodyFactory

    @StringForgery(regex = "https://[a-z]+\\.com")
    lateinit var fakeEndpoint: String

    @Forgery
    lateinit var fakeSegment: MobileSegment

    @Forgery
    lateinit var fakeSerializedSegment: JsonObject

    lateinit var fakeCompressedSegment: ByteArray

    lateinit var fakeBatchData: List<ByteArray>

    @Forgery
    lateinit var fakeDatadogContext: DatadogContext

    @Mock
    lateinit var mockRequestBody: RequestBody

    lateinit var fakeMediaType: MediaType

    var fakeBatchMetadata: ByteArray? = null

    @Mock
    lateinit var mockInternalLogger: Logger

    @BeforeEach
    fun `set up`(forge: Forge) {
        fakeMediaType = forge.anElementFrom(
            listOf(
                MultipartBody.FORM,
                MultipartBody.ALTERNATIVE,
                MultipartBody.MIXED,
                MultipartBody.PARALLEL
            )
        )
        whenever(mockRequestBody.contentType()).thenReturn(fakeMediaType)
        fakeCompressedSegment = forge.aString().toByteArray()
        fakeBatchMetadata = forge.aNullable { forge.aString().toByteArray() }
        fakeBatchData = forge.aList { forge.aString().toByteArray() }
        whenever(mockRequestBodyFactory.create(fakeSegment, fakeSerializedSegment))
            .thenReturn(mockRequestBody)
        whenever(mockBatchesToSegmentsMapper.map(fakeBatchData))
            .thenReturn(Pair(fakeSegment, fakeSerializedSegment))
        testedRequestFactory = SessionReplayRequestFactory(
            fakeEndpoint,
            mockBatchesToSegmentsMapper,
            mockRequestBodyFactory
        )
    }

    // region Request

    @Test
    fun `M return a valid Request W create`() {
        // When
        val request = testedRequestFactory.create(
            fakeDatadogContext,
            fakeBatchData,
            fakeBatchMetadata
        )

        // Then
        assertThat(request.url).isEqualTo(expectedUrl(fakeEndpoint))
        assertThat(request.contentType).isEqualTo(fakeMediaType.toString())
        assertThat(request.headers.minus(RequestFactory.HEADER_REQUEST_ID)).isEqualTo(
            mapOf(
                RequestFactory.HEADER_API_KEY to fakeDatadogContext.clientToken,
                RequestFactory.HEADER_EVP_ORIGIN to fakeDatadogContext.source,
                RequestFactory.HEADER_EVP_ORIGIN_VERSION to fakeDatadogContext.sdkVersion
            )
        )
        assertThat(request.headers[RequestFactory.HEADER_REQUEST_ID]).isNotEmpty()
        assertThat(request.id).isEqualTo(request.headers[RequestFactory.HEADER_REQUEST_ID])
        assertThat(request.description).isEqualTo("Session Replay Segment Upload Request")
        assertThat(request.body).isEqualTo(mockRequestBody.toByteArray())
    }

    @Test
    fun `M return an empty Request W create(){ payload is broken }`() {
        // Given
        whenever(mockBatchesToSegmentsMapper.map(fakeBatchData))
            .thenReturn(null)

        // When
        val request = testedRequestFactory.create(
            fakeDatadogContext,
            fakeBatchData,
            fakeBatchMetadata
        )

        // Then
        assertThat(request.body).isEmpty()
        assertThat(request.url).isEmpty()
        assertThat(request.description).isEmpty()
        assertThat(request.headers).isEmpty()
    }

    // endregion

    // region broken Body

    @Test
    fun `M return an empty Request W create(){ request body could not be created }`() {
        // Given
        whenever(mockRequestBodyFactory.create(any(), any())).thenReturn(null)

        // When
        val request = testedRequestFactory.create(
            fakeDatadogContext,
            fakeBatchData,
            fakeBatchMetadata
        )

        // Then
        assertThat(request.body).isEmpty()
        assertThat(request.url).isEmpty()
        assertThat(request.description).isEmpty()
        assertThat(request.headers).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("throwableAndLogMessageValues")
    fun `M return an empty Request W create(){ bodyToByteArray throws }`(
        fakeThrowable: Throwable
    ) {
        // Given
        testedRequestFactory = SessionReplayRequestFactory(
            fakeEndpoint,
            mockBatchesToSegmentsMapper,
            mockRequestBodyFactory,
            bodyToByteArrayFactory = {
                throw fakeThrowable
            }
        )

        // When
        val request = testedRequestFactory.create(
            fakeDatadogContext,
            fakeBatchData,
            fakeBatchMetadata
        )

        // Then
        assertThat(request.body).isEmpty()
        assertThat(request.url).isEmpty()
        assertThat(request.description).isEmpty()
        assertThat(request.headers).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("throwableAndLogMessageValues")
    fun `M log exception W create(){ bodyToByteArray throws }`(
        fakeThrowable: Throwable,
        errorMessage: String
    ) {
        // Given
        testedRequestFactory = SessionReplayRequestFactory(
            fakeEndpoint,
            mockBatchesToSegmentsMapper,
            mockRequestBodyFactory,
            internalLogger = mockInternalLogger,
            bodyToByteArrayFactory = {
                throw fakeThrowable
            }
        )

        // When
        testedRequestFactory.create(
            fakeDatadogContext,
            fakeBatchData,
            fakeBatchMetadata
        )

        // Then
        verify(mockInternalLogger).e(
            errorMessage,
            fakeThrowable
        )
        verifyNoMoreInteractions(mockInternalLogger)
    }

    // endregion

    // region Internal

    private fun expectedUrl(endpointUrl: String): String {
        val queryTags = mutableListOf(
            "${RumAttributes.SERVICE_NAME}:${fakeDatadogContext.service}",
            "${RumAttributes.APPLICATION_VERSION}:${fakeDatadogContext.version}",
            "${RumAttributes.SDK_VERSION}:${fakeDatadogContext.sdkVersion}",
            "${RumAttributes.ENV}:${fakeDatadogContext.env}"
        )

        if (fakeDatadogContext.variant.isNotEmpty()) {
            queryTags.add("${RumAttributes.VARIANT}:${fakeDatadogContext.variant}")
        }

        return "$endpointUrl/api/v2/replay?${RequestFactory.QUERY_PARAM_SOURCE}=" +
            "${fakeDatadogContext.source}&${RequestFactory.QUERY_PARAM_TAGS}=" +
            queryTags.joinToString(",")
    }

    private fun RequestBody.toByteArray(): ByteArray {
        val buffer = Buffer()
        writeTo(buffer)
        return buffer.readByteArray()
    }

    // endregion

    companion object {

        @JvmStatic
        fun throwableAndLogMessageValues(): Stream<Arguments> {
            return listOf(
                Arguments.of(
                    AssertionError(),
                    SessionReplayRequestFactory.EXTRACT_BYTE_ARRAY_FROM_BODY_ERROR_MESSAGE
                ),
                Arguments.of(
                    IllegalArgumentException(),
                    SessionReplayRequestFactory.EXTRACT_BYTE_ARRAY_FROM_BODY_ERROR_MESSAGE
                ),
                Arguments.of(
                    IOException(),
                    SessionReplayRequestFactory.EXTRACT_BYTE_ARRAY_FROM_BODY_ERROR_MESSAGE
                )
            )
                .stream()
        }
    }
}