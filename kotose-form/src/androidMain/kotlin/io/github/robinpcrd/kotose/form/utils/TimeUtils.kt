/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

val UtcZone: ZoneId = ZoneId.of("UTC")

fun LocalDate.toInstant(): Instant = atStartOfDay(UtcZone).toInstant()

fun LocalDate.toEpochMilli(): Long = toInstant().toEpochMilli()

fun Instant.toLocalDate(): LocalDate = atOffset(ZoneOffset.UTC).toLocalDate()

/**
 * Convert Epoch Millis to LocalDate
 */
fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(UtcZone).toLocalDate()
