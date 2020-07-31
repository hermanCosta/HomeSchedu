package com.herman.homeschedu.Interface

import com.herman.homeschedu.Model.Place

interface IPlaceLoadListener {
    fun onPlaceLoadSuccess(placeList: List<Place>)
    fun onPlaceLoadFailed(message: String)
}