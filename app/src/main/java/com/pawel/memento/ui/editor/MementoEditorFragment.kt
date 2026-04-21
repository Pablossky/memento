package com.pawel.memento.ui.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.pawel.memento.R
import com.pawel.memento.data.model.*
import com.pawel.memento.databinding.FragmentMementoEditorBinding
import com.pawel.memento.notification.AlarmScheduler
import com.pawel.memento.util.ColorUtils
import com.pawel.memento.util.DateTimeUtils
import java.util.Calendar

class MementoEditorFragment : Fragment() {
    private var _binding: FragmentMementoEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MementoEditorViewModel by viewModels()
    private val args: MementoEditorFragmentArgs by navArgs()
    private var selectedDueDateTime: Long? = null
    private var selectedCategoryId: Long?  = null
    private var selectedColorIndex: Int    = 0
    private var selectedPriority           = Priority.MEDIUM
    private var selectedReminderType       = ReminderType.NOTIFICATION
    private var selectedRepeatType         = RepeatType.NONE
    private var selectedDailyCount: Int    = 1
    private val categoryIdMap              = mutableMapOf<String, Long>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMementoEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupColorPicker()
        setupPriorityChips()
        setupReminderChips()
        setupRepeatChips()
        setupDateTimePicker()
        setupFrequencyPicker()
        setupSave()
        observeData()
        requireActivity().title = if (args.mementoId != 0) {
            viewModel.loadMemento(args.mementoId.toLong()); getString(R.string.edit_memento)
        } else getString(R.string.new_memento)
    }

    private fun setupColorPicker() {
        ColorUtils.PASTEL_COLORS.forEachIndexed { i, color ->
            binding.chipGroupColors.addView(Chip(requireContext()).apply {
                isCheckable = true; isChecked = (i == 0)
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(color); text = ""
                setOnClickListener { selectedColorIndex = i; binding.cardPreview.setCardBackgroundColor(color) }
            })
        }
        binding.cardPreview.setCardBackgroundColor(ColorUtils.PASTEL_COLORS[0])
    }

    private fun setupPriorityChips() {
        val labels = listOf(R.string.priority_low, R.string.priority_medium, R.string.priority_high, R.string.priority_urgent)
        Priority.values().forEachIndexed { i, p ->
            binding.chipGroupPriority.addView(Chip(requireContext()).apply {
                text = getString(labels[i]); isCheckable = true; isChecked = (p == Priority.MEDIUM)
                setOnClickListener { selectedPriority = p }
            })
        }
    }

    private fun setupReminderChips() {
        listOf(
            ReminderType.NONE         to R.string.reminder_none,
            ReminderType.NOTIFICATION to R.string.reminder_notification,
            ReminderType.ALARM        to R.string.reminder_alarm
        ).forEach { (type, res) ->
            binding.chipGroupReminderType.addView(Chip(requireContext()).apply {
                text = getString(res); isCheckable = true; isChecked = (type == ReminderType.NOTIFICATION)
                setOnClickListener {
                    selectedReminderType = type
                    binding.layoutSoundVibration.isVisible = type != ReminderType.NONE
                }
            })
        }
    }

    private fun setupRepeatChips() {
        listOf(
            RepeatType.NONE    to R.string.repeat_none,
            RepeatType.DAILY   to R.string.repeat_daily,
            RepeatType.WEEKLY  to R.string.repeat_weekly,
            RepeatType.MONTHLY to R.string.repeat_monthly
        ).forEach { (type, res) ->
            binding.chipGroupRepeat.addView(Chip(requireContext()).apply {
                text = getString(res); isCheckable = true; isChecked = (type == RepeatType.NONE)
                setOnClickListener { selectedRepeatType = type }
            })
        }
    }

    private fun setupFrequencyPicker() {
        updateFrequencyDisplay()
        binding.btnIncreaseCount.setOnClickListener {
            if (selectedDailyCount < 5) { selectedDailyCount++; updateFrequencyDisplay() }
        }
        binding.btnDecreaseCount.setOnClickListener {
            if (selectedDailyCount > 1) { selectedDailyCount--; updateFrequencyDisplay() }
        }
    }

    private fun updateFrequencyDisplay() {
        binding.tvDailyCount.text = selectedDailyCount.toString()
    }

    private fun setupDateTimePicker() {
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance().also { c -> selectedDueDateTime?.let { c.timeInMillis = it } }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                TimePickerDialog(requireContext(), { _, h, min ->
                    selectedDueDateTime = Calendar.getInstance().apply {
                        set(y, m, d, h, min, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    refreshDateDisplay()
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.btnClearDate.setOnClickListener { selectedDueDateTime = null; refreshDateDisplay() }
    }

    private fun refreshDateDisplay() {
        val has = selectedDueDateTime != null
        binding.tvSelectedDateTime.isVisible   = has
        binding.btnClearDate.isVisible         = has
        binding.groupReminderOptions.isVisible = has
        if (has) binding.tvSelectedDateTime.text = DateTimeUtils.formatDateTime(selectedDueDateTime!!)
    }

    private fun setupSave() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isBlank()) {
                binding.tilTitle.error = getString(R.string.title_required)
                return@setOnClickListener
            }
            binding.tilTitle.error = null
            viewModel.saveMemento(
                args.mementoId.toLong(), title, binding.etDescription.text.toString().trim(),
                selectedDueDateTime, selectedCategoryId, selectedPriority, selectedColorIndex,
                if (selectedDueDateTime != null) selectedReminderType else ReminderType.NONE,
                binding.switchSound.isChecked, binding.switchVibration.isChecked,
                selectedRepeatType, selectedDailyCount
            )
        }
    }

    private fun observeData() {
        viewModel.allCategories.observe(viewLifecycleOwner) { cats ->
            categoryIdMap.clear()
            val names = mutableListOf(getString(R.string.no_category))
            cats.forEach { names.add(it.name); categoryIdMap[it.name] = it.id }
            binding.spinnerCategory.adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, names
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            selectedCategoryId?.let { id ->
                val idx = cats.indexOfFirst { it.id == id }
                if (idx >= 0) binding.spinnerCategory.setSelection(idx + 1)
            }
            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedCategoryId = if (pos == 0) null else categoryIdMap[names[pos]]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        viewModel.memento.observe(viewLifecycleOwner) { m ->
            m ?: return@observe
            binding.etTitle.setText(m.title)
            binding.etDescription.setText(m.description)
            selectedDueDateTime = m.dueDateTime; selectedCategoryId = m.categoryId
            selectedColorIndex = m.colorTagIndex; selectedPriority = m.priority
            selectedReminderType = m.reminderType; selectedRepeatType = m.repeatType
            selectedDailyCount = m.dailyCount.coerceIn(1, 5)
            updateFrequencyDisplay()
            binding.switchSound.isChecked = m.soundEnabled
            binding.switchVibration.isChecked = m.vibrationEnabled
            binding.cardPreview.setCardBackgroundColor(
                ColorUtils.PASTEL_COLORS[m.colorTagIndex % ColorUtils.PASTEL_COLORS.size])
            refreshDateDisplay()
        }
        viewModel.savedId.observe(viewLifecycleOwner) { sid ->
            sid ?: return@observe
            if (args.mementoId != 0) AlarmScheduler.cancel(requireContext(), args.mementoId)
            AlarmScheduler.scheduleByParams(requireContext(), sid,
                binding.etTitle.text.toString(), selectedDueDateTime,
                if (selectedDueDateTime != null) selectedReminderType else ReminderType.NONE,
                binding.switchSound.isChecked, binding.switchVibration.isChecked,
                binding.etDescription.text.toString())
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
