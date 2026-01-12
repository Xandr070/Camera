package com.example.camera.ui.theme.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object CameraIcons {
    val ArrowBack: ImageVector
        get() {
            if (_arrowBack != null) {
                return _arrowBack!!
            }
            _arrowBack = Builder(
                name = "ArrowBack",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(20.0f, 11.0f)
                    horizontalLineTo(7.83f)
                    lineToRelative(5.59f, -5.59f)
                    lineTo(12.0f, 4.0f)
                    lineToRelative(-8.0f, 8.0f)
                    lineToRelative(8.0f, 8.0f)
                    lineToRelative(1.41f, -1.41f)
                    lineTo(7.83f, 13.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(11.0f)
                    close()
                }
            }.build()
            return _arrowBack!!
        }
    private var _arrowBack: ImageVector? = null

    val Delete: ImageVector
        get() {
            if (_delete != null) {
                return _delete!!
            }
            _delete = Builder(
                name = "Delete",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(6.0f, 19.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(8.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(7.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(19.0f)
                    close()
                    moveTo(19.0f, 4.0f)
                    horizontalLineToRelative(-3.5f)
                    lineToRelative(-1.0f, -1.0f)
                    horizontalLineToRelative(-5.0f)
                    lineToRelative(-1.0f, 1.0f)
                    horizontalLineTo(5.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineTo(4.0f)
                    close()
                }
            }.build()
            return _delete!!
        }
    private var _delete: ImageVector? = null

    val ArrowForward: ImageVector
        get() {
            if (_arrowForward != null) {
                return _arrowForward!!
            }
            _arrowForward = Builder(
                name = "ArrowForward",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(12.0f, 4.0f)
                    lineToRelative(-1.41f, 1.41f)
                    lineTo(16.17f, 11.0f)
                    horizontalLineTo(4.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(12.17f)
                    lineToRelative(-5.58f, 5.59f)
                    lineTo(12.0f, 20.0f)
                    lineToRelative(8.0f, -8.0f)
                    lineToRelative(-8.0f, -8.0f)
                    close()
                }
            }.build()
            return _arrowForward!!
        }
    private var _arrowForward: ImageVector? = null

    val Home: ImageVector
        get() {
            if (_home != null) {
                return _home!!
            }
            _home = Builder(
                name = "Home",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(10.0f, 20.0f)
                    verticalLineToRelative(-6.0f)
                    horizontalLineToRelative(4.0f)
                    verticalLineToRelative(6.0f)
                    horizontalLineToRelative(5.0f)
                    verticalLineToRelative(-8.0f)
                    horizontalLineToRelative(3.0f)
                    lineToRelative(-8.0f, -8.0f)
                    lineToRelative(-8.0f, 8.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(8.0f)
                    close()
                }
            }.build()
            return _home!!
        }
    private var _home: ImageVector? = null

    val PlayArrow: ImageVector
        get() {
            if (_playArrow != null) {
                return _playArrow!!
            }
            _playArrow = Builder(
                name = "PlayArrow",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(8.0f, 5.0f)
                    verticalLineToRelative(14.0f)
                    lineToRelative(11.0f, -7.0f)
                    close()
                }
            }.build()
            return _playArrow!!
        }
    private var _playArrow: ImageVector? = null

    val FlashOn: ImageVector
        get() {
            if (_flashOn != null) {
                return _flashOn!!
            }
            _flashOn = Builder(
                name = "FlashOn",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(7.0f, 2.0f)
                    verticalLineToRelative(11.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(9.0f)
                    lineToRelative(7.0f, -12.0f)
                    horizontalLineToRelative(-4.0f)
                    lineToRelative(4.0f, -8.0f)
                    close()
                }
            }.build()
            return _flashOn!!
        }
    private var _flashOn: ImageVector? = null

    val FlashOff: ImageVector
        get() {
            if (_flashOff != null) {
                return _flashOff!!
            }
            _flashOff = Builder(
                name = "FlashOff",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(3.27f, 3.0f)
                    lineToRelative(17.73f, 17.73f)
                    lineToRelative(-1.41f, 1.41f)
                    lineToRelative(-17.73f, -17.73f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(7.0f, 2.0f)
                    verticalLineToRelative(11.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(9.0f)
                    lineToRelative(7.0f, -12.0f)
                    horizontalLineToRelative(-4.0f)
                    lineToRelative(4.0f, -8.0f)
                    close()
                }
            }.build()
            return _flashOff!!
        }
    private var _flashOff: ImageVector? = null

    val FlashAuto: ImageVector
        get() {
            if (_flashAuto != null) {
                return _flashAuto!!
            }
            _flashAuto = Builder(
                name = "FlashAuto",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(3.0f, 2.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(2.0f)
                    lineToRelative(1.6f, 7.0f)
                    horizontalLineToRelative(4.4f)
                    lineToRelative(-1.6f, 7.0f)
                    horizontalLineToRelative(-2.0f)
                    lineToRelative(1.6f, -7.0f)
                    horizontalLineToRelative(-4.4f)
                    lineToRelative(1.6f, -7.0f)
                    horizontalLineToRelative(-2.0f)
                    close()
                    moveTo(7.0f, 14.0f)
                    verticalLineToRelative(7.0f)
                    lineToRelative(4.0f, -6.0f)
                    horizontalLineToRelative(-4.0f)
                    close()
                }
            }.build()
            return _flashAuto!!
        }
    private var _flashAuto: ImageVector? = null
}
