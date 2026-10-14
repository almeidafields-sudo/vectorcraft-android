#!/usr/bin/env bash
set -euo pipefail
# Run from repository root. Requires Rust >= 1.95, Android NDK, cargo-ndk, Gradle/JDK17.
cd "$(dirname "$0")/.."
rustup target add aarch64-linux-android
cargo ndk -t arm64-v8a -o android/app/src/main/jniLibs build --release -p vectorcraft-android-bridge
cd android
gradle assembleDebug
printf 'APK: %s\n' "$(pwd)/app/build/outputs/apk/debug/app-debug.apk"
