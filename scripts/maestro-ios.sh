#!/usr/bin/env bash
# Builds the iOS example for the booted simulator, installs it, and runs the Maestro flows
# on the native and Compose renderers. DARK=1 switches the simulator to dark appearance first.
set -euo pipefail
cd "$(dirname "$0")/.."

udid=$(xcrun simctl list devices booted | grep -o -E '[0-9A-F-]{36}' | head -1)
if [ -z "$udid" ]; then
    echo "No booted iOS simulator found" >&2
    exit 1
fi

xcodebuild -project example/iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
    -destination "platform=iOS Simulator,id=$udid" -derivedDataPath build/ios-example \
    CODE_SIGNING_ALLOWED=NO build -quiet
xcrun simctl install "$udid" build/ios-example/Build/Products/Debug-iphonesimulator/KotoseForm.app
xcrun simctl ui "$udid" appearance "$([ "${DARK:-0}" = 1 ] && echo dark || echo light)"

out=build/maestro-ios
for renderer in native compose-ios; do
    for flow in .maestro/flows/*.yaml; do
        maestro --device "$udid" test -e "RENDERER=$renderer" -e "PAGE=${PAGE:-text}" \
            --test-output-dir "$out/$renderer-$(basename "$flow" .yaml)" "$flow"
    done
done

# Native-only keyboard regressions: the last field after Done and mixed input handoff.
for flow in .maestro/ios/*.yaml; do
    maestro --device "$udid" test \
        --test-output-dir "$out/native-$(basename "$flow" .yaml)" "$flow"
done
