package com.example.wellnest_mobile_application.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.example.wellnest_mobile_application.services.HabitTimerService
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.adapters.HabitAdapter
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.Habit
import com.example.wellnest_mobile_application.models.HabitType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HabitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var databaseManager: DatabaseManager
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var progressDaily: ProgressBar
    private lateinit var tvProgress: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        databaseManager = DatabaseManager(requireContext())

        recyclerView = view.findViewById(R.id.recyclerHabits)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)
        progressDaily = view.findViewById(R.id.progressDaily)
        tvProgress = view.findViewById(R.id.tvProgress)

        setupRecyclerView()
        setupFab()
        updateProgress()

        return view
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            habits = mutableListOf(), // Will be updated by database
            onEdit = { position -> showEditDialog(position) },
            onDelete = { position -> deleteHabit(position) },
            onToggle = { position, isChecked -> toggleHabit(position, isChecked) },
            onStepsUpdate = { position, newSteps -> updateSteps(position, newSteps) },
            onTimeUpdate = { position, timeSpent -> updateTime(position, timeSpent) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Load habits from database
        loadHabits()
    }
    
    private fun loadHabits() {
        lifecycleScope.launch {
            databaseManager.habitRepository.getHabits().collect { habits ->
                adapter.updateHabits(habits)
                updateProgress()
            }
        }
    }

    private fun setupFab() {
        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)

        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val tilDuration = dialogView.findViewById<TextInputLayout>(R.id.tilDuration)
        val tilSteps = dialogView.findViewById<TextInputLayout>(R.id.tilSteps)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val etSteps = dialogView.findViewById<EditText>(R.id.etSteps)
        val rbTime = dialogView.findViewById<RadioButton>(R.id.rbTime)
        val rbSteps = dialogView.findViewById<RadioButton>(R.id.rbSteps)
        val tvInfoText = dialogView.findViewById<TextView>(R.id.tvInfoText)


        rbTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tilDuration.visibility = View.VISIBLE
                tilSteps.visibility = View.GONE
                tvInfoText.text = "Track time-based habits like meditation, reading, or exercise"
            }
        }

        rbSteps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tilDuration.visibility = View.GONE
                tilSteps.visibility = View.VISIBLE
                tvInfoText.text = "Track step-based habits like walking or running goals"
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = etHabitName.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val habit = if (rbTime.isChecked) {
                    val duration = etDuration.text.toString().toIntOrNull() ?: 0
                    if (duration <= 0) {
                        Toast.makeText(requireContext(), "Please enter a valid duration", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    Habit(
                        id = 0,
                        name = name,
                        type = HabitType.TIME,
                        durationMinutes = duration,
                        timeRemaining = duration
                    )
                } else {
                    val steps = etSteps.text.toString().toIntOrNull() ?: 0
                    if (steps <= 0) {
                        Toast.makeText(requireContext(), "Please enter a valid step goal", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    Habit(
                        id = 0,
                        name = name,
                        type = HabitType.STEPS,
                        stepGoal = steps,
                        stepsDone = 0
                    )
                }

                lifecycleScope.launch {
                    databaseManager.habitRepository.addHabit(habit)
                    Toast.makeText(requireContext(), "✨ Habit '$name' created!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showEditDialog(position: Int) {
        val habit = adapter.getHabitAt(position)

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)

        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val tilDuration = dialogView.findViewById<TextInputLayout>(R.id.tilDuration)
        val tilSteps = dialogView.findViewById<TextInputLayout>(R.id.tilSteps)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val etSteps = dialogView.findViewById<EditText>(R.id.etSteps)
        val rbTime = dialogView.findViewById<RadioButton>(R.id.rbTime)
        val rbSteps = dialogView.findViewById<RadioButton>(R.id.rbSteps)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)


        tvDialogTitle.text = "Edit Habit"
        etHabitName.setText(habit.name)

        when (habit.type) {
            HabitType.TIME -> {
                rbTime.isChecked = true
                tilDuration.visibility = View.VISIBLE
                tilSteps.visibility = View.GONE
                etDuration.setText(habit.durationMinutes.toString())
            }
            HabitType.STEPS -> {
                rbSteps.isChecked = true
                tilDuration.visibility = View.GONE
                tilSteps.visibility = View.VISIBLE
                etSteps.setText(habit.stepGoal.toString())
            }
        }


        rbTime.isEnabled = false
        rbSteps.isEnabled = false

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = etHabitName.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedHabit = when (habit.type) {
                    HabitType.TIME -> {
                        val duration = etDuration.text.toString().toIntOrNull() ?: habit.durationMinutes
                        habit.copy(name = name, durationMinutes = duration)
                    }
                    HabitType.STEPS -> {
                        val steps = etSteps.text.toString().toIntOrNull() ?: habit.stepGoal
                        habit.copy(name = name, stepGoal = steps)
                    }
                }

                lifecycleScope.launch {
                    databaseManager.habitRepository.updateHabit(updatedHabit)
                    Toast.makeText(requireContext(), "✏️ Habit updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun deleteHabit(position: Int) {
        val habit = adapter.getHabitAt(position)

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    databaseManager.habitRepository.deleteHabit(habit.id)
                }
            }
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleHabit(position: Int, isChecked: Boolean) {
        val habit = adapter.getHabitAt(position)
        lifecycleScope.launch {
            databaseManager.habitRepository.updateHabitCompletion(habit.id, isChecked)
        }
    }

    private fun updateSteps(position: Int, newSteps: Int) {
        val habit = adapter.getHabitAt(position)
        lifecycleScope.launch {
            databaseManager.habitRepository.updateHabitStepsDone(habit.id, newSteps)
        }
    }

    private fun updateTime(position: Int, timeSpent: Int) {
        val habit = adapter.getHabitAt(position)
        val newTimeRemaining = habit.timeRemaining - timeSpent
        val isCompleted = newTimeRemaining <= 0
        
        lifecycleScope.launch {
            databaseManager.habitRepository.updateHabitTimeRemaining(habit.id, maxOf(0, newTimeRemaining))
            if (isCompleted) {
                databaseManager.habitRepository.updateHabitCompletion(habit.id, true)
            }
        }
    }

    private fun updateProgress() {
        lifecycleScope.launch {
            val completionRate = databaseManager.habitRepository.getHabitCompletionPercentage()
            progressDaily.progress = completionRate.toInt()

            val habits = adapter.getHabits()
            val completed = habits.count { it.isCompleted }
            val total = habits.size

            tvProgress.text = when {
                total == 0 -> "Start building healthy habits!"
                completed == total -> "🎉 All habits completed! Amazing!"
                completionRate >= 75 -> "Almost there! $completed/$total completed"
                completionRate >= 50 -> "Great progress! $completed/$total completed"
                completed > 0 -> "Keep going! $completed/$total completed"
                else -> "Let's get started! 0/$total completed"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startTimerService()
    }

    private fun startTimerService() {
        lifecycleScope.launch {
            val habits = adapter.getHabits()
            val activeTimeHabits = habits.filter { it.type == HabitType.TIME && !it.isCompleted }
            if (activeTimeHabits.isNotEmpty()) {
                val intent = Intent(requireContext(), HabitTimerService::class.java)
                requireContext().startService(intent)
            }
        }
    }
}