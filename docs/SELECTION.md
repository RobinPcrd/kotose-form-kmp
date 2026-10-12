# Selection presentations

A selection can be shown inline, as a menu, in a sheet, on a separate page or as a segmented
control. You pick the presentation once in the form definition, with an optional iOS override,
and each renderer draws its own platform control. Changing it does not change the value type,
validation or submission.

```kotlin
+singleSelectionGroup<Plan> {
    tag = "plan"
    title = planTitle
    presentation = SelectionPresentation.Inline      // every platform
    iosPresentation = SelectionPresentation.Page     // iOS, with Compose and SwiftUI
    +selectionOption("free", Plan.Free, freeLabel)
    +selectionOption("pro", Plan.Pro, proLabel)
}

+singleChoiceField(
    tag = "country",
    possibleValues = countries,
    placeholder = countryLabel,
    presentation = SelectionPresentation.Sheet,
)
```

- `presentation` defaults to `Inline` for groups and `Menu` for `singleChoiceField`.
- `iosPresentation` defaults to null, which means the same as `presentation`. It depends on
  the platform, not the renderer: an app that wants identical screens everywhere leaves it null.
- A group shown as anything but `Inline`, on any platform, needs a `title`: it labels the row
  and the presented list. Building the form fails otherwise, on every platform, so Android
  tests also catch an iOS-only mistake.

## What each presentation looks like

| Value | SwiftUI | Compose (Android and iOS) |
| --- | --- | --- |
| `Inline` | Options listed in a card, with a checkmark | Radio rows |
| `Menu` | Row whose value opens a system menu | Dropdown row opening a menu |
| `Sheet` | Row opening a sheet at medium and large heights, with Cancel | Dropdown row opening a bottom sheet |
| `Page` | Row pushing a list onto the navigation stack | Dropdown row opening a full-screen dialog with a close button |
| `Segmented` | Segmented control under the title | Segmented buttons under the title |

- Apart from `Inline`, the row shows the title and the selected label. Picking an option closes
  the menu, sheet or page. Tapping the selected option clears it only when the group allows
  deselection or the field is clearable, as with `Inline`.
- A disabled row does not open. An open sheet or page closes when the selection becomes hidden
  or disabled.
- Messages render under the row. Scrolling to an error targets the row.
- Custom option views apply to `Inline` only; the other presentations show labels and subtitles.

### Segmented

- Meant for a few short options: on iPhone, Apple suggests at most about five segments. The
  library does not enforce a count. Segments show labels only.
- Tapping the selected segment keeps it selected, even when the group allows deselection,
  because neither platform control can clear a selection.
- A disabled option is a disabled segment.

### Multiple selection

`multipleSelectionGroup` takes the same `presentation` and `iosPresentation`, limited to
`Inline`, `Sheet` and `Page`; building fails on `Menu` and `Segmented`.

- The row shows the selected labels, comma-separated.
- The sheet or page toggles an option on each tap and stays open. On SwiftUI the sheet closes
  with Done and the page with the back button; on Compose both close with the close button.

## SwiftUI pages need a navigation stack

`Page` on SwiftUI pushes a `NavigationLink`, which only works inside a `NavigationStack` or
`NavigationSplitView`. Without one, the row looks dimmed and does nothing, and the library
cannot detect it. Hosts without a SwiftUI stack (a bare `UIHostingController`, UIKit
navigation, a sheet without its own stack) should use `Sheet`, which needs nothing from the host.

Pushing the page hides the form, as leaving it does: the keyboard closes and revealed passwords
are masked again.

## Test identifiers

- The row uses the group key or the field key.
- Options keep their keys; a `singleChoiceField` option is `<field key>-option-<option id>`.
- The SwiftUI sheet's Cancel is `<key>-cancel` and the multiple selection sheet's Done is
  `<key>-done`. The Compose close button is `<key>-close`.
- Compose segments use the option ids. UIKit segments have no identifier; find them by text.
