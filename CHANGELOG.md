# Changelog

## [0.1.1] - 2026-10-24

- `rememberPhoneCountryPicker(field)` opens the field's country picker from a custom phone input, as
  the built-in country button does; with `phoneNumberOutputTransformation` it lets an app draw its
  own phone input.
- The Compose phone country button has the test tag `<key>-country`, like the SwiftUI one.
- Compose phone, dropdown, date and date-time fields keep the `colors` you pass when they have no
  error; before, they used the default colors until an error appeared.
- These fields draw warnings in `KotoseFormStyle.warning`, like text inputs, instead of always the
  default harmonized warning color. The default style keeps the same color.
- These fields take their corner radius from `KotoseFormStyle.cornerRadius`, like text inputs,
  instead of `MaterialTheme.shapes.extraSmall`. The default style keeps the same 4 dp radius.
  Migration: an app that shaped them through `MaterialTheme.shapes.extraSmall` sets the radius with
  `KotoseFormTheme(KotoseFormStyle.fromMaterial().copy(cornerRadius = ...))`, which also applies
  to text inputs.

## [0.1.0] - 2026-10-12

- First public release.
