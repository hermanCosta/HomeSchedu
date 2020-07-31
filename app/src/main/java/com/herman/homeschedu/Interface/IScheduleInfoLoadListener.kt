package com.herman.homeschedu.Interface

import com.herman.homeschedu.Model.ScheduleInformation

interface IScheduleInfoLoadListener {
    fun onScheduleInfoLoadEmpty()
    fun onScheduleInfoLoadSuccess(bookingInformation: ScheduleInformation, scheduleId: String)
    fun onScheduleInfoLoadFailed(message: String)
}