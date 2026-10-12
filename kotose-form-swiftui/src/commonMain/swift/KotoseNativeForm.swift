/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

import SwiftUI
import UIKit

/// Experimental native controls backed by the same Kotlin Form used by the Compose renderer.
@available(iOS 16.0, *)
public struct KotoseNativeForm: View {
    private let adapter: NativeFormAdapter
    private let scrollProxy: ScrollViewProxy?
    private let customField: (NativeFieldSnapshot, NativeFormAdapter) -> AnyView
    @State private var snapshot: NativeFormSnapshot?
    @State private var revealedPasswords: Set<String> = []
    @State private var presentedSheet: PresentedSheet?
    @State private var activeWheels: WheelsTarget?
    @State private var countryQuery = ""
    @State private var focusedInput: InputFocus?
    @State private var inputs = NativeInputRegistry()
    @ScaledMetric private var dialCodeWidth: CGFloat = 56
    @Environment(\.kotoseFormStyle) private var style

    public init(
        adapter: NativeFormAdapter,
        scrollProxy: ScrollViewProxy? = nil,
        customField: @escaping (NativeFieldSnapshot, NativeFormAdapter) -> AnyView
    ) {
        self.adapter = adapter
        self.scrollProxy = scrollProxy
        self.customField = customField
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: style.fieldSpacing) {
            if let snapshot {
                ForEach(snapshot.groups, id: \.key) { group in
                    let sections = FieldSection.split(group)
                    let endsWithCard = sections.last?.isCard == true
                    VStack(alignment: .leading, spacing: style.fieldSpacing) {
                        ForEach(sections) { section in
                            switch section {
                            case .field(let field):
                                fieldView(field)
                            case .card(let header, let options):
                                // A card that ends the group takes the group messages as its footer.
                                let footer = endsWithCard && section.id == sections.last?.id ? group : nil
                                if group.presentation == .inline {
                                    optionCard(header: header, options: options, footer: footer)
                                } else {
                                    collapsedCard(group: group, options: options, footer: footer)
                                }
                            }
                        }
                        if !endsWithCard {
                            messages(group.validationMessages, ownerKey: group.key)
                                .id(group.key)
                        }
                    }
                }
            } else {
                ProgressView()
            }
        }
        .tint(style.accent)
        // Like the Compose renderer's hideKeyboardOnDrag: a user drag on the enclosing scroll view
        // clears focus, so UIKit slides the keyboard away. scrollDismissesKeyboard(.immediately)
        // removes it without animation and a SwiftUI DragGesture would steal the scroll.
        .background(ScrollDragObserver {
            inputs.focus(nil)
            activeWheels = nil
        })
        .task(id: ObjectIdentifier(adapter)) {
            for await value in adapter.snapshots {
                snapshot = value
            }
        }
        .onChange(of: snapshot?.navigationRequest?.id) { _ in
            if let request = snapshot?.navigationRequest {
                if adapter.isNavigationCurrent(requestId: request.id) {
                    reveal(request.targetKey)
                    inputs.focus(focusTarget(snapshot?.focusedFieldKey))
                }
                _ = adapter.completeNavigation(requestId: request.id)
            }
        }
        .onChange(of: snapshot?.focusedFieldKey) { key in
            inputs.focus(focusTarget(key))
        }
        .onChange(of: snapshot?.fields.map(\.key)) { keys in
            revealedPasswords.formIntersection(keys ?? [])
            if let key = activeWheels?.key, keys?.contains(key) != true { activeWheels = nil }
        }
        // Keyboard notifications only adjust scrolling. Editing delegates report actual focus,
        // including transfers that leave the keyboard visible and hardware-keyboard input.
        .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardDidShowNotification)) { _ in
            guard let key = focusedInput?.key ?? activeWheels?.key else { return }
            reveal(key)
        }
        .sheet(item: $presentedSheet) { sheet in
            switch sheet {
            case .phoneCountry(let key):
                phoneCountrySheet(key)
            case .selection(let key):
                NavigationStack {
                    SelectionList(adapter: adapter, key: key, style: style, snapshot: snapshot)
                        .toolbar {
                            // Multiple selection applies each tap, so its sheet closes with Done.
                            if snapshot.flatMap({ SelectionChoices.find(key, in: $0) })?.isMultiple == true {
                                ToolbarItem(placement: .confirmationAction) {
                                    Button("Done") { presentedSheet = nil }
                                        .accessibilityIdentifier(key + "-done")
                                }
                            } else {
                                ToolbarItem(placement: .cancellationAction) {
                                    Button("Cancel") { presentedSheet = nil }
                                        .accessibilityIdentifier(key + "-cancel")
                                }
                            }
                        }
                }
                .presentationDetents([.medium, .large])
            }
        }
        .onDisappear {
            inputs.focus(nil)
            revealedPasswords.removeAll()
            presentedSheet = nil
            activeWheels = nil
            adapter.stopEditing()
        }
    }

    private func phoneCountrySheet(_ key: String) -> some View {
        NavigationStack {
            if let field = snapshot?.fields.first(where: { $0.key == key }),
               let phone = field.phone {
                List(phone.countries.filter {
                    countryQuery.isEmpty || $0.label.localizedCaseInsensitiveContains(countryQuery) ||
                    $0.id.localizedCaseInsensitiveContains(countryQuery)
                }, id: \.id) { country in
                    Button(country.label) {
                        _ = adapter.selectPhoneCountry(key: field.key, region: country.id)
                        presentedSheet = nil
                    }
                    .disabled(!field.enabled || !phone.countrySelectionEnabled)
                    .accessibilityIdentifier(field.key + "-country-" + country.id)
                }
                .searchable(text: $countryQuery)
                .navigationTitle(Text("Country"))
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { presentedSheet = nil }
                    }
                }
            }
        }
    }

    private func focusNext(from field: NativeFieldSnapshot) {
        adapter.focusNext(key: field.key)
        inputs.focus(focusTarget(field.nextFocusKey, forward: true))
    }

    private func focusPrevious(from field: NativeFieldSnapshot) {
        adapter.focusPrevious(key: field.key)
        inputs.focus(focusTarget(field.previousFocusKey, forward: false))
    }

    private func reveal(_ key: String) {
        // An option or message of a collapsed group has no row of its own; its row stands in.
        let collapsed = snapshot?.groups.first { group in
            group.presentation != .inline &&
                (group.key == key || group.fields.contains { $0.isSelectionOption && $0.key == key })
        }
        scrollProxy?.scrollTo(collapsed.map(\.collapsedRowId) ?? key, anchor: .center)
    }

    /// The input to focus for a Kotlin field. `forward` is set when Next or Previous moves through
    /// the form, which then visits both inputs of a phone row without a country picker.
    private func focusTarget(_ key: String?, forward: Bool? = nil) -> InputFocus? {
        key.map { key in
            // Kotlin focuses fields, not inputs: keep whichever input of the field has focus.
            if let focusedInput, focusedInput.key == key, focusedInput.revealed == revealedPasswords.contains(key) {
                return focusedInput
            }
            // Next enters such a phone row on its dial code and Previous on its number. Other
            // focus starts on the dial code while it is empty.
            let phone = snapshot?.groups.lazy.flatMap(\.fields).first { $0.key == key }?.phone
            let dialCode = phone.map { !$0.hasCountryPicker && (forward ?? $0.dialCodeDigits.isEmpty) } ?? false
            return InputFocus(key: key, revealed: revealedPasswords.contains(key), dialCode: dialCode)
        }
    }

    private func textInput(_ field: NativeFieldSnapshot, revealed: Bool = false, dialCode: Bool = false) -> some View {
        let id = InputFocus(key: field.key, revealed: revealed, dialCode: dialCode)
        return NativeTextInput(
            field: field,
            id: id,
            inputs: inputs,
            style: style,
            onTextChanged: { text in
                if dialCode { return adapter.setPhoneDialCode(key: field.key, dialCode: text) }
                _ = adapter.setText(key: field.key, value: text)
                return nil
            },
            onFocusChanged: { focused in
                if focused { focusedInput = id }
                else if focusedInput == id { focusedInput = nil }
                adapter.onFocusChanged(key: field.key, focused: focused)
            },
            keys: keyboardActions(field, dialCode: dialCode)
        )
    }

    /// The keyboard bar of one input. A phone row without a country picker has two inputs, the
    /// dial code then the number; otherwise Kotlin supplies the previous and next fields.
    private func keyboardActions(_ field: NativeFieldSnapshot, dialCode: Bool = false) -> KeyboardActions {
        let typesDialCode = field.phone.map { !$0.hasCountryPicker } ?? false
        var previous: (() -> Void)?
        if typesDialCode && !dialCode {
            previous = { inputs.focus(InputFocus(key: field.key, dialCode: true)) }
        } else if field.previousFocusKey != nil {
            previous = { focusPrevious(from: field) }
        }
        var next: (() -> Void)?
        if dialCode {
            next = { inputs.focus(InputFocus(key: field.key)) }
        } else if field.nextFocusKey != nil {
            next = { focusNext(from: field) }
        }
        return KeyboardActions(previous: previous, next: next, done: { inputs.focus(nil) })
    }

    private func multilineInput(_ field: NativeFieldSnapshot, lines: NativeLineLimits) -> some View {
        let id = InputFocus(key: field.key)
        return NativeMultilineInput(
            field: field,
            lines: lines,
            id: id,
            inputs: inputs,
            style: style,
            onTextChanged: { _ = adapter.setText(key: field.key, value: $0) },
            onFocusChanged: { focused in
                if focused { focusedInput = id }
                else if focusedInput == id { focusedInput = nil }
                adapter.onFocusChanged(key: field.key, focused: focused)
            },
            keys: keyboardActions(field)
        )
    }

    private func fieldView(_ field: NativeFieldSnapshot) -> some View {
        VStack(alignment: .leading, spacing: style.messageSpacing) {
            control(field)
                .disabled(!field.enabled)
            messages(field.validationMessages, ownerKey: field.key)
        }
        .id(field.key)
    }

    /// Selection options as one card, like an inset grouped Settings section: the label field
    /// before them as header, full-width rows with separators, and messages as footer. Without
    /// the card, an option list with nothing selected reads as plain text lines.
    private func optionCard(
        header: CardHeader?,
        options: [NativeFieldSnapshot],
        footer group: NativeGroupSnapshot?
    ) -> some View {
        VStack(alignment: .leading, spacing: style.messageSpacing) {
            if let header {
                cardHeader(header)
            }
            VStack(spacing: 0) {
                ForEach(Array(options.enumerated()), id: \.element.key) { index, option in
                    if index > 0 {
                        Divider().padding(.leading, style.groupRowPadding.leading)
                    }
                    control(option)
                        .disabled(!option.enabled)
                        .id(option.key)
                }
            }
            .background(style.groupBackground)
            .clipShape(style.groupShape)
            VStack(alignment: .leading, spacing: style.messageSpacing) {
                ForEach(options, id: \.key) { option in
                    messages(option.validationMessages, ownerKey: option.key)
                }
                if let group {
                    messages(group.validationMessages, ownerKey: group.key)
                        .id(group.key)
                }
            }
            .padding(.horizontal, style.groupRowPadding.leading)
        }
    }

    /// Full-width tappable option row inside a card. The whole row is the hit target, unlike a
    /// `Toggle`, which only reacts on its switch knob. Kotlin decides whether the tap selects or
    /// deselects.
    private func selectionRow<Glyph: View>(
        _ field: NativeFieldSnapshot,
        leading: Bool,
        @ViewBuilder glyph: () -> Glyph
    ) -> some View {
        selectionRow(
            label: field.label,
            subtitle: field.subtitle,
            selected: field.selected,
            identifier: field.key,
            leading: leading,
            action: { _ = adapter.setSelected(key: field.key, selected: !field.selected) },
            glyph: glyph
        )
    }

    private func selectionRow<Glyph: View>(
        label: String,
        subtitle: String?,
        selected: Bool,
        identifier: String,
        leading: Bool,
        action: @escaping () -> Void,
        @ViewBuilder glyph: () -> Glyph
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 12) {
                if leading { glyph() }
                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                    if let subtitle {
                        Text(subtitle)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }
                .multilineTextAlignment(.leading)
                Spacer()
                if !leading { glyph() }
            }
            .padding(style.groupRowPadding)
            .frame(minHeight: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(SelectionRowButtonStyle(style: style))
        .accessibilityAddTraits(selected ? [.isSelected] : [])
        .accessibilityValue(selected ? "1" : "0")
        .accessibilityIdentifier(identifier)
    }

    private func cardHeader(_ header: CardHeader) -> some View {
        Text(header.text)
            .font(.footnote)
            .foregroundStyle(.secondary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, style.groupRowPadding.leading)
            .accessibilityAddTraits(.isHeader)
            .accessibilityIdentifier(header.key)
            .id(header.key)
    }

    /// A collapsed single selection group: its one row in a card, with every option and group
    /// message under it because the options themselves are not on screen.
    private func collapsedCard(
        group: NativeGroupSnapshot,
        options: [NativeFieldSnapshot],
        footer: NativeGroupSnapshot?
    ) -> some View {
        VStack(alignment: .leading, spacing: style.messageSpacing) {
            collapsedSelection(SelectionChoices(group: group))
            VStack(alignment: .leading, spacing: style.messageSpacing) {
                ForEach(options, id: \.key) { option in
                    messages(option.validationMessages, ownerKey: option.key)
                }
                if let footer {
                    messages(footer.validationMessages, ownerKey: footer.key)
                }
            }
            .padding(.horizontal, style.groupRowPadding.leading)
        }
        .id(group.collapsedRowId)
    }

    /// The row of a collapsed single selection: title, selected label and a chevron. A menu lists
    /// checkmark items; a sheet or a pushed page shows the options as a list.
    @ViewBuilder
    private func collapsedSelection(_ choices: SelectionChoices) -> some View {
        switch choices.style {
        case .menu:
            // Like a menu picker in a Form: the title stays, and only the trailing value is the
            // pop-up button, so the menu grows out of the value instead of swallowing the row.
            HStack(spacing: 12) {
                Text(choices.title)
                    .foregroundStyle(choices.enabled ? style.text : style.disabledText)
                    .accessibilityHidden(true)
                Spacer()
                Menu {
                    ForEach(choices.items) { item in
                        // A toggle is the menu's checkmark item; the tap keeps the inline toggle intent.
                        Toggle(isOn: Binding(get: { item.selected }, set: { _ in choices.tap(item, adapter: adapter) })) {
                            Text(item.label)
                            if let subtitle = item.subtitle { Text(subtitle) }
                        }
                        .disabled(!item.enabled)
                        .accessibilityIdentifier(item.identifier)
                    }
                } label: {
                    HStack(spacing: 4) {
                        if let value = choices.selectedLabel {
                            Text(value).lineLimit(1)
                        }
                        Image(systemName: "chevron.up.chevron.down")
                            .font(.footnote.weight(.semibold))
                    }
                    .frame(minWidth: 44, minHeight: 44, alignment: .trailing)
                    .contentShape(Rectangle())
                }
                .collapsedRowAccessibility(choices)
            }
            .padding(.leading, style.groupRowPadding.leading)
            .padding(.trailing, style.groupRowPadding.trailing)
            .background(style.groupBackground)
            .clipShape(style.groupShape)
        case .page:
            NavigationLink {
                SelectionList(adapter: adapter, key: choices.key, style: style, snapshot: snapshot)
            } label: {
                collapsedRowLabel(choices, chevron: "chevron.right")
            }
            .buttonStyle(SelectionRowButtonStyle(style: style))
            .collapsedRowAccessibility(choices)
            .background(style.groupBackground)
            .clipShape(style.groupShape)
        case .sheet:
            Button {
                presentedSheet = .selection(choices.key)
            } label: {
                collapsedRowLabel(choices, chevron: "chevron.up.chevron.down")
            }
            .buttonStyle(SelectionRowButtonStyle(style: style))
            .collapsedRowAccessibility(choices)
            .background(style.groupBackground)
            .clipShape(style.groupShape)
        case .segmented:
            // The title heads the control like an inline card; the control draws its own background.
            VStack(alignment: .leading, spacing: style.messageSpacing) {
                if !choices.title.isEmpty {
                    cardHeader(CardHeader(text: choices.title, key: choices.key + "-title"))
                }
                SegmentedControl(items: choices.items) { choices.tap($0, adapter: adapter) }
                    .frame(maxWidth: .infinity, minHeight: 44)
                    .accessibilityIdentifier(choices.key)
            }
        }
    }

    private func collapsedRowLabel(_ choices: SelectionChoices, chevron: String) -> some View {
        HStack(spacing: 12) {
            Text(choices.title)
            Spacer()
            if let value = choices.selectedLabel {
                Text(value)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
            Image(systemName: chevron)
                .font(.footnote.weight(.semibold))
                .foregroundStyle(.tertiary)
        }
        .padding(style.groupRowPadding)
        .frame(minHeight: 44)
        .contentShape(Rectangle())
    }

    /// Options of an inline single choice field, drawn like an inline group card under its label.
    private func choiceCard(_ field: NativeFieldSnapshot, _ choice: NativeChoiceSnapshot) -> some View {
        VStack(alignment: .leading, spacing: style.messageSpacing) {
            if !field.label.isEmpty {
                cardHeader(CardHeader(text: field.label, key: field.key + "-title"))
            }
            VStack(spacing: 0) {
                ForEach(Array(choice.options.enumerated()), id: \.element.id) { index, option in
                    if index > 0 {
                        Divider().padding(.leading, style.groupRowPadding.leading)
                    }
                    let selected = option.id == choice.selectedId
                    selectionRow(
                        label: option.label,
                        subtitle: nil,
                        selected: selected,
                        identifier: field.key + "-option-" + option.id,
                        leading: false,
                        action: {
                            _ = adapter.selectOption(key: field.key, optionId: selected && choice.isClearable ? nil : option.id)
                        },
                        glyph: { checkmark(selected) }
                    )
                }
            }
            .background(style.groupBackground)
            .clipShape(style.groupShape)
        }
    }

    private func choicePicker(_ field: NativeFieldSnapshot, _ choice: NativeChoiceSnapshot) -> some View {
        Picker(field.label, selection: Binding(
            get: { choice.selectedId ?? "" },
            set: { _ = adapter.selectOption(key: field.key, optionId: $0.isEmpty ? nil : $0) }
        )) {
            if choice.selectedId == nil || choice.isClearable {
                Text(field.label).tag("")
                    .disabled(!choice.isClearable)
                    .accessibilityIdentifier(field.key + "-clear")
            }
            ForEach(choice.options, id: \.id) { option in
                Text(option.label).tag(option.id)
                    .accessibilityIdentifier(field.key + "-option-" + option.id)
            }
        }
        .pickerStyle(.menu)
        .frame(minHeight: 44)
        .accessibilityIdentifier(field.key)
    }

    @ViewBuilder
    private func checkmark(_ selected: Bool) -> some View {
        if selected {
            Image(systemName: "checkmark")
                .fontWeight(.semibold)
                .foregroundStyle(.tint)
        }
    }

    /// Settings-style text row: when the placeholder differs from the label, the label leads inside
    /// the bordered row and a single-line input, placeholder included, aligns to the trailing edge.
    private func labeledInput<Input: View>(
        _ field: NativeFieldSnapshot,
        alignment: VerticalAlignment = .center,
        @ViewBuilder input: () -> Input
    ) -> some View {
        HStack(alignment: alignment, spacing: 12) {
            if field.showsRowLabel {
                Text(field.label)
                    .font(.system(style.inputTextStyle))
                    .foregroundStyle(field.enabled ? style.text : style.disabledText)
                    .fixedSize()
            }
            input()
        }
    }

    private func chrome(_ field: NativeFieldSnapshot) -> InputChrome {
        InputChrome(
            style: style,
            tone: FieldTone(field.validationMessages),
            focused: focusedInput?.key == field.key,
            enabled: field.enabled
        )
    }

    private func messages(_ messages: [NativeValidationMessage], ownerKey: String) -> some View {
        ForEach(Array(messages.enumerated()), id: \.offset) { index, message in
            Label {
                Text(message.text)
            } icon: {
                if style.showsMessageIcons {
                    Image(systemName: message.asWarning ? "exclamationmark.triangle" : "exclamationmark.circle")
                }
            }
            .font(.system(style.messageTextStyle))
            .foregroundStyle(message.asWarning ? style.warning : style.error)
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityIdentifier((message.asWarning ? "warning-" : "error-") + ownerKey + "-" + String(index))
        }
    }

    @ViewBuilder
    private func control(_ field: NativeFieldSnapshot) -> some View {
        switch field.kind {
        case .phone:
            if let phone = field.phone, !phone.hasCountryPicker {
                HStack {
                    textInput(field, dialCode: true)
                        .frame(width: dialCodeWidth)
                        .modifier(chrome(field))
                    textInput(field).modifier(chrome(field))
                }
            } else if let phone = field.phone {
                HStack {
                    Button(phone.flag + " " + phone.dialCode) {
                        countryQuery = ""
                        presentedSheet = .phoneCountry(field.key)
                    }
                    .disabled(!phone.countrySelectionEnabled)
                    .frame(minWidth: 44, minHeight: 44)
                    .accessibilityLabel(Text("Country"))
                    .accessibilityValue(phone.region + " " + phone.dialCode)
                    .accessibilityIdentifier(field.key + "-country")

                    textInput(field).modifier(chrome(field))
                }
            }
        case .password:
            passwordControl(field)
        case .multipleSelection:
            // iOS keeps switches for list-row settings (HIG "Toggles"); option lists mark selected
            // items with a checkmark. The leading circle is the List edit-mode multi-select glyph,
            // which tells these rows apart from the single-selection checkmarks below.
            selectionRow(field, leading: true) {
                Image(systemName: field.selected ? "checkmark.circle.fill" : "circle")
                    .font(.title3)
                    .foregroundStyle(field.selected ? AnyShapeStyle(.tint) : AnyShapeStyle(.secondary))
            }
        case .singleSelection:
            // iOS single selection is a checkmark list, like Settings. Same toggle semantics
            // as the Compose radio row; Kotlin decides whether a selected option may be deselected.
            selectionRow(field, leading: false) { checkmark(field.selected) }
        case .singleChoice:
            if let choice = field.choice {
                switch choice.presentation {
                case .inline:
                    choiceCard(field, choice)
                case .sheet, .page, .segmented:
                    if let choices = SelectionChoices(field: field) {
                        collapsedSelection(choices)
                    }
                case .menu:
                    choicePicker(field, choice)
                }
            }
        case .text:
            if let lines = field.lines {
                labeledInput(field, alignment: .top) { multilineInput(field, lines: lines) }
                    .modifier(chrome(field))
            } else {
                labeledInput(field) { textInput(field) }.modifier(chrome(field))
            }
        case .toggle:
            Toggle(field.label, isOn: Binding(
                get: { field.toggleValue },
                set: { _ = adapter.setToggle(key: field.key, value: $0) }
            ))
            .frame(minHeight: 44)
            .contentShape(Rectangle())
            // A plain Toggle only reacts on its knob. Like the Compose switch row, the whole row
            // toggles; taps on the knob stay with the switch, so they do not toggle twice.
            .onTapGesture {
                if field.enabled { _ = adapter.setToggle(key: field.key, value: !field.toggleValue) }
            }
            .accessibilityIdentifier(field.key)
        case .label:
            Text(field.label)
                .font(field.display?.isTitle == true ? style.titleFont : .system(style.inputTextStyle))
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityAddTraits(field.display?.isTitle == true ? [.isHeader] : [])
                .accessibilityIdentifier(field.key)
        case .paragraph:
            VStack(alignment: .leading, spacing: 4) {
                ForEach(Array((field.display?.lines ?? []).enumerated()), id: \.offset) { _, line in
                    Text(line).font(.callout)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
            .background(style.paragraphBackground, in: style.inputShape)
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier(field.key)
        case .action:
            Button(field.label) { _ = adapter.performAction(key: field.key) }
                .buttonStyle(.bordered)
                .frame(minHeight: 44)
                .accessibilityIdentifier(field.key)
        case .date, .dateTime:
            dateControl(field)
        case .custom:
            customField(field, adapter)
        }
    }

    @ViewBuilder
    private func dateControl(_ field: NativeFieldSnapshot) -> some View {
        if let date = field.date {
            if date.pickerMode == .wheels {
                wheelsDateRow(field, date)
            } else if date.hasValue {
                DatePicker(
                    field.label,
                    selection: Binding(
                        get: { dateValue(date) },
                        set: { applyDate($0, to: field) }
                    ),
                    displayedComponents: date.includesTime ? [.date, .hourAndMinute] : [.date]
                )
                .datePickerStyle(.compact)
                .accessibilityIdentifier(field.key)
            } else {
                // Like the Compose pickers, start from the current date; Kotlin then owns the value.
                Button(field.label) { applyDate(Date(), to: field) }
                    .frame(minHeight: 44)
                    .accessibilityIdentifier(field.key)
            }
        }
    }

    /// Wheels: the row shows the value in pills like the compact picker. The date pill brings up date
    /// wheels and the time pill time wheels in place of the keyboard, one set at a time; an empty
    /// field brings up the date wheels. One responder sits behind the whole row, so it stays
    /// mounted when the first turn of the wheels replaces the placeholder with pills, and switching
    /// pills only changes its picker's mode.
    private func wheelsDateRow(_ field: NativeFieldSnapshot, _ date: NativeDateSnapshot) -> some View {
        let dateTarget = WheelsTarget(key: field.key, time: false)
        let timeTarget = WheelsTarget(key: field.key, time: true)
        return HStack(spacing: 6) {
            if date.hasValue {
                Text(field.label)
                    .foregroundStyle(field.enabled ? style.text : style.disabledText)
                Spacer()
                wheelsPill(dateValue(date).formatted(date: .abbreviated, time: .omitted), field: field, active: activeWheels == dateTarget) {
                    activeWheels = dateTarget
                }
                .accessibilityIdentifier(field.key + "-date")
                if date.includesTime {
                    wheelsPill(dateValue(date).formatted(date: .omitted, time: .shortened), field: field, active: activeWheels == timeTarget) {
                        activeWheels = timeTarget
                    }
                    .accessibilityIdentifier(field.key + "-time")
                }
            } else {
                Button(field.label) { activeWheels = dateTarget }
                Spacer()
            }
        }
        .frame(minHeight: 44)
        .background(wheelsInput(field, date))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(field.key)
    }

    private func wheelsInput(_ field: NativeFieldSnapshot, _ date: NativeDateSnapshot) -> some View {
        let active = activeWheels?.key == field.key ? activeWheels : nil
        return DateWheelsInput(
            isActive: active != nil && field.enabled,
            anyActive: activeWheels != nil,
            date: dateValue(date),
            time: active?.time == true,
            onChange: { applyDate($0, to: field) },
            onDone: { if !date.hasValue { applyDate($0, to: field) } },
            onEnd: { if activeWheels?.key == field.key { activeWheels = nil } }
        )
    }

    /// The pill being edited takes the accent color, like the compact picker's.
    private func wheelsPill(_ text: String, field: NativeFieldSnapshot, active: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(text)
                .foregroundStyle(active ? style.accent : field.enabled ? style.text : style.disabledText)
                .padding(.horizontal, 11)
                .padding(.vertical, 6)
                .background(Color(uiColor: .tertiarySystemFill), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
        }
        .buttonStyle(.plain)
    }

    private func dateValue(_ date: NativeDateSnapshot) -> Date {
        guard date.hasValue else { return Date() }
        return Calendar.current.date(from: DateComponents(
            year: Int(date.year), month: Int(date.month), day: Int(date.day),
            hour: Int(date.hour), minute: Int(date.minute)
        )) ?? Date()
    }

    private func applyDate(_ date: Date, to field: NativeFieldSnapshot) {
        let parts = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
        _ = adapter.setDate(
            key: field.key,
            year: Int32(parts.year ?? 0), month: Int32(parts.month ?? 0), day: Int32(parts.day ?? 0),
            hour: Int32(parts.hour ?? 0), minute: Int32(parts.minute ?? 0)
        )
    }

    private func passwordControl(_ field: NativeFieldSnapshot) -> some View {
        let hidden = !revealedPasswords.contains(field.key)
        return HStack {
            // Keep both controls mounted so reveal can transfer focus without replacing the input.
            labeledInput(field) {
                ZStack {
                    textInput(field)
                        .opacity(hidden ? 1 : 0)
                        .allowsHitTesting(hidden)
                        .accessibilityHidden(!hidden)
                    textInput(field, revealed: true)
                        .opacity(hidden ? 0 : 1)
                        .allowsHitTesting(!hidden)
                        .accessibilityHidden(hidden)
                }
            }
            .modifier(chrome(field))
            Button {
                let wasFocused = focusedInput?.key == field.key
                if hidden { revealedPasswords.insert(field.key) }
                else { revealedPasswords.remove(field.key) }
                if wasFocused { inputs.focus(focusTarget(field.key)) }
            } label: {
                Label(hidden ? LocalizedStringKey("Show password") : LocalizedStringKey("Hide password"), systemImage: hidden ? "eye" : "eye.slash")
                    .labelStyle(.iconOnly)
                    .frame(minWidth: 44, minHeight: 44)
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier(field.key + "-reveal")
        }
    }
}

private struct InputFocus: Hashable {
    let key: String
    var revealed = false
    /// The typed dial code of a phone row without a country picker.
    var dialCode = false
}

/// A UIKit text input the registry can hand first responder to.
private protocol NativeInputView: UIView {
    var acceptsFocus: Bool { get }
}

/// Owns only native responders. Kotlin still supplies the next field and navigation requests.
@available(iOS 16.0, *)
private final class NativeInputRegistry {
    private final class Entry {
        weak var view: NativeInputView?
        init(_ view: NativeInputView) { self.view = view }
    }

    private var entries: [InputFocus: Entry] = [:]
    private var target: InputFocus?
    /// One bar for every input: UIKit keeps it in place when focus moves, while swapping
    /// accessories leaves SwiftUI's keyboard inset one bar too high.
    private(set) lazy var bar = KeyboardBar()

    func register(_ view: NativeInputView, for id: InputFocus) {
        entries[id] = Entry(view)
    }

    func unregister(_ view: NativeInputView, for id: InputFocus) {
        if entries[id]?.view === view { entries.removeValue(forKey: id) }
    }

    func focus(_ id: InputFocus?) {
        target = id
        if let id {
            guard let view = entries[id]?.view, view.window != nil, view.acceptsFocus else { return }
            // Let UIKit transfer first responder directly. Resigning the old input first starts
            // keyboard dismissal and changes the enclosing scroll view's safe area.
            if !view.isFirstResponder { view.becomeFirstResponder() }
        } else {
            entries.values.first { $0.view?.isFirstResponder == true }?.view?.resignFirstResponder()
        }
    }

    func didMount(_ id: InputFocus) {
        if target == id { focus(id) }
    }

    func didBeginEditing(_ id: InputFocus) { target = id }

    func isFocused(_ key: String) -> Bool {
        entries.contains { $0.key.key == key && $0.value.view?.isFirstResponder == true }
    }

    func didEndEditing(_ id: InputFocus) {
        if target == id { target = nil }
    }
}

/// A public UIKit delegate intercepts Return before SwiftUI can dismiss the keyboard.
@available(iOS 16.0, *)
private struct NativeTextInput: UIViewRepresentable {
    let field: NativeFieldSnapshot
    let id: InputFocus
    let inputs: NativeInputRegistry
    let style: KotoseFormStyle
    /// Returns the text to show instead, when Kotlin keeps a different value.
    let onTextChanged: (String) -> String?
    let onFocusChanged: (Bool) -> Void
    let keys: KeyboardActions

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> InputView {
        let view = InputView()
        // SwiftUI draws the chrome (InputChrome); UIKit only renders text and caret.
        view.borderStyle = .none
        view.backgroundColor = .clear
        view.adjustsFontForContentSizeCategory = true
        view.setContentHuggingPriority(.defaultLow, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        view.delegate = context.coordinator
        view.addTarget(context.coordinator, action: #selector(Coordinator.changed(_:)), for: .editingChanged)
        if id.dialCode {
            let plus = UILabel()
            plus.text = "+"
            plus.font = .preferredFont(forTextStyle: style.inputTextStyle.uiTextStyle)
            plus.adjustsFontForContentSizeCategory = true
            plus.sizeToFit()
            view.leftView = plus
            view.leftViewMode = .always
            view.accessibilityLabel = "Dial code"
        }
        inputs.register(view, for: id)
        view.onMount = { inputs.didMount(id) }
        return view
    }

    func updateUIView(_ view: InputView, context: Context) {
        context.coordinator.parent = self
        // Assign only what changed: rewriting input traits or the keyboard bar of a first
        // responder makes UIKit re-layout the keyboard, which re-enters SwiftUI and loops.
        func set<T: Equatable>(_ path: ReferenceWritableKeyPath<InputView, T>, _ value: T) {
            if view[keyPath: path] != value { view[keyPath: path] = value }
        }
        // Text first: a secure field only repaints its bullets for text present when secure entry
        // is switched on. Do not disturb the caret or an in-progress IME composition.
        let text = id.dialCode ? field.phone?.dialCodeDigits ?? "" : field.textValue
        if view.text != text, view.markedTextRange == nil {
            let selection = view.selectedTextRange
            view.text = text
            if view.isFirstResponder { view.selectedTextRange = selection }
        }
        set(\.font, .preferredFont(forTextStyle: style.inputTextStyle.uiTextStyle))
        set(\.textColor, UIColor(field.enabled ? style.text : style.disabledText))
        if let plus = view.leftView as? UILabel, plus.textColor != view.textColor { plus.textColor = view.textColor }
        set(\.tintColor, UIColor(style.accent))
        let placeholder = NSAttributedString(string: id.dialCode ? "" : field.placeholder, attributes: [.foregroundColor: UIColor(style.placeholder)])
        if view.attributedPlaceholder != placeholder { view.attributedPlaceholder = placeholder }
        let trailing: NSTextAlignment = view.effectiveUserInterfaceLayoutDirection == .rightToLeft ? .left : .right
        set(\.textAlignment, field.showsRowLabel && !id.dialCode ? trailing : .natural)
        set(\.accessibilityIdentifier, id.dialCode ? field.key + "-dial-code" : field.key)
        set(\.isEnabled, field.enabled)
        set(\.isSecureTextEntry, field.kind == .password && !id.revealed)
        let typesDialCode = field.phone.map { !$0.hasCountryPicker } ?? false
        set(\.keyboardType, typesDialCode ? .numberPad : field.kind == .phone ? .phonePad : .default)
        set(\.textContentType, id.dialCode ? nil : field.kind == .phone ? .telephoneNumber : field.kind == .password ? .password : nil)
        set(\.autocapitalizationType, field.kind == .password ? .none : .sentences)
        set(\.autocorrectionType, field.kind == .password ? .no : .default)
        set(\.returnKeyType, keys.next == nil ? .done : .next)
        let bar = style.showsKeyboardBar ? inputs.bar : nil
        if view.inputAccessoryView !== bar {
            view.inputAccessoryView = bar
            if view.isFirstResponder { view.reloadInputViews() }
        }
        if view.isFirstResponder { inputs.bar.update(keys, id: barId) }
    }

    private var barId: String { field.key + (id.dialCode ? "-dial-code" : "") }

    func sizeThatFits(_ proposal: ProposedViewSize, uiView: InputView, context: Context) -> CGSize? {
        CGSize(width: proposal.width ?? uiView.intrinsicContentSize.width, height: uiView.intrinsicContentSize.height)
    }

    static func dismantleUIView(_ view: InputView, coordinator: Coordinator) {
        coordinator.parent.inputs.unregister(view, for: coordinator.parent.id)
        view.delegate = nil
        view.onMount = {}
    }

    final class InputView: UITextField, NativeInputView {
        var onMount: () -> Void = {}
        var acceptsFocus: Bool { isEnabled }

        override func didMoveToWindow() {
            super.didMoveToWindow()
            if window != nil { onMount() }
        }
    }

    final class Coordinator: NSObject, UITextFieldDelegate {
        var parent: NativeTextInput

        init(_ parent: NativeTextInput) { self.parent = parent }

        @objc func changed(_ textField: UITextField) {
            let text = textField.text ?? ""
            if let shown = parent.onTextChanged(text), shown != text { textField.text = shown }
        }

        func textFieldDidBeginEditing(_ textField: UITextField) {
            parent.inputs.bar.update(parent.keys, id: parent.barId)
            parent.inputs.didBeginEditing(parent.id)
            parent.onFocusChanged(true)
        }

        func textFieldDidEndEditing(_ textField: UITextField) {
            // UIKit ends the old input before beginning the new one. Report blur after that
            // synchronous handoff, and do not clear Kotlin focus when revealing the same field.
            DispatchQueue.main.async { [weak self] in
                guard let self, !self.parent.inputs.isFocused(self.parent.id.key) else { return }
                self.parent.inputs.didEndEditing(self.parent.id)
                self.parent.onFocusChanged(false)
            }
        }

        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            (parent.keys.next ?? parent.keys.done)()
            return false
        }
    }
}

/// Multi-line text on a UITextView. Return inserts a newline, so only the keyboard bar moves
/// on. Height follows the content between the Kotlin min and max line counts, then the
/// view scrolls internally.
@available(iOS 16.0, *)
private struct NativeMultilineInput: UIViewRepresentable {
    let field: NativeFieldSnapshot
    let lines: NativeLineLimits
    let id: InputFocus
    let inputs: NativeInputRegistry
    let style: KotoseFormStyle
    let onTextChanged: (String) -> Void
    let onFocusChanged: (Bool) -> Void
    let keys: KeyboardActions

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> InputView {
        let view = InputView()
        // SwiftUI draws the chrome (InputChrome); UIKit only renders text and caret.
        view.backgroundColor = .clear
        view.textContainerInset = .zero
        view.textContainer.lineFragmentPadding = 0
        view.adjustsFontForContentSizeCategory = true
        view.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        view.delegate = context.coordinator
        inputs.register(view, for: id)
        view.onMount = { inputs.didMount(id) }
        return view
    }

    func updateUIView(_ view: InputView, context: Context) {
        context.coordinator.parent = self
        func set<T: Equatable>(_ path: ReferenceWritableKeyPath<InputView, T>, _ value: T) {
            if view[keyPath: path] != value { view[keyPath: path] = value }
        }
        if view.text != field.textValue, view.markedTextRange == nil {
            let selection = view.selectedTextRange
            view.text = field.textValue
            if view.isFirstResponder { view.selectedTextRange = selection }
        }
        set(\.font, .preferredFont(forTextStyle: style.inputTextStyle.uiTextStyle))
        set(\.textColor, UIColor(field.enabled ? style.text : style.disabledText))
        set(\.tintColor, UIColor(style.accent))
        view.placeholderLabel.font = view.font
        set(\.placeholderLabel.text, field.placeholder)
        set(\.placeholderLabel.textColor, UIColor(style.placeholder))
        set(\.placeholderLabel.isHidden, !field.textValue.isEmpty)
        set(\.accessibilityIdentifier, field.key)
        set(\.isEditable, field.enabled)
        set(\.isUserInteractionEnabled, field.enabled)
        set(\.minLines, Int(lines.minLines))
        set(\.maxLines, Int(lines.maxLines))
        let bar = style.showsKeyboardBar ? inputs.bar : nil
        if view.inputAccessoryView !== bar {
            view.inputAccessoryView = bar
            if view.isFirstResponder { view.reloadInputViews() }
        }
        if view.isFirstResponder { inputs.bar.update(keys, id: field.key) }
    }

    func sizeThatFits(_ proposal: ProposedViewSize, uiView: InputView, context: Context) -> CGSize? {
        let width = proposal.width ?? uiView.intrinsicContentSize.width
        return CGSize(width: width, height: uiView.height(forWidth: width))
    }

    static func dismantleUIView(_ view: InputView, coordinator: Coordinator) {
        coordinator.parent.inputs.unregister(view, for: coordinator.parent.id)
        view.delegate = nil
        view.onMount = {}
    }

    final class InputView: UITextView, NativeInputView {
        var onMount: () -> Void = {}
        var minLines = 1
        var maxLines = Int(Int32.max)
        let placeholderLabel = UILabel()
        var acceptsFocus: Bool { isEditable }

        override init(frame: CGRect, textContainer: NSTextContainer?) {
            super.init(frame: frame, textContainer: textContainer)
            placeholderLabel.numberOfLines = 1
            placeholderLabel.lineBreakMode = .byTruncatingTail
            placeholderLabel.adjustsFontForContentSizeCategory = true
            placeholderLabel.isAccessibilityElement = false
            addSubview(placeholderLabel)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

        override func didMoveToWindow() {
            super.didMoveToWindow()
            if window != nil { onMount() }
        }

        override func layoutSubviews() {
            super.layoutSubviews()
            placeholderLabel.frame = CGRect(x: 0, y: 0, width: bounds.width, height: lineHeight)
        }

        private var lineHeight: CGFloat { (font ?? .preferredFont(forTextStyle: .body)).lineHeight }

        /// Content height clamped to the line bounds. Scrolling turns on only past the maximum.
        func height(forWidth width: CGFloat) -> CGFloat {
            let content = sizeThatFits(CGSize(width: width, height: .greatestFiniteMagnitude)).height
            let lower = ceil(lineHeight * CGFloat(minLines))
            let cap = maxLines == Int(Int32.max) ? CGFloat.greatestFiniteMagnitude : ceil(lineHeight * CGFloat(maxLines))
            let scrolls = content > cap
            if isScrollEnabled != scrolls { isScrollEnabled = scrolls }
            return min(max(ceil(content), lower), cap)
        }
    }

    final class Coordinator: NSObject, UITextViewDelegate {
        var parent: NativeMultilineInput

        init(_ parent: NativeMultilineInput) { self.parent = parent }

        func textViewDidChange(_ textView: UITextView) {
            (textView as? InputView)?.placeholderLabel.isHidden = !textView.text.isEmpty
            textView.invalidateIntrinsicContentSize()
            parent.onTextChanged(textView.text ?? "")
        }

        func textViewDidBeginEditing(_ textView: UITextView) {
            parent.inputs.bar.update(parent.keys, id: parent.field.key)
            parent.inputs.didBeginEditing(parent.id)
            parent.onFocusChanged(true)
        }

        func textViewDidEndEditing(_ textView: UITextView) {
            // Same handoff as the single-line input: report blur after UIKit moved first responder.
            DispatchQueue.main.async { [weak self] in
                guard let self, !self.parent.inputs.isFocused(self.parent.id.key) else { return }
                self.parent.inputs.didEndEditing(self.parent.id)
                self.parent.onFocusChanged(false)
            }
        }
    }
}

/// What the keyboard bar of one input does. A nil action disables its button.
private struct KeyboardActions {
    let previous: (() -> Void)?
    let next: (() -> Void)?
    let done: () -> Void
}

/// Previous, Next and Done above the keyboard of every text input. The system draws the bar:
/// Liquid Glass buttons over the form from iOS 26, a toolbar background before.
@available(iOS 16.0, *)
private final class KeyboardBar: UIToolbar {
    private var actions = KeyboardActions(previous: nil, next: nil, done: {})
    private lazy var previousItem = UIBarButtonItem(
        image: UIImage(systemName: "chevron.up"), style: .plain, target: self, action: #selector(previousTapped)
    )
    private lazy var nextItem = UIBarButtonItem(
        image: UIImage(systemName: "chevron.down"), style: .plain, target: self, action: #selector(nextTapped)
    )
    private lazy var doneItem = UIBarButtonItem(
        title: String(localized: "Done"), style: .done, target: self, action: #selector(doneTapped)
    )

    init() {
        super.init(frame: CGRect(x: 0, y: 0, width: 0, height: 44))
        previousItem.accessibilityLabel = String(localized: "Previous")
        nextItem.accessibilityLabel = String(localized: "Next")
        items = [previousItem, nextItem, .flexibleSpace(), doneItem]
        sizeToFit()
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

    /// Assigns only what changed: rewriting the items of a first responder's accessory makes
    /// UIKit re-layout the keyboard, which re-enters SwiftUI and loops.
    func update(_ actions: KeyboardActions, id: String) {
        self.actions = actions
        func set(_ item: UIBarButtonItem, enabled: Bool, id: String) {
            if item.isEnabled != enabled { item.isEnabled = enabled }
            if item.accessibilityIdentifier != id { item.accessibilityIdentifier = id }
        }
        set(previousItem, enabled: actions.previous != nil, id: id + "-previous")
        set(nextItem, enabled: actions.next != nil, id: id + "-next")
        set(doneItem, enabled: true, id: id + "-done")
    }

    @objc private func previousTapped() { actions.previous?() }
    @objc private func nextTapped() { actions.next?() }
    @objc private func doneTapped() { actions.done() }
}

/// Bridges the nearest enclosing UIScrollView: reports the start of a user pan (programmatic
/// scrolls do not trigger it) and pulls the content back when the keyboard hides, which SwiftUI
/// leaves scrolled past the end when focus was cleared without a scroll gesture.
private struct ScrollDragObserver: UIViewRepresentable {
    let onDragStart: () -> Void

    func makeUIView(context: Context) -> ObserverView {
        let view = ObserverView()
        view.isUserInteractionEnabled = false
        return view
    }

    func updateUIView(_ view: ObserverView, context: Context) {
        view.onDragStart = onDragStart
    }

    final class ObserverView: UIView {
        var onDragStart: () -> Void = {}
        private weak var observed: UIScrollView?

        override func didMoveToWindow() {
            super.didMoveToWindow()
            if window == nil {
                observed?.panGestureRecognizer.removeTarget(self, action: nil)
                observed = nil
                NotificationCenter.default.removeObserver(self)
                return
            }
            guard observed == nil else { return }
            var candidate = superview
            while let view = candidate, !(view is UIScrollView) { candidate = view.superview }
            guard let scrollView = candidate as? UIScrollView else { return }
            scrollView.panGestureRecognizer.addTarget(self, action: #selector(panned(_:)))
            observed = scrollView
            NotificationCenter.default.addObserver(
                self, selector: #selector(keyboardWillHide(_:)),
                name: UIResponder.keyboardWillHideNotification, object: nil
            )
        }

        @objc private func panned(_ recognizer: UIPanGestureRecognizer) {
            if recognizer.state == .began { onDragStart() }
        }

        @objc private func keyboardWillHide(_ notification: Foundation.Notification) {
            guard let scrollView = observed else { return }
            let inset = scrollView.adjustedContentInset
            let maxY = scrollView.contentSize.height + inset.bottom - scrollView.bounds.height
            let target = max(min(scrollView.contentOffset.y, maxY), -inset.top)
            guard target != scrollView.contentOffset.y else { return }
            let duration = notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double
            UIView.animate(withDuration: duration ?? 0.25) { scrollView.contentOffset.y = target }
        }
    }
}

extension NativeFieldSnapshot {
    /// A distinct placeholder fills the input, so the label leads the text row.
    var showsRowLabel: Bool { !label.isEmpty && label != placeholder }

    var isSelectionOption: Bool { kind == .singleSelection || kind == .multipleSelection }
}

/// Header of an option card: the selection group's title, or a label field right before the options.
private struct CardHeader {
    let text: String
    let key: String
}

/// A group's fields in render order: plain fields, and cards made of each run of consecutive
/// selection options. The group title heads its first card; otherwise a non-title label field right
/// before a run becomes that card's header.
@available(iOS 16.0, *)
private enum FieldSection: Identifiable {
    case field(NativeFieldSnapshot)
    case card(header: CardHeader?, options: [NativeFieldSnapshot])

    var id: String {
        switch self {
        case .field(let field): return field.key
        case .card(_, let options): return "card-" + (options.first?.key ?? "")
        }
    }

    var isCard: Bool {
        if case .card = self { return true }
        return false
    }

    static func split(_ group: NativeGroupSnapshot) -> [FieldSection] {
        var sections: [FieldSection] = []
        var title = group.title.map { CardHeader(text: $0, key: group.key + "-title") }
        for field in group.fields {
            guard field.kind == .singleSelection || field.kind == .multipleSelection else {
                sections.append(.field(field))
                continue
            }
            switch sections.last {
            case .card(let header, let options):
                sections[sections.count - 1] = .card(header: header, options: options + [field])
            case .field(let previous) where title == nil && previous.kind == .label && previous.display?.isTitle != true:
                sections[sections.count - 1] = .card(header: CardHeader(text: previous.label, key: previous.key), options: [field])
            default:
                sections.append(.card(header: title, options: [field]))
                title = nil
            }
        }
        return sections
    }
}

/// Option row press and disabled states. `.plain` would draw no pressed feedback on a card row.
@available(iOS 16.0, *)
private struct SelectionRowButtonStyle: ButtonStyle {
    let style: KotoseFormStyle

    func makeBody(configuration: ButtonStyleConfiguration) -> some View {
        Row(configuration: configuration, style: style)
    }

    private struct Row: View {
        let configuration: ButtonStyleConfiguration
        let style: KotoseFormStyle
        @Environment(\.isEnabled) private var isEnabled

        var body: some View {
            configuration.label
                .foregroundStyle(isEnabled ? style.text : style.disabledText)
                .background(configuration.isPressed ? Color(uiColor: .systemGray4) : Color.clear)
        }
    }
}

/// A sheet shown over the form; one `sheet(item:)` serves every kind.
private enum PresentedSheet: Identifiable {
    case phoneCountry(String)
    case selection(String)

    var id: String {
        switch self {
        case .phoneCountry(let key): return "phone-" + key
        case .selection(let key): return "selection-" + key
        }
    }
}

extension NativeGroupSnapshot {
    /// Scroll identity of a collapsed selection group's row.
    fileprivate var collapsedRowId: String { key + "-selection" }
}

/// A single selection as its collapsed row and presented list read it: the options of a collapsed
/// group or of a single choice field. Kotlin keeps the selection rules; a tap only reports intent.
private struct SelectionChoices {
    struct Item: Identifiable {
        let id: String
        let label: String
        let subtitle: String?
        let selected: Bool
        let enabled: Bool
        let identifier: String
    }

    /// Consumers may export only this module, so Swift never names the core presentation type.
    enum Style { case menu, sheet, page, segmented }

    let key: String
    let title: String
    let style: Style
    let items: [Item]
    /// A multiple selection keeps its list open on a tap.
    let isMultiple: Bool
    private let choice: NativeChoiceSnapshot?

    var enabled: Bool { items.contains { $0.enabled } }
    /// The selected labels, comma-separated when several options are selected.
    var selectedLabel: String? {
        let labels = items.filter(\.selected).map(\.label)
        return labels.isEmpty ? nil : labels.joined(separator: ", ")
    }

    init(group: NativeGroupSnapshot) {
        key = group.key
        title = group.title ?? ""
        isMultiple = group.fields.contains { $0.kind == .multipleSelection }
        switch group.presentation {
        case .menu: style = .menu
        case .page: style = .page
        case .segmented: style = .segmented
        default: style = .sheet
        }
        items = group.fields.filter(\.isSelectionOption).map {
            Item(id: $0.key, label: $0.label, subtitle: $0.subtitle, selected: $0.selected, enabled: $0.enabled, identifier: $0.key)
        }
        choice = nil
    }

    init?(field: NativeFieldSnapshot) {
        guard let choice = field.choice else { return nil }
        key = field.key
        title = field.label
        isMultiple = false
        switch choice.presentation {
        case .menu: style = .menu
        case .page: style = .page
        case .segmented: style = .segmented
        default: style = .sheet
        }
        items = choice.options.map {
            Item(
                id: $0.id, label: $0.label, subtitle: nil, selected: $0.id == choice.selectedId,
                enabled: field.enabled, identifier: field.key + "-option-" + $0.id
            )
        }
        self.choice = choice
    }

    static func find(_ key: String, in snapshot: NativeFormSnapshot) -> SelectionChoices? {
        if let group = snapshot.groups.first(where: { $0.key == key && $0.presentation != .inline }) {
            return SelectionChoices(group: group)
        }
        return snapshot.fields.first { $0.key == key && $0.kind == .singleChoice }.flatMap(SelectionChoices.init(field:))
    }

    /// The same intent as an inline row: Kotlin decides whether tapping the selected option clears it.
    func tap(_ item: Item, adapter: NativeFormAdapter) {
        if let choice {
            _ = adapter.selectOption(key: key, optionId: item.selected && choice.isClearable ? nil : item.id)
        } else {
            _ = adapter.setSelected(key: item.id, selected: !item.selected)
        }
    }
}

@available(iOS 16.0, *)
extension View {
    /// VoiceOver reads the title and the selected option; UI tests find the row by its key.
    fileprivate func collapsedRowAccessibility(_ choices: SelectionChoices) -> some View {
        disabled(!choices.enabled)
            .accessibilityLabel(choices.title)
            .accessibilityValue(choices.selectedLabel ?? "")
            .accessibilityIdentifier(choices.key)
    }
}

/// Options of a collapsed selection, on a sheet or a pushed page. Pushing hides the form, which
/// cancels its snapshot task, so the list follows the adapter itself. A single selection closes
/// once an option is tapped; a multiple selection stays open. Both close when the selection is
/// hidden or disabled.
@available(iOS 16.0, *)
private struct SelectionList: View {
    let adapter: NativeFormAdapter
    let key: String
    let style: KotoseFormStyle
    @State var snapshot: NativeFormSnapshot?
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let choices = snapshot.flatMap { SelectionChoices.find(key, in: $0) }
        List(choices?.items ?? []) { item in
            Button {
                choices?.tap(item, adapter: adapter)
                if choices?.isMultiple != true { dismiss() }
            } label: {
                HStack(spacing: 12) {
                    // A List button tints its label; the row keeps the form's text color instead.
                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.label)
                        if let subtitle = item.subtitle {
                            Text(subtitle)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .foregroundStyle(item.enabled ? style.text : style.disabledText)
                    .multilineTextAlignment(.leading)
                    Spacer()
                    if item.selected {
                        Image(systemName: "checkmark")
                            .fontWeight(.semibold)
                            .foregroundStyle(.tint)
                    }
                }
                .contentShape(Rectangle())
            }
            .disabled(!item.enabled)
            .accessibilityAddTraits(item.selected ? [.isSelected] : [])
            .accessibilityValue(item.selected ? "1" : "0")
            .accessibilityIdentifier(item.identifier)
        }
        .navigationTitle(choices?.title ?? "")
        .navigationBarTitleDisplayMode(.inline)
        .tint(style.accent)
        .task(id: ObjectIdentifier(adapter)) {
            for await value in adapter.snapshots {
                snapshot = value
            }
        }
        .onChange(of: choices?.enabled != true) { gone in
            if gone { dismiss() }
        }
    }
}

/// A UIKit segmented control: a segmented Picker ignores disabled segments, this draws them and
/// supports no selection. Tapping the selected segment sends nothing, like the platform control.
@available(iOS 16.0, *)
private struct SegmentedControl: UIViewRepresentable {
    let items: [SelectionChoices.Item]
    let onSelect: (SelectionChoices.Item) -> Void

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> UISegmentedControl {
        let control = UISegmentedControl()
        control.setContentHuggingPriority(.defaultLow, for: .horizontal)
        control.addTarget(context.coordinator, action: #selector(Coordinator.changed(_:)), for: .valueChanged)
        return control
    }

    func updateUIView(_ control: UISegmentedControl, context: Context) {
        context.coordinator.parent = self
        let titles = items.map(\.label)
        if (0..<control.numberOfSegments).map({ control.titleForSegment(at: $0) ?? "" }) != titles {
            control.removeAllSegments()
            for (index, title) in titles.enumerated() {
                control.insertSegment(withTitle: title, at: index, animated: false)
            }
        }
        for (index, item) in items.enumerated() {
            control.setEnabled(item.enabled, forSegmentAt: index)
        }
        control.isEnabled = context.environment.isEnabled
        control.selectedSegmentIndex = items.firstIndex(where: \.selected) ?? UISegmentedControl.noSegment
    }

    final class Coordinator: NSObject {
        var parent: SegmentedControl

        init(_ parent: SegmentedControl) {
            self.parent = parent
        }

        @objc func changed(_ control: UISegmentedControl) {
            guard parent.items.indices.contains(control.selectedSegmentIndex) else { return }
            parent.onSelect(parent.items[control.selectedSegmentIndex])
        }
    }
}

/// The date or time wheels of one date field, shown in place of the keyboard.
private struct WheelsTarget: Hashable {
    let key: String
    let time: Bool
}

/// A hidden first responder whose keyboard is date or time wheels under a Done bar, drawn on the
/// keyboard background. Turning the wheels applies the value; Done closes them. It never resigns
/// while other wheels are taking over, so UIKit swaps the input views without hiding them.
@available(iOS 16.0, *)
private struct DateWheelsInput: UIViewRepresentable {
    let isActive: Bool
    let anyActive: Bool
    let date: Date
    let time: Bool
    let onChange: (Date) -> Void
    let onDone: (Date) -> Void
    let onEnd: () -> Void

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> ResponderView {
        let view = ResponderView()
        view.isUserInteractionEnabled = false
        view.picker.addTarget(context.coordinator, action: #selector(Coordinator.changed(_:)), for: .valueChanged)
        view.doneButton.addTarget(context.coordinator, action: #selector(Coordinator.done), for: .touchUpInside)
        view.onResign = { [weak coordinator = context.coordinator] in
            // Resigning can happen inside a SwiftUI update; report it after that update.
            DispatchQueue.main.async { coordinator?.parent.onEnd() }
        }
        context.coordinator.view = view
        return view
    }

    func updateUIView(_ view: ResponderView, context: Context) {
        context.coordinator.parent = self
        let mode: UIDatePicker.Mode = time ? .time : .date
        if view.picker.datePickerMode != mode { view.picker.datePickerMode = mode }
        if view.picker.date != date { view.picker.date = date }
        if isActive, !view.isFirstResponder {
            view.becomeFirstResponder()
        } else if !isActive, !anyActive, view.isFirstResponder {
            view.resignFirstResponder()
        }
    }

    static func dismantleUIView(_ view: ResponderView, coordinator: Coordinator) {
        if view.isFirstResponder { view.resignFirstResponder() }
    }

    final class ResponderView: UIView {
        let picker = UIDatePicker()
        let doneButton = UIButton(type: .system)
        var onResign: () -> Void = {}

        /// The Done bar and the wheels in one keyboard-style view, instead of a toolbar accessory,
        /// which Liquid Glass leaves transparent and too short for its button.
        private lazy var wheels: UIInputView = {
            picker.preferredDatePickerStyle = .wheels
            picker.translatesAutoresizingMaskIntoConstraints = false
            doneButton.setTitle(String(localized: "Done"), for: .normal)
            doneButton.titleLabel?.font = .preferredFont(forTextStyle: .headline)
            doneButton.accessibilityIdentifier = "date-wheels-done"
            doneButton.translatesAutoresizingMaskIntoConstraints = false
            let container = UIInputView(frame: CGRect(x: 0, y: 0, width: 0, height: 260), inputViewStyle: .keyboard)
            container.allowsSelfSizing = true
            container.addSubview(doneButton)
            container.addSubview(picker)
            NSLayoutConstraint.activate([
                doneButton.topAnchor.constraint(equalTo: container.topAnchor, constant: 4),
                doneButton.trailingAnchor.constraint(equalTo: container.layoutMarginsGuide.trailingAnchor),
                doneButton.heightAnchor.constraint(equalToConstant: 44),
                picker.topAnchor.constraint(equalTo: doneButton.bottomAnchor),
                picker.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                picker.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                picker.bottomAnchor.constraint(equalTo: container.safeAreaLayoutGuide.bottomAnchor),
            ])
            return container
        }()

        override var canBecomeFirstResponder: Bool { true }
        override var inputView: UIView? { wheels }

        override func resignFirstResponder() -> Bool {
            let resigned = super.resignFirstResponder()
            if resigned { onResign() }
            return resigned
        }
    }

    final class Coordinator: NSObject {
        var parent: DateWheelsInput
        weak var view: ResponderView?

        init(_ parent: DateWheelsInput) { self.parent = parent }

        @objc func changed(_ picker: UIDatePicker) { parent.onChange(picker.date) }

        @objc func done() {
            guard let view else { return }
            parent.onDone(view.picker.date)
            view.resignFirstResponder()
        }
    }
}
