package com.example.wellnest_mobile_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.models.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onToggle: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabit: TextView = itemView.findViewById(R.id.tvHabitName)
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbHabitCompleted)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressHabit)
        val tvCountdown: TextView = itemView.findViewById(R.id.tvCountdown)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvHabit.text = habit.name
        holder.cbCompleted.isChecked = habit.isCompleted

        // Calculate progress
        val progress = if (habit.durationMinutes > 0) {
            val completed = habit.durationMinutes - habit.timeRemaining
            ((completed.toFloat() / habit.durationMinutes) * 100).toInt().coerceIn(0, 100)
        } else {
            if (habit.isCompleted) 100 else 0
        }

        holder.progressBar.progress = progress

        // Update countdown text based on completion status
        when {
            habit.isCompleted -> {
                holder.tvCountdown.text = "✓ Completed"
                holder.tvCountdown.setTextColor(
                    holder.itemView.context.getColor(R.color.primary_green)
                )
            }
            habit.timeRemaining > 0 -> {
                holder.tvCountdown.text = "${habit.timeRemaining} min left"
                holder.tvCountdown.setTextColor(
                    holder.itemView.context.getColor(R.color.text_secondary)
                )
            }
            else -> {
                holder.tvCountdown.text = "Ready to start"
                holder.tvCountdown.setTextColor(
                    holder.itemView.context.getColor(R.color.accent_blue)
                )
            }
        }

        // Set completion checkbox listener
        holder.cbCompleted.setOnCheckedChangeListener(null) // Clear previous listener
        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            if (isChecked && habit.timeRemaining > 0) {
                habit.timeRemaining = 0 // Mark as fully completed
            } else if (!isChecked) {
                habit.timeRemaining = habit.durationMinutes // Reset timer
            }
            onToggle(position, isChecked)
            notifyItemChanged(position) // Refresh this item
        }

        holder.btnEdit.setOnClickListener { onEdit(position) }
        holder.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = habits.size
}
