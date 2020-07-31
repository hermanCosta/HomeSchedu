package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_enter_existing_house.*

class EnterExistingHouse : AppCompatActivity() {
    lateinit var houseRef: DocumentReference
    lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    lateinit var houseId: String

    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_existing_house)


        mAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        btn_enter_existing_house.setOnClickListener { putUserToExistingHouse() }
        tv_create_a_new_house.setOnClickListener { openCreateHouseActivity() }
    }

    private fun putUserToExistingHouse() {
        dialog.show()
        houseId = et_enter_existing_house.text.toString()

        if (houseId.isEmpty()) {
            dialog.dismiss()
            Toast.makeText(baseContext, "Please enter house ID", Toast.LENGTH_SHORT).show()
            return
        }

        houseRef = FirebaseFirestore.getInstance()
            .collection("/houses")
            .document(houseId)
        houseRef.get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val houseDoc = it.result
                    if (houseDoc!!.exists()) {

                        Common.HOUSE_ID = houseId

                        // Update houseId field of the user
                        FirebaseFirestore.getInstance()
                            .collection("/users")
                            .document(uid)
                            .update("houseId", houseId)

                        saveUserToHouse()
                        sendEmailVerification()
                        openMainActivity()
                        dialog.dismiss()

                    } else {
                        Toast.makeText(
                            baseContext,
                            "House not found, Please check house ID ",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }

            }.addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }

    //save user to the current house
    private fun saveUserToHouse() {
        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        userRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val userDocument = it.result
                if (userDocument!!.exists()) {
                    val userData = userDocument.data

                    val houseRef = FirebaseFirestore.getInstance()
                        .collection("/houses")
                        .document(houseId)
                        .collection("/Housemate")
                        .document(uid)

                    Log.d("CreateHouseActivity", "houseID: $houseId")

                    houseRef.set(userData!!).addOnSuccessListener {
                        Log.d("EnterExistingHouse", "User saved to the house")
                    }
                }
            }
        }.addOnFailureListener {
            Log.d("EnterExistingHouse", it.message!!)
        }
    }

    private fun sendEmailVerification() {
        val user = mAuth.currentUser

        if (user!!.isEmailVerified) {
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()

        } else {

            user.sendEmailVerification().addOnSuccessListener {
                Toast.makeText(
                    baseContext,
                    "Account registered, a verification link has been sent to your email",
                    Toast.LENGTH_LONG
                ).show()
                Log.i("Authentication", "Verification email sent")
            }
                .addOnFailureListener {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openCreateHouseActivity() {
        val intent = Intent(this, CreateHouseActivity::class.java)
        startActivity(intent)
    }

    private fun openMainActivity() {
        mAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
