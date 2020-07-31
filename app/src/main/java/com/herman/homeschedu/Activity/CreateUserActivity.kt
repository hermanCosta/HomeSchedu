package com.herman.homeschedu.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Model.User
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var user: User
    lateinit var dialog: AlertDialog
    lateinit var mAuth: FirebaseAuth
    lateinit var userRef: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseFirestore.getInstance()

        btn_create_account.setOnClickListener { createAccount() }
        btn_sign_up_photo.setOnClickListener { selectPicture() }

        tv_login_in.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            dialog.show()
        }
    }


    private fun createAccount() {
        dialog.show()


        val firstName = et_first_name.text.toString()
        val lastName = et_last_name.text.toString()
        val signUpEmail = et_sign_up_email.text.toString()
        val signUpPassword = et_sign_up_password.text.toString()
        val confirmPassword = et_confirm_password.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || signUpEmail.isEmpty() ||
            signUpPassword.isEmpty() || confirmPassword.isEmpty()
        ) {

            dialog.dismiss()
            Toast.makeText(
                baseContext, getString(R.string.enterAllFields),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        user = User(
            "",
            firstName,
            lastName,
            signUpEmail,
            signUpPassword,
            Common.DEFAULT_IMAGE_URL
        )

        Log.d("CreateUserActivity", "User saved to firestore ")

        mAuth.createUserWithEmailAndPassword(user.email!!, user.password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    user.password = ""
                    Common.UID = FirebaseAuth.getInstance().uid ?: ""
                    Common.HOUSE_ID = user.houseId

                    // Save user to the database
                    userRef.collection("/users")
                        .document(Common.UID)
                        .set(user).addOnSuccessListener {
                            finish()
                            uploadImageToFirebase()
                            openCreateHouseActivity()

                            // Sign in success, update UI with the signed-in user's information
                            // Toast.makeText(baseContext, getString(R.string.userCreatedSuccessfully), Toast.LENGTH_SHORT).show()
                            Log.d("CreateUserActivity", "User created successfully")
                            dialog.dismiss()
                        }


                }
            }
            .addOnFailureListener {
                Log.i("CreateUserActivity", it.message!!)
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }

    private fun selectPicture() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
        dialog.show()
    }

    private fun uploadImageToFirebase() {
        if (selectedPhotoUri == null)
            return
        val filename = UUID.randomUUID().toString()

        val imageRef = FirebaseStorage.getInstance()
            .getReference("/images/$filename")

        imageRef.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                dialog.dismiss()
                Log.d("CreateUserActivity", "Successfully updated image: ${it.metadata?.path}")

                imageRef.downloadUrl.addOnSuccessListener { task ->
                    Log.d("CreateUserActivity", "File location: $it")

                    user.profileImageUrl = task.toString()

                    userRef.collection("/users")
                        .document(Common.UID)
                        .update("profileImageUrl", user.profileImageUrl)
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

    }


    private var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what that selected image was...
            Log.d("CreateUserActivity", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circle_sign_up_photo.setImageBitmap(bitmap)
            btn_sign_up_photo.alpha = 0f
            dialog.dismiss()
        } else {
            circle_sign_up_photo.background
            dialog.dismiss()
        }
    }

    private fun openCreateHouseActivity() {
        val intent = Intent(this, CreateHouseActivity::class.java)
        startActivity(intent)
    }
}

