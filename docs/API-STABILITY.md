# API stability

Kotose Form is pre-1.0. This page says which parts of the API are meant to stay, and what can
still change.

## Modules

- **`kotose-form-core`**: `Form`, `Group`, `Element`, `Field` and `FocusableField`; conditions,
  validation rules, errors, transactions, events and navigation; the form and group DSL; the
  text, password, toggle, single choice, selection and date fields. It uses Compose Runtime
  state, and `kotose-resources` brings Compose UI and Foundation transitively.
- **`kotose-form-phone`**: `PhoneNumberField`, `PhoneData`, countries, and the
  `PhoneNumberBackend` interface with `PhoneNumberBackend.current`. No parsing library. Both
  renderers expose it; forms that only use the core don't need it.
- **`kotose-form-phone-backend`**: optional. `LibPhoneNumberBackend` (libphonenumber on
  Android, PhoneNumberKit on iOS) and its Swift package. No other module depends on it.
- **`kotose-form`**: the Compose renderer, its field classes, appearance types, composables
  and the styled Compose DSL.
- **`kotose-form-swiftui`**: the SwiftUI renderer `KotoseNativeForm` and its Kotlin adapter
  `NativeFormAdapter`. Experimental as a whole.

## Experimental APIs

These need an explicit opt-in and can change in any release:

- `ExperimentalKotoseSwiftUIApi`: the whole SwiftUI adapter. Swift does not enforce Kotlin
  opt-ins, so `KotoseNativeForm` states it in its documentation.
- `ExperimentalKotoseFormApi`: `CustomField` (see [Custom fields](CUSTOM-FIELDS.md)) and
  implementing `PhoneNumberBackend`. Using `PhoneNumberBackend.current` needs no opt-in.

Native snapshots are read-only and built by the adapter; their constructors are internal, so
new snapshot data is not a breaking change.

## Compatibility policy

- A change to a non-experimental API keeps binary and source compatibility, or goes through a
  documented deprecation.
- Until 1.0, declarations may still move between artifacts while keeping their Kotlin
  packages. Rebuild your app after upgrading; previously compiled binaries are not guaranteed
  to keep working. The [changelog](../CHANGELOG.md) lists every migration.
- The checked-in ABI dumps (`<module>/api/`) record the published Kotlin surface, so every API
  change shows up in review. They do not cover the bundled Swift code; the iOS consumer build
  in CI does.
