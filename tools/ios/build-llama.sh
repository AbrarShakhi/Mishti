#!/bin/sh
set -eu

case "${PLATFORM_NAME:?Run this script from the Xcode iOS build phase}" in
    iphoneos|iphonesimulator) ;;
    *) echo "Unsupported iOS SDK: $PLATFORM_NAME" >&2; exit 1 ;;
esac
if ! command -v cmake >/dev/null 2>&1; then
    echo "CMake 3.22.1 or newer is required. Install CMake and make it available to Xcode's PATH." >&2
    exit 1
fi

mishti_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
mishti_build="${DERIVED_FILE_DIR:?}/llama-build"
mishti_install="$DERIVED_FILE_DIR/llama-install"
mishti_sdk=$(xcrun --sdk "$PLATFORM_NAME" --show-sdk-path)
mishti_jobs=$(sysctl -n hw.ncpu)

cmake -S "$mishti_root/tools/ios/native" -B "$mishti_build" -G "Unix Makefiles" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_SYSTEM_PROCESSOR=arm64 \
    -DCMAKE_OSX_SYSROOT="$mishti_sdk" \
    -DCMAKE_OSX_ARCHITECTURES="${ARCHS:-arm64}" \
    -DCMAKE_OSX_DEPLOYMENT_TARGET="${IPHONEOS_DEPLOYMENT_TARGET:-18.2}" \
    -DCMAKE_BUILD_TYPE="${CONFIGURATION:-Debug}" \
    -DCMAKE_INSTALL_PREFIX="$mishti_install"
cmake --build "$mishti_build" --target mishti_llama_ios --parallel "$mishti_jobs"
cmake --install "$mishti_build"
for mishti_library in mishti_llama_ios llama ggml ggml-cpu ggml-base; do
    test -f "$mishti_install/lib/lib$mishti_library.a"
done
