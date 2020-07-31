package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.herman.homeschedu.Model.User
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var user: User
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.colorPrimary
        )

        mAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        btn_reset_submit.setOnClickListener { resetPassword() }
        tv_resend_email.setOnClickListener { resetPassword() }

        tv_back_to_login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            dialog.show()
        }
    }


    private fun resetPassword() {


        val email = et_reset_email.text.toString()

        if (email.isEmpty()) {
            Log.i("Authentication", "Please enter your email")
            Toast.makeText(
                baseContext, getString(R.string.enterYourEmail),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        user = User("", "", "", email, "", "")

        mAuth.sendPasswordResetEmail(user.email!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext, "Please check your email inbox", Toast.LENGTH_LONG
                    ).show()
                    Log.i("Authentication", "Please check your email inbox")

                } else {
                    // If sending email request fails, display a message to the user.
                    val emailFailed: String = try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Log.i("Authentication", "This account has never been created")
                        getString(R.string.thisAccountDoesNotExist)
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Log.i("Authentication", "Please enter a valid email")
                        getString(R.string.invalidEmail)
                    } catch (e: Exception) {
                        getString(R.string.unknownError)
                    }
                    Toast.makeText(
                        baseContext, emailFailed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
