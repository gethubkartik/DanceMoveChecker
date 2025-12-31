package com.kartikeya.DanceScoreAI

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScoreHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scorehistory)

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)
        val empty = findViewById<TextView>(R.id.tvEmpty)

        val prefs = getSharedPreferences("score_prefs", MODE_PRIVATE)
        val raw = prefs.getStringSet("scores", emptySet()) ?: emptySet()

        if (raw.isEmpty()) {
            empty.visibility = View.VISIBLE
            return
        }

        val list = raw.map {
            val parts = it.split("|")
            ScoreItem(parts[0].toInt(), parts[1])
        }.sortedByDescending { it.date }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = ScoreHistoryAdapter(list)
    }
}

