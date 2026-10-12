// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "KotosePhoneNumberKitBridge",
    platforms: [.iOS(.v12)],
    products: [
        .library(
            name: "KotosePhoneNumberKitBridge",
            targets: ["KotosePhoneNumberKitBridge"]
        )
    ],
    dependencies: [
        .package(
            url: "https://github.com/marmelroy/PhoneNumberKit.git",
            exact: "4.2.8"
        )
    ],
    targets: [
        .target(
            name: "KotosePhoneNumberKitBridge",
            dependencies: [
                .product(name: "PhoneNumberKit", package: "PhoneNumberKit")
            ],
            path: "kotose-form/spm/KotosePhoneNumberKitBridge/Sources/KotosePhoneNumberKitBridge"
        )
    ]
)
