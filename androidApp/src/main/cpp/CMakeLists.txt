cmake_minimum_required(VERSION 3.22.1)

project("mishti_llama" CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(MISHTI_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/../../../..)
set(CORE_LLM_NATIVE ${MISHTI_ROOT}/core/llm/native)
set(LLAMA_SRC ${CORE_LLM_NATIVE}/llama.cpp)

set(LLAMA_OPENSSL  OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP    OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE    OFF CACHE BOOL "" FORCE)

add_subdirectory(${LLAMA_SRC} build-llama)

add_library(mishti_llama SHARED
    llama_bridge.cpp
    ${CORE_LLM_NATIVE}/common/llama_bridge_common.cpp
)

target_include_directories(mishti_llama PRIVATE
    ${LLAMA_SRC}/include
    ${LLAMA_SRC}/ggml/include
    ${CORE_LLM_NATIVE}/common
)

target_link_libraries(mishti_llama
    llama
    android
    log
)
