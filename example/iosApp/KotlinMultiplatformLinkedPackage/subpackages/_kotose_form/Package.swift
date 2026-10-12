// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "_kotose_form",
    platforms: [
        .iOS("15.0")
    ],
    products: [
        .library(
            name: "_kotose_form",
            type: .none,
            targets: ["_kotose_form"]
        )
    ],
    dependencies: [
        .package(
            path: "../../../../../kotose-form/spm/KotosePhoneNumberKitBridge"
        )
    ],
    targets: [
        .target(
            name: "_kotose_form",
            dependencies: [
                .product(
                    name: "KotosePhoneNumberKitBridge",
                    package: "KotosePhoneNumberKitBridge"
                )
            ]
        )
    ]
)
