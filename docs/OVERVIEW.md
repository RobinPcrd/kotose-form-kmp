# Overview

A form is a list of groups, and each group a list of fields. You build it once in shared Kotlin;
it owns the values, validation, conditions and focus. A renderer only draws it: `FormScreen`
with Compose on Android and iOS, or `KotoseNativeForm` with SwiftUI on iOS. One renderer shows
a form at a time, and an app can switch renderers at runtime.

```kotlin
val profile = form {
    +group {
        +displayText(text = title, isTitle = true)
        +textField(tag = "name", label = nameLabel) { addRule(RequiredFieldRule()) }
        +toggleField(tag = "newsletter", label = newsletterLabel)
        +textField(tag = "email", label = emailLabel) {
            isVisibleCondition = Condition("newsletter") { it.fieldValue<Boolean>("newsletter") == true }
        }
    }
}
```

`+` adds a field to its group, or a group to the form. A field's `tag` is its key in the
submitted values; labels are `StrRes` from `kotose-resources`.

## Field types

| DSL | Value | Shown as |
| --- | --- | --- |
| `textField` | `String` | Text input, single line or `TextLineLimits.MultiLine(minLines, maxLines)` |
| `secureTextField` | `String` | Masked input with a reveal button |
| `toggleField` | `Boolean` | Switch |
| `singleChoiceField` | `T` | One value from a list of `DropdownOption`s, as a menu by default |
| `singleSelectionGroup` with `selectionOption`s | `T` | A group of options, each with an optional `subtitle` |
| `multipleSelectionGroup` with `selectionOption`s | `List<T>` | A group of options, any number selected |
| `localDatePicker`, `dateTimePicker` | `LocalDate`, `LocalDateTime` | Date, or date and time, picker |
| `phoneNumberField` | `PhoneData`, submitted as `+33612345678` | Dial code or country, and number |
| `displayText`, `displayParagraph` | none | Text or title, and a tinted box of lines |
| `actionField` | none | Button running Kotlin code |
| Your own `CustomField` | your type | Your view on each renderer |

Guides: [selection presentations](SELECTION.md) (inline, menu, sheet, page, segmented),
[date and time](DATE-TIME.md), [phone numbers](PHONE-NUMBERS.md),
[custom fields](CUSTOM-FIELDS.md).

## Validation

- Add rules to a field with `addRule(...)`. `RequiredFieldRule()` is built in; a custom rule
  extends `ValidationRule.Field<T>` and returns a `ValidationError`, or null when valid.
- A `ValidationError` that overrides `asWarning` to true is a warning: shown, but it does not
  block submission. One that overrides `isSilent` to true blocks submission without a message.
- Built-in messages are settable once at startup, for example
  `ValidationError.Required.error = requiredLabel`.
- A single selection group with `isRequired = true` needs one option selected.
- `validationOption` sets when a field shows its errors: `ON_DEMAND` (default) when the form
  is validated, `ON_VALUE_CHANGE` after the first edit, `ALWAYS` from the start. A shown error
  clears as soon as the value is fixed.
- `form.validate()` returns the blocking errors and moves to the first one: it scrolls there
  and focuses the input. An empty list means the form is valid.

## Conditions

`isVisibleCondition` and `isEnabledCondition` show, hide, enable or disable a field or a group.
A `Condition` lists the tags it depends on and is evaluated again when one of them changes.
Hidden fields are not validated and not submitted. `CustomCondition` runs your own code
when tagged fields change.

## Values

- `form.allValues()` returns the visible values keyed by tag. A selection group with a `tag`
  submits its selected value (or list) under that tag.
- `form.setValues(mapOf("name" to "Ada"))` presets or resets fields.
- `form.onValueChange` and `form.events` report every change; `form.fieldValue<T>(tag)` and
  `form.getFieldByTag(tag)` read one field.

## Changing the form at runtime

Add or remove fields and groups after the form is built in a transaction. Its operations run
in order, then the renderers update once.

```kotlin
val notes = form.getGroupByTag("notes") ?: return
val note = TextField("note", noteLabel).apply { addRule(RequiredFieldRule()) }
val extra = GroupBuilder().apply { +toggleField("extra", label = extraLabel) }.build()

form.transaction {
    add(note, notes)              // at the end of the group, or at idx = 0
    remove(oldField)              // from its own group
    add(extra)                    // groups too, at the end or at an index
    remove(obsoleteGroup)
}
```

- Create the fields with their classes (`TextField(tag, label)`, `ToggleField(tag, label)`, ...),
  and groups with `GroupBuilder`, which takes the usual DSL.
- A field can only be added to a group that is already in the form; otherwise the transaction
  throws.
- Adding fields does not recheck the group's own rules, such as a required selection; pass
  `skipGroupValidation = false` to `add` to recheck them.
- `commit()` inside the block applies the operations queued so far. `transaction(reload = false)`
  skips the final update, for when you call `form.reload()` yourself.
- The structural methods on `Form`, `Group` and `Field` (`add`, `addField`, `remove`, ...) also
  work outside a transaction, but take `reload = false` by default: renderers only see the change
  after `form.reload()`. A transaction does that for you.

## Keyboard and focus

Next moves through the inputs in form order, skipping hidden and disabled ones, and Done on the
last one closes the keyboard. On iOS, every text input gets a keyboard bar with Previous, Next
and Done, in both renderers, which also covers the number and phone pads without a Return key;
Done closes the keyboard from any input, and `showsKeyboardBar` in the style turns the bar off.
`form.stopEditing()` closes the keyboard from code.

## Styling

`KotoseFormStyle` maps one brand to both renderers: colors, corner radius, borders and text
styles. See [Styling](STYLING.md).
