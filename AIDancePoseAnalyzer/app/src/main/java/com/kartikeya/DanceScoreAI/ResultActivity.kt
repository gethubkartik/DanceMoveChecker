package com.kartikeya.DanceScoreAI

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kartikeya.DanceScoreAI.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("FINAL_SCORE", 0)
        saveScore(score)
        binding.tvScore.text = "$score%"
        binding.tvFeedback.text = getFeedback(score)
        binding.ratingBar.rating = getStars(score)

        binding.btnRetry.setOnClickListener {
            finish() // back to CameraActivity
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun getStars(score: Int): Float {
        return when {
            score >= 90 -> 5f
            score >= 75 -> 4f
            score >= 60 -> 3f
            score >= 40 -> 2f
            else -> 1f
        }
    }

    private fun getFeedback(score: Int): String {
        return when {
            score >= 90 -> "Excellent! Perfect moves 🔥"
            score >= 75 -> "Great job! Keep practicing 💪"
            score >= 60 -> "Good! Improve timing ⏱️"
            score >= 40 -> "Nice try! Focus on posture 🧍"
            else -> "Keep practicing! You’ll improve 👍"
        }
    }


    private fun saveScore(score: Int) {
        val prefs = getSharedPreferences("score_prefs", MODE_PRIVATE)
        val set = prefs.getStringSet("scores", mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

        val time = java.text.SimpleDateFormat(
            "dd MMM yyyy • hh:mm a",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        set.add("$score|$time")
        prefs.edit().putStringSet("scores", set).apply()
    }

}
