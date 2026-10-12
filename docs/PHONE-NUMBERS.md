# Phone numbers

```kotlin
+phoneNumberField(tag = "phone", placeholder = phoneLabel) {
    addRule(RequiredFieldRule())
    addRule(IsValidPhoneRule)
}
```

The value is a `PhoneData` (country and national digits). The field submits it as one string,
`+<dial code><digits>`.

## Two modes

Phone fields work without extra dependencies. For a country picker, per-country validation
and formatting as the user types, add the optional backend, which uses libphonenumber on
Android and PhoneNumberKit on iOS, and set it once at startup, before building forms:

```kotlin
commonMain.dependencies {
    implementation("io.github.robinpcrd:kotose-form-phone-backend:<version>")
}
```

```kotlin
PhoneNumberBackend.current = LibPhoneNumberBackend
```

On iOS the backend needs one more setup step; see [iOS setup](../README.md#ios-setup).

| | Without the backend | With the backend |
| --- | --- | --- |
| Row | A dial code input after a fixed `+`, then the number input | A country button, then the number input |
| Dial code | Typed: digits only, at most 3, no leading 0 | From the selected country |
| Number | Digits only, no length limit, no formatting | Formatted as the user types on Compose |
| Keyboard | Number pad for both inputs | Phone pad |
| `IsValidPhoneRule` | Warns when a number has no dial code | Warns when the number is not possible for the country |
| Country settings (`setOfAllowedRegion`, `isCountrySelectionEnabled`) | No effect | Apply |

- `RequiredFieldRule` blocks an empty number. `IsValidPhoneRule` is a warning: it does not
  block submission.
- `current` is observable state, so both renderers follow a change at runtime. A value keeps
  its dial code when the mode changes.

## Without the backend

- The `+` is drawn by the renderer: it cannot be deleted and is not part of the value.
- Next on the dial code moves to the number, and Next on the number to the next field.
  Focusing the field lands on the dial code while it is empty, otherwise on the number. On iOS,
  the keyboard bar visits both inputs: Next enters the row on the dial code, Previous on the
  number, and Previous on the number moves back to the dial code.
- Text is never split into a dial code and a number: only the libraries know where a dial code
  ends (`+336 12 34 56 78` is `+33`). Pasted text stays in the input it was pasted into, as digits.
- Preset a `PhoneData` to fill both parts. A string preset (`+33612345678`) goes entirely into
  the number, with an empty dial code, so `IsValidPhoneRule` warns about it.
- National rules are not known. For example, French numbers are dialed `06...` at home, and the
  leading 0 is dropped after `+33` (Italy keeps it).

## With the backend

- The country button opens a searchable list of countries. `setOfAllowedRegion` limits it, and
  `isCountrySelectionEnabled = false` locks the country.
- The default country follows the device region.
- Typing or presetting an international number (`+32...`) selects its country. Changing the
  country keeps the national digits.
- Compose formats the number as the user types; SwiftUI shows the digits.

## Your own backend

`PhoneNumberBackend` is an interface, so another parsing library can back the field.
Implementing it requires opting in to `ExperimentalKotoseFormApi`.

## Test identifiers

The number input uses the field key and, without the backend, the dial code input
`<key>-dial-code`. On iOS, the keyboard bar buttons of both renderers are `<key>-previous`,
`<key>-next` and `<key>-done`, or `<key>-dial-code-previous` and so on while the dial code has
focus.
