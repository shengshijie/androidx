/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.graphics

import kotlin.math.round
import org.jetbrains.skija.BlendMode as SkijaBlendMode
import org.jetbrains.skija.ColorFilter as SkijaColorFilter
import org.jetbrains.skija.FilterQuality as SkijaFilterQuality
import org.jetbrains.skija.PaintMode as SkijaPaintMode
import org.jetbrains.skija.PaintStrokeCap as SkijaPaintStrokeCap
import org.jetbrains.skija.PaintStrokeJoin as SkijaPaintStrokeJoin

class DesktopPaint : Paint {
    private var nativePaint = android.graphics.Paint()
    private val skija get() = nativePaint.skijaPaint

    override fun asFrameworkPaint(): NativePaint = nativePaint

    override var alpha: Float
        get() = alphaInt / 255f
        set(value) {
            alphaInt = round(value * 255.0f).toInt()
        }

    private var alphaInt: Int
        get() = (skija.color shr 24) and 0xff
        set(value) {
            skija.color = (value shl 24) or (skija.color and 0x00ffffff)
        }

    override var isAntiAlias: Boolean
        get() = skija.isAntiAlias
        set(value) {
            skija.isAntiAlias = value
        }

    override var color: Color
        get() = Color(skija.color)
        set(color) {
            skija.color = color.toArgb()
        }

    override var blendMode: BlendMode = BlendMode.SrcOver
        set(value) {
            skija.blendMode = value.toSkija()
            field = value
        }

    override var style: PaintingStyle = PaintingStyle.fill
        set(value) {
            skija.mode = value.toSkija()
            field = value
        }

    override var strokeWidth: Float
        get() = skija.strokeWidth
        set(value) {
            skija.strokeWidth = value
        }

    override var strokeCap: StrokeCap = StrokeCap.butt
        set(value) {
            skija.strokeCap = value.toSkija()
            field = value
        }

    override var strokeJoin: StrokeJoin = StrokeJoin.round
        set(value) {
            skija.strokeJoin = value.toSkija()
            field = value
        }

    override var strokeMiterLimit: Float = 0f
        set(value) {
            skija.strokeMiter = value
            field = value
        }

    override var filterQuality: FilterQuality = FilterQuality.none
        set(value) {
            skija.filterQuality = value.toSkija()
            field = value
        }

    override var shader: Shader? = null
        set(value) {
            skija.shader = value?.skija
            field = value
        }

    override var colorFilter: ColorFilter? = null
        set(value) {
            skija.colorFilter = if (value != null) {
                SkijaColorFilter.makeBlend(
                    value.color.toArgb(),
                    value.blendMode.toSkija()
                )
            } else {
                null
            }
            field = value
        }

    override var nativePathEffect: NativePathEffect? = null
        set(value) {
            skija.pathEffect = value?.skija
            field = value
        }

    private fun BlendMode.toSkija() = when (this) {
        BlendMode.Clear -> SkijaBlendMode.CLEAR
        BlendMode.Src -> SkijaBlendMode.SRC
        BlendMode.Dst -> SkijaBlendMode.DST
        BlendMode.SrcOver -> SkijaBlendMode.SRC_OVER
        BlendMode.DstOver -> SkijaBlendMode.DST_OVER
        BlendMode.SrcIn -> SkijaBlendMode.SRC_IN
        BlendMode.DstIn -> SkijaBlendMode.DST_IN
        BlendMode.SrcOut -> SkijaBlendMode.SRC_OUT
        BlendMode.DstOut -> SkijaBlendMode.DST_OUT
        BlendMode.SrcAtop -> SkijaBlendMode.SRC_ATOP
        BlendMode.DstAtop -> SkijaBlendMode.DST_ATOP
        BlendMode.Xor -> SkijaBlendMode.XOR
        BlendMode.Plus -> SkijaBlendMode.PLUS
        BlendMode.Modulate -> SkijaBlendMode.MODULATE
        BlendMode.Screen -> SkijaBlendMode.SCREEN
        BlendMode.Overlay -> SkijaBlendMode.OVERLAY
        BlendMode.Darken -> SkijaBlendMode.DARKEN
        BlendMode.Lighten -> SkijaBlendMode.LIGHTEN
        BlendMode.ColorDodge -> SkijaBlendMode.COLOR_DODGE
        BlendMode.ColorBurn -> SkijaBlendMode.COLOR_BURN
        BlendMode.Hardlight -> SkijaBlendMode.HARD_LIGHT
        BlendMode.Softlight -> SkijaBlendMode.SOFT_LIGHT
        BlendMode.Difference -> SkijaBlendMode.DIFFERENCE
        BlendMode.Exclusion -> SkijaBlendMode.EXCLUSION
        BlendMode.Multiply -> SkijaBlendMode.MULTIPLY
        BlendMode.Hue -> SkijaBlendMode.HUE
        BlendMode.Saturation -> SkijaBlendMode.SATURATION
        BlendMode.Color -> SkijaBlendMode.COLOR
        BlendMode.Luminosity -> SkijaBlendMode.LUMINOSITY
    }

    private fun PaintingStyle.toSkija() = when (this) {
        PaintingStyle.fill -> SkijaPaintMode.FILL
        PaintingStyle.stroke -> SkijaPaintMode.STROKE
    }

    private fun StrokeCap.toSkija() = when (this) {
        StrokeCap.butt -> SkijaPaintStrokeCap.BUTT
        StrokeCap.round -> SkijaPaintStrokeCap.ROUND
        StrokeCap.square -> SkijaPaintStrokeCap.SQUARE
    }

    private fun StrokeJoin.toSkija() = when (this) {
        StrokeJoin.miter -> SkijaPaintStrokeJoin.MITER
        StrokeJoin.round -> SkijaPaintStrokeJoin.ROUND
        StrokeJoin.bevel -> SkijaPaintStrokeJoin.BEVEL
    }

    private fun FilterQuality.toSkija() = when (this) {
        FilterQuality.none -> SkijaFilterQuality.NONE
        FilterQuality.low -> SkijaFilterQuality.LOW
        FilterQuality.medium -> SkijaFilterQuality.MEDIUM
        FilterQuality.high -> SkijaFilterQuality.HIGH
    }
}