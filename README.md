# Kotose Form KMP

A Kotlin Multiplatform form library. You define a form once in shared Kotlin: fields, groups,
validation, conditions and submission. It then renders natively on Android with Compose, and
on iOS with either Compose Multiplatform or native SwiftUI. Kotlin keeps the state and the
rules; each renderer only draws the platform controls.

## Modules

| Artifact | Contents |
| --- | --- |
| `io.github.robinpcrd:kotose-form-core` | Renderer-neutral model: `Form`, groups, fields, validation, conditions, navigation and the DSL |
| `io.github.robinpcrd:kotose-form-phone` | Phone number field and countries; without a backend it takes a typed dial code and number |
| `io.github.robinpcrd:kotose-form-phone-backend` | Optional: country picker, parsing, validation and formatting with libphonenumber on Android and PhoneNumberKit on iOS |
| `io.github.robinpcrd:kotose-form` | Compose renderer (`FormScreen`) for Android and iOS, styled fields and the legacy Compose DSL |
| `io.github.robinpcrd:kotose-form-swiftui` | Native SwiftUI renderer (`KotoseNativeForm`) and its Kotlin adapter (`NativeFormAdapter`), experimental |

## Features

- **Kotlin DSL** for text, password, phone, toggle, date and date-time fields, single and
  multiple selection, read-only text and actions, plus typed custom fields.
- **Validation** per field and per group, on demand or live, with warnings, silent errors and
  scroll to the first error.
- **Conditions** that show, hide, enable or disable fields from other values; hidden values
  are not submitted.
- **Keyboard navigation** with Next and Done across the form's inputs.
- **Presentations** for selections: inline, menu, sheet, page or segmented, with an optional
  iOS override. iOS dates use the compact picker or keyboard-style wheels.
- **Styling** through `KotoseFormStyle` on both renderers.

## Installation

Compose on Android and iOS:

```kotlin
commonMain.dependencies {
    implementation("io.github.robinpcrd:kotose-form:<version>")
}
```

Native SwiftUI uses the SwiftUI artifact, which brings the core and phone modules but not
Compose. Add it to the shared KMP module that builds the iOS framework; that module applies
[SKIE](https://skie.touchlab.co), builds one static framework and exports the SwiftUI artifact:

```kotlin
plugins { id("co.touchlab.skie") }

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            isStatic = true
            export("io.github.robinpcrd:kotose-form-swiftui:<version>")
        }
    }
    sourceSets.commonMain.dependencies {
        api("io.github.robinpcrd:kotose-form-swiftui:<version>")
    }
}
```

### iOS setup

- Use Kotlin 2.4.20 or later (Xcode 27 needs it), with a SKIE version that supports it.
- The Kotlin build phase in Xcode must run `embedAndSignAppleFrameworkForXcode`.
- The SwiftUI renderer's Swift sources are compiled into your framework, next to every Kotlin
  type it exports. Exporting `kotose-form-swiftui` alone is enough. A compile error in
  `KotoseNativeForm.swift` is a bug: please report it.
- To use your own custom fields from Swift, also export the module that defines them; see
  [Custom fields](docs/CUSTOM-FIELDS.md).

Only the optional phone backend needs a Swift package (PhoneNumberKit), through Kotlin's
[SwiftPM import](https://kotlinlang.org/docs/multiplatform/multiplatform-spm-import.html),
which is still Alpha. Connect it to your Xcode project once:

```sh
XCODEPROJ_PATH=/path/to/iosApp.xcodeproj ./gradlew :shared:integrateLinkagePackage
```

- The task finds the Kotlin build phase by the word `gradle` in its script. A phase that only
  calls a wrapper script fails with "embedAndSign integration wasn't found"; mentioning
  `gradle` in the phase, even in a comment, fixes it.
- It rewrites `project.pbxproj` in Xcode's canonical format, so expect a large formatting diff.
- The generated package targets iOS 15. Set `swiftPMDependencies { iosMinimumDeploymentTarget }`
  for another minimum.

### Phone numbers

Phone fields work without extra dependencies: the row takes a dial code, after a fixed `+`,
and the number, as digits. For the country picker, per-country validation and formatting as
the user types, add the backend and set it once at startup, before building forms:

```kotlin
commonMain.dependencies {
    implementation("io.github.robinpcrd:kotose-form-phone-backend:<version>")
}
```

```kotlin
PhoneNumberBackend.current = LibPhoneNumberBackend
```

On iOS the backend needs the Swift package step from [iOS setup](#ios-setup); apps without it
skip that step. See [Phone numbers](docs/PHONE-NUMBERS.md) for what each mode does.

## Platforms

- **Android** SDK 24+
- **iOS** arm64 and simulator arm64; the SwiftUI renderer needs iOS 16+

## Quick start

Define the form in shared Kotlin:

```kotlin
data class Plan(val code: Int)

val signUp = form {
    +group {
        +displayText(text = StrRes(text = "Create an account"), isTitle = true)
        +textField(tag = "name", label = StrRes(text = "Name")) { addRule(RequiredFieldRule()) }
        +secureTextField(tag = "password", label = StrRes(text = "Password"))
        +toggleField(tag = "terms", label = StrRes(text = "Accept the terms"))
    }
    +singleSelectionGroup<Plan> {
        tag = "plan"
        title = StrRes(text = "Plan")
        presentation = SelectionPresentation.Menu
        +selectionOption("basic", Plan(1), StrRes(text = "Basic"))
        +selectionOption("pro", Plan(2), StrRes(text = "Pro"))
    }
}

// On submit: an empty list means valid, and values are keyed by tag.
val errors = signUp.validate()
val values = signUp.allValues()
```

Render it with Compose:

```kotlin
@Composable
fun SignUpScreen() {
    FormScreen(form = signUp)
}
```

Or with SwiftUI, from a `NativeFormAdapter(signUp)` created in Kotlin. The adapter is
experimental: opt in with `@OptIn(ExperimentalKotoseSwiftUIApi::class)`.

```swift
struct SignUpView: View {
    let adapter: NativeFormAdapter

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                KotoseNativeForm(adapter: adapter, scrollProxy: proxy) { _, _ in AnyView(EmptyView()) }
                    .padding(.horizontal)
            }
        }
    }
}
```

The closure draws custom fields; see [Custom fields](docs/CUSTOM-FIELDS.md). A selection shown
as a page needs a `NavigationStack` around the form.

## Documentation

- [Overview](docs/OVERVIEW.md): field types, validation, conditions, values, runtime changes and focus
- [Selection presentations](docs/SELECTION.md): inline, menu, sheet, page and segmented selections
- [Date and time](docs/DATE-TIME.md): calendar values and the iOS pickers
- [Phone numbers](docs/PHONE-NUMBERS.md): the basic input and the optional backend
- [Styling](docs/STYLING.md): shared style tokens for both renderers
- [Custom fields](docs/CUSTOM-FIELDS.md): typed custom fields on Compose and SwiftUI
- [API stability](docs/API-STABILITY.md): stable and experimental APIs

The `example/` app shows every field on the three renderers, with a Compose and SwiftUI switch
on each page; see [Contributing](CONTRIBUTING.md) to run it.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for build, test, and contribution guidelines.

## License

```
Copyright 2025-present Robin PICARD and contributors.
Licensed under the Apache License, Version 2.0.
```

---

Originally created at [PandaLab](https://www.pandalab.fr/).
