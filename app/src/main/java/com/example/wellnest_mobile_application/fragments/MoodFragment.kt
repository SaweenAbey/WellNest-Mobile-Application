package com.example.wellnest_mobile_application.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnest_mobile_application.MoodActivity
import com.example.wellnest_mobile_application.R
import com.example.wellnest_mobile_application.adapters.MoodAdapter
import com.example.wellnest_mobile_application.database.DatabaseManager
import com.example.wellnest_mobile_application.models.MoodEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

class MoodFragment : Fragment() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var btnPrevDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var recycler: RecyclerView
    private lateinit var fab: FloatingActionButton

    private lateinit var databaseManager: DatabaseManager
    private val items = mutableListOf<MoodEntry>()
    private lateinit var adapter: MoodAdapter

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedCal: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        databaseManager = DatabaseManager(requireContext())

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        btnPrevDay = view.findViewById(R.id.btnPrevDay)
        btnNextDay = view.findViewById(R.id.btnNextDay)
        recycler = view.findViewById(R.id.recyclerMoods)
        fab = view.findViewById(R.id.fabAddMood)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = MoodAdapter(items) { pos -> deleteMood(pos) }
        recycler.adapter = adapter

        updateSelectedDateText()
        loadForSelectedDate()

        tvSelectedDate.setOnClickListener { openDatePicker() }
        btnPrevDay.setOnClickListener { changeDay(-1) }
        btnNextDay.setOnClickListener { changeDay(1) }
        fab.setOnClickListener { 
            val intent = Intent(requireContext(), MoodActivity::class.java)
            startActivity(intent)
        }

        updateFabEnabled()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadForSelectedDate()
    }

    private fun changeDay(delta: Int) {
        selectedCal.add(Calendar.DAY_OF_YEAR, delta)
        updateSelectedDateText()
        loadForSelectedDate()
        updateFabEnabled()
    }

    private fun openDatePicker() {
        val y = selectedCal.get(Calendar.YEAR)
        val m = selectedCal.get(Calendar.MONTH)
        val d = selectedCal.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            selectedCal.set(year, month, dayOfMonth)
            updateSelectedDateText()
            loadForSelectedDate()
            updateFabEnabled()
        }, y, m, d).show()
    }

    private fun updateSelectedDateText() {
        tvSelectedDate.text = dateFormat.format(selectedCal.time)
    }

    private fun loadForSelectedDate() {
        lifecycleScope.launch {
            try {
                items.clear()
                val date = dateFormat.format(selectedCal.time)
                val moodEntries = databaseManager.moodEntryRepository.getMoodEntriesByDate(date)
                items.addAll(moodEntries)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showAddMoodDialog() {
        try {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_add_mood, null)

            val spMood = view.findViewById<Spinner>(R.id.spMood)
            val etEmoji = view.findViewById<EditText>(R.id.etEmoji)
            val etNote = view.findViewById<EditText>(R.id.etNote)
            val etDuration = view.findViewById<EditText>(R.id.etDuration)

            val moods = listOf("Happy", "Calm", "Sad", "Angry", "Excited", "Tired", "Anxious")
            spMood.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, moods)

            spMood.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                    val mood = moods[position]
                    etEmoji.setText(mapEmojiForMood(mood, etEmoji.text.toString()))
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) { }
            }

            builder.setView(view)
            builder.setTitle("Add Mood")

            builder.setPositiveButton("Save") { dialog, _ ->
                try {
                    val mood = spMood.selectedItem?.toString()?.trim().orEmpty()
                    val emoji = etEmoji.text.toString().trim().ifEmpty { "ðŸ˜Š" }
                    val note = etNote.text.toString().trim()
                    val duration = etDuration.text.toString().trim().toIntOrNull() ?: 0

                    if (mood.isEmpty()) {
                        Toast.makeText(context, "Please select mood", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (duration <= 0 || duration > 1440) {
                        Toast.makeText(context, "Please enter duration between 1 and 1440", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }


                    val todayStr = dateFormat.format(Calendar.getInstance().time)
                    val selectedDateStr = dateFormat.format(selectedCal.time)
                    if (todayStr != selectedDateStr) {
                        Toast.makeText(context, "You can only add mood for today", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val now = Calendar.getInstance()
                    val selectedDate = selectedDateStr
                    val timeStr = timeFormat.format(now.time)

                    val emojiMapped = mapEmojiForMood(mood, emoji)

                    val existing = runBlocking { databaseManager.moodEntryRepository.getMoodEntries().first() }
                    val newId = if (existing.isNotEmpty()) existing.maxOf { it.id } + 1 else 1
                    val entry = MoodEntry(
                        id = newId,
                        mood = mood,
                        emoji = emojiMapped,
                        date = selectedDate,
                        time = timeStr,
                        note = note,
                        durationMinutes = duration
                    )

                    lifecycleScope.launch {
                        databaseManager.moodEntryRepository.saveMoodEntry(entry)
                        loadForSelectedDate()
                    }
                    dialog.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error saving mood", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.create().show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mapEmojiForMood(mood: String, fallback: String): String {
        return when (mood.lowercase(Locale.getDefault())) {
            "happy" -> "😀"
            "sick" -> "🤒"
            "sad" -> "😢"
            "angry" -> "😡"
            "excited" -> "😃"
            "tired" -> "😫"
            "anxious" -> "😰"
            "loving" -> "🥰"
            else -> if (fallback.isNotBlank()) fallback else "🤦‍♀️"
        }
    }

    private fun updateFabEnabled() {
        val todayStr = dateFormat.format(Calendar.getInstance().time)
        val selectedStr = dateFormat.format(selectedCal.time)
        val isToday = todayStr == selectedStr
        fab.isEnabled = isToday
        fab.alpha = if (isToday) 1f else 0.5f
    }

    private fun deleteMood(position: Int) {
        try {
            if (position < 0 || position >= items.size) return
            val target = items[position]
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Mood")
                .setMessage("Do you delete mood?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        databaseManager.moodEntryRepository.deleteMoodEntryById(target.id)
                        loadForSelectedDate()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}