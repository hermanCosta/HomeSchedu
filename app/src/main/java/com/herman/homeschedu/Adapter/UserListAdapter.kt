package com.herman.homeschedu.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.herman.homeschedu.Model.Item
import com.herman.homeschedu.R
import kotlinx.android.synthetic.main.layout_housemate.view.*


//class UserListAdapter(options: FirestoreRecyclerOptions<Housemate>) :
//    FirebaseRecyclerAdapter<Barber, UserListAdapter.MyViewHolder>(options) {
class UserListAdapter() : RecyclerView.Adapter<UserListAdapter.MyViewHolder>() {

    private lateinit var userList: List<Item>


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.tv_housemate_list_first_name
        val userName = itemView.tv_housemate_list_last_name
        val password = itemView.tv_housemate_list_email
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_housemate,
            parent, false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
       userList = ArrayList()
        return userList.size }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = userList[position].name
    }

//    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Barber) {
//        holder.name.text = model.name
//        holder.userName.text = model.userName
//        holder.password.text = model.password
//    }


}