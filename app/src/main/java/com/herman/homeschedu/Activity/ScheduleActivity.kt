package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.herman.homeschedu.Adapter.ViewPageAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Model.Item
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_schedule.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var dialog: AlertDialog
    private lateinit var itemRef: CollectionReference
    private lateinit var houseId: String

    val uid = FirebaseAuth.getInstance().uid ?: ""


    //Broadcast Receiver
    private val buttonNextReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val step = intent.getIntExtra(Common.KEY_STEP, 0)
            if (step == 1)
                Common.currentPlace = intent.getParcelableExtra(Common.KEY_PLACE_STORE)
            else if (step == 2)
                Common.currentItem = intent.getParcelableExtra(Common.KEY_ITEM_SELECTED)
            else if (step == 3)
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT, -1)
            btn_next_step.isEnabled = true
            setColorButton()
        }
    }

    override fun onDestroy() {
        localBroadcastManager.unregisterReceiver(buttonNextReceiver)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        supportActionBar?.hide()


        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(
            buttonNextReceiver,
            IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT)
        )

        //Event
        btn_next_step.setOnClickListener { nextClick() }
        btn_previous_step.setOnClickListener { previousClick() }
        btn_back_home.setOnClickListener {
            finish()
        }


        setupStepView()
        setColorButton()

        //View
        view_pager.adapter = ViewPageAdapter(supportFragmentManager)
        view_pager.offscreenPageLimit =
            4 // We have four fragment, so we need keep state of these 4 screen pages
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageSelected(position: Int) {
                step_view.go(position, true)

                if (position != 0) {
                    btn_previous_step.isEnabled = true
                    btn_back_home.visibility = View.GONE
                    btn_previous_step.visibility = View.VISIBLE

                    //Set disable button next
                    btn_next_step.isEnabled = false
                    setColorButton()
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }
        })
    } // END onCreate

    //Event
    private fun previousClick() {

        if (Common.step > 0) {
            Common.step--
            view_pager.currentItem = Common.step

            if (Common.step < 3) {
                btn_next_step.isEnabled = true
                setColorButton()

                if (Common.step == 0) {
                    btn_previous_step.isEnabled = false
                    btn_previous_step.visibility = View.GONE
                    btn_back_home.visibility = View.VISIBLE
                }
            }

        } else {

            btn_previous_step.visibility = View.GONE
            btn_back_home.visibility = View.VISIBLE
        }
    }

    private fun nextClick() {
        if (Common.step < 3) {
            Common.step++

            if (Common.step == 1) { // After choose Place
                if (Common.currentPlace != null)
                    loadItemByPlace(Common.currentPlace!!.placeId)
            } else if (Common.step == 2) { // pick time slot
                if (Common.currentItem != null)
                    loadTimeSlotOfItem()
            } else if (Common.step == 3) { // confirm
                if (Common.currentTimeSlot != -1)
                    confirmSchedule()
            }
            view_pager.currentItem = Common.step
        }
    }

    private fun confirmSchedule() {
        // Send broadcast to fragment step 4
        val intent = Intent(Common.KEY_CONFIRM_SCHEDULE)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun loadTimeSlotOfItem() {
        // Send local Broadcast to fragment step 3
        val intent = Intent(Common.KEY_DISPLAY_TIME_SLOT)
        localBroadcastManager.sendBroadcast(intent)

    }


    private fun loadItemByPlace(placeId: String?) {
        dialog.show()

        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        userRef.addSnapshotListener { userDocument, _ ->
            houseId = userDocument?.getString("houseId").toString()

            if (!TextUtils.isEmpty(Common.place)) {

                itemRef = FirebaseFirestore.getInstance()
                    .collection("/houses")
                    .document(houseId)
                    .collection("/Resource")
                    .document(Common.place)
                    .collection("/Place")
                    .document(placeId!!)
                    .collection("/Item")

                itemRef.get().addOnCompleteListener {

                    val list: ArrayList<Item> = ArrayList()

                    for (itemSnapshot in it.result!!) {

                        val item = itemSnapshot.toObject(Item::class.java)
                        item.itemId = itemSnapshot.id //get ID of item
                        list.add(item)
                    }

                    // send broadcast to ScheduleStep2Fragment to load recycler
                    val intent = Intent(Common.KEY_ITEM_LOAD_DONE)
                    intent.putParcelableArrayListExtra(
                        Common.KEY_ITEM_LOAD_DONE,
                        list
                    )
                    localBroadcastManager.sendBroadcast(intent)
                    dialog.dismiss()

                }
                    .addOnFailureListener {
                        dialog.dismiss()
                    }
            }
        }
    }


    private fun setupStepView() {
        val stepList = ArrayList<String>()
        stepList.add("Resource")
        stepList.add("Place")
        stepList.add("Time")
        stepList.add("Confirm")
        step_view.setSteps(stepList)
    }

    private fun setColorButton() {
        if (btn_next_step.isEnabled) {
            btn_next_step.setBackgroundResource(R.color.colorPrimary)

        } else {
            btn_next_step.setBackgroundResource(R.color.disableColor)
        }

        if (btn_previous_step.isEnabled) {
            btn_back_home.visibility = View.GONE
            btn_previous_step.setBackgroundResource(R.color.colorPrimary)

        } else {
            //btn_previous_step.setBackgroundResource(R.color.disableColor)
            btn_previous_step.visibility = View.GONE
            btn_back_home.visibility = View.VISIBLE
        }
    }
}


