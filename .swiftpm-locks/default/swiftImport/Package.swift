// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_example_composeApp"),
    .package(path: "subpackages/_kotose-form-phone-backend"),
    .package(path: "subpackages/_kotose_form_phone_backend")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_example_composeApp", package: "_example_composeApp"),
        .product(name: "_kotose-form-phone-backend", package: "_kotose-form-phone-backend"),
        .product(name: "_kotose_form_phone_backend", package: "_kotose_form_phone_backend")
      ]
    )
  ]
)
