/*************************************************************************/
/*  web_view_impl_android.cpp                                            */
/*************************************************************************/
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/*************************************************************************/
/* Copyright (c) 2007-2019 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2019 Godot Engine contributors (cf. AUTHORS.md)    */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
/*************************************************************************/

/*


#include "web_view_impl_android.h"

WebViewImplAndroid::WebViewImplAndroid(const Point2i& pos, const Size2i& size)
    : m_webView(WebViewAndroidPtr(memnew(WebViewAndroid), memdelete<WebViewAndroid>))
{
    m_webView->init(pos.x, pos.y, size.width, size.height);
    m_webView->set_visible(false);
}

void WebViewImplAndroid::set_visible(bool visible)
{
    m_webView->set_visible(visible);
}

void WebViewImplAndroid::add_user_script(const char* source)
{
    ERR_PRINTS("Not Implemented for this platform");
}

void WebViewImplAndroid::evaluate_javascript(const String& code)
{
    m_webView->evaluate_javascript(code);
}

void WebViewImplAndroid::load_url(const String& urlStr)
{
    m_webView->load_url(urlStr);
}

void WebViewImplAndroid::load_html_string(const String& htmlStr, const String& baseString)
{
    m_webView->load_html_string(htmlStr, baseString);
}

void WebViewImplAndroid::add_message_handler(const String& messageName, const MessageHandler& messageHandler)
{
    m_webView->add_script_message_handler(messageName, messageHandler);
}

void WebViewImplAndroid::set_page_load_callback(const OnPageLoadCallback& pageCallback)
{
    ERR_PRINTS("Not Implemented for this platform");
}

void WebViewImplAndroid::render_to_texture(Ref<ImageTexture> imageTexture)
{
    if (m_webView->has_new_frame())
    {
        // copy pixels to storage
        const auto width = imageTexture->get_width();
        const auto height = imageTexture->get_height();
        const auto format = imageTexture->get_format();
        PoolVector<uint8_t> data = imageTexture->get_data()->get_data();

        m_webView->get_frame_pixels(data);

        Ref<Image> new_image = memnew(Image(width, height, false, format, data));
        imageTexture->set_data(new_image);

        // send next request for capturing frame
        m_webView->capture_frame();
    }

    m_webView->process_messages();
}

void WebViewImplAndroid::send_background()
{
    ERR_PRINTS("Not Implemented for this platform");
}

void WebViewImplAndroid::send_foreground()
{
    ERR_PRINTS("Not Implemented for this platform");
}


*/