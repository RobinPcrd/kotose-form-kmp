# Date and time

```kotlin
+localDatePicker(tag = "birthday", placeholder = birthdayLabel) {
    addRule(RequiredFieldRule())
}
+dateTimePicker(
    tag = "meeting",
    placeholder = meetingLabel,
    iosPickerMode = IosDatePickerMode.Wheels,
)
```

- Values are calendar values without a time zone, from kotlinx-datetime: `LocalDate` for
  `localDatePicker` and `LocalDateTime` for `dateTimePicker`. `kotose-form-core` exposes
  kotlinx-datetime as an API dependency.
- Presets and `Form.setValues` take the matching type; any other value clears the field.
  Submission returns the typed value.
- `formatter` turns the value into the text Compose shows; the SwiftUI pills use the system
  format. The default is English (`May 30, 2026`, and `May 30, 2026 14:05` with a time): pass
  your own for other languages.
- There is no time-only field and no minimum or maximum date yet.

## Pickers

Android always uses the Material date and time dialogs. On iOS, `iosPickerMode` chooses the
picker:

| Mode | SwiftUI | Compose iOS |
| --- | --- | --- |
| `Compact` (default) | Apple's compact picker: a date pill, plus a time pill for a date and time field, opening the system calendar or time picker. Tapping an empty field fills in today. | A row opening a sheet with a calendar |
| `Wheels` | The date pill brings up date wheels and the time pill time wheels, in place of the keyboard, with Done above them. Turning the wheels applies the value; Done or dragging the form closes them. | A row opening a sheet with wheels |

With `Wheels` on SwiftUI, an empty field opens the date wheels, and Done applies today.

## Your own picker

Pass `showPicker` to open your own picker instead: the field keeps a tap-to-open row and calls
it. Apply the result with `applyPickedDate(date)` or `applyPickedDateTime(date, hour, minute)`.
