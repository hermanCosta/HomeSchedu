package com.herman.homeschedu.Common

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.herman.homeschedu.Model.Item
import com.herman.homeschedu.Model.Place
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class Common {

    companion object {
        var HOUSE_TO_DELETE: String? = null
        var USER_TO_DELETE: FirebaseUser? = null
        var UID_TO_DELETE: String? = ""
        var HOUSE_ID: String? = ""
        const val DEFAULT_IMAGE_URL = "https://i.dlpng.com/static/png/6542357_preview.png"
        val fStore = FirebaseFirestore.getInstance()
        var UID = FirebaseAuth.getInstance().uid ?: ""
        val currentUser = FirebaseAuth.getInstance().currentUser
        var scheduleDate: Calendar = Calendar.getInstance()
        val KEY_CONFIRM_SCHEDULE: String? = "CONFIRM_SCHEDULE"
        val KEY_TIME_SLOT: String? = "TIME_SLOT"
        val DISABLE_TAG: String = "DISABLE"
        const val TIME_SLOT_TOTAL: Int = 48
        val KEY_ITEM_SELECTED: String? = "ITEM_SELECTED"
        val KEY_STEP: String = "STEP"
        val KEY_ITEM_LOAD_DONE: String = "KEY_ITEM_LOAD_DONE"
        val KEY_ENABLE_BUTTON_NEXT: String = "ENABLE_BUTTON_NEXT"
        val KEY_DISPLAY_TIME_SLOT: String = "DISPLAY_TIME_SLOT"
        val KEY_PLACE_STORE: String = "PLACE_SAVE"
        var currentPlace: Place? = null
        var currentItem: Item? = null
        var step: Int = 0 //init the first step
        var place: String = ""
        var currentTimeSlot: Int = -1
        val simpleDateFormat = SimpleDateFormat("dd_MM_yyyy") // use only to format the database key
        val todayDate: LocalDateTime = LocalDateTime.now()


        fun convertTimestampToStringKey(timestamp: com.google.firebase.Timestamp): String {
            val date = timestamp.toDate()
            val simpleDateFormat = SimpleDateFormat("dd_MM_yyyy")
            return simpleDateFormat.format(date)
        }

        fun convertTimeSlotToString(slot: Int): String {
            when (slot) {

                1 ->
                    return "00:30 - 01:00"
                2 ->
                    return "01:00 - 01:30"
                3 ->
                    return "01:30 - 02:00"
                4 ->
                    return "02:00 - 02:30"
                5 ->
                    return "02:30 - 03:00"
                6 ->
                    return "03:00 - 03:30"
                7 ->
                    return "03:30 - 04:00"
                8 ->
                    return "04:00 - 04:30"
                9 ->
                    return "04:30 - 05:00"
                10 ->
                    return "05:00 - 05:30"
                11 ->
                    return "05:30 - 06:00"
                12 ->
                    return "06:00 - 06:30"
                13 ->
                    return "06:30 - 07:00"
                14 ->
                    return "07:00 - 07:30"
                15 ->
                    return "07:30 - 08:00"
                16 ->
                    return "08:00 - 08:30"
                17 ->
                    return "08:30 - 09:00"
                18 ->
                    return "09:00 - 09:30"
                19 ->
                    return "09:30 - 10:00"
                20 ->
                    return "10:00 - 10:30"
                21 ->
                    return "10:30 - 11:00"
                22 ->
                    return "11:00 - 11:30"
                23 ->
                    return "11:30 - 12:00"
                24 ->
                    return "12:00 - 12:30"
                25 ->
                    return "12:30 - 13:00"
                26 ->
                    return "13:00 - 13:30"
                27 ->
                    return "13:30 - 14:00"
                28 ->
                    return "14:00 - 14:30"
                29 ->
                    return "14:30 - 15:00"
                30 ->
                    return "15:00 - 15:30"
                31 ->
                    return "15:30 - 16:00"
                32 ->
                    return "16:00 - 16:30"
                33 ->
                    return "16:30 - 17:00"
                34 ->
                    return "17:00 - 17:30"
                35 ->
                    return "17:30 - 18:00"
                36 ->
                    return "18:00 - 18:30"
                37 ->
                    return "18:30 - 19:00"
                38 ->
                    return "19:00 - 19:30"
                39 ->
                    return "19:30 - 20:00"
                40 ->
                    return "20:00 - 20:30"
                41 ->
                    return "20:30 - 21:00"
                42 ->
                    return "21:00 - 21:30"
                43 ->
                    return "21:30 - 22:00"
                44 ->
                    return "22:00 - 22:30"
                45 ->
                    return "22:30 - 23:00"
                46 ->
                    return "23:00 - 23:30"
                47 ->
                    return "23:30 - 00:00"
                else -> return "00:00 - 00:30"

            }
        }
    }
}