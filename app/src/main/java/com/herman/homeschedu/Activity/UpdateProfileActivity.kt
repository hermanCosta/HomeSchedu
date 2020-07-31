package com.herman.homeschedu.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.Common.Companion.fStore
import com.herman.homeschedu.R
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_update_profife.*
import java.time.format.DateTimeFormatter
import java.util.*


@Suppress("DEPRECATION")
class UpdateProfileActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var userRef: FirebaseFirestore
    lateinit var dialog: AlertDialog
    lateinit var houseId: String

    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profife)

        userRef = fStore
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        currentUserProfileInfo()

        btn_update_profile.setOnClickListener { updateProfile() }
        btn_update_profile_photo.setOnClickListener { selectPicture() }
        update_profile_back_home.setOnClickListener { finish() }
    }

    private fun currentUserProfileInfo() {
        dialog.show()

        userRef
            .collection("/users")
            .document(uid)

            .addSnapshotListener { userDocument, _ ->

                et_update_first_name.hint = userDocument?.getString("firstName")
                et_update_last_name.hint = userDocument?.getString("lastName")

                Picasso.get().load(userDocument?.getString("profileImageUrl"))
                    .into(circle_update_profile_photo)
                btn_update_profile_photo.alpha = 0f
                dialog.dismiss()

            }
    }


    private fun updateProfile() {
        dialog.show()

        val firstName = et_update_first_name.text.toString()
        val lastName = et_update_last_name.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(baseContext, "Please enter all fields ", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            return
        }


        // Update user profile
        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        userRef.update("firstName", firstName, "lastName", lastName)
            .addOnCompleteListener() {
                Log.d("UpdateProfileActivity", "Profile updated in User collection")

                updateUserInHousemateList()
                updateUserInHouseBooking()
                updateUserInTheMySchedule()

                Toast.makeText(baseContext, "User updated successfully ", Toast.LENGTH_LONG).show()
                uploadImageToFirebase()
                finish()


            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }


    }

    private fun updateUserInHousemateList() {
        val userRef = fStore
            .collection("/users")
            .document(uid)

        userRef.addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
            val houseId = documentSnapshot!!["houseId"].toString()
            val firstName = documentSnapshot["firstName"].toString()
            val lastName = documentSnapshot["lastName"].toString()
            val profileImageUrl = documentSnapshot["profileImageUrl"].toString()

            fStore
                .collection("/houses")
                .document(houseId)
                .collection("Housemate")
                .document(uid)
                .update(
                    "firstName",
                    firstName,
                    "lastName",
                    lastName,
                    "profileImageUrl",
                    profileImageUrl
                )
                .addOnCompleteListener {
                    Log.d("UpdateProfileActivity", "Profile updated in House collection")


                }
        }
    }

    private fun updateUserInHouseBooking() {
        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()

        val userRef = fStore
            .collection("/users")
            .document(uid)

        userRef.addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
            val userFieldMap = hashMapOf<String, Any>()

            val houseId = documentSnapshot!!["houseId"].toString()
            val firstName = documentSnapshot["firstName"].toString()
            val lastName = documentSnapshot["lastName"].toString()
            val profileImageUrl = documentSnapshot["profileImageUrl"].toString()

            userFieldMap["firstName"] = firstName
            userFieldMap["lastName"] = lastName
            userFieldMap["profileImageUrl"] = profileImageUrl


            val bookingRef = fStore
                .collection("/houses")
                .document(houseId)
                .collection("/Booking")
                .document("AllBooking")
                //.collection(Common.todayDate.toString())
                .collection(dateFormat)
                .whereEqualTo("userId", uid)

            bookingRef.get().addOnCompleteListener {
                for (querySnapshot in it.result!!) {
                    val userId = querySnapshot.id

                    fStore
                        .collection("/houses")
                        .document(houseId)
                        .collection("/Booking")
                        .document("AllBooking")
                        //.collection(Common.todayDate.toString())
                        .collection(dateFormat)
                        .document(userId)
                        .update(userFieldMap).addOnSuccessListener {
                            Log.d("UpdateProfileActivity", "Profile updated in Booking collection")
                        }
                }
            }
        }
    }


    private fun updateUserInTheMySchedule() {

        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
        val dateFormat = Common.todayDate.format(formatter).toString()


        val userRef = fStore
            .collection("/users")
            .document(uid)

        // Get User Document from user collection
        userRef.addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
            val userFieldMap = hashMapOf<String, Any>()

            val firstName = documentSnapshot!!["firstName"]!!.toString()
            val lastName = documentSnapshot.get("lastName").toString()
            val profileImageUrl = documentSnapshot["profileImageUrl"].toString()

            userFieldMap["firstName"] = firstName
            userFieldMap["lastName"] = lastName
            userFieldMap["profileImageUrl"] = profileImageUrl


            userRef
                .collection(dateFormat)
                .get().addOnCompleteListener {
                    for (querySnapshot in it.result!!) {
                        val userId = querySnapshot.id
                        fStore
                            .collection("/users")
                            .document(uid)
                            .collection(dateFormat)
                            .document(userId)

                            .update(userFieldMap).addOnSuccessListener {
                                Log.d(
                                    "UpdateProfileActivity",
                                    "Profile updated in UserBooking collection"
                                )
                            }
                    }
                }

        }
    }

    private fun selectPicture() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    private fun uploadImageToFirebase() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {

                Log.d("UpdateProfileActivity", "Successfully updated image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { it ->
                    Log.d("UpdateProfileActivity", "File location: $it")

                    updatePhotoProfile(it.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            iv_photo_camera.visibility = View.GONE
            // proceed and check what that selected image was...
            Log.d("UpdateProfileActivity", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circle_update_profile_photo.setImageBitmap(bitmap)
            btn_update_profile_photo.alpha = 0f
        }
    }

    private fun updatePhotoProfile(profileImageUrl: String) {
        val userRef = FirebaseFirestore.getInstance()
            .collection("/users")
            .document(uid)

        userRef.update("profileImageUrl", profileImageUrl)

    }
}
