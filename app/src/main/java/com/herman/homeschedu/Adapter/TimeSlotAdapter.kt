package com.herman.homeschedu.Activity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.herman.homeschedu.R
import com.herman.homeschedu.Model.TimeSlot
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Interface.IRecyclerItemSelectedListener
import kotlinx.android.synthetic.main.layout_time_slot.view.*
import kotlinx.android.synthetic.main.layout_time_slot.view.tv_time_slot

open class TimeSlotAdapter constructor(): RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    lateinit var context: Context
    private lateinit var timeSlotList: List<TimeSlot>
    private lateinit var cardViewList: ArrayList<CardView>
    private lateinit var localBroadcastManager: LocalBroadcastManager

    constructor(context: Context) : this() {
        this.context = context
        this.timeSlotList = ArrayList()
        this.cardViewList = ArrayList()
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context)
    }

    constructor(
        context: Context,
        timeSlotList: List<TimeSlot>
    ) : this() {
        this.context = context
        this.timeSlotList = timeSlotList
        this.cardViewList = ArrayList()
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context)
    }


    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var cardTimeSlot: CardView = itemView.card_time_slot
        var timeSlot: TextView = itemView.tv_time_slot
        var timeSlotDescription: TextView = itemView.tv_time_description
        //var cardItem: CardView = itemView.barber_card_item

        var iRecyclerItemSelectedListener: IRecyclerItemSelectedListener? = null

        fun setiRecyclerItemSelectedListener(iRecyclerItemSelectedListener: IRecyclerItemSelectedListener?) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            iRecyclerItemSelectedListener!!.onItemSelectedListener(v!!, absoluteAdapterPosition)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.layout_time_slot, parent, false)

        return TimeSlotViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {

        holder.timeSlot.text = StringBuilder(Common.convertTimeSlotToString(position)).toString()
        if (timeSlotList.isEmpty()) { //if all positions are available, list them

            // if all slots are empty, all card is enabled
            holder.cardTimeSlot.isEnabled = true

            holder.timeSlotDescription.text = "Available"
            holder.timeSlotDescription.setTextColor(
                ContextCompat.getColor(context, android.R.color.black))
            holder.timeSlot.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            holder.cardTimeSlot.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.white
                )
            )
        } else { // if position is full
            for (slotValue: TimeSlot in timeSlotList) {
                val slotItem = Integer.parseInt(slotValue.slot.toString())
                if (slotItem == position) {
                    // Loop all time slot from server and set different color
                    holder.cardTimeSlot.isEnabled = false
                    holder.cardTimeSlot.tag = Common.DISABLE_TAG
                    holder.timeSlotDescription.text = "Full"
                    holder.timeSlotDescription.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    holder.timeSlot.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    holder.cardTimeSlot.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))

                }
            }
        }

        // Add only available time_slot card to list
        //  don't add card that is already in cardViewList
        if (!cardViewList.contains(holder.cardTimeSlot)) {
            cardViewList.add(holder.cardTimeSlot)

            //check if card time slot is available
            holder.setiRecyclerItemSelectedListener(object : IRecyclerItemSelectedListener {
                override fun onItemSelectedListener(view: View, pos: Int) {
                    // Loop all card in card list
                    for (cardView: CardView in cardViewList) {
                        if (cardView.tag == null) //only available card time slot can be changed
                            cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))

                    }
                    // The selected card now can be changed
                    holder.cardTimeSlot.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))


                    // After that, send broadcast to enable button NEXT
                    val intent = Intent(Common.KEY_ENABLE_BUTTON_NEXT)
                    intent.putExtra(Common.KEY_TIME_SLOT, pos) // set index on selected time slot
                    intent.putExtra(Common.KEY_STEP, 3) // go to step 3
                    localBroadcastManager.sendBroadcast(intent)
                }

            })
        }
    }

    override fun getItemCount(): Int {
        return Common.TIME_SLOT_TOTAL
    }
}