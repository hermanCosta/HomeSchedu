package com.herman.homeschedu.Interface

import com.herman.homeschedu.Model.Item

interface ILoadHousemate {
    fun onAllSalonLoadSuccess(housemateList: List<Item>)

}