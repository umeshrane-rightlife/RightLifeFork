package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.addCallback
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityJournalListBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetDeleteTagBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JournalListActivity : BaseActivity() {

    private lateinit var binding: ActivityJournalListBinding
    private lateinit var adapter: JournalListAdapter
    private val journalList = mutableListOf<JournalEntry>()

    private val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    private var calendarDays = mutableListOf<CalendarDay>()
    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate: CalendarDay? = null
    //private var startDate = ""
    var isFromThinkRight: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalListBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        //startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)

        binding.addEntryButton.setOnClickListener {
            it.disableViewForSeconds()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = sdf.format(Date())
            if (selectedDate == null || formattedDate == selectedDate?.dateString) {
                startActivity(Intent(this, JournalNewActivity::class.java).apply {
                    //putExtra("StartDate", startDate)
                    putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                })
            } else
                showCustomToast("You can create entry for current date only")
        }

        binding.btnBack.setOnClickListener {
            callPostMindFullDataAPI()
            closeActivity()
        }

        onBackPressedDispatcher.addCallback(this) {
            callPostMindFullDataAPI()
            closeActivity()
        }

        binding.btnInfo.setOnClickListener {
            it.disableViewForSeconds()
            val htmlText = """
    <p>This page gives you a bird’s-eye view of your journaling habit.</p>
    <p>Tap on a day to see your entries, moods, and themes.</p>
    <p>It’s a space to notice patterns, celebrate consistency, and reconnect with what you’ve felt and written over time.</p>
""".trimIndent()

            DialogUtils.showJournalCommonDialog(this, "Your Entries", htmlText)
        }

        adapter = JournalListAdapter(journalList, object : JournalListAdapter.OnItemClickListener {
            override fun onMenuClick(journalEntry: JournalEntry, view: View) {
                showCustomPopupMenu(view, journalEntry)
            }

            override fun onItemClick(journalEntry: JournalEntry) {

            }

        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.rvCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCalendar.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.spacing)))
        calendarAdapter = CalendarAdapter(calendarDays) { selectedDay ->
            // Toggle selection
            if (!isFutureDate(selectedDay.dateString)) {
                calendarDays.forEach { it.isSelected = false }
                selectedDay.isSelected = true

                selectedDay.isChecked = !selectedDay.isChecked

                calendarAdapter.updateData(calendarDays)
                selectedDate = selectedDay
                getJournalList(selectedDay.dateString)
            } else
                showCustomToast("Hold up, we haven’t lived that day yet!")
        }

        binding.rvCalendar.adapter = calendarAdapter

        updateWeekView()

        binding.ivPrev.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekView()
        }

        binding.ivNext.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekView()
        }
    }

    override fun onResume() {
        super.onResume()
        if (selectedDate == null)
            getJournalList(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(calendar.time)
            )
        else
            selectedDate?.dateString?.let { getJournalList(it) }
    }

    private fun showDeleteBottomSheet(journalEntry: JournalEntry) {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetDeleteTagBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.tvTitle.text = "Delete Journal?"
        dialogBinding.tvDescription.text = "Are you sure you want to delete? This cannot be undone."

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnDelete.setOnClickListener {
            deleteJournal(journalEntry)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun initSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            private val deleteIcon = getDrawable(R.drawable.delete_journal) // your trash icon
            private val background = ColorDrawable(Color.RED)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                adapter.deleteItem(position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                if (dX < 0) { // Swiping left
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    deleteIcon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        val iconBottom = iconTop + it.intrinsicHeight

                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun showCustomPopupMenu(view: View, journalEntry: JournalEntry) {
        val menuItems = listOf(
            MenuItemData("Edit", R.drawable.edit_journal, R.color.black),
            MenuItemData("Delete", R.drawable.delete_journal, R.color.delete_color_journal)
        )

        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_custom_menu, null)
        val listView = popupView.findViewById<ListView>(R.id.listView)

        val popupWindow = PopupWindow(popupView, 400, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        listView.adapter = PopupMenuAdapter(this, menuItems) { item ->
            if (item.title == "Edit") {
                val intent: Intent = Intent(this, Journal4QuestionsActivity::class.java)
                   /* when (journalEntry.title) {
                        "Free Form" -> Intent(this, FreeFormJournalActivity::class.java)
                        "Bullet" -> Intent(this, BulletJournalActivity::class.java)
                        else -> {
                            Intent(this, GriefJournalActivity::class.java)
                        }

                    }*/
                startActivity(intent.apply {
                    putExtra("JournalEntry", journalEntry)
                    //putExtra("StartDate", startDate)
                    putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                })
            } else {
                showDeleteBottomSheet(journalEntry)
            }
            popupWindow.dismiss()
        }

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(view, -100, 0)
    }

    private fun updateWeekView() {
        val (currentWeekString, currentWeek) = getWeekDates()
        var isFutureDatePresent = false
        calendarDays.clear()

        for (i in daysOfWeek.indices) {
            val isToday = currentWeek[i] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, currentWeek[i])
            cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            calendarDays.add(
                CalendarDay(
                    daysOfWeek[i], currentWeek[i], isSelected = isToday,
                    currentWeekString[i]
                )
            )
            if (isFutureDate(calendarDays[i].dateString)) {
                isFutureDatePresent = true
            }
        }

        binding.tvSelectedDate.text =
            SimpleDateFormat("EEE . dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        calendarAdapter.updateData(calendarDays)
        binding.ivNext.isEnabled = !isFutureDatePresent
        binding.ivNext.setImageResource(if (isFutureDatePresent) R.drawable.right_arrow_journal else R.drawable.right_arrow_journal_enabled)
    }

    private fun getWeekDates(): Pair<List<String>, List<Int>> {
        val calendarCopy = Calendar.getInstance()
        calendarCopy.time = calendar.time // Copy original calendar's time

        // Start week from Monday
        calendarCopy.firstDayOfWeek = Calendar.MONDAY
        calendarCopy.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateStrings = mutableListOf<String>()
        val dayOfMonths = mutableListOf<Int>()

        repeat(7) {
            dateStrings.add(dateFormat.format(calendarCopy.time))
            dayOfMonths.add(calendarCopy.get(Calendar.DAY_OF_MONTH))
            calendarCopy.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateStrings to dayOfMonths
    }


    private fun getJournalList(date: String) {
        Utils.showLoader(this)
        val call = apiService.getJournalList(sharedPreferenceManager.accessToken, date)
        call.enqueue(object : Callback<JournalListResponse> {
            override fun onResponse(
                call: Call<JournalListResponse>,
                response: Response<JournalListResponse>
            ) {
                Utils.dismissLoader(this@JournalListActivity)
                if (response.isSuccessful && response.body() != null) {
                    journalList.clear()
                    response.body()?.data?.let { journalList.addAll(it) }
                    adapter.notifyDataSetChanged()
                    //initSwipeToDelete()
                } else {
                    Toast.makeText(
                        this@JournalListActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JournalListResponse>, t: Throwable) {
                Utils.dismissLoader(this@JournalListActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun deleteJournal(journalEntry: JournalEntry) {
        Utils.showLoader(this)
        val call =
            apiService.deleteJournalEntry(sharedPreferenceManager.accessToken, journalEntry.id)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Utils.dismissLoader(this@JournalListActivity)
                if (response.isSuccessful && response.body() != null) {
                    showCustomToast("Journal Entry Deleted")
                    journalList.remove(journalEntry)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@JournalListActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utils.dismissLoader(this@JournalListActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun callPostMindFullDataAPI() {
        /*val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(this, "Journaling", startDate, endDate)*/
    }

    private fun isFutureDate(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false

            val inputDate = dateFormat.parse(dateString)
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            inputDate?.after(today.time) ?: false
        } catch (e: Exception) {
            false // return false for invalid date format
        }
    }

    private fun closeActivity() {
        if (isFromThinkRight) {
            val intent = Intent(this, HomeNewActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("finish_Journal", true)
                putExtra("FROM_THINK_RIGHT", isFromThinkRight)
            }
            startActivity(intent)
        }
        finish()
    }


}