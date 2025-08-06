package com.example.videotest

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.videotest.databinding.ActivityWebViewTestBinding

/**
 * WebView에서 HTML5 <video> 태그로 재생 테스트
 * - 브라우저 엔진이 해당 코덱 지원 여부에 따라 재생 여부 결정
 */
class WebViewTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        val webView: WebView = binding.webview
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        val html = buildString {
            append("<html><body>")
            for (video in VideoTestData.testVideos) {
                append("<p>${video.extension}</p>")
                append("<video width='100%' height='auto' controls>")
                append("<source src='${video.url}' type='video/${video.extension}'>")
                append("</video><br/>")
            }
            append("</body></html>")
        }

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}