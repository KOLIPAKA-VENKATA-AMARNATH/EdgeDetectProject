#include <jni.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "edgeproc"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static bool g_edges = true;

extern "C" JNIEXPORT void JNICALL
Java_com_edge_detect_MainActivity_00024Companion_nativeInit(JNIEnv* env, jobject thiz) {}

extern "C" JNIEXPORT void JNICALL
Java_com_edge_detect_MainActivity_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("nativeInit");
}

extern "C" JNIEXPORT void JNICALL
Java_com_edge_detect_MainActivity_nativeSetMode(JNIEnv* env, jobject thiz, jboolean edge) {
    g_edges = edge == JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_edge_detect_MainActivity_nativeOnFrameRGBA(JNIEnv* env, jobject thiz, jbyteArray data,
                                                    jint width, jint height, jint stride) {
    jboolean isCopy = JNI_FALSE;
    jbyte* ptr = env->GetByteArrayElements(data, &isCopy);
    if (!ptr) return;

    // Create Mat view with step = stride
    cv::Mat rgba((int)height, (int)width, CV_8UC4, (unsigned char*)ptr, (size_t)stride);

    if (g_edges) {
        cv::Mat gray, edges, out;
        cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
        cv::Canny(gray, edges, 50.0, 150.0, 3);
        cv::cvtColor(edges, out, cv::COLOR_GRAY2RGBA);
        out.copyTo(rgba);
    } else {
        // Optional: grayscale preview instead of raw
        cv::Mat gray, out;
        cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
        cv::cvtColor(gray, out, cv::COLOR_GRAY2RGBA);
        out.copyTo(rgba);
    }

    env->ReleaseByteArrayElements(data, ptr, 0); // commit changes back to Java byte[]
}
