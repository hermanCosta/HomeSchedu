package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Model.User
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var user: User
    private lateinit var userRef: FirebaseFirestore
    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        userRef = Common.fStore
        mAuth = FirebaseAuth.getInstance()

        btn_sign_in.setOnClickListener { authenticate() }


        //Intent to Register a new user
        tv_sign_up.setOnClickListener {
            val intent = Intent(
                this, CreateUserActivity::class.java
            )
            startActivity(intent)
            dialog.show()
        }

        //Intent to reset user password
        tv_forgot_password.setOnClickListener() {
            val intent = Intent(
                this,
                ResetPasswordActivity::class.java
            )
            startActivity(intent)
            dialog.show()
        }
    }


    public override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            finish()
            openHomeActivity()
        }
    }


    private fun authenticate() {
        dialog.show()
        val email = et_email.text.toString()
        val password = et_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            dialog.dismiss()

            Log.i("Authentication", "createUserWithEmail:empty email or password")

            Toast.makeText(
                baseContext, "Please enter your email/password.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        user = User("", "", "", email, password, "")

        mAuth.signInWithEmailAndPassword(user.email!!, user.password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = mAuth.currentUser
                    val uid = mAuth.currentUser!!.uid
                    userRef.collection("/users")
                        .document(uid)
                        .addSnapshotListener { userDoc: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                            val houseId = userDoc?.getString("houseId").isNullOrEmpty()

                            //currentUser!!.reload()
                            if (currentUser!!.isEmailVerified && !houseId) {
                                // Sign in success, update UI with the signed xs-in user's information
                                finish()
                                Toast.makeText(
                                    baseContext,
                                    getString(R.string.signInSuccessfully), Toast.LENGTH_SHORT
                                ).show()
                                openHomeActivity()
                                dialog.dismiss()
                                Log.i("Authentication", "Sign in successfully")

                            } else if (!houseId && !currentUser.isEmailVerified) {
                                btn_resend_verification_code.visibility = View.VISIBLE
                                Toast.makeText(
                                    baseContext,
                                    "Please check your email box to verify your account",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()

                                btn_resend_verification_code.setOnClickListener {
                                    currentUser.sendEmailVerification().addOnSuccessListener {
                                        Toast.makeText(
                                            baseContext,
                                            "Verification code resent, please check your email box",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.i("Authentication", "Verification email resent")
                                        btn_resend_verification_code.visibility = View.GONE
                                        dialog.dismiss()
                                    }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                baseContext,
                                                it.message,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            btn_resend_verification_code.visibility = View.GONE
                                            dialog.dismiss()
                                        }
                                }

                            } else if (houseId && !currentUser.isEmailVerified) {
                                finish()
                                val intent = Intent(this, CreateHouseActivity::class.java)
                                startActivity(intent)
                                dialog.dismiss()
                            } else if (currentUser.isEmailVerified && houseId) {
                                finish()
                                val intent = Intent(this, CloseAccountActivity::class.java)
                                startActivity(intent)
                                dialog.dismiss()
                            }

                        }
                }

            }.addOnFailureListener {
                Log.i("Authentication", it.message!!)
                Toast.makeText(
                    baseContext, it.message,
                    Toast.LENGTH_LONG
                ).show()
                cleanFields()
                dialog.dismiss()
            }
    }

    private fun cleanFields() {
        et_email.text.clear()
        et_password.text.clear()
    }

    private fun openHomeActivity() {
        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
    }
}

