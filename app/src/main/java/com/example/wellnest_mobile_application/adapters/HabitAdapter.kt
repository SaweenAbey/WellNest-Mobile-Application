package com.example.wellnest_mobile_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.models.Habit
import com.example.wellnest_mobile_application.models.HabitType
import com.google.android.material.button.MaterialButton

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onToggle: (Int, Boolean) -> Unit,
    private val onStepsUpdate: (Int, Int) -> Unit,
    private val onTimeUpdate: (Int, Int) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitIcon: TextView = itemView.findViewById(R.id.tvHabitIcon)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvHabitType: TextView = itemView.findViewById(R.id.tvHabitType)
        val tvCountdown: TextView = itemView.findViewById(R.id.tvCountdown)
        val tvSubStatus: TextView = itemView.findViewById(R.id.tvSubStatus)
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbHabitCompleted)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressHabit)
        val tvProgressPercentage: TextView = itemView.findViewById(R.id.tvProgressPercentage)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val stepControls: LinearLayout = itemView.findViewById(R.id.stepControls)
        val btnAddSteps: MaterialButton = itemView.findViewById(R.id.btnAddSteps)
        val btnRemoveSteps: MaterialButton = itemView.findViewById(R.id.btnRemoveSteps)
        val tvStepIncrement: TextView = itemView.findViewById(R.id.tvStepIncrement)
        val timeControls: LinearLayout = itemView.findViewById(R.id.timeControls)
        val btnAddTime: MaterialButton = itemView.findViewById(R.id.btnAddTime)
        val btnRemoveTime: MaterialButton = itemView.findViewById(R.id.btnRemoveTime)
        val tvTimeIncrement: TextView = itemView.findViewById(R.id.tvTimeIncrement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        try {
            if (position >= habits.size) return

            val habit = habits[position]
            val context = holder.itemView.context


            holder.tvHabitName.text = habit.name


            when (habit.type) {
                HabitType.TIME -> {
                    holder.tvHabitIcon.text = "⏱️"
                    holder.tvHabitType.text = "Time-based (Auto)"
                    holder.stepControls.visibility = View.GONE
                    holder.timeControls.visibility = View.GONE
                }
                HabitType.STEPS -> {
                    holder.tvHabitIcon.text = "👟"
                    holder.tvHabitType.text = "Steps-based"
                    holder.stepControls.visibility = View.VISIBLE
                    holder.timeControls.visibility = View.GONE
                }
            }


            holder.cbCompleted.isChecked = habit.isCompleted


            val progress = try {
                when (habit.type) {
                    HabitType.TIME -> if (habit.durationMinutes > 0) {
                        val completed = habit.durationMinutes - habit.timeRemaining
                        ((completed.toFloat() / habit.durationMinutes) * 100).toInt().coerceIn(0, 100)
                    } else {
                        if (habit.isCompleted) 100 else 0
                    }
                    HabitType.STEPS -> if (habit.stepGoal > 0) {
                        ((habit.stepsDone.toFloat() / habit.stepGoal) * 100).toInt().coerceIn(0, 100)
                    } else {
                        if (habit.isCompleted) 100 else 0
                    }
                }
            } catch (e: Exception) {
                if (habit.isCompleted) 100 else 0
            }

            holder.progressBar.progress = progress
            holder.tvProgressPercentage.text = "$progress%"


            when (habit.type) {
                HabitType.TIME -> {
                    when {
                        habit.isCompleted -> {
                            holder.tvCountdown.text = "✅ Completed"
                            holder.tvSubStatus.text = "Great job!"
                        }
                        habit.timeRemaining > 0 -> {
                            holder.tvCountdown.text = "${habit.timeRemaining} min remaining"
                            holder.tvSubStatus.text = "Auto-tracking in progress..."
                        }
                        else -> {
                            holder.tvCountdown.text = "${habit.durationMinutes} min goal"
                            holder.tvSubStatus.text = "Ready to start auto-timer"
                        }
                    }
                }
                HabitType.STEPS -> {
                    holder.tvCountdown.text = "${habit.stepsDone} / ${habit.stepGoal} steps"
                    when {
                        habit.isCompleted -> {
                            holder.tvSubStatus.text = "Goal achieved! 🎉"
                        }
                        progress >= 75 -> {
                            holder.tvSubStatus.text = "Almost there!"
                        }
                        progress >= 50 -> {
                            holder.tvSubStatus.text = "Halfway there!"
                        }
                        progress > 0 -> {
                            holder.tvSubStatus.text = "Great start!"
                        }
                        else -> {
                            holder.tvSubStatus.text = "Let's get moving!"
                        }
                    }
                }
            }


            val stepIncrement = when {
                habit.stepGoal >= 10000 -> 500
                habit.stepGoal >= 5000 -> 250
                else -> 100
            }
            holder.tvStepIncrement.text = stepIncrement.toString()


            val timeIncrement = when {
                habit.durationMinutes >= 60 -> 15
                habit.durationMinutes >= 30 -> 10
                else -> 5
            }
            holder.tvTimeIncrement.text = timeIncrement.toString()


            holder.btnAddSteps.setOnClickListener {
                try {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION &&
                        holder.adapterPosition < habits.size) {
                        val currentHabit = habits[holder.adapterPosition]
                        val newSteps = currentHabit.stepsDone + stepIncrement

                        onStepsUpdate(holder.adapterPosition, newSteps)

                        // Check if goal is reached
                        if (newSteps >= currentHabit.stepGoal && !currentHabit.isCompleted) {
                            Toast.makeText(
                                context,
                                "🎉 Congratulations! You've completed '${currentHabit.name}'!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            holder.btnRemoveSteps.setOnClickListener {
                try {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION &&
                        holder.adapterPosition < habits.size) {
                        val currentHabit = habits[holder.adapterPosition]
                        val newSteps = (currentHabit.stepsDone - stepIncrement).coerceAtLeast(0)

                        onStepsUpdate(holder.adapterPosition, newSteps)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }




            holder.cbCompleted.setOnCheckedChangeListener(null)
            holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                try {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION &&
                        holder.adapterPosition < habits.size) {

                        val currentHabit = habits[holder.adapterPosition]
                        onToggle(holder.adapterPosition, isChecked)


                        if (isChecked) {
                            Toast.makeText(
                                context,
                                "✅ '${currentHabit.name}' marked as completed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "⏸️ '${currentHabit.name}' marked as incomplete",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            holder.btnEdit.setOnClickListener {
                try {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION &&
                        holder.adapterPosition < habits.size) {
                        onEdit(holder.adapterPosition)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            holder.btnDelete.setOnClickListener {
                try {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION &&
                        holder.adapterPosition < habits.size) {
                        val habitName = habits[holder.adapterPosition].name
                        onDelete(holder.adapterPosition)

                        Toast.makeText(
                            context,
                            "🗑️ '$habitName' deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = habits.size


    fun updateHabit(position: Int, updatedHabit: Habit) {
        if (position in habits.indices) {
            habits[position] = updatedHabit
            notifyItemChanged(position)
        }
    }
    
    fun getHabitAt(position: Int): Habit {
        return habits[position]
    }
    
    fun getHabits(): List<Habit> {
        return habits.toList()
    }
    
    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }
}