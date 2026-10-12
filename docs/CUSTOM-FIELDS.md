# Custom fields

A custom field is your own class extending `Field<T>` and implementing the experimental
`CustomField` marker. The marker only gives a `kind`, used to pick the view, and an optional
`label`. The typed value, validation, conditions, submission and any domain methods stay on
your class.

```kotlin
@OptIn(ExperimentalKotoseFormApi::class)
class RatingField(override val label: StrRes) : Field<Int>("rating"), CustomField {
    override val kind = "rating"
    fun increment() { value = ((value ?: 0) + 1) % 6 }
    override fun setBaseValue(value: Any?) { this.value = (value as? Int)?.coerceIn(0, 5) }
}
```

Presets and programmatic updates go through `setBaseValue`.

## Compose

Draw it in the `fieldView` slot of `FormScreen`. The slot receives the field itself, so the view
reads its state and calls its methods directly:

```kotlin
FormScreen(form = form) { field ->
    when (field) {
        is RatingField -> RatingRow(field.value ?: 0, onClick = field::increment)
        else -> DefaultFieldView(field)
    }
}
```

A field can also implement `ComposeField` when its module uses the Compose compiler. A field
that is neither built in nor a `CustomField` fails in the default view.

## SwiftUI

`KotoseNativeForm` calls its closure for each custom field with the field's snapshot and the
adapter. The snapshot has `customKind`, the resolved `label` and the value's `toString()` in
`textValue`. `adapter.customField(key:)` returns the live Kotlin field while it is visible, so
Swift calls the same methods as Compose:

```swift
KotoseNativeForm(adapter: adapter, scrollProxy: proxy) { field, adapter in
    AnyView(
        Button("Rating: \(field.textValue)") {
            (adapter.customField(key: field.key) as? RatingField)?.increment()
        }
        .disabled(!field.enabled)
    )
}
```

- For the cast to compile, export the module that defines the field from the iOS framework,
  not only depend on it:

  ```kotlin
  target.binaries.framework {
      export(project(":model"))
  }
  sourceSets.commonMain.dependencies { api(project(":model")) }
  ```

- The view must honor `enabled` itself: `customField` also returns a disabled field, so the
  view can still read it.

## Limits

- The renderers follow the field's value, enabled and visible state, and validation. Other
  state the field keeps outside `value` is up to your view.
- Focus inside a custom view is up to the view.
- The contract is experimental (`ExperimentalKotoseFormApi`).
