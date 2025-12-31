package com.kartikeya.DanceScoreAI

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    private var selectedSong: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnStartDance = findViewById<MaterialButton>(R.id.btnStartDance)
        val btnHistory = findViewById<MaterialButton>(R.id.btnHistory)
        val btnAbout = findViewById<MaterialButton>(R.id.btnAbout)
        val autoCompleteSong =
            findViewById<AutoCompleteTextView>(R.id.autoCompleteSong)

        // Song list (dropdown)
        val songs = listOf(
            "Song One",
            "Song Two",
            "Song Three"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            songs
        )
        autoCompleteSong.setAdapter(adapter)

        autoCompleteSong.setOnItemClickListener { _, _, position, _ ->
            selectedSong = songs[position]
        }

        // Start Dance
        btnStartDance.setOnClickListener {

            if (selectedSong == null) {
                Toast.makeText(this, "Please select a song", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CameraActivity::class.java)

            when (selectedSong) {
                "Song One" -> {
                    intent.putExtra("SONG_AUDIO", R.raw.song1)
                    intent.putExtra("REFERENCE_VIDEO_ASSET", "ref_dance.mp4")
                }
                "Song Two" -> {
                    intent.putExtra("SONG_AUDIO", R.raw.song1)
                    intent.putExtra("REFERENCE_VIDEO_ASSET", "ref_dance.mp4")
                }
                "Song Three" -> {
                    intent.putExtra("SONG_AUDIO", R.raw.song1)
                    intent.putExtra("REFERENCE_VIDEO_ASSET", "ref_dance.mp4")
                }
            }

            startActivity(intent)
        }

        // OLD buttons (UNCHANGED)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, ScoreHistoryActivity::class.java))
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
