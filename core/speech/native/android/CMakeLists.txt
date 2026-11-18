set(WHISTLE_RUNTIME_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../build/whistle/android-arm64")

add_library(cactus_runtime STATIC IMPORTED)
set_target_properties(cactus_runtime PROPERTIES
    IMPORTED_LOCATION "${WHISTLE_RUNTIME_DIR}/libcactus.a"
    INTERFACE_INCLUDE_DIRECTORIES "${WHISTLE_RUNTIME_DIR}"
)

add_library(mishti_speech SHARED whistle_jni.cpp)
target_link_libraries(mishti_speech PRIVATE cactus_runtime)
target_link_options(mishti_speech PRIVATE -Wl,--exclude-libs,ALL -Wl,--no-undefined)
