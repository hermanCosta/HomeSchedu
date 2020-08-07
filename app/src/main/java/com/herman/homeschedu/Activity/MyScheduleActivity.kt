package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.herman.homeschedu.Adapter.MyScheduleAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Model.ScheduleInformation
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_my_schedule.*
import java.time.format.DateTimeFormatter


class MyScheduleActivity : AppCompatActivity() {

    lateinit var adapter: MyScheduleAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var dialog: AlertDialog
    private lateinit var houseId: String

    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_schedule)
        val typeface: Typeface = Typeface.SANS_SERIF

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        setRecyclerView()

        fab_add_my_schedule.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }

        my_schedule_back_home.setOnClickListener { finish() }
    }


    private fun setRecyclerView() {
        dialog.show()

        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()

        val userRef = fStore
            .collection("/users")
            .document(uid)

        userRef.addSnapshotListener { userDoc: DocumentSnapshot?, _: FirebaseFirestoreException? ->
            houseId = userDoc?.getString("houseId").toString()

            val houseRef = fStore
                .collection("/houses")
                .document(houseId)
                .collection("/Booking")
                .document("AllBooking")
                .collection(dateFormat)
                .whereEqualTo("userId", uid)

            houseRef.get().addOnCompleteListener {
                val query = houseRef.orderBy("timestamp", Query.Direction.ASCENDING)

                val options = FirestoreRecyclerOptions.Builder<ScheduleInformation>()
                    .setQuery(query, ScheduleInformation::class.java)
                    .build()

                adapter = MyScheduleAdapter(options)

                my_schedule_recycler_view.layoutManager = LinearLayoutManager(this)
                my_schedule_recycler_view.setHasFixedSize(true)
                my_schedule_recycler_view.adapter = adapter

                adapter.startListening()
                dialog.dismiss()

                ItemTouchHelper(object : SwipeToDeleteCallBack(this) {

                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int
                    ) {

                        val alertDialogBuilder = AlertDialog.Builder(viewHolder.itemView.context)
                        alertDialogBuilder.setIcon(R.drawable.ic_logo_icon)
                        alertDialogBuilder.setTitle("Delete schedule")
                        alertDialogBuilder.setMessage("Are you sure ?")
                        alertDialogBuilder.setCancelable(false)

                        alertDialogBuilder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                            deleteScheduleOfItem(adapter, viewHolder)

                        }

                        alertDialogBuilder.setNegativeButton("No") { _: DialogInterface, _: Int ->
                            adapter.notifyItemChanged(viewHolder.absoluteAdapterPosition);
                        }

                        alertDialogBuilder.show()
                    }

                }).attachToRecyclerView(my_schedule_recycler_view)
            }

                .addOnFailureListener {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                }
        }

    }

    override fun onResume() {
        super.onResume()
        if (dialog.isShowing)
            dialog.dismiss()
        setRecyclerView()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    abstract class SwipeToDeleteCallBack(context: Context) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
        private val intrinsicWidth = deleteIcon!!.intrinsicWidth
        private val intrinsicHeight = deleteIcon!!.intrinsicHeight
        private val background = ColorDrawable()
        private val backgroundColor = Color.parseColor("#F44336")
        private val clearPaint =
            Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                clearCanvas(
                    c,
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                return
            }

            // Draw the red delete background
            background.color = backgroundColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            // Calculate position of delete icon
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            // Draw the delete icon
            deleteIcon!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
            c?.drawRect(left, top, right, bottom, clearPaint)
        }
    }


    private fun deleteScheduleOfItem(
        adapter: MyScheduleAdapter,
        viewHolder: RecyclerView.ViewHolder
    ) {

        dialog.show()

        val scheduleId = adapter.snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id
        Log.d("MyScheduleActivity", "schedule ID: $scheduleId")

        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()

        val mHouseRef = fStore
            .collection("/houses")
            .document(houseId)
            .collection("/Booking")
            .document("AllBooking")
            .collection(dateFormat)
            .document(scheduleId)

        mHouseRef.get().addOnSuccessListener { scheduleDoc ->
            val resource = scheduleDoc?.get("resource").toString()
            val placeId = scheduleDoc?.get("placeId").toString()
            val itemId = scheduleDoc?.get("itemId").toString()
            val slot = scheduleDoc?.get("slot").toString()
            val calendarUri = scheduleDoc?.get("calendarUri").toString()


            // Delete From The Item Collection First
            val mMHouseRef = fStore
                .collection("/houses")
                .document(houseId)
                .collection("/Resource")
                .document(resource)
                .collection("/Place")
                .document(placeId)
                .collection("/Item")
                .document(itemId)
                .collection(dateFormat)
                .document(slot)

            mMHouseRef.delete()
                .addOnCompleteListener { itemSchedule ->
                    if (itemSchedule.isSuccessful) {
                        Log.d(
                            "My ScheduleAdapter",
                            "Schedule deleted from Item Collection"
                        )

                        // If Deleted from Item Collection, Delete From the House Collection
                        Common.fStore
                            .collection("/houses")
                            .document(houseId)
                            .collection("/Booking")
                            .document("AllBooking")
                            .collection(dateFormat)
                            .document(scheduleId)

                            .delete()
                            .addOnCompleteListener { houseSchedule ->
                                if (houseSchedule.isSuccessful) {
                                    adapter.deleteSchedule(viewHolder.absoluteAdapterPosition)
                                    Log.d(
                                        "MyScheduleActivity",
                                        "Schedule deleted from the House"
                                    )
                                    if (calendarUri.isEmpty()) {
                                        // finish()
                                        this.setRecyclerView()

                                    } else {
                                        val eventUri = Uri.parse(calendarUri)
                                        this.contentResolver.delete(
                                            eventUri,
                                            null,
                                            null
                                        )
                                        //finish()
                                        this.setRecyclerView()
                                    }


                                    Toast.makeText(
                                        baseContext,
                                        "Schedule Deleted Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                    dialog.dismiss()
                                    Toast.makeText(
                                        baseContext,
                                        "Schedule Deleted Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d(
                                        "MyScheduleActivity",
                                        "Schedule deleted from recyclerView"
                                    )
                                }
                            }
                    }
                }
        }
    }
}

