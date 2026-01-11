package com.example.camera.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.unit.round

fun Offset.takeOrZero(): Offset = if (isSpecified) this else Offset.Zero

fun Offset.toRoundedOffset() = takeOrZero().round()

