package com.example.wellnest_mobile_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.models.MoodEntry

class MoodAdapter(
    private val items: MutableList<MoodEntry>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvMood: TextView = itemView.findViewById(R.id.tvMood)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        if (position >= items.size) return
        val entry = items[position]
        holder.tvEmoji.text = entry.emoji
        holder.tvMood.text = entry.mood
        holder.tvTime.text = entry.time
        holder.tvNote.text = entry.note
        holder.tvDuration.text = if (entry.durationMinutes > 0) "${entry.durationMinutes} min" else ""
        holder.btnDelete.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < items.size) {
                onDelete(holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}