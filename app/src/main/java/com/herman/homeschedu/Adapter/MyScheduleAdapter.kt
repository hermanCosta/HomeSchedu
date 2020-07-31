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
import kotlinx.android.synthetic.main.layout_my_schedule.view.*


class MyScheduleAdapter (options: FirestoreRecyclerOptions<ScheduleInformation>) :
FirestoreRecyclerAdapter<ScheduleInformation, MyScheduleAdapter.MyScheduleViewHolder>(options) {


    class MyScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val userFirstName: TextView = itemView.tv_my_schedule_first_name
        val userLastName: TextView = itemView.tv_my_schedule_last_name
        val itemName: TextView = itemView.tv_my_schedule_item_name
        val scheduleTime: TextView = itemView.tv_my_schedule_time
        val placeAddress: TextView = itemView.tv_my_schedule_place_address
        val placeInformation: TextView = itemView.tv_my_schedule_place_information
        val imageProfile: CircleImageView = itemView.my_schedule_circle_image_view
        val scheduleImageView: ImageView = itemView.iv_my_schedule_user_photo
    }

    fun deleteSchedule(position: Int) {
        snapshots.getSnapshot(position).reference.delete()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyScheduleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_my_schedule, parent, false)

        return MyScheduleViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MyScheduleViewHolder,
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