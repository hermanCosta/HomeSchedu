package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.herman.homeschedu.Adapter.HousemateListAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Model.Housemate
import com.herman.homeschedu.Model.User
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_housemate_list.*
import java.time.format.DateTimeFormatter

class HousemateListActivity : AppCompatActivity() {


    lateinit var mAuth: FirebaseAuth
    lateinit var recyclerList: ArrayList<User>
    lateinit var houseRef: FirebaseFirestore
    lateinit var userRef: DocumentReference
    lateinit var adapter: HousemateListAdapter
    lateinit var dialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_housemate_list)
        supportActionBar?.title = "HOUSEMATES"

        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()
        Log.d("HomeScreenActivity", "date Format: $dateFormat")

        centerTitle()

        recyclerList = ArrayList()
        houseRef = FirebaseFirestore.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        setRecycleView()
        setHouseId()
        tv_house_id.setOnClickListener { copyHouseId() }
        housemate_list_back_home.setOnClickListener { finish() }

    }

    private fun setHouseId() {
        //get current user houseID
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = Common.fStore
            .collection("users")
            .document(uid)

        userRef.addSnapshotListener { documentSnapshot, _ ->
            val houseId = documentSnapshot?.getString("houseId")
            tv_house_id.text = houseId
        }
    }

    private fun copyHouseId() {
        val houseId = tv_house_id.text.toString()
        val clipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied String", houseId)
        clipboardManager.setPrimaryClip(clip)
        true // Or false if not consumed

        Toast.makeText(baseContext, "house ID copied", Toast.LENGTH_SHORT).show()
    }

    private fun setRecycleView() {
        dialog.show()
        //get user Id from
        val uid = FirebaseAuth.getInstance().uid ?: ""


        userRef = FirebaseFirestore.getInstance()
            .collection("/users")
            .document(uid)
        Log.d("HousemateListActivity", "user ID: $uid")


        //get ID of the house
        userRef.addSnapshotListener { userDocument, _ ->
            val houseId = userDocument!!.getString("houseId")

            val houseRef = FirebaseFirestore.getInstance()
                .collection("houses")
                .document(houseId!!)
                .collection("Housemate")

            Log.d("HousemateListActivity", "House ID: $uid")


            val query = houseRef.orderBy("firstName", Query.Direction.ASCENDING)

            val options = FirestoreRecyclerOptions.Builder<Housemate>()
                .setQuery(query, Housemate::class.java)
                .build()


            adapter = HousemateListAdapter(options)

            housemate_list_recycler_view.layoutManager = LinearLayoutManager(this)
            housemate_list_recycler_view.setHasFixedSize(true)
            housemate_list_recycler_view.adapter = adapter

            adapter.startListening()
            dialog.dismiss()


        }

    }


    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun centerTitle() {
        val textViews = java.util.ArrayList<View>()
        window.decorView.findViewsWithText(textViews, title, View.FIND_VIEWS_WITH_TEXT)
        if (textViews.size > 0) {
            var appCompatTextView: AppCompatTextView? = null
            if (textViews.size == 1)
                appCompatTextView = textViews[0] as AppCompatTextView
            else {
                for (v in textViews) {
                    if (v.parent is Toolbar) {
                        appCompatTextView = v as AppCompatTextView
                        break
                    }
                }
            }
            if (appCompatTextView != null) {
                val params = appCompatTextView.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                appCompatTextView.layoutParams = params
                appCompatTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        }
    }
}






