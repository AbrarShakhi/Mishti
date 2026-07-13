cmake_minimum_required(VERSION 3.14...3.28)
project("mishti")

# ── Disable everything in llama.cpp we don't need on Android ──────────────────
# These flags stop llama.cpp from trying to build its own CLI tools, tests,
# examples, and server — which would fail or bloat the APK.
set(LLAMA_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER   OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON   OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP      OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS    OFF CACHE BOOL "" FORCE)

# ── Pull in the llama.cpp submodule ───────────────────────────────────────────
# This compiles ggml + llama as static libraries we can link against.
add_subdirectory(llama.cpp)

# ── Our JNI bridge ────────────────────────────────────────────────────────────
# This is the .so file that Android loads. It wraps the llama C API
# so Kotlin can call it via JNI (Java Native Interface).
add_library(
        mishti-llm          # name → libmishti-llm.so
        SHARED
        llm_bridge.cpp      # our C++ glue code
)

target_include_directories(mishti-llm PRIVATE
        llama.cpp/include   # llama.h lives here
        llama.cpp/ggml/include
)

target_link_libraries(mishti-llm
        llama               # the llama.cpp static lib
        android             # Android system lib (for logging etc.)
        log                 # __android_log_print
)