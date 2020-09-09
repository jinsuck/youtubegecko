/*
//
//  web_view_android.cpp
//  AppBuilderIndex
//
//  Created by Bogdan Mytnyk on 8/9/19.
//  Copyright Â© 2019 Bogdan Mytnyk. All rights reserved.
//

#include "web_view_android.h"

#include "platform/android/thread_jandroid.h"
#include "platform/android/android_looper.h"
#include "platform/android/java_godot_lib_jni.h"
#include "platform/android/util.h"
#include "core/engine.h"

#include <unistd.h>
#include <functional>
#include <queue>
#include <memory>
#include <thread>
#include <mutex>
#include <unordered_map>

#include <android/bitmap.h>

using MainThreadFunction = std::function<void()>;
using MainThreadFunctionPtr = std::shared_ptr<MainThreadFunction>;

namespace
{
    enum AndroidViewVisibility
    {
        VIEW_VISIBLE = 0,
        VIEW_INVISIBLE = 4,
        VIEW_GONE = 8
    };

    jclass s_geckoWebViewProxyCls = nullptr;

    int s_idCounter = 0;

    std::mutex s_registryLock;

    std::unordered_map<jint, WebViewAndroid*> s_registry;
};


WebViewAndroid::WebViewAndroid():
    m_webView(nullptr),
    m_creatingNow(false),
    m_id(0),
    m_hasNewFrame(true),
    m_currentFrame(nullptr)
{
    std::lock_guard<std::mutex> guard {s_registryLock};
    m_id = s_idCounter++;
    s_registry.insert(std::make_pair(m_id, this));
}

WebViewAndroid::~WebViewAndroid()
{
    if (m_webView != nullptr)
    {
        jobject webview = m_webView;

        MainThreadFunction cleanupFunc = [webview] ()
        {
            JNIEnv* env = ThreadAndroid::get_env();

            const jmethodID releaseMethodId = env->GetMethodID(s_geckoWebViewProxyCls, "release", "()V");
            ERR_FAIL_COND(releaseMethodId == nullptr)

            env->CallVoidMethod(webview, releaseMethodId);
            if (env->ExceptionOccurred())
            {
                env->ExceptionDescribe();
                env->ExceptionClear();
            }

            env->DeleteGlobalRef(webview);
        };

        AndroidLooper::instance().post_to_execute(cleanupFunc);
    }

    std::lock_guard<std::mutex> guard {s_registryLock};
    s_registry.erase(m_id);
}

void WebViewAndroid::init(int left, int top, int width, int height)
{
    ERR_FAIL_COND(m_webView != nullptr);
    ERR_FAIL_COND(m_creatingNow);

    m_creatingNow = true;

    MainThreadFunction initFunctionPtr = [this, width, height] ()
    {
        JNIEnv* env = ThreadAndroid::get_env();

        JavaClassWrapper* activityWrapper = static_cast<JavaClassWrapper*>(Engine::get_singleton()->get_singleton_object("JavaClassWrapper"));
        if (activityWrapper != nullptr)
        {
            jobject activityGlobalRef = activityWrapper->get_activity_object();
            if (activityGlobalRef != nullptr)
            {
                jmethodID constructor = env->GetMethodID(s_geckoWebViewProxyCls, "<init>", "(Landroid/content/Context;III)V");
                jobject webView = env->NewObject(s_geckoWebViewProxyCls, constructor, activityGlobalRef, m_id, width, height);
                if (webView != nullptr)
                {
                    // We need to keep global reference
                    m_webView = env->NewGlobalRef(webView);

                    // Remove old local reference
                    env->DeleteLocalRef(webView);
                }
                else
                {
                    ERR_PRINT("Failed to create WebView");
                }
            }
            else
            {
                ERR_PRINT("Null activity object");
            }
        }
        else
        {
            ERR_PRINT("Failed to find JavaClassWrapper");
        }

        if (env->ExceptionOccurred())
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }

        // Is not in process of creating anymore
        m_creatingNow = false;
    };

    AndroidLooper::instance().post_to_execute(initFunctionPtr);
}

void WebViewAndroid::set_visible(bool visible)
{
    MainThreadFunction setVisibleFunction = [this, visible] ()
    {
        ERR_FAIL_COND(m_webView == nullptr);

        JNIEnv* env = ThreadAndroid::get_env();

        jmethodID setVisibleMethodId = env->GetMethodID(s_geckoWebViewProxyCls, "setVisibility", "(I)V");
        ERR_FAIL_COND(setVisibleMethodId == nullptr)

        env->CallVoidMethod(m_webView, setVisibleMethodId, visible ? VIEW_VISIBLE : VIEW_INVISIBLE);
        if (env->ExceptionOccurred())
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
    };

    AndroidLooper::instance().post_to_execute(setVisibleFunction);
}

void WebViewAndroid::evaluate_javascript(const String& code)
{
    jobject webview = m_webView;
    MainThreadFunction evalJsFuncPtr = [webview, code] ()
    {
        ERR_FAIL_COND(webview == nullptr);

        JNIEnv* env = ThreadAndroid::get_env();
        jmethodID evalJsMethodId = env->GetMethodID(s_geckoWebViewProxyCls, "evaluateJavascript", "(Ljava/lang/String;)V");
        ERR_FAIL_COND(evalJsMethodId == nullptr);

        jstring jsCode = env->NewStringUTF(code.utf8().get_data());
        env->CallVoidMethod(webview, evalJsMethodId, jsCode, nullptr);
        if (env->ExceptionOccurred())
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }

        env->DeleteLocalRef(jsCode);
    };

    AndroidLooper::instance().post_to_execute(evalJsFuncPtr);
}

void WebViewAndroid::load_url(const String& urlStr)
{
    MainThreadFunction loadUrlFunc = [this, urlStr] ()
    {
        ERR_FAIL_COND (m_webView == nullptr);

        JNIEnv* env = ThreadAndroid::get_env();
        jmethodID loadUrlMethodId = env->GetMethodID(s_geckoWebViewProxyCls, "loadUrl", "(Ljava/lang/String;)V");
        ERR_FAIL_COND(loadUrlMethodId == nullptr)

        jstring jUrl = env->NewStringUTF(urlStr.utf8().get_data());
        env->CallVoidMethod(m_webView, loadUrlMethodId, jUrl);
        if (env->ExceptionOccurred())
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }

        env->DeleteLocalRef(jUrl);
    };

    AndroidLooper::instance().post_to_execute(loadUrlFunc);
}

void WebViewAndroid::load_html_string(const String& htmlStr, const String& baseUrlStr)
{
    jobject webView = m_webView;

    MainThreadFunction loadHTMLStringFunc = [webView, htmlStr, baseUrlStr] ()
    {
        ERR_FAIL_COND(webView == nullptr);

        JNIEnv* env = ThreadAndroid::get_env();
        static const char* const kLoadHtmlSignature {"(Ljava/lang/String;)V"};
        jmethodID loadHtmlStringId = env->GetMethodID(s_geckoWebViewProxyCls, "loadHtmlData", kLoadHtmlSignature);
        ERR_FAIL_COND(loadHtmlStringId == nullptr)

        jstring jHtmlData = env->NewStringUTF(htmlStr.utf8().get_data());

        env->CallVoidMethod(webView, loadHtmlStringId, jHtmlData);
        if (env->ExceptionOccurred())
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }

        env->DeleteLocalRef(jHtmlData);
    };

    AndroidLooper::instance().post_to_execute(loadHTMLStringFunc);
}

void WebViewAndroid::add_script_message_handler(const String& messageName, const MessageHandler& messageHandler)
{
    std::lock_guard<std::mutex> autoLock{m_messageHandlersLock};

    auto insertPair = m_messageHandlers.insert(std::make_pair(messageName, messageHandler));
    if (!insertPair.second)
    {
        String warning {"Failed to add handler for message - "};
        warning += messageName;
        warning += String {"; Corresponded handler already exists"};
        WARN_PRINTS(warning);
    }
}

void WebViewAndroid::on_message_received(const String& messageName, const Variant& arg)
{
    std::lock_guard<std::mutex> autoLock {m_messageHandlersLock};

    m_messages.emplace(messageName, arg);
}

void WebViewAndroid::process_messages()
{
    std::queue<MessageInfo> messagesCopy;

    {
        std::lock_guard<std::mutex> autoLock {m_messageHandlersLock};
        messagesCopy.swap(m_messages);
    }

    while (!messagesCopy.empty())
    {
        const auto& message = messagesCopy.front();

        //WARN_PRINTS(String("Message processing - ") + message.m_name);

        auto elemIter = m_messageHandlers.find(message.m_name);
        if (elemIter != m_messageHandlers.end())
        {
            //WARN_PRINTS(String("Handler found"));
            (elemIter->second)(message.m_argument);
        }

        messagesCopy.pop();
    }
}

void WebViewAndroid::capture_frame()
{
    // This trick allows us to capture one local variable instead of
    // capturing all object context via this
    // It's more safe
    jobject webView = m_webView;
    MainThreadFunction captureFrameFunc = [webView] ()
    {
        ERR_FAIL_COND (webView == nullptr);

        JNIEnv* env = ThreadAndroid::get_env();

        static const char* const kCaptureSignature {"()V"};
        jmethodID captureId = env->GetMethodID(s_geckoWebViewProxyCls, "capturePicture", kCaptureSignature);
        ERR_FAIL_COND (captureId == nullptr);

        env->CallVoidMethod(webView, captureId);
        if (env->ExceptionOccurred())
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
    };

    AndroidLooper::instance().post_to_execute(captureFrameFunc);
}

void WebViewAndroid::get_frame_pixels(PoolVector<uint8_t>& data) const
{
    std::lock_guard<std::mutex> autoLock {m_frameLock};

    if (m_currentFrame != nullptr)
    {
        PoolVector<uint8_t>::Write w = data.write();
        auto ptr = w.ptr();

        // extract pixels
        JNIEnv* env = ThreadAndroid::get_env();
        AndroidBitmapInfo info = {0};
        int error = AndroidBitmap_getInfo(env, m_currentFrame, &info);
        ERR_FAIL_COND (error != 0);

        // currently support only 8888 format of bitmap
        ERR_FAIL_COND (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888);

        uint8_t* pixels = nullptr;
        error = AndroidBitmap_lockPixels(env, m_currentFrame, reinterpret_cast<void**>(&pixels));
        ERR_FAIL_COND (error != 0);

        // In case of stride equal width * bytesPerPixel we can optimize
        // by use memcpy on all bytes
        const uint32_t bytesPerPixel = 4;
        const uint32_t dstStride = info.width * bytesPerPixel;
        if (info.stride == dstStride)
        {
            memcpy(ptr, pixels, dstStride * info.height);
        }
        else
        {
            uint8_t* dst = ptr;
            const uint8_t* src = pixels;
            for (uint32_t i = 0; i < info.height; ++i)
            {
                memcpy(dst, src, dstStride);

                dst += dstStride;
                src += info.stride;
            }
        }

        AndroidBitmap_unlockPixels(env, m_currentFrame);
    }
}

bool WebViewAndroid::has_new_frame() const
{
    return m_hasNewFrame;
};

void WebViewAndroid::set_new_frame(jobject frameBitmap)
{
    std::lock_guard<std::mutex> autoLock {m_frameLock};

    JNIEnv* env = ThreadAndroid::get_env();
    if (m_currentFrame != nullptr)
    {
        env->DeleteGlobalRef(m_currentFrame);
    }

    m_currentFrame = (frameBitmap != nullptr) ? env->NewGlobalRef(frameBitmap) : nullptr;
    m_hasNewFrame = true;
};

extern "C" JNIEXPORT void JNICALL Java_org_godotengine_godot_modules_webview_core_GeckoWebViewProxy_pictureCaptureSucceededNative
    (JNIEnv *env, jobject obj, jobject bitmap, jint id)
{
    ERR_FAIL_COND(obj == nullptr);
    ERR_FAIL_COND(bitmap == nullptr);

    auto iter = s_registry.find(id);
    ERR_FAIL_COND (iter == s_registry.end());

    iter->second->set_new_frame(bitmap);
}

extern "C" JNIEXPORT void JNICALL Java_org_godotengine_godot_modules_webview_core_GeckoWebViewProxy_pictureCaptureFailedNative
    (JNIEnv *env, jobject obj, jint id)
{
    ERR_FAIL_COND(obj == nullptr);

    auto iter = s_registry.find(id);
    ERR_FAIL_COND (iter == s_registry.end());

    iter->second->set_new_frame(nullptr);
}

extern "C" JNIEXPORT void JNICALL Java_org_godotengine_godot_modules_webview_core_GeckoWebViewProxy_onMessageReceivedNative
    (JNIEnv *env, jobject obj, jint jId, jstring jMessageName, jobject jArg)
{
    ERR_FAIL_COND(obj == nullptr);
    ERR_FAIL_COND(jMessageName == nullptr);

    auto iter = s_registry.find(jId);
    ERR_FAIL_COND (iter == s_registry.end());

    Variant varArg;
    if (jArg != nullptr)
        varArg = jobject_to_variant(env, jArg);

    const char* cstr = env->GetStringUTFChars(jMessageName, NULL);
    String messageName(cstr);
    env->ReleaseStringUTFChars(jMessageName, cstr);

    iter->second->on_message_received(messageName, varArg);
}

extern "C" JNIEXPORT void JNICALL Java_org_godotengine_godot_modules_webview_core_GeckoWebViewProxy_registerClass
    (JNIEnv *env, jclass geckoWebViewClass)
{
    ERR_FAIL_COND(geckoWebViewClass == nullptr);
    ERR_FAIL_COND(s_geckoWebViewProxyCls != nullptr);

    // create global reference from local
    s_geckoWebViewProxyCls = reinterpret_cast<jclass>(env->NewGlobalRef(geckoWebViewClass));
}

*/