package com.herman.homeschedu.Interface

interface IAllResourceLoadListener {
    fun onAllResourceLoadSuccess(resourceNameList: List<String>)
    fun onAllResourceLoadFailed(message: String)

}