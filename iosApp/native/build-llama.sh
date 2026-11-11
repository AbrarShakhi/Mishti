#!/bin/sh
set -eu

case "${PLATFORM_NAME:-iphonesimulator}" in
    iphoneos|iphonesimulator) ;;
    *) echo "Unsupported iOS SDK: ${PLATFORM_NAME:-}" >&2; exit 1 ;;
esac

if ! command -v cmake >/dev/null 2>&1; then
    echo "CMake 3.22.1 or newer is required. Install CMake and make it available in PATH." >&2
    exit 1
fi

mishti_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
mishti_platform="${PLATFORM_NAME:-iphonesimulator}"
mishti_build="${DERIVED_FILE_DIR:-$mishti_root/iosApp/build/native-build}/llama-build"
mishti_install="${DERIVED_FILE_DIR:-$mishti_root/iosApp/build/native-build}/llama-install"
mishti_sdk=$(xcrun --sdk "$mishti_platform" --show-sdk-path)
mishti_jobs=$(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo 4)

cmake -S "$mishti_root/iosApp/native" -B "$mishti_build" -G "Unix Makefiles" \
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
