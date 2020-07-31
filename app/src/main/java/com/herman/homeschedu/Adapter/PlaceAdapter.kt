package com.herman.homeschedu.Adapter

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
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Interface.IRecyclerItemSelectedListener
import com.herman.homeschedu.Model.Place
import com.herman.homeschedu.R
import kotlinx.android.synthetic.main.layout_place.view.*

class PlaceAdapter constructor(): RecyclerView.Adapter<PlaceAdapter.MyViewHolder>(),
    View.OnClickListener, View.OnContextClickListener {

    lateinit var context: Context
    private lateinit var placeList: List<Place>
    private lateinit var cardViewList: ArrayList<CardView>
    lateinit var localBroadcastManager: LocalBroadcastManager

    constructor(context: Context, placeList: List<Place>) : this() {
        this.context = context
        this.placeList = placeList
        this.cardViewList = ArrayList()
        localBroadcastManager = LocalBroadcastManager.getInstance(context)

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var placeName: TextView = itemView.tv_place_name
        var placeLocal: TextView = itemView.tv_place_local
        var placeInformation: TextView = itemView.tv_place_information
        var cardViewPlace: CardView = itemView.card_view_place

        var iRecyclerItemSelectedListener: IRecyclerItemSelectedListener? = null
        fun setiRecyclerItemSelectedListener(iRecyclerItemSelectedListener: IRecyclerItemSelectedListener?) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            iRecyclerItemSelectedListener!!.onItemSelectedListener(v!!,absoluteAdapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.layout_place, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.placeName.text = placeList[position].name
        holder.placeLocal.text = placeList[position].local
        holder.placeInformation.text = placeList[position].information

        if (!cardViewList.contains(holder.cardViewPlace))
            cardViewList.add(holder.cardViewPlace)


        holder.setiRecyclerItemSelectedListener( object : IRecyclerItemSelectedListener {
            override fun onItemSelectedListener(view: View, pos: Int) {

                // Set white background for all cards not selected
                for (cardView in cardViewList) cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, android.R.color.white))

                // set selected BG in selected item only
                holder.cardViewPlace.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))


                // send broadcast telling BookingActivity to enable button Next
                val intent = Intent(Common.KEY_ENABLE_BUTTON_NEXT)
                intent.putExtra(Common.KEY_PLACE_STORE, placeList[pos])
                intent.putExtra(Common.KEY_STEP,1)
                localBroadcastManager.sendBroadcast(intent)
            }
        })
    }

    override fun getItemCount(): Int { return placeList.size }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onContextClick(v: View?): Boolean {
        TODO("Not yet implemented")
    }

}



