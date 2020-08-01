package com.herman.homeschedu.Fragments

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.herman.homeschedu.Activity.TimeSlotAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Interface.ITimeSlotLoadListener
import com.herman.homeschedu.Model.TimeSlot
import com.herman.homeschedu.R
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_schedule_step_three.*
import kotlinx.android.synthetic.main.fragment_schedule_step_three.view.rv_time_slot_view
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScheduleStep3Fragment : Fragment(), ITimeSlotLoadListener {

    private lateinit var itemDoc: DocumentReference
    private lateinit var iTimeSlotLoadListener: ITimeSlotLoadListener
    private lateinit var dialog: AlertDialog
    private lateinit var localBroadCastManager: LocalBroadcastManager
    private lateinit var simpleDateFormat: SimpleDateFormat
    private lateinit var mAuth: FirebaseAuth

    companion object {
        private var instance: ScheduleStep3Fragment? = null
        fun getInstance(): ScheduleStep3Fragment? {
            if (instance == null) {
                instance = ScheduleStep3Fragment()
            }
            return instance
        }
    }

    private val displayTimeSlot = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val date = Calendar.getInstance()
            date.add(Calendar.DATE, 0)
            loadAvailableTimeSlotOfItem(Common.currentItem!!.itemId,
                simpleDateFormat.format(date.time)
            )
        }
    }

    private fun loadAvailableTimeSlotOfItem(itemId: String?, bookDate: String) {
        dialog.show()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        userRef.addSnapshotListener { documentSnapshot, _ ->
            val houseId = documentSnapshot?.getString("houseId")


            itemDoc = FirebaseFirestore.getInstance()
                .collection("/houses")
                .document(houseId!!)
                .collection("/Resource")
                .document(Common.place)
                .collection("/Place")
                .document(Common.currentPlace!!.placeId!!)
                .collection("/Item")
                .document(itemId!!)

            //Get information of this Item
            itemDoc.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot1 = task.result!!
                    if (documentSnapshot1.exists()) {//if item is available
                        //Get information of booking
                        //If not created, return empty
                        val date = FirebaseFirestore.getInstance()
                            .collection("/houses")
                            .document(houseId)
                            .collection("/Resource")
                            .document(Common.place)
                            .collection("/Place")
                            .document(Common.currentPlace!!.placeId!!)
                            .collection("/Item")
                            .document(Common.currentItem!!.itemId!!)
                            .collection(bookDate) // bookDate is the date simpleFormat with dd_MM_yyyy


                        date.get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                val querySnapshot = it.result
                                if (querySnapshot!!.isEmpty)
                                    iTimeSlotLoadListener.onTimeSlotLoadEmpty()
                                else {
                                    // There is appointment available
                                    val timeSlots: ArrayList<TimeSlot> = ArrayList()
                                    for (queryDocumentSnapshot in it.result!!)
                                        timeSlots.add(queryDocumentSnapshot.toObject(TimeSlot::class.java))
                                    iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots)
                                    Log.d(
                                        "ScheduleStep3Fragment", "Full slot total: ${timeSlots.size}")
                                }
                            }
                        }
                            .addOnFailureListener {
                                iTimeSlotLoadListener.onTimeSlotFailure(it.message!!)
                            }
                    }
                }

            }
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iTimeSlotLoadListener = this

        localBroadCastManager = LocalBroadcastManager.getInstance(context!!)
        localBroadCastManager.registerReceiver(displayTimeSlot, IntentFilter(Common.KEY_DISPLAY_TIME_SLOT))

        simpleDateFormat = SimpleDateFormat("dd_MM_yyyy") // This is the key

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()

    }

    override fun onDestroy() {
        localBroadCastManager.unregisterReceiver(displayTimeSlot)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val itemView = inflater.inflate(R.layout.fragment_schedule_step_three, container, false)
        val recyclerTimeSlot = itemView.rv_time_slot_view

        init(itemView, recyclerTimeSlot)

    return itemView
}

    private fun init(itemView: View , recyclerTimeSlot: RecyclerView) {
       // dialog.show()
        recyclerTimeSlot.setHasFixedSize(true)
        recyclerTimeSlot.layoutManager = GridLayoutManager(activity, 3)



        horizontalCalendar(itemView)

    }

    private fun horizontalCalendar(itemView: View) {
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.DATE, 0)
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.DATE, 2) // 2 days left

        val horizontalCalendar = HorizontalCalendar.Builder(itemView, R.id.step_three_calendar_view)
            .range(startDate, endDate)
            .datesNumberOnScreen(1)
            .mode(HorizontalCalendar.Mode.DAYS)
            .defaultSelectedDate(startDate)
            .build()

        horizontalCalendar.calendarListener = object : HorizontalCalendarListener() {
            override fun onDateSelected(date: Calendar, position: Int) {
                if (Common.scheduleDate.timeInMillis != date.timeInMillis) {

                    Common.scheduleDate = date // This code will not load again if selected new day on the same day selected
                    loadAvailableTimeSlotOfItem(Common.currentItem!!.itemId!!,
                        simpleDateFormat.format(date.time))
                }
            }
        }
    }



    override fun onTimeSlotLoadSuccess(timeSlotList: List<TimeSlot>) {
        //dialog.show()
        val adapter = TimeSlotAdapter(context!!, timeSlotList)
        rv_time_slot_view.adapter = adapter

        dialog.dismiss()
    }

    override fun onTimeSlotFailure(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    override fun onTimeSlotLoadEmpty() {
        val adapter = TimeSlotAdapter(context!!)
        rv_time_slot_view.adapter = adapter

        dialog.dismiss()
    }
}