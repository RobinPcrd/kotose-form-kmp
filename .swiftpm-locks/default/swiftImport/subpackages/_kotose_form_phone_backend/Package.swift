// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_kotose_form_phone_backend",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_kotose_form_phone_backend",
      type: .none,
      targets: ["_kotose_form_phone_backend"]
    )
  ],
  dependencies: [
    .package(
      path: "../../../../../kotose-form/spm/KotosePhoneNumberKitBridge"
    )
  ],
  targets: [
    .target(
      name: "_kotose_form_phone_backend",
      dependencies: [
        .product(
          name: "KotosePhoneNumberKitBridge",
          package: "KotosePhoneNumberKitBridge"
        )
      ]
    )
  ]
)
