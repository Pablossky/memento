package com.pawel.memento.ui.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawel.memento.R
import com.pawel.memento.data.model.RepeatType
import com.pawel.memento.databinding.FragmentCalendarBinding
import com.pawel.memento.ui.adapters.MementoAdapter
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var mementoAdapter: MementoAdapter
    private val monthFmt = SimpleDateFormat("LLLL yyyy", Locale("pl"))
    private val dayFmt   = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendarAdapter = CalendarDayAdapter { ts -> viewModel.selectDay(ts) }
        binding.recyclerCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }
        mementoAdapter = MementoAdapter(
            onToggleComplete = { viewModel.toggleCompleted(it.memento) },
            onClick = {},
            onLongClick = { item ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_memento)
                    .setMessage(getString(R.string.delete_memento_confirm, item.memento.title))
                    .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteMemento(item.memento) }
                    .setNegativeButton(R.string.cancel, null).show()
                true
            },
            onToggleOccurrence = { item, idx, checked ->
                viewModel.toggleOccurrence(item.memento, idx, checked)
            }
        )
        binding.recyclerDayMementos.adapter = mementoAdapter
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }

        val rebuild: (Any?) -> Unit = { rebuildCalendar() }
        viewModel.currentYear.observe(viewLifecycleOwner, rebuild)
        viewModel.currentMonth.observe(viewLifecycleOwner, rebuild)
        viewModel.allActiveMementos.observe(viewLifecycleOwner, rebuild)

        viewModel.selectedDay.observe(viewLifecycleOwner) { ts ->
            binding.tvSelectedDate.text = if (ts != null)
                dayFmt.format(Date(ts)).replaceFirstChar { it.uppercase() }
            else ""
            rebuildCalendar()
        }
        viewModel.dayMementos.observe(viewLifecycleOwner) { list ->
            mementoAdapter.submitList(list)
            val hasSel = viewModel.selectedDay.value != null
            binding.tvNoMementos.visibility        = if (hasSel && list.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerDayMementos.visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun rebuildCalendar() {
        val year  = viewModel.currentYear.value  ?: return
        val month = viewModel.currentMonth.value ?: return
        val cal   = Calendar.getInstance().apply { set(year, month, 1) }
        binding.tvMonthYear.text = monthFmt.format(cal.time).replaceFirstChar { it.uppercase() }
        var firstDow = cal.get(Calendar.DAY_OF_WEEK) - 2
        if (firstDow < 0) firstDow = 6
        val daysInMonth     = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today           = Calendar.getInstance()
        val allMementos     = viewModel.allActiveMementos.value ?: emptyList()
        val selectedTs      = viewModel.selectedDay.value
        val dailyRepeatCount = allMementos.count { it.memento.repeatType == RepeatType.DAILY }
        val days = mutableListOf<CalendarDayAdapter.Day>()
        repeat(firstDow) { days.add(CalendarDayAdapter.Day(0, 0, false, false, 0)) }
        for (d in 1..daysInMonth) {
            val dayTs = Calendar.getInstance().apply {
                set(year, month, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val isToday = year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) && d == today.get(Calendar.DAY_OF_MONTH)
            val isSelected = selectedTs?.let {
                val sc = Calendar.getInstance().apply { timeInMillis = it }
                sc.get(Calendar.YEAR) == year && sc.get(Calendar.MONTH) == month &&
                sc.get(Calendar.DAY_OF_MONTH) == d
            } ?: false
            val dayOfWeek = Calendar.getInstance().apply { set(year, month, d) }.get(Calendar.DAY_OF_WEEK)
            val specificCount = allMementos.count { mwc ->
                val mem = mwc.memento
                if (mem.repeatType == RepeatType.DAILY) return@count false
                val dt = mem.dueDateTime ?: return@count false
                val c = Calendar.getInstance().apply { timeInMillis = dt }
                when (mem.repeatType) {
                    RepeatType.NONE    -> c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month && c.get(Calendar.DAY_OF_MONTH) == d
                    RepeatType.WEEKLY  -> c.get(Calendar.DAY_OF_WEEK) == dayOfWeek
                    RepeatType.MONTHLY -> c.get(Calendar.DAY_OF_MONTH) == d
                    else -> false
                }
            }
            days.add(CalendarDayAdapter.Day(d, dayTs, isToday, isSelected, specificCount + dailyRepeatCount))
        }
        calendarAdapter.submitList(days)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class CalendarDayAdapter(private val onDayClick: (Long) -> Unit) :
        RecyclerView.Adapter<CalendarDayAdapter.VH>() {

    data class Day(val dayOfMonth: Int, val timestamp: Long,
                   val isToday: Boolean, val isSelected: Boolean, val mementoCount: Int)

    private val items = mutableListOf<Day>()
    fun submitList(list: List<Day>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun getItemCount() = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos], onDayClick)

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay:   TextView = itemView.findViewById(R.id.tvDay)
        private val viewDot: View     = itemView.findViewById(R.id.viewDot)
        fun bind(day: Day, onDayClick: (Long) -> Unit) {
            if (day.dayOfMonth == 0) {
                tvDay.text = ""; tvDay.background = null
                viewDot.visibility = View.INVISIBLE; itemView.isClickable = false; return
            }
            itemView.isClickable = true
            tvDay.text = day.dayOfMonth.toString()
            viewDot.visibility = if (day.mementoCount > 0) View.VISIBLE else View.INVISIBLE
            if (day.mementoCount > 0) viewDot.background?.setTint(Color.parseColor("#7B5EA7"))
            when {
                day.isSelected -> {
                    tvDay.setBackgroundResource(R.drawable.bg_color_circle)
                    tvDay.background.setTint(Color.parseColor("#B5C8E2"))
                    tvDay.setTextColor(Color.parseColor("#1A1A1A"))
                }
                day.isToday -> {
                    tvDay.setBackgroundResource(R.drawable.bg_color_circle)
                    tvDay.background.setTint(Color.parseColor("#F4C2C2"))
                    tvDay.setTextColor(Color.parseColor("#1A1A1A"))
                }
                else -> { tvDay.background = null; tvDay.setTextColor(Color.parseColor("#1A1A1A")) }
            }
            itemView.setOnClickListener { onDayClick(day.timestamp) }
        }
    }
}
