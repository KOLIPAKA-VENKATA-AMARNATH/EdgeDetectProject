# Keep JNI OnLoad and native symbols
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep CameraX and lifecycle annotations
-keep class androidx.camera.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.**

# OpenCV native
-dontwarn org.opencv.**
