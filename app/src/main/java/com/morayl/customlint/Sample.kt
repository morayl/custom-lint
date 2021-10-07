package com.morayl.customlint

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
data class Sample(
    val a: String,
    val b: String?,
    val c: List<String>?,
    @DrawableRes
    val d: Int?,
)