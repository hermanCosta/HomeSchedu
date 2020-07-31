package com.herman.homeschedu.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.herman.homeschedu.Model.Housemate
import com.herman.homeschedu.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_housemate.view.*

class HousemateListAdapter(options: FirestoreRecyclerOptions<Housemate>) : FirestoreRecyclerAdapter<Housemate,
        HousemateListAdapter.HousemateListViewHolder>(options){

    class HousemateListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val housemateFirstName: TextView = itemView.tv_housemate_list_first_name
        val housemateLastName: TextView = itemView.tv_housemate_list_last_name
        val housemateEmail: TextView = itemView.tv_housemate_list_email
        val housemateImageProfile: CircleImageView = itemView.circle_housemate_image_view
        val housemateImageView: ImageView = itemView.iv_housemate_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HousemateListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_housemate,
            parent, false)
        return HousemateListViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: HousemateListViewHolder,
        position: Int,
        model: Housemate
    ) {
        holder.housemateFirstName.text = model.firstName
        holder.housemateLastName.text = model.lastName
        holder.housemateEmail.text = model.email

        Picasso.get().load(model.profileImageUrl).into(holder.housemateImageProfile)
        holder.housemateImageView.alpha = 0f

    }

}




