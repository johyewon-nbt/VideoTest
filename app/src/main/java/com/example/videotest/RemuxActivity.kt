package com.example.videotest

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.videotest.databinding.ActivityRemuxBinding
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer

class RemuxActivity : AppCompatActivity() {
    private val TAG = "MPTestLog"
    private lateinit var binding: ActivityRemuxBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemuxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        binding.startRemuxBtn.setOnClickListener {
            binding.remuxStatus.text = "테스트 시작..."
            Thread {
                VideoTestData.testVideos.forEach { video ->


                    val originalPlayable = testOriginalPlayback(video.url)
                    val remuxResult = tryRemux(video.url, video.extension)
                    val remuxPlayable = if (remuxResult.startsWith("mp4 래핑 완료")) {
                        playVideo(remuxResult.substringAfter("→ ").trim())
                        true
                    } else false

                    val resultLine =
                        "[${video.extension}] 원본=${if (originalPlayable) "✅" else "❌"} / 래핑=${if (remuxPlayable) "✅" else "❌"} / $remuxResult"

                    Log.i(TAG, resultLine)
                    runOnUiThread { binding.remuxStatus.append("\n$resultLine") }
                }
            }.start()
        }
    }

    private fun testOriginalPlayback(url: String): Boolean {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(this, Uri.parse(url))
            var success = false
            val latch = Object()
            mp.setOnPreparedListener {
                success = true
                synchronized(latch) { latch.notify() }
            }
            mp.setOnErrorListener { _, _, _ ->
                synchronized(latch) { latch.notify() }
                true
            }
            mp.prepareAsync()
            synchronized(latch) { latch.wait(5000) } // 최대 5초 대기
            mp.release()
            success
        } catch (e: Exception) {
            false
        }
    }

    private fun tryRemux(url: String, ext: String): String {
        if (ext.equals("mp4", ignoreCase = true)) {
            return "mp4는 래핑 불필요"
        }

        return try {
            val inputFile = downloadFile(url, "input_$ext") ?: return "다운로드 실패"
            val outputFile = File(cacheDir, "output_${ext}_wrapped.mp4")

            val extractor = MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackIndexMap = HashMap<Int, Int>()

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                if (mime.contains("vp8", ignoreCase = true) || mime.contains("vp9", ignoreCase = true) ||
                    mime.contains("vorbis", ignoreCase = true) || mime.contains("opus", ignoreCase = true)) {
                    Log.w(TAG, "[$ext] 코덱 미지원으로 mp4 래핑 불가: $mime")
                    continue
                }

                if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                    extractor.selectTrack(i)
                    val newIndex = muxer.addTrack(format)
                    trackIndexMap[i] = newIndex
                    Log.i(TAG, "[$ext] 트랙 추가 성공: $mime")
                } else {
                    Log.w(TAG, "[$ext] 트랙 무시: $mime")
                }
            }

            if (trackIndexMap.isEmpty()) {
                extractor.release()
                muxer.release()
                return "mp4로 래핑 가능한 트랙 없음 (코덱 미지원)"
            }

            muxer.start()

            val bufferSize = 1024 * 1024
            val buffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            for (trackIndex in trackIndexMap.keys) {
                extractor.selectTrack(trackIndex)
                while (true) {
                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) break

                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG
                    muxer.writeSampleData(trackIndexMap[trackIndex]!!, buffer, bufferInfo)
                    extractor.advance()
                }
            }

            muxer.stop()
            muxer.release()
            extractor.release()

            "mp4 래핑 완료 → ${outputFile.absolutePath}"
        } catch (e: Exception) {
            Log.e(TAG, "[$ext] 래핑 실패: ${e.message}", e)
            "래핑 실패: ${e.message}"
        }
    }

    private fun playVideo(path: String) {
        runOnUiThread {
            binding.videoView.setVideoPath(path)
            binding.videoView.setOnPreparedListener { it.start() }
        }
    }

    private fun downloadFile(urlStr: String, fileName: String): File? {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connect()
            if (conn.responseCode != 200) {
                Log.e(TAG, "HTTP 오류: ${conn.responseCode}")
                return null
            }
            val file = File(cacheDir, "$fileName.tmp")
            conn.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "다운로드 실패: ${e.message}")
            null
        }
    }
}