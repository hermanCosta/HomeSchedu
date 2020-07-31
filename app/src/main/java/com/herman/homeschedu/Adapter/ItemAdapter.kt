package com.herman.homeschedu.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Interface.IRecyclerItemSelectedListener
import com.herman.homeschedu.Model.Item
import com.herman.homeschedu.R
import kotlinx.android.synthetic.main.layout_item.view.*

class ItemAdapter constructor() : RecyclerView.Adapter<ItemAdapter.MyViewHolder>() {

    lateinit var context: Context
    lateinit var itemList: List<Item>
    lateinit var cardViewList: ArrayList<CardView>
    lateinit var loadBroadcastManager: LocalBroadcastManager

    constructor(context: Context, itemList: List<Item>) : this() {
        this.context = context
        this.itemList = itemList
        this.cardViewList = ArrayList()
        this.loadBroadcastManager = LocalBroadcastManager.getInstance(context)
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val itemName: TextView = itemView.tv_item_name
        val cardItem: CardView = itemView.item_card_item



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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.layout_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemName.text = itemList[position].name
        //holder.ratingBar.rating  = barberList[position].rating!!.toFloat()

        if (!cardViewList.contains(holder.cardItem))
            cardViewList.add(holder.cardItem)

//        if (holder.itemName.text == "Bathroom #1")
//             holder.itemName.tv_item_name.background = Drawable.createFromPath(R.drawable.ic_bathtub.toString())

        holder.setiRecyclerItemSelectedListener(object : IRecyclerItemSelectedListener {
            override fun onItemSelectedListener(view: View, pos: Int) {
                // Set background for all not selected item
                for (cardView: CardView in cardViewList) {
                    cardView.setCardBackgroundColor(context.resources.getColor(android.R.color.white))
                }

                //Set background for selected item
                holder.cardItem.setCardBackgroundColor(context.resources.getColor(android.R.color.holo_orange_dark))

                val intent = Intent(Common.KEY_ENABLE_BUTTON_NEXT)
                intent.putExtra(Common.KEY_ITEM_SELECTED, itemList[pos])
                intent.putExtra(Common.KEY_STEP,2)
                loadBroadcastManager.sendBroadcast(intent)
            }
        })

    }
}


