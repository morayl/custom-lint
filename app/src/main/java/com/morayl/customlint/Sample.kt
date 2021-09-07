package com.morayl.customlint

import kotlinx.serialization.Serializable

@Serializable
data class Sample(
    val a: String,
    val b: String?
)