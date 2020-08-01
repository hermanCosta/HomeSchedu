package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.Common.Companion.fStore
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import java.time.format.DateTimeFormatter

class CloseAccountActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_account)

        mAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        mAuth.signOut()

    }

    private fun deleteUserFromHousemateList() {

        if (Common.HOUSE_TO_DELETE == null || mAuth.currentUser != null) {
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

                            deleteFromUserCollection()

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

                itemRef.delete().addOnCompleteListener() { task ->
                    if (task.isSuccessful) {

                    }
                    Log.d("CloseAccountActivity", "Item Deleted ")

                    deleteScheduleFromHouse()
                }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

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

               deleteUserFromApp()
            }
            .addOnFailureListener {
                Log.d("CloseAccountActivity", it.message!!)
            }
    }

    private fun deleteUserFromApp() {
        mAuth.currentUser!!.delete().addOnCompleteListener() {
            Log.d("CloseAccountActivity", "User Deleted From Firebase")

            val intent = Intent(this, CloseAccountActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

        }
            .addOnFailureListener {
                Log.d("CloseAccountActivity", it.message!!)
            }
    }
}