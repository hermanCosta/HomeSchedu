package com.herman.homeschedu.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.herman.homeschedu.Model.ScheduleInformation
import com.herman.homeschedu.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_schedule_list.view.*

class ScheduleAdapter(options: FirestoreRecyclerOptions<ScheduleInformation>) :
    FirestoreRecyclerAdapter<ScheduleInformation, ScheduleAdapter.ScheduleListViewHolder>(options) {

    class ScheduleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userFirstName: TextView = itemView.tv_schedule_list_first_name
        val userLastName: TextView = itemView.tv_schedule_list_last_name
        val itemName: TextView = itemView.tv_schedule_list_item
        val scheduleTime: TextView = itemView.tv_schedule_list_time
        val placeAddress: TextView = itemView.tv_schedule_list_place_address
        val placeInformation: TextView = itemView.tv_schedule_list_place_information
        val imageProfile: CircleImageView = itemView.schedule_list_circle_image_view
        val scheduleImageView: ImageView = itemView.iv_schedule_list_user_photo

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_schedule_list,
            parent,false)

        return ScheduleListViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ScheduleListViewHolder,
        position: Int,
        model: ScheduleInformation
    ) {
        holder.userFirstName.text = model.firstName
        holder.userLastName.text = model.lastName
        holder.itemName.text = model.itemName
        holder.scheduleTime.text = model.time
        holder.placeAddress.text = model.placeAddress
        holder.placeInformation.text = model.placeInformation
        Picasso.get().load(model.profileImageUrl).into(holder.imageProfile)
        holder.scheduleImageView.alpha = 0f
    }
}