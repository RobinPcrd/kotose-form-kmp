/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:JvmName("DateTimeFieldAndroid")
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package io.github.robinpcrd.kotose.form.compose.fields

import android.text.format.DateFormat
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.robinpcrd.kotose.form.fields.DateTimeField
import io.github.robinpcrd.kotose.form.fields.applyPickedDateTime
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.date_picker_cancel
import io.github.robinpcrd.kotose_form.generated.resources.date_picker_confirm
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
actual fun rememberDefaultDateTimePicker(field: DateTimeField): (() -> Unit)? {
    val context = LocalContext.current
    val is24HourFormat = DateFormat.is24HourFormat(context)
    val value by field.valueState
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }

    val currentValue by rememberUpdatedState(value)
    val currentField by rememberUpdatedState(field)

    if (showDatePicker) {
        val initialDateTime = currentValue ?: currentLocalDateTime()
        DefaultDatePickerDialog(
            initialDate = initialDateTime.date,
            onDismissRequest = { showDatePicker = false },
            onDatePicked = { date ->
                pendingDate = date
                showDatePicker = false
                showTimePicker = true
            },
        )
    }

    if (showTimePicker) {
        val initialDateTime = currentValue ?: currentLocalDateTime()
        DefaultTimePickerDialog(
            initialHour = initialDateTime.hour,
            initialMinute = initialDateTime.minute,
            is24HourFormat = is24HourFormat,
            onDismissRequest = { showTimePicker = false },
            onTimePicked = { hour, minute ->
                val date = pendingDate
                showTimePicker = false
                pendingDate = null
                date?.let {
                    currentField.applyPickedDateTime(
                        date = it,
                        hour = hour,
                        minute = minute,
                    )
                }
            },
        )
    }

    return {
        pendingDate = null
        showDatePicker = true
    }
}

@Composable
internal fun DefaultDatePickerDialog(
    initialDate: LocalDate,
    onDismissRequest: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
) {
    val initialDateMillis = remember(initialDate) { initialDate.toUtcMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        initialDisplayedMonthMillis = initialDateMillis,
        initialDisplayMode = DisplayMode.Picker,
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                        onDatePicked(selectedDateMillis.toUtcLocalDate())
                    }
                },
            ) {
                Text(stringResource(Res.string.date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.date_picker_cancel))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun DefaultTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24HourFormat: Boolean,
    onDismissRequest: () -> Unit,
    onTimePicked: (Int, Int) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24HourFormat,
    )

    TimePickerDialog(
        title = {},
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimePicked(timePickerState.hour, timePickerState.minute)
                },
            ) {
                Text(stringResource(Res.string.date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.date_picker_cancel))
            }
        },
    ) {
        TimePicker(state = timePickerState)
    }
}

private fun LocalDate.toUtcMillis(): Long =
    LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = 0,
        minute = 0,
    ).toInstant(TimeZone.UTC).toEpochMilliseconds()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

internal fun currentLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
