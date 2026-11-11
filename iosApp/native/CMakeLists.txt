cmake_minimum_required(VERSION 3.22.1)
project(mishti_llama_ios LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(GGML_BACKEND_DL OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_METAL OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(CMAKE_INSTALL_LIBDIR lib CACHE STRING "" FORCE)

get_filename_component(MISHTI_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../.." ABSOLUTE)
set(CORE_LLM_NATIVE "${MISHTI_ROOT}/core/llm/native")
set(LLAMA_SRC "${CORE_LLM_NATIVE}/llama.cpp")

add_subdirectory("${LLAMA_SRC}" llama)

add_library(mishti_llama_ios STATIC
    llama_bridge_ios.cpp
    "${CORE_LLM_NATIVE}/common/llama_bridge_common.cpp"
)

target_include_directories(mishti_llama_ios PRIVATE
    "${LLAMA_SRC}/include"
    "${LLAMA_SRC}/ggml/include"
    "${CORE_LLM_NATIVE}/common"
    "${CMAKE_CURRENT_SOURCE_DIR}"
)

target_link_libraries(mishti_llama_ios PRIVATE llama)

install(TARGETS mishti_llama_ios llama ggml ggml-base ggml-cpu
    ARCHIVE DESTINATION lib
    PUBLIC_HEADER DESTINATION include
)
