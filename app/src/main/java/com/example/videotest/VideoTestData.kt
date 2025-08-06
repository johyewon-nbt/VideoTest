package com.example.videotest

/**
 * 각 테스트 영상의 메타 정보
 * - expected: 이론적으로 예상되는 재생 가능 여부
 * - expectedReason: 예상 근거
 */
data class VideoInfo(
    val extension: String,
    val url: String,
    val expected: Boolean,
    val expectedReason: String
)

object VideoTestData {
    val testVideos = listOf(
        VideoInfo(
            "mp4",
            "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4",
            expected = true,
            expectedReason = "Android 기본 지원 코덱 (H.264/AAC)"
        ),
        VideoInfo(
            "webm",
            "https://mdn.github.io/learning-area/html/multimedia-and-embedding/video-and-audio-content/rabbit320.webm",
            expected = true,
            expectedReason = "VP8/VP9 하드웨어 디코더 필요, 일부 기기만 지원"
        ),
        VideoInfo(
            "mpg",
            "https://filesamples.com/samples/video/mpg/sample_640x360.mpg",
            expected = false,
            expectedReason = "MPEG-2 비디오 / MP2 오디오 코덱 미지원"
        ),
        VideoInfo(
            "mov",
            "https://filesamples.com/samples/video/mov/sample_640x360.mov",
            expected = false,
            expectedReason = "H.264/AAC일 경우만 재생 가능, 그 외 코덱 미지원"
        ),
        VideoInfo(
            "avi",
            "https://file-examples.com/wp-content/uploads/2018/04/file_example_AVI_480_750kB.avi",
            expected = false,
            expectedReason = "DivX/Xvid, MPEG-4 ASP 코덱 미지원"
        ),
        VideoInfo(
            "wmv",
            "https://archive.org/download/WindowsVista.InboxMedium.Video/WindowsVista.InboxMedium.Video.wmv",
            expected = false,
            expectedReason = "VC-1 비디오 코덱 미지원"
        )
    )
}