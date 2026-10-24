# Styling

Both renderers draw their fields from one set of visual tokens named `KotoseFormStyle`: a
Kotlin data class for Compose and a Swift struct for SwiftUI, with the same token names, so an
app can map one brand to both. Kotlin still owns the behavior (enabled, errors, warnings,
focus, labels and actions); the style only decides how it is drawn.

## Compose (Android and iOS)

Every token defaults to the current `MaterialTheme` through `KotoseFormStyle.fromMaterial()`,
so a form inside the app's `MaterialTheme` already follows it. `KotoseFormTheme(style) { ... }`
overrides the tokens below it, and `kotoseFormStyle()` reads the style in effect.

```kotlin
val brand = KotoseFormStyle.fromMaterial().copy(cornerRadius = 12.dp, inputBorder = Color.Transparent)
KotoseFormTheme(brand) { FormScreen(form = form) }
```

The per-kind `Appearance` objects and `FieldDefaults` still work. To draw a field yourself, use the `fieldView` slot of `FormScreen`, and fall back to
`DefaultFieldView(field)` for the others.

## SwiftUI

`KotoseFormStyle` is a Swift struct whose default reproduces the platform look. Set it with
`.kotoseFormStyle(_:)` on any ancestor of a `KotoseNativeForm`, typically once at the app root.

```swift
var brand = KotoseFormStyle()
brand.accent = .indigo
brand.cornerRadius = 12
brand.inputBackground = Color(uiColor: .secondarySystemBackground)
brand.inputBorder = .clear

NavigationStack { RootView() }
    .kotoseFormStyle(brand)
```

Text, password and phone inputs share one SwiftUI container (background, rounded shape, and a
border that takes the error or warning color, or the focused color while editing), so they
match the other rows. Toggles, pickers, checkmarks and date pickers stay native controls and
take the accent color.

## Tokens

| Token | Compose default | SwiftUI default |
| --- | --- | --- |
| `accent` | `primary` | app tint |
| `text`, `placeholder` | `onSurface`, `onSurfaceVariant` at 60 percent | label, placeholder text |
| `disabledText`, `disabledBackground` | `onSurface` at 60 percent, `onSurface` at 6 percent | secondary label, gray 6 |
| `inputBackground`, `inputBorder`, `focusedBorder` | transparent, `outline`, `primary` | system background, gray 4, gray 4 |
| `error`, `warning` | `error`, harmonized warning | red, orange |
| `paragraphBackground` | `primary` at 10 percent | tint at 10 percent |
| `cornerRadius`, `borderWidth` | 4 dp, 1 dp | 8 pt, 1 pt |
| `inputTextStyle`, `messageTextStyle` | `bodyLarge`, `labelLarge` semibold | body, callout |
| Title: `titleTextStyle` (Compose), `titleFont` (SwiftUI) | `titleLarge` | title2 semibold |
| `showsMessageIcons` | on | on |
| `showsKeyboardBar` (Previous, Next and Done above the iOS keyboard) | on (iOS only) | on |
| `inputPadding`, `fieldSpacing`, `messageSpacing` | SwiftUI only | 8 by 10 pt, 16 pt, 4 pt |
| `groupBackground`, `groupCornerRadius`, `groupRowPadding` (selection cards) | SwiftUI only | secondary system background, 12 pt, 11 by 16 pt |

Known gaps: Compose has no padding or spacing tokens yet, so Material inputs stay taller than
the iOS ones. The Compose phone, dropdown, date and date-time fields take only `cornerRadius` and
`warning` from the style; their other colors come from the `FieldColors` they receive,
`FieldDefaults.fieldColors()` by default, which follows `MaterialTheme`.
