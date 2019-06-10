cmake_minimum_required(VERSION 3.10.2)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_VERBOSE_MAKEFILE on)

add_library(openal SHARED IMPORTED )
set_target_properties(openal PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopenal.so )

add_library(SoundGenerator
            SHARED
            src/main/cpp/src/SoundGenerator/SoundGenerator.cpp)

target_include_directories(SoundGenerator PRIVATE ${PROJECT_SOURCE_DIR}/src/main/cpp/include/)
target_include_directories(SoundGenerator PRIVATE /home/jaycee/src/github.com/openal-soft/include/)

target_link_libraries(SoundGenerator
                      openal
                      log)

add_library(MarkerDetector
            SHARED
            src/main/cpp/src/MarkerDetector/MarkerDetector.cpp)

target_include_directories(MarkerDetector PRIVATE src/main/cpp/include/)

add_library(JNI
            SHARED
            src/main/cpp/src/jniWrapper.cpp)

target_include_directories(JNI PRIVATE src/main/cpp/include/)
target_include_directories(JNI PRIVATE /home/jaycee/src/github.com/openal-soft/include/)

target_link_libraries(JNI
                    SoundGenerator
                    MarkerDetector)