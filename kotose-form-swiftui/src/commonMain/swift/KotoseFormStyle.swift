/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

import SwiftUI
import UIKit

/// Visual tokens for the SwiftUI renderer. Kotlin owns every semantic (enabled, error, warning,
/// focus, labels); this only decides how the renderer draws them. Set it once on the form with
/// `.kotoseFormStyle(_:)`; the default reproduces the platform look.
@available(iOS 16.0, *)
public struct KotoseFormStyle {
    /// Tint for native controls: toggles, pickers, checkmarks, buttons and the text caret.
    /// Defaults are UIKit dynamic colors: they convert back to UIColor for the text inputs and
    /// follow light and dark appearance, which SwiftUI semantic colors do not when converted.
    public var accent: Color = Color(uiColor: .tintColor)
    public var text: Color = Color(uiColor: .label)
    public var placeholder: Color = Color(uiColor: .placeholderText)
    public var inputBackground: Color = Color(uiColor: .systemBackground)
    public var inputBorder: Color = Color(uiColor: .systemGray4)
    /// Border of the input being edited. Defaults to the plain border, like iOS text fields.
    public var focusedBorder: Color = Color(uiColor: .systemGray4)
    /// Disabled inputs keep their text readable on a greyed container instead of fading like a placeholder.
    public var disabledText: Color = Color(uiColor: .secondaryLabel)
    public var disabledBackground: Color = Color(uiColor: .systemGray6)
    public var error: Color = .red
    public var warning: Color = .orange
    public var paragraphBackground: Color = Color(uiColor: .tintColor).opacity(0.1)
    /// Card behind selection options, drawn like an inset grouped section.
    public var groupBackground: Color = Color(uiColor: .secondarySystemBackground)
    public var groupCornerRadius: CGFloat = 12
    /// Padding of one option row inside the card; the leading inset also aligns the header,
    /// the separators and the footer messages with the row text.
    public var groupRowPadding = EdgeInsets(top: 11, leading: 16, bottom: 11, trailing: 16)

    public var cornerRadius: CGFloat = 8
    public var borderWidth: CGFloat = 1
    public var inputPadding = EdgeInsets(top: 8, leading: 10, bottom: 8, trailing: 10)
    public var fieldSpacing: CGFloat = 16
    public var messageSpacing: CGFloat = 4

    public var inputTextStyle: Font.TextStyle = .body
    public var messageTextStyle: Font.TextStyle = .callout
    public var titleFont: Font = .title2.weight(.semibold)
    public var showsMessageIcons = true
    /// Previous, Next and Done above the keyboard of every text input. Without it, a field whose
    /// keyboard has no Return key, like the phone pad, is left by tapping elsewhere.
    public var showsKeyboardBar = true

    public init() {}

    var inputShape: RoundedRectangle { RoundedRectangle(cornerRadius: cornerRadius, style: .continuous) }
    var groupShape: RoundedRectangle { RoundedRectangle(cornerRadius: groupCornerRadius, style: .continuous) }
}

@available(iOS 16.0, *)
private struct KotoseFormStyleKey: EnvironmentKey {
    static let defaultValue = KotoseFormStyle()
}

@available(iOS 16.0, *)
public extension EnvironmentValues {
    var kotoseFormStyle: KotoseFormStyle {
        get { self[KotoseFormStyleKey.self] }
        set { self[KotoseFormStyleKey.self] = newValue }
    }
}

@available(iOS 16.0, *)
public extension View {
    /// Applies the tokens to every `KotoseNativeForm` below this view.
    func kotoseFormStyle(_ style: KotoseFormStyle) -> some View {
        environment(\.kotoseFormStyle, style)
    }
}

/// Validation state a field row is drawn in. Derived from the Kotlin messages, never decided here.
enum FieldTone {
    case plain, warning, error

    init(_ messages: [NativeValidationMessage]) {
        if messages.contains(where: { !$0.asWarning }) { self = .error }
        else if messages.isEmpty { self = .plain }
        else { self = .warning }
    }
}

/// The one container every text-like input gets: background, shape and state border drawn by
/// SwiftUI, so the UIKit text field underneath only renders text and caret.
@available(iOS 16.0, *)
struct InputChrome: ViewModifier {
    let style: KotoseFormStyle
    let tone: FieldTone
    let focused: Bool
    let enabled: Bool

    func body(content: Content) -> some View {
        content
            .padding(style.inputPadding)
            .background(style.inputShape.fill(enabled ? style.inputBackground : style.disabledBackground))
            .overlay(style.inputShape.strokeBorder(borderColor, lineWidth: style.borderWidth))
    }

    private var borderColor: Color {
        switch tone {
        case .error: return style.error
        case .warning: return style.warning
        case .plain: return focused ? style.focusedBorder : style.inputBorder
        }
    }
}

@available(iOS 16.0, *)
extension Font.TextStyle {
    var uiTextStyle: UIFont.TextStyle {
        switch self {
        case .largeTitle: return .largeTitle
        case .title: return .title1
        case .title2: return .title2
        case .title3: return .title3
        case .headline: return .headline
        case .subheadline: return .subheadline
        case .callout: return .callout
        case .footnote: return .footnote
        case .caption: return .caption1
        case .caption2: return .caption2
        default: return .body
        }
    }
}
