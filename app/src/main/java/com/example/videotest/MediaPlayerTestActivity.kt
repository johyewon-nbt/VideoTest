package com.example.videotest

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.videotest.databinding.ActivityMediaPlayerTestBinding
import java.net.HttpURLConnection
import java.net.URL

/**
 * MediaPlayer 테스트
 * - 각 영상 URL에 대해 네트워크 연결 → 응답 코드 확인 → 재생 시도
 * - 예상과 실제를 모두 로그에 출력
 */
class MediaPlayerTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayerTestBinding
    private val tag = "MPTestLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        Thread {
            for (video in VideoTestData.testVideos) {
                logWithTime("----- [${video.extension.uppercase()}] 테스트 시작 -----")

                // 1. 네트워크 연결 확인
                val reachable = checkUrlReachable(video.url)
                if (!reachable) {
                    logWithTime("[ERROR] [${video.extension.uppercase()}] URL에 접근 불가 → 테스트 스킵")
                    continue
                }

                // 2. MediaPlayer 준비
                try {
                    val mp = MediaPlayer()
                    mp.setDataSource(this, Uri.parse(video.url))
                    mp.setOnPreparedListener {
                        logWithTime("[SUCCESS] [${video.extension.uppercase()}] 실제 재생 준비 완료 → 재생 가능")
                        mp.start()
                    }
                    mp.setOnErrorListener { _, what, extra ->
                        logWithTime("[FAIL] [${video.extension.uppercase()}] 재생 불가 / what=$what, extra=$extra")
                        logWithTime("----- [${video.extension.uppercase()}] 테스트 종료 -----")
                        mp.release()
                        true
                    }
                    mp.setOnCompletionListener {
                        logWithTime("[INFO] [${video.extension.uppercase()}] 재생 완료")
                        logWithTime("----- [${video.extension.uppercase()}] 테스트 종료 -----")
                        mp.release()
                    }
                    mp.prepareAsync()
                } catch (e: Exception) {
                    logWithTime("[ERROR] [${video.extension.uppercase()}] 예외 발생: ${e.message}")
                }
            }
        }.start()
    }

    /**
     * URL 접속 가능 여부 + HTTP 상태 코드 확인
     */
    private fun checkUrlReachable(urlStr: String): Boolean {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val code = conn.responseCode
            code in 200..399
        } catch (e: Exception) {
            logWithTime("[NETWORK ERROR] ${e.message}")
            false
        }
    }

    /**
     * 로그에 타임스탬프 포함
     */
    private fun logWithTime(message: String) {
        val time = System.currentTimeMillis() % 100000
        Log.i(tag, "[$time] $message")
    }
}