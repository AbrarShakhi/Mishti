cmake_minimum_required(VERSION 3.22.1)

project("mishti_llama" CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(LLAMA_SRC ${CMAKE_CURRENT_LIST_DIR}/llama.cpp)

# llama.cpp reads these as cache entries before its own option() calls run, so they must be
# set with FORCE here rather than passed per-target.
#
# Most of llama.cpp's build targets already default to OFF, because LLAMA_STANDALONE is false
# when it is pulled in with add_subdirectory. Only the ones whose defaults are wrong for a
# cross-compiled Android library are overridden:
set(LLAMA_OPENSSL  OFF CACHE BOOL "" FORCE)  # defaults ON; there is no OpenSSL in the NDK
set(GGML_OPENMP    OFF CACHE BOOL "" FORCE)  # defaults ON; NDK libomp linkage is fragile
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)  # sgemm path, not worth the build risk here
set(GGML_NATIVE    OFF CACHE BOOL "" FORCE)  # never probe the *host* CPU when cross-compiling

# Build the engine. `build-llama` is just where its objects land inside our build dir.
add_subdirectory(${LLAMA_SRC} build-llama)

add_library(mishti_llama SHARED llama_bridge.cpp)

target_include_directories(mishti_llama PRIVATE
        ${LLAMA_SRC}/include
        ${LLAMA_SRC}/ggml/include)

target_link_libraries(mishti_llama
        llama
        android
        log)
