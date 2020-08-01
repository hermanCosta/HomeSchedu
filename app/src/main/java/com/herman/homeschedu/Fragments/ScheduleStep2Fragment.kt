package com.herman.homeschedu.Fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.herman.homeschedu.Adapter.ItemAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.SpacesItemDecoration
import com.herman.homeschedu.Model.Item
import com.herman.homeschedu.R
import kotlinx.android.synthetic.main.fragment_schedule_step_two.*
import kotlinx.android.synthetic.main.fragment_schedule_step_two.view.*
import java.util.ArrayList

class ScheduleStep2Fragment : Fragment() {

    lateinit var localBroadcastManager: LocalBroadcastManager

    private val itemDoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val itemArrayList: ArrayList<Item> = intent.getParcelableArrayListExtra(Common.KEY_ITEM_LOAD_DONE)
            val itemAdapter = ItemAdapter(context, itemArrayList)
            recycler_item.adapter = itemAdapter
        }
    }

    companion object {
        private var instance: ScheduleStep2Fragment? = null
        fun getInstance() : ScheduleStep2Fragment? {
            if (instance == null) {
                instance = ScheduleStep2Fragment()
            }
            return instance
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localBroadcastManager = LocalBroadcastManager.getInstance(context!!)
        localBroadcastManager.registerReceiver(itemDoneReceiver, IntentFilter(Common.KEY_ITEM_LOAD_DONE))
    }

    override fun onDestroy() {
        localBroadcastManager.unregisterReceiver(itemDoneReceiver)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val itemView =  inflater.inflate(R.layout.fragment_schedule_step_two, container, false)
        val recyclerItem = itemView.recycler_item

        init(recyclerItem)
        return itemView
    }

    private fun init(recyclerItem: RecyclerView?) {
        recyclerItem!!.setHasFixedSize(true)
        recyclerItem.layoutManager = GridLayoutManager(activity, 2)
        recyclerItem.addItemDecoration(SpacesItemDecoration(4))
    }
}