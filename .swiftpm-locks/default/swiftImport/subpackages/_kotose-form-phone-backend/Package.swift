// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_kotose-form-phone-backend",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_kotose-form-phone-backend",
      type: .none,
      targets: ["_kotose-form-phone-backend"]
    )
  ],
  dependencies: [
    .package(
      path: "../../../../../kotose-form/spm/KotosePhoneNumberKitBridge"
    )
  ],
  targets: [
    .target(
      name: "_kotose-form-phone-backend",
      dependencies: [
        .product(
          name: "KotosePhoneNumberKitBridge",
          package: "KotosePhoneNumberKitBridge"
        )
      ]
    )
  ]
)
