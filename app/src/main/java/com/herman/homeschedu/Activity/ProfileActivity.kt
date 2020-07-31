package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.herman.homeschedu.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    lateinit var userRef: DocumentReference
    lateinit var houseRef: DocumentReference
    lateinit var dialog: AlertDialog

    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        iv_profile_edit.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java)
            startActivity(intent)
        }

        profile_back_home.setOnClickListener { finish() }

        mAuth = FirebaseAuth.getInstance()

        userRef = FirebaseFirestore.getInstance().collection("/users")
            .document(uid)

        userRef.addSnapshotListener { documentSnapshot, _ ->
            tv_profile_first_name.text = documentSnapshot?.getString("firstName")
            tv_profile_last_name.text = documentSnapshot?.getString("lastName")
            tv_profile_email.text = documentSnapshot?.getString("email")

            Picasso.get().load(documentSnapshot?.getString("profileImageUrl"))
                .into(circle_profile_photo)
            iv_profile_photo.alpha = 0f
        }

        userRef.addSnapshotListener { userDocument, _ ->
            val houseId = userDocument?.getString("houseId").toString()

            houseRef = FirebaseFirestore.getInstance().collection("/houses").document(houseId)
            houseRef.addSnapshotListener { houseDocument, _ ->
                tv_profile_number.text = houseDocument?.getString("houseNumber")
                tv_profile_street.text = houseDocument?.getString("street")
                tv_profile_zipCode.text = houseDocument?.getString("zipCode")
                tv_profile_eirCode.text = houseDocument?.getString("eirCode")

            }
        }

    }
}
