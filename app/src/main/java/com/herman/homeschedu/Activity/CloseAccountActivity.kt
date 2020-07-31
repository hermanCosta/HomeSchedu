package com.herman.homeschedu.Activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.Common.Companion.fStore
import com.herman.homeschedu.R
import java.time.format.DateTimeFormatter

class CloseAccountActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_account)

        mAuth = FirebaseAuth.getInstance()
        deleteUserFromHousemateList()
        deleteFromUserCollection()
        deleteScheduleFromHouse()
        deleteUserFromApp()


    }

    private fun deleteUserFromHousemateList() {

        if (Common.HOUSE_TO_DELETE == null) {
            deleteUserFromApp()

        } else {
            val houseRef = fStore
                .collection("/houses")
                .document(Common.HOUSE_TO_DELETE!!)
                .collection("/Housemate")
                .document(uid)

            houseRef.delete().addOnSuccessListener {
                Log.d("CloseAccountActivity", "User deleted from Housemate Collection")

                deleteScheduleFromItem()

            }
                .addOnFailureListener {
                    Log.d("CloseAccountActivity", it.message!!)
                }
        }
    }

    private fun deleteScheduleFromHouse() {
        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()

        fStore
            .collection("/houses")
            .document(Common.HOUSE_TO_DELETE!!)
            .collection("/Booking")
            .document("AllBooking")
            .collection(dateFormat)

            .whereEqualTo("userId", uid)
            .get().addOnCompleteListener {
                for (querySnapshot in it.result!!) {
                    val scheduleId = querySnapshot.id

                    fStore
                        .collection("/houses")
                        .document(Common.HOUSE_TO_DELETE!!)
                        .collection("/Booking")
                        .document("AllBooking")
                        .collection(dateFormat)
                        .document(scheduleId)
                        .delete().addOnSuccessListener() {
                            Log.d(
                                "CloseAccountActivity",
                                "House deleted from houses Collection"
                            )

//                            deleteFromUserCollection()

                        }
                }
            }
    }

    private fun deleteScheduleFromItem() {
        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()

        val houseBooking = fStore
            .collection("/houses")
            .document(Common.HOUSE_TO_DELETE!!)
            .collection("/Booking")
            .document("AllBooking")
            .collection(dateFormat)
            .whereEqualTo("userId", uid)

        houseBooking.get().addOnCompleteListener {
            for (querySnapshot in it.result!!) {

                val resource = querySnapshot?.get("resource").toString()
                val placeId = querySnapshot?.get("placeId").toString()
                val itemId = querySnapshot?.get("itemId").toString()
                val slot = querySnapshot?.get("slot").toString()

                // Delete From The Item Collection First
                val itemRef = fStore
                    .collection("/houses")
                    .document(Common.HOUSE_TO_DELETE!!)
                    .collection("/Resource")
                    .document(resource)
                    .collection("/Place")
                    .document(placeId)
                    .collection("/Item")
                    .document(itemId)
                    .collection(dateFormat)
                    .document(slot)

                itemRef.delete().addOnSuccessListener {
                    Log.d("CloseAccountActivity", "Item Deleted ")

                    //deleteScheduleFromHouse()
                }
            }
        }
    }


    private fun deleteFromUserCollection() {

        fStore
            .collection("/users")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("CloseAccountActivity", "User deleted from Housemate Collection")

               // deleteUserFromApp()
            }
            .addOnFailureListener {
                Log.d("CloseAccountActivity", it.message!!)
            }
    }

    private fun deleteUserFromApp() {
        mAuth.currentUser!!.delete().addOnSuccessListener {
            Log.d("CloseAccountActivity", "User Deleted From Firebase")
            mAuth.signOut()
        }
            .addOnFailureListener {
                Log.d("CloseAccountActivity", it.message!!)
            }
    }
}