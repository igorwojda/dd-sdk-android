/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.recorder.mapper

import android.os.Build
import android.widget.CheckedTextView
import com.datadog.android.sessionreplay.internal.recorder.GlobalBounds
import com.datadog.android.sessionreplay.internal.recorder.SystemInformation
import com.datadog.android.sessionreplay.internal.recorder.ViewUtils
import com.datadog.android.sessionreplay.internal.recorder.densityNormalized
import com.datadog.android.sessionreplay.internal.utils.StringUtils
import com.datadog.android.sessionreplay.model.MobileSegment

internal open class CheckedTextViewMapper(
    private val textWireframeMapper: TextWireframeMapper,
    private val stringUtils: StringUtils = StringUtils,
    uniqueIdentifierGenerator: UniqueIdentifierResolver =
        UniqueIdentifierResolver,
    viewUtils: ViewUtils = ViewUtils()
) : CheckableWireframeMapper<CheckedTextView>(uniqueIdentifierGenerator, viewUtils) {

    override fun map(view: CheckedTextView, systemInformation: SystemInformation):
        List<MobileSegment.Wireframe> {
        val mainWireframeList = textWireframeMapper.map(view, systemInformation)
        resolveCheckableWireframe(view, systemInformation.screenDensity)?.let { wireframe ->
            return mainWireframeList + wireframe
        }
        return mainWireframeList
    }

    override fun resolveCheckableColor(view: CheckedTextView): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.checkMarkTintList?.let {
                return stringUtils.formatColorAndAlphaAsHexa(
                    it.defaultColor,
                    OPAQUE_ALPHA_VALUE
                )
            }
        }
        return stringUtils.formatColorAndAlphaAsHexa(view.currentTextColor, OPAQUE_ALPHA_VALUE)
    }

    override fun resolveCheckableBounds(view: CheckedTextView, pixelsDensity: Float): GlobalBounds {
        val viewGlobalBounds = resolveViewGlobalBounds(view, pixelsDensity)
        val textViewPaddingRight =
            view.totalPaddingRight.toLong().densityNormalized(pixelsDensity)
        var checkBoxHeight = 0L
        val checkMarkDrawable = view.checkMarkDrawable
        if (checkMarkDrawable != null && checkMarkDrawable.intrinsicHeight > 0) {
            val height = checkMarkDrawable.intrinsicHeight -
                view.totalPaddingTop -
                view.totalPaddingBottom
            // to solve the current font issues on the player side we lower the original font
            // size with 1 unit. We will need to normalize the current checkbox size
            // to this new size
            checkBoxHeight = (height * (view.textSize - 1) / view.textSize)
                .toLong().densityNormalized(pixelsDensity)
        }

        return GlobalBounds(
            x = viewGlobalBounds.x + viewGlobalBounds.width - textViewPaddingRight,
            y = viewGlobalBounds.y,
            width = checkBoxHeight,
            height = checkBoxHeight

        )
    }
}
