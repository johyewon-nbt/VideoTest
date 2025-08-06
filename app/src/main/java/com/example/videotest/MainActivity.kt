package com.example.videotest

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videotest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnMediaPlayer.setOnClickListener {
            startActivity(Intent(this, MediaPlayerTestActivity::class.java))
        }
        binding.btnWebView.setOnClickListener {
            startActivity(Intent(this, WebViewTestActivity::class.java))
        }
        binding.btnRemux.setOnClickListener {
            startActivity(Intent(this, RemuxActivity::class.java))
        }
    }
}