package com.kartikeya.DanceScoreAI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kartikeya.DanceScoreAI.ScoreHistoryAdapter.VH

class ScoreHistoryAdapter(
    private val list: List<ScoreItem>
) : RecyclerView.Adapter<VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val score = view.findViewById<TextView>(R.id.tvScore)
        val date = view.findViewById<TextView>(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.score.text = "${list[position].score}%"
        holder.date.text = list[position].date
    }

    override fun getItemCount() = list.size
}
