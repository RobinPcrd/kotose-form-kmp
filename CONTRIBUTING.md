# Contributing

Contributions are welcome! Here's how to get started.

## Submitting changes

1. Fork the repository and create a feature branch from `main`.
2. Make your changes and ensure tests pass.
3. Submit a Pull Request with a clear description of the change.

## Building

```bash
./gradlew build
```

## Running tests

```bash
# All tests (requires Android SDK for device tests)
./gradlew test

# Common tests only (runs on all targets without a device)
./gradlew :kotose-form:allTests
```

## Reviewing public API changes

Every published module has a checked-in Kotlin ABI reference under `<module>/api/` (the
Android target as JVM signatures under `api/android/`). `./gradlew check` verifies them; to
check only the references:

```bash
./gradlew :kotose-form-core:checkKotlinAbi \
    :kotose-form-phone:checkKotlinAbi \
    :kotose-form-phone-backend:checkKotlinAbi \
    :kotose-form:checkKotlinAbi \
    :kotose-form-swiftui:checkKotlinAbi
```

Run the corresponding `updateKotlinAbi` tasks only for an intentional, reviewed API
change, then include the reference dump diff in the same commit. See
[API stability](docs/API-STABILITY.md) for the compatibility policy. The dumps do not cover
the bundled Swift sources: changes there need a full Xcode build of `example/iosApp`.

## Running the example app

```bash
# Android
./gradlew :example:androidApp:installDebug
```

On iOS, open `example/iosApp/iosApp.xcodeproj` in Xcode and run it; its build phase builds the
Kotlin framework.

## Trying a local checkout in an app

Include the checkout as a Gradle composite build and substitute the five modules. Both builds
must use the same Android Gradle plugin version. On iOS with the phone backend, rerun
`integrateLinkagePackage` after switching, so the linkage package points at the checkout.

```kotlin
// settings.gradle.kts of the app
includeBuild("/path/to/kotose-form-kmp") {
    dependencySubstitution {
        listOf(
            "kotose-form-core", "kotose-form-phone", "kotose-form-phone-backend",
            "kotose-form", "kotose-form-swiftui",
        ).forEach {
            substitute(module("io.github.robinpcrd:$it")).using(project(":$it"))
        }
    }
}
```

## Running UI flows

The example app is covered by [Maestro](https://maestro.mobile.dev) flows in `.maestro/`.
They run on both platforms from the same YAML files.

```bash
# Android: an emulator or device must be connected
./gradlew :example:androidApp:installDebug
maestro --device <serial> test -e RENDERER=android -e PAGE=text --test-output-dir build/maestro-android .maestro

# iOS: a simulator must be booted; builds, installs and runs both iOS renderers
scripts/maestro-ios.sh
```

Flows select elements by stable ids: `page-<id>`, `action-<id>`, `result`, `back`, `renderer`,
the field keys, and `error-<key>` or `warning-<key>` for messages.

## License

By contributing, you agree that your contributions will be licensed under
the [Apache License 2.0](LICENSE).
