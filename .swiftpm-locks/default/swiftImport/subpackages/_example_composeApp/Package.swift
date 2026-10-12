// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_example_composeApp",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_example_composeApp",
      type: .none,
      targets: ["_example_composeApp"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_example_composeApp",
      dependencies: [
      ]
    )
  ]
)
