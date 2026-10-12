/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package io.github.robinpcrd.kotose.form.compose.fields

import io.github.robinpcrd.kotose.form.fields.IosDatePickerMode
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIColor
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UINavigationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.navigationItem
import platform.UIKit.sheetPresentationController
import platform.UIKit.systemBackgroundColor
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal fun showNativeDatePicker(
    presentingViewController: UIViewController,
    mode: UIDatePickerMode,
    initialDate: NSDate,
    pickerMode: IosDatePickerMode,
    onDatePicked: (NSDate) -> Unit,
) {
    val pickerController = nativePickerViewController(
        mode = mode,
        initialDate = initialDate,
        pickerMode = pickerMode,
        onDatePicked = onDatePicked,
    )
    val navigationController = UINavigationController(rootViewController = pickerController)
    navigationController.modalPresentationStyle = UIModalPresentationPageSheet
    navigationController.sheetPresentationController?.detents =
        listOf(
            pickerSheetDetent(mode = mode, pickerMode = pickerMode),
            UISheetPresentationControllerDetent.largeDetent(),
        )
    navigationController.sheetPresentationController?.prefersGrabberVisible = true

    presentingViewController
        .topPresentedViewController()
        .presentViewController(
            viewControllerToPresent = navigationController,
            animated = true,
            completion = null,
        )
}

@OptIn(ExperimentalTime::class)
internal fun currentLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

internal fun LocalDate.toNSDate(): NSDate =
    LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = 12,
        minute = 0,
    ).toNSDate()

internal fun LocalDateTime.toNSDate(): NSDate =
    toInstant(TimeZone.currentSystemDefault()).toNSDate()

internal fun NSDate.toLocalDate(): LocalDate =
    toLocalDateTime().date

internal fun NSDate.toLocalDateTime(): LocalDateTime {
    val epochSeconds = timeIntervalSinceReferenceDate + unixSecondsAtReferenceDate
    val epochMilliseconds = (epochSeconds * millisecondsPerSecond).roundToLong()
    return Instant.fromEpochMilliseconds(epochMilliseconds)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

private fun Instant.toNSDate(): NSDate {
    val referenceDateSeconds =
        toEpochMilliseconds() / millisecondsPerSecond - unixSecondsAtReferenceDate
    return NSDate(timeIntervalSinceReferenceDate = referenceDateSeconds)
}

private const val unixSecondsAtReferenceDate = 978_307_200.0
private const val millisecondsPerSecond = 1_000.0
private const val spinnerPickerSheetHeight = 320.0
private const val dateTimeSpinnerPickerSheetHeight = 500.0
private const val datePickerSheetHeight = 470.0
private const val dateTimePickerSheetHeight = 540.0
private const val spinnerLocaleIdentifier = "en_GB"

private fun pickerSheetDetent(
    mode: UIDatePickerMode,
    pickerMode: IosDatePickerMode,
): UISheetPresentationControllerDetent {
    val preferredHeight = when {
        mode == UIDatePickerMode.UIDatePickerModeDateAndTime &&
            pickerMode == IosDatePickerMode.Wheels -> dateTimeSpinnerPickerSheetHeight

        pickerMode == IosDatePickerMode.Wheels -> spinnerPickerSheetHeight
        mode == UIDatePickerMode.UIDatePickerModeDateAndTime -> dateTimePickerSheetHeight
        else -> datePickerSheetHeight
    }
    return UISheetPresentationControllerDetent.customDetentWithIdentifier(
        identifier = "io.github.robinpcrd.kotose.form.date-picker",
    ) { context ->
        minOf(preferredHeight, context?.maximumDetentValue ?: preferredHeight)
    }
}

private tailrec fun UIViewController.topPresentedViewController(): UIViewController {
    val nextViewController = presentedViewController
    return if (nextViewController == null) {
        this
    } else {
        nextViewController.topPresentedViewController()
    }
}

private fun nativePickerViewController(
    mode: UIDatePickerMode,
    initialDate: NSDate,
    pickerMode: IosDatePickerMode,
    onDatePicked: (NSDate) -> Unit,
): UIViewController =
    if (
        mode == UIDatePickerMode.UIDatePickerModeDateAndTime &&
        pickerMode == IosDatePickerMode.Wheels
    ) {
        NativeDateTimeSpinnerViewController(
            initialDate = initialDate,
            onDatePicked = onDatePicked,
        )
    } else {
        NativeDatePickerViewController(
            mode = mode,
            initialDate = initialDate,
            pickerMode = pickerMode,
            onDatePicked = onDatePicked,
        )
    }

private class NativeDatePickerViewController(
    private val mode: UIDatePickerMode,
    initialDate: NSDate,
    pickerMode: IosDatePickerMode,
    private val onDatePicked: (NSDate) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val datePicker = UIDatePicker().apply {
        datePickerMode = mode
        date = initialDate
        preferredDatePickerStyle = pickerMode.toDatePickerStyle()
        if (pickerMode == IosDatePickerMode.Wheels) {
            locale = NSLocale(localeIdentifier = spinnerLocaleIdentifier)
        }
        translatesAutoresizingMaskIntoConstraints = false
    }

    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.systemBackgroundColor
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemCancel,
            target = this,
            action = NSSelectorFromString("cancelPicker"),
        )
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemDone,
            target = this,
            action = NSSelectorFromString("finishPicker"),
        )

        view.addSubview(datePicker)
        datePicker.centerXAnchor.constraintEqualToAnchor(view.centerXAnchor).active = true
        datePicker.topAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.topAnchor)
            .active = true
        datePicker.leadingAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.leadingAnchor)
            .active = true
        datePicker.trailingAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.trailingAnchor)
            .active = true
    }

    @ObjCAction
    fun cancelPicker() {
        dismissViewControllerAnimated(flag = true, completion = null)
    }

    @ObjCAction
    fun finishPicker() {
        onDatePicked(datePicker.date)
        dismissViewControllerAnimated(flag = true, completion = null)
    }
}

private class NativeDateTimeSpinnerViewController(
    initialDate: NSDate,
    private val onDatePicked: (NSDate) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val datePicker = UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        date = initialDate
        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
        locale = NSLocale(localeIdentifier = spinnerLocaleIdentifier)
        translatesAutoresizingMaskIntoConstraints = false
    }

    private val timePicker = UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeTime
        date = initialDate
        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
        locale = NSLocale(localeIdentifier = spinnerLocaleIdentifier)
        translatesAutoresizingMaskIntoConstraints = false
    }

    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.systemBackgroundColor
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemCancel,
            target = this,
            action = NSSelectorFromString("cancelPicker"),
        )
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemDone,
            target = this,
            action = NSSelectorFromString("finishPicker"),
        )

        view.addSubview(datePicker)
        view.addSubview(timePicker)

        datePicker.centerXAnchor.constraintEqualToAnchor(view.centerXAnchor).active = true
        datePicker.topAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.topAnchor)
            .active = true
        datePicker.leadingAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.leadingAnchor)
            .active = true
        datePicker.trailingAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.trailingAnchor)
            .active = true

        timePicker.centerXAnchor.constraintEqualToAnchor(view.centerXAnchor).active = true
        timePicker.topAnchor
            .constraintEqualToAnchor(datePicker.bottomAnchor)
            .active = true
        timePicker.leadingAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.leadingAnchor)
            .active = true
        timePicker.trailingAnchor
            .constraintEqualToAnchor(view.layoutMarginsGuide.trailingAnchor)
            .active = true
    }

    @ObjCAction
    fun cancelPicker() {
        dismissViewControllerAnimated(flag = true, completion = null)
    }

    @ObjCAction
    fun finishPicker() {
        onDatePicked(combinePickedDateAndTime(datePicker.date, timePicker.date))
        dismissViewControllerAnimated(flag = true, completion = null)
    }
}

private fun combinePickedDateAndTime(date: NSDate, time: NSDate): NSDate {
    val selectedDate = date.toLocalDateTime().date
    val selectedTime = time.toLocalDateTime()
    return LocalDateTime(
        year = selectedDate.year,
        month = selectedDate.month,
        day = selectedDate.day,
        hour = selectedTime.hour,
        minute = selectedTime.minute,
    ).toNSDate()
}

private fun IosDatePickerMode.toDatePickerStyle(): UIDatePickerStyle =
    when (this) {
        IosDatePickerMode.Wheels -> UIDatePickerStyle.UIDatePickerStyleWheels
        IosDatePickerMode.Compact -> UIDatePickerStyle.UIDatePickerStyleInline
    }
