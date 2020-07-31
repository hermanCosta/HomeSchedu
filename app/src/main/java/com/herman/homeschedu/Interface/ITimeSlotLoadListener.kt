package com.herman.homeschedu.Interface

import com.herman.homeschedu.Model.TimeSlot

interface ITimeSlotLoadListener {
    fun onTimeSlotLoadSuccess(timeSlotList: List<TimeSlot>)
    fun onTimeSlotFailure(message: String)
    fun onTimeSlotLoadEmpty()

}