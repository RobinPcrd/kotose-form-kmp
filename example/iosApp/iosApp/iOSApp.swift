/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @State private var catalog = Catalog()
    @State private var brand = false

    var body: some Scene {
        WindowGroup {
            NavigationStack { CatalogList(catalog: catalog, brand: $brand) }
                .kotoseFormStyle(brand ? Brand.style : KotoseFormStyle())
                // The Compose pages read the same flag through ExampleTheme.
                .onChange(of: brand) { Look.shared.brand = $0 }
        }
    }
}

/// One brand for both renderers; the values come from the Kotlin `Look` object.
enum Brand {
    static var style: KotoseFormStyle {
        let accent = Color(argb: Look.shared.accentArgb)
        var style = KotoseFormStyle()
        style.accent = accent
        style.focusedBorder = accent
        style.inputBorder = .clear
        style.inputBackground = Color(uiColor: .secondarySystemBackground)
        style.error = Color(argb: Look.shared.errorArgb)
        style.paragraphBackground = accent.opacity(0.12)
        style.cornerRadius = CGFloat(Look.shared.cornerRadiusDp)
        return style
    }
}

extension Color {
    init(argb: Int64) {
        self.init(
            red: Double((argb >> 16) & 0xFF) / 255,
            green: Double((argb >> 8) & 0xFF) / 255,
            blue: Double(argb & 0xFF) / 255,
            opacity: Double((argb >> 24) & 0xFF) / 255
        )
    }
}

/// Resolves Kotlin display text; Swift never receives raw strings from the catalog.
struct ResText: View {
    let catalog: Catalog
    let res: StrRes
    @State private var text = ""

    var body: some View {
        Text(text).task(id: ObjectIdentifier(res)) { text = (try? await catalog.text(res: res)) ?? "" }
    }
}

private struct ComposeAppRoute: Hashable {}

struct CatalogList: View {
    let catalog: Catalog
    @Binding var brand: Bool

    var body: some View {
        List {
            section(.fields, title: "Fields")
            section(.behaviors, title: "Behaviors")
        }
        .navigationTitle("Kotose Form catalog")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                // Button style: the whole control toggles, and a toolbar item has a stable frame.
                Toggle(isOn: $brand) { Label("Brand", systemImage: "paintpalette") }
                    .toggleStyle(.button)
                    .accessibilityIdentifier("look-brand")
            }
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: ComposeAppRoute()) { Text("Compose app") }
                    .accessibilityIdentifier("compose-app")
            }
        }
        .navigationDestination(for: String.self) { id in
            let page = catalog.byId(id: id)
            PageView(catalog: catalog, page: page, adapter: catalog.adapter(page: page))
        }
        .navigationDestination(for: ComposeAppRoute.self) { _ in
            ComposeAppView().ignoresSafeArea().navigationBarTitleDisplayMode(.inline)
        }
    }

    private func section(_ section: CatalogPage.Section, title: String) -> some View {
        Section(title) {
            ForEach(catalog.pages.filter { $0.section == section }, id: \.id) { page in
                NavigationLink(value: page.id) { ResText(catalog: catalog, res: page.title) }
                    .accessibilityIdentifier("page-\(page.id)")
            }
        }
    }
}

/// One catalog page with a renderer switch. The SwiftUI branch pins its action bar to the bottom,
/// where the keyboard covers it; the Compose branch owns its own bar and insets.
struct PageView: View {
    let catalog: Catalog
    let page: CatalogPage
    let adapter: NativeFormAdapter
    @State private var compose = false
    @State private var result: StrRes?
    @State private var barHeight: CGFloat = 0

    var body: some View {
        VStack(spacing: 0) {
            Picker("Renderer", selection: $compose) {
                Text("SwiftUI").tag(false)
                Text("Compose").tag(true)
            }
            .pickerStyle(.segmented)
            .padding([.horizontal, .bottom])
            .accessibilityIdentifier("renderer")
            if compose {
                ComposePageView(page: page).ignoresSafeArea(edges: .bottom)
            } else {
                ScrollViewReader { proxy in
                    ScrollView {
                        KotoseNativeForm(adapter: adapter, scrollProxy: proxy) { field, adapter in
                            AnyView(StepperRow(field: field, adapter: adapter))
                        }
                        .padding(.horizontal)
                    }
                    // The form ends above the action bar, which overlays it.
                    .contentMargins(.bottom, barHeight, for: .scrollContent)
                }
                .overlay(alignment: .bottom) {
                    ActionBar(catalog: catalog, page: page, result: result)
                        .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { barHeight = $0 }
                        .ignoresSafeArea(.keyboard, edges: .bottom)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { ResText(catalog: catalog, res: page.title) } }
        .task(id: ObjectIdentifier(page)) { for await value in page.result { result = value } }
    }
}

struct ActionBar: View {
    let catalog: Catalog
    let page: CatalogPage
    let result: StrRes?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let result { ResText(catalog: catalog, res: result).accessibilityIdentifier("result") }
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 130))], alignment: .leading) {
                ForEach(page.actions, id: \.id) { action in
                    Button { action.run() } label: { ResText(catalog: catalog, res: action.label) }
                        .buttonStyle(.bordered)
                        .accessibilityIdentifier("action-\(action.id)")
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.bar)
    }
}

/// The catalog's custom field, rendered natively; both buttons call the same Kotlin methods.
struct StepperRow: View {
    let field: NativeFieldSnapshot
    let adapter: NativeFormAdapter

    private var stepper: StepperField? { adapter.customField(key: field.key) as? StepperField }

    var body: some View {
        HStack {
            Text(field.label)
            Spacer()
            Button("-") { stepper?.decrement() }
                .buttonStyle(.bordered)
                .accessibilityIdentifier(field.key + "-minus")
            Text(field.textValue)
                .frame(minWidth: 24)
                .accessibilityIdentifier(field.key + "-value")
            Button("+") { stepper?.increment() }
                .buttonStyle(.bordered)
                .accessibilityIdentifier(field.key + "-plus")
        }
        .disabled(!field.enabled)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(field.key)
    }
}

struct ComposePageView: UIViewControllerRepresentable {
    let page: CatalogPage

    func makeUIViewController(context: Context) -> UIViewController {
        PageHostKt.makePageViewController(page: page)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The full Compose app, including its Navigation 3 back stack, hosted as one destination.
struct ComposeAppView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

