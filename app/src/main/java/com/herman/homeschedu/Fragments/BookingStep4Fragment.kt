package com.herman.homeschedu.Fragments

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.Common.Companion.convertTimeSlotToString
import com.herman.homeschedu.Common.Common.Companion.currentItem
import com.herman.homeschedu.Common.Common.Companion.currentTimeSlot
import com.herman.homeschedu.Common.Common.Companion.fStore
import com.herman.homeschedu.Common.Common.Companion.scheduleDate
import com.herman.homeschedu.Model.ScheduleInformation
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_booking_step_four.*
import kotlinx.android.synthetic.main.fragment_booking_step_four.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.herman.homeschedu.Common.Common.Companion as Common1

class BookingStep4Fragment : Fragment() {

    lateinit var simpleDateFormat: SimpleDateFormat
    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var scheduleInformation: ScheduleInformation
    lateinit var dialog: AlertDialog
    lateinit var mAuth: FirebaseAuth
    lateinit var houseId: String

    // Current User Id
    val uid = FirebaseAuth.getInstance().uid ?: ""

    private var confirmBookingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setData()
        }
    }

    private fun setData() {

        tv_schedule_item_text.text = Common1.currentItem!!.name
        tv_schedule_time_text.text = StringBuilder(convertTimeSlotToString(currentTimeSlot))
            .append(" at ")
            .append(simpleDateFormat.format(Common1.scheduleDate.time))
        tv_schedule_place_address.text = Common1.currentPlace!!.local
        tv_schedule_place_name_text.text = Common1.currentPlace!!.name
        tv_schedule_place_information.text = Common1.currentPlace!!.information

    }

    companion object {
        private var instance: BookingStep4Fragment? = null
        fun getInstance(): BookingStep4Fragment? {
            if (instance == null) {
                instance = BookingStep4Fragment()
            }
            return instance
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply date format for display on confirm
        simpleDateFormat = SimpleDateFormat("dd_MM_yyyy")
        localBroadcastManager = LocalBroadcastManager.getInstance(context!!)
        localBroadcastManager.registerReceiver(
            confirmBookingReceiver,
            IntentFilter(Common1.KEY_CONFIRM_SCHEDULE))

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()

        mAuth = FirebaseAuth.getInstance()

    }

    override fun onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val itemView = inflater.inflate(R.layout.fragment_booking_step_four, container, false)

        val button = itemView.btn_confirm as Button
        button.setOnClickListener { confirmBooking() }

        return itemView
    }

    private fun confirmBooking() {

        dialog.show()

        //Process timestamp
        //we will use TimeStamp to filter all booking with date is greater than today
        //display all future booking
        val startTime: String = convertTimeSlotToString(Common1.currentTimeSlot)
        val convertTime = startTime.split("-") //split time

        //get start time: get 9:00
        val startTimeConvert = convertTime[0].split(":")
        val startHourInt = Integer.parseInt(startTimeConvert[0].trim()) // we get 9
        val startMinInt = Integer.parseInt(startTimeConvert[1].trim()) // we get 00

        val bookingDateWithoutHouse = Calendar.getInstance()
        bookingDateWithoutHouse.timeInMillis = scheduleDate.timeInMillis
        bookingDateWithoutHouse.set(Calendar.HOUR_OF_DAY, startHourInt)
        bookingDateWithoutHouse.set(Calendar.MINUTE, startMinInt)

        //Create TimeStamp object and apply to BookingActivity
        val timeStamp = com.google.firebase.Timestamp(bookingDateWithoutHouse.time)

        //Get current user details from database
        //val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        Log.d("BookingStep4Fragment", "User ID: $uid")

        userRef.addSnapshotListener { userDocument, _ ->

            val firstName = userDocument?.getString("firstName")
            val lastName = userDocument?.getString("lastName")
            val email = userDocument?.getString("email")
            val profileImage = userDocument?.getString("profileImageUrl")



            scheduleInformation = ScheduleInformation()
            scheduleInformation.resource = Common.place
            scheduleInformation.timestamp = timeStamp
            scheduleInformation.itemId = Common1.currentItem!!.itemId
            scheduleInformation.itemName = Common1.currentItem!!.name
            scheduleInformation.userId = uid
            scheduleInformation.firstName = firstName
            scheduleInformation.lastName = lastName
            scheduleInformation.profileImageUrl = profileImage
            scheduleInformation.email = email
            scheduleInformation.placeId = Common1.currentPlace!!.placeId
            scheduleInformation.placeName = Common1.currentPlace!!.name
            scheduleInformation.placeAddress = Common1.currentPlace!!.local
            scheduleInformation.placeInformation = Common1.currentPlace!!.information
            scheduleInformation.slot = java.lang.Long.valueOf(currentTimeSlot.toLong())
            scheduleInformation.time = StringBuilder(convertTimeSlotToString(currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(bookingDateWithoutHouse.time)).toString()


            //get current user houseID
            //val uid = FirebaseAuth.getInstance().uid ?: ""
            val userRef = Common.fStore
                .collection("/users")
                .document(uid)

            userRef.addSnapshotListener { userDocument, _ ->

                houseId = userDocument?.getString("houseId").toString()


                // SUBMIT TO ITEM DOCUMENT
                val bookingDate = FirebaseFirestore.getInstance()
                    .collection("/houses")
                    .document(houseId)
                    .collection("Resource")
                    .document(Common1.place)
                    .collection("Place")
                    .document(Common1.currentPlace!!.placeId!!)
                    .collection("Item")
                    .document(Common1.currentItem!!.itemId!!)
                    .collection(Common1.simpleDateFormat.format(scheduleDate.time))
                    .document(currentTimeSlot.toString())

                // Save Data into Item Collection
                bookingDate.set(scheduleInformation)
                    .addOnSuccessListener {

                        addToUserHouse(scheduleInformation)

                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun addToUserHouse(
        bookingInformation: ScheduleInformation
    ) {

            //Save Schedule into the House
            val houseBooking = FirebaseFirestore.getInstance()
                .collection("/houses")
                .document(houseId)
                .collection("/Booking")
                .document("AllBooking")
                .collection(Common1.simpleDateFormat.format(scheduleDate.time))
                .document()



            houseBooking.set(bookingInformation)
                .addOnSuccessListener {
                    Log.d("BookingStep4Fragment", "Booking saved to the user house")

                    if (dialog.isShowing)
                        dialog.dismiss()

                    activity!!.finish()
                    addToCalendar(scheduleDate, convertTimeSlotToString(currentTimeSlot))
                }
        }


    private fun addToCalendar(bookingDate: Calendar?, startDate: String) {

        val startTime: String = convertTimeSlotToString(Common1.currentTimeSlot)
        val convertTime = startTime.split("-") //split time

        //get start time: e.g get 9:00
        val startTimeConvert = convertTime[0].split(":")
        val startHourInt = Integer.parseInt(startTimeConvert[0].trim()) // we get 9
        val startMinInt = Integer.parseInt(startTimeConvert[1].trim()) // we get 00

        val endTimeConvert = convertTime[1].split(":")
        val endHourInt = Integer.parseInt(endTimeConvert[0].trim()) // we get 9
        val endMinInt = Integer.parseInt(endTimeConvert[1].trim()) // we get 00

        val startEvent = Calendar.getInstance()
        startEvent.timeInMillis = bookingDate!!.timeInMillis
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt) // set event start time
        startEvent.set(Calendar.MINUTE, startMinInt) // set event start min

        val endEvent = Calendar.getInstance()
        endEvent.timeInMillis = bookingDate.timeInMillis
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt) // set event end time
        endEvent.set(Calendar.MINUTE, endMinInt) // set event end min

        //after having startEvent and endEvent, convert to format String
        val calendarDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        val startEventTime = calendarDateFormat.format(startEvent.time)
        val endEventTime = calendarDateFormat.format(endEvent.time)

        addToDeviceCalendar(
            startEventTime, endEventTime, "Home Schedule",
            StringBuilder("Schedule from ")
                .append(startTime)
                .append(" at ")
                .append(currentItem!!.name)
                .append(" at the ")
                .append(Common1.currentPlace!!.name).toString(),
            StringBuilder("Local: ")
                .append(Common1.currentPlace!!.local).toString()
        )
    }

    private fun addToDeviceCalendar(
        startEventTime: String,
        endEventTime: String,
        title: String,
        description: String,
        location: String
    ) {


            val calendarDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")

            try {
                val start = calendarDateFormat.parse(startEventTime)
                val end = calendarDateFormat.parse(endEventTime)

                val event = ContentValues()

                if (getCalendar(context).isEmpty()) {
                    activity!!.finish()
                    resetStaticData()
                }
                 else {
                    //put
                    event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(context))
                    event.put(CalendarContract.Events.TITLE, title)
                    event.put(CalendarContract.Events.DESCRIPTION, description)
                    event.put(CalendarContract.Events.EVENT_LOCATION, location)


                    //Time
                    event.put(CalendarContract.Events.DTSTART, start!!.time)
                    event.put(CalendarContract.Events.DTEND, end!!.time)
                    event.put(CalendarContract.Events.ALL_DAY, 0)
                    event.put(CalendarContract.Events.HAS_ALARM, 1)

                    val timeZone = TimeZone.getDefault().id


                    event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)

                    val calendars: Uri = Uri.parse("content://com.android.calendar/events")

                    val uriSave = activity!!.contentResolver.insert(calendars, event)


                    val calendarUri = hashMapOf("calendarUri" to uriSave.toString())

                    Log.d("BookingStep4Fragment", "Calendar Uri: ${uriSave.toString()}")

                    // Merge to Item Collection
                    val houseRef = fStore
                        .collection("/houses")
                        .document(houseId)
                        .collection("Resource")
                        .document(Common1.place)
                        .collection("Place")
                        .document(Common1.currentPlace!!.placeId!!)
                        .collection("Item")
                        .document(Common1.currentItem!!.itemId!!)
                        .collection(Common1.simpleDateFormat.format(scheduleDate.time))
                        .document(currentTimeSlot.toString())

                    houseRef.update("calendarUri",calendarUri)
                        .addOnSuccessListener {
                            Log.d(
                                "BookingStep4Fragment",
                                "CalendarUri Updated in Item Collection"
                            )


                            // Merge to House Booking Collection
                            val mHouseRef = fStore
                                .collection("/houses")
                                .document(houseId)
                                .collection("/Booking")
                                .document("AllBooking")
                                .collection(Common1.simpleDateFormat.format(scheduleDate.time))
                                .whereEqualTo("itemId", currentItem!!.itemId)
                                .whereEqualTo("slot", currentTimeSlot.toLong())
                                .whereEqualTo("userId", uid)
                            mHouseRef.get().addOnCompleteListener {
                                for (querySnapshot in it.result!!) {
                                    val scheduleId = querySnapshot.id

                                    fStore
                                        .collection("/houses")
                                        .document(houseId)
                                        .collection("/Booking")
                                        .document("AllBooking")
                                        .collection(
                                            Common1.simpleDateFormat.format(
                                                scheduleDate.time
                                            )
                                        )
                                        .document(scheduleId)
                                        .set(calendarUri, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d(
                                                "BookingStep4Fragment",
                                                "CalendarUri Updated in House Booking Collection"
                                            )
                                            resetStaticData()

                                        }
                                }
                            }
                        }

                }

            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }



    private fun getCalendar(context: Context?): String {


            //Get default calendar ID of Calendar of gmail
            var gmailIdCalendar = ""
            val projection = arrayOf("_id", "calendar_displayName")
            val calendars = Uri.parse("content://com.android.calendar/calendars")


            val contentResolver = context!!.contentResolver
            if (contentResolver == null) {
                activity!!.finish()
                resetStaticData()
            }
                //select all calendars
                val managedCursor = contentResolver.query(calendars, projection, null, null, null)
                if (managedCursor!!.moveToFirst()) {
                    var calName: String
                    val nameCol = managedCursor.getColumnIndex(projection[1])
                    val idCol = managedCursor.getColumnIndex(projection[0])
                    do {
                        calName = managedCursor.getString(nameCol)
                        if (calName.contains("@gmail.com")) {

                            gmailIdCalendar = managedCursor.getString(idCol)
                            break //exit as soon the id is gotten
                        }


                    } while (managedCursor.moveToNext())
                    managedCursor.close()

                }

            return gmailIdCalendar
    }

    private fun resetStaticData() {
        Common1.step = 0
        currentTimeSlot = -1
        Common1.currentPlace = null
        currentItem = null
        scheduleDate.add(Calendar.DATE, 0) // current date added
    }
}


