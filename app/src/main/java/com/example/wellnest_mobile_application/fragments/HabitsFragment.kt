package com.example.wellnest_mobile_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.adapters.HabitAdapter
import com.example.wellnest_mobile_application.data.SharedPrefManager
import com.example.wellnest_mobile_application.models.Habit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class HabitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private val habits = mutableListOf<Habit>()
    private lateinit var prefManager: SharedPrefManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)
        prefManager = SharedPrefManager(requireContext())

        initViews(view)
        setupRecyclerView()
        loadHabits()
        updateProgress()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerHabits)
        progressBar = view.findViewById(R.id.progressDaily)
        tvProgress = view.findViewById(R.id.tvProgress)

        view.findViewById<FloatingActionButton>(R.id.fabAddHabit).setOnClickListener {
            showHabitDialog()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HabitAdapter(
            habits,
            onEdit = { editHabit(it) },
            onDelete = { deleteHabit(it) },
            onToggle = { pos, checked ->
                habits[pos].isCompleted = checked
                prefManager.updateHabit(habits[pos])
                updateProgress()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun loadHabits() {
        habits.clear()
        habits.addAll(prefManager.getHabits())
        adapter.notifyDataSetChanged()
    }

    private fun updateProgress() {
        if (habits.isEmpty()) {
            progressBar.progress = 0
            tvProgress.text = "Add your first habit to get started!"
            return
        }

        val completedCount = habits.count { it.isCompleted }
        val totalCount = habits.size
        val progressPercentage = ((completedCount.toFloat() / totalCount) * 100).toInt()

        progressBar.progress = progressPercentage
        tvProgress.text = "$completedCount of $totalCount habits completed today"
    }

    private fun showHabitDialog(editIndex: Int? = null) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<TextInputEditText>(R.id.etHabitName)
        val etDuration = dialogView.findViewById<TextInputEditText>(R.id.etDuration)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        editIndex?.let {
            etHabitName.setText(habits[it].name)
            etDuration.setText(habits[it].durationMinutes.toString())
        }

        val dialog = builder.setView(dialogView).create()

        btnSave.setOnClickListener {
            val name = etHabitName.text.toString().trim()
            val duration = etDuration.text.toString().toIntOrNull() ?: 30

            if (name.isNotBlank()) {
                if (editIndex != null) {
                    habits[editIndex].name = name
                    habits[editIndex].durationMinutes = duration
                    habits[editIndex].timeRemaining = if (!habits[editIndex].isCompleted) duration else 0
                    prefManager.updateHabit(habits[editIndex])
                    adapter.notifyItemChanged(editIndex)
                } else {
                    val newHabit = Habit(
                        id = (habits.maxOfOrNull { it.id } ?: 0) + 1,
                        name = name,
                        durationMinutes = duration,
                        timeRemaining = duration
                    )
                    prefManager.addHabit(newHabit)
                    habits.add(newHabit)
                    adapter.notifyItemInserted(habits.size - 1)
                }
                updateProgress()
                dialog.dismiss()
            } else {
                etHabitName.error = "Please enter habit name"
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun editHabit(position: Int) {
        showHabitDialog(position)
    }

    private fun deleteHabit(position: Int) {
        val habitToDelete = habits[position]

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habitToDelete.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                prefManager.deleteHabit(habitToDelete.id)
                habits.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateProgress()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}