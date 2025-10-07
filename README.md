# EdgeDetectProject

Real-time Android edge detection pipeline using CameraX (Java/Kotlin), OpenCV in C++ via JNI (NDK), and OpenGL ES 2.0 for rendering. Includes a minimal TypeScript web viewer to display a processed frame and overlay basic stats.

## Features
- CameraX live feed using `PreviewView` (`app/src/main/java/com/edge/detect/MainActivity.kt`).
- Native processing in C++ with OpenCV (Canny or grayscale) via JNI (`app/src/main/cpp/native-lib.cpp`).
- OpenGL ES 2.0 textured quad renderer in Kotlin (`app/src/main/java/com/edge/detect/gl/GLRenderer.kt`).
- Toggle between raw/grayscale and edge output; simple FPS counter.
- Minimal TypeScript web viewer (`web/`) showing a sample processed frame and stats.

## Project Structure
- `app/` Android application module
  - `src/main/java/com/edge/detect/MainActivity.kt` camera + JNI bridge
  - `src/main/java/com/edge/detect/gl/GLRenderer.kt` OpenGL renderer
  - `src/main/cpp/` CMake + native OpenCV processing
  - `src/main/res/layout/activity_main.xml` UI layout
  - `src/main/AndroidManifest.xml` permissions and activity
- `web/` TypeScript web viewer
- Root Gradle: `settings.gradle`, `build.gradle`, `gradle.properties`

## Prerequisites
- Android Studio Giraffe/Koala or newer.
- Android SDK + NDK (install from Android Studio SDK Manager).
- OpenCV for Android SDK (OpenCV-android-sdk). Download from https://opencv.org/releases/ and unzip.
- Java 17+.

## OpenCV SDK Setup
1. Download and unzip `OpenCV-android-sdk` (e.g., `C:/dev/OpenCV-android-sdk`).
2. Create `local.properties` at repository root (same folder as `settings.gradle`) with:
   ```properties
   sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk
   ndk.dir=C:/Users/<you>/AppData/Local/Android/Sdk/ndk/<version>
   opencvsdk.dir=C:/dev/OpenCV-android-sdk
   ```
3. `app/build.gradle` passes `-DOpenCV_DIR=${opencvsdk.dir}/sdk/native/jni` to CMake.

## Build & Run (Android)
- Open the project in Android Studio.
- Let Gradle sync; ensure the NDK and CMake are installed.
- Build and run on a physical device (minSdk 24). Grant camera permission.
- Use the Toggle button to switch between views. FPS is shown at the top.

## Notes on Performance
- `ImageAnalysis` outputs `RGBA_8888` data. JNI processes into the same buffer and the Kotlin GL renderer uploads that RGBA as a texture.
- For higher throughput, consider using direct ByteBuffer or external OES textures with SurfaceTexture and native GL upload; for this assessment the current approach targets 10–15 FPS.

## Web Viewer (TypeScript)
- Navigate to `web/` and install dev deps:
  ```bash
  npm install
  npm run build
  npm start
  ```
- Open http://localhost:5173 to see the sample processed frame, FPS and resolution.

## Commit Strategy
Incremental, meaningful commits will be pushed to this repository to reflect development stages:
1. Bootstrap Gradle project and .gitignore.
2. Android app module with CameraX and basic UI.
3. OpenGL ES renderer in Kotlin.
4. NDK + CMake + JNI + OpenCV integration.
5. TypeScript web viewer.
6. Documentation and setup instructions.

## Troubleshooting
- If CMake fails to find OpenCV, re-check `opencvsdk.dir` in `local.properties`.
- If the app launches to a black screen, verify camera permission and that `PreviewView` is bound.
- Build issues on Windows paths: ensure backslashes are escaped or prefer forward slashes in `local.properties`.

## License
For assessment purposes only.
