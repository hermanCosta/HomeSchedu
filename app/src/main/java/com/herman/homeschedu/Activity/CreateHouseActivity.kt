package com.herman.homeschedu.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.Common.Companion.fStore
import com.herman.homeschedu.Model.House
import com.herman.homeschedu.Model.Item
import com.herman.homeschedu.Model.Place
import com.herman.homeschedu.Model.User
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_create_house.*

class CreateHouseActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var house: House
    lateinit var dialog: android.app.AlertDialog
    lateinit var houseId: String

    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_house)
        supportActionBar?.hide()


        mAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        btn_create_house.setOnClickListener { createHouse() }
        tv_enter_existing_house.setOnClickListener { enterExistingHouse() }

    }

    private fun enterExistingHouse() {
        val intent = Intent(this, EnterExistingHouse::class.java)
        startActivity(intent)
    }

    private fun createHouse() {
        dialog.show()
        val houseNumber = et_house_number.text.toString()
        val street = et_street.text.toString()
        val zipCode = et_zipcode.text.toString()
        val eirCode = et_eircode.text.toString()

        if (houseNumber.isEmpty() || street.isEmpty() || zipCode.isEmpty() || eirCode.isEmpty()) {

            dialog.dismiss()
            Toast.makeText(baseContext, "Please, enter all fields", Toast.LENGTH_SHORT).show()
            return
        }


        house = House(
            houseNumber,
            street,
            zipCode,
            eirCode
        )


        val houseRef = fStore

        houseRef.collection("/houses")
            .add(house).addOnSuccessListener {
                Common.HOUSE_ID = it.id
                houseId = it.id

                //first generate a new collection
                // and a new document just to match to the query
                val docName = hashMapOf("name" to "AllBooking")

                val houseDocRef = fStore
                    .collection("/houses")
                    .document(houseId)
                    .collection("/Booking")
                    .document("AllBooking")

                houseDocRef.set(docName).addOnSuccessListener {
                    Log.d(
                        "CreateHouseActivity",
                        "Booking Collection created into the house "
                    )


                    // Pass the houseId to the user collection
                    FirebaseFirestore.getInstance()
                        .collection("/users")
                        .document(uid)
                        .update("houseId", houseId)

                        .addOnSuccessListener {
                            Log.d(
                                "CreateHouseActivity",
                                "House $houseId saved in the current user"
                            )

                            createBathroomDocument()
                            createWashMachineDocument()
                            saveUserToHouse()
                            sendEmailVerification()
                            openMainActivity()
                            dialog.dismiss()

                        }

                }

                    .addOnFailureListener {
                        Log.d("CreateHouseActivity", it.message)
                        dialog.dismiss()
                    }

            }

    }

    //save user to the current house
    private fun saveUserToHouse() {

        val userRef = fStore
            .collection("/users")
            .document(uid)

        userRef.get().addOnCompleteListener { it ->
            if (it.isSuccessful) {
                val userDocument = it.result
                if (userDocument!!.exists()) {
                    val userData = userDocument.data

                    val houseRef = FirebaseFirestore.getInstance()
                        .collection("/houses")
                        .document(houseId)
                        .collection("/Housemate")
                        .document(uid)

                    houseRef.set(userData!!).addOnSuccessListener {
                        Log.d("CreateHouseActivity", "Current user saved into the house")
                    }

                        .addOnFailureListener {
                            Log.d("CreateHouseActivity", it.message!!)
                        }

                }

            }
        }
    }

    private fun createWashMachineDocument() {
        //val resourceName = Resource("Washing Machine")

        val resourceMap = hashMapOf<String, Any>()
        resourceMap["resourceName"] = "Washing Machine"

        // Save Resource with the same document ID
        val houseRef = FirebaseFirestore.getInstance()
            .collection("/houses")
            .document(houseId)
            .collection("/Resource")
            .document("Washing Machine")

        houseRef.set(resourceMap).addOnSuccessListener {

            // Once the Resource is created, create place document into it
            val placeName = Place()
            placeName.name = "First Place"
            placeName.local = "Ground floor area"
            placeName.information = "at the main area of your home"


            val mHouseRef = FirebaseFirestore.getInstance()
                .collection("/houses")
                .document(houseId)
                .collection("/Resource")
                .document("Washing Machine")
                .collection("/Place")

            mHouseRef.whereEqualTo("name", placeName.name)
            mHouseRef.get().addOnCompleteListener {
                if (it.result!!.size() < 2) { //create maximum 2 documents

                    mHouseRef.add(placeName).addOnSuccessListener { placeDoc1 ->
                        placeName.placeId = placeDoc1.id
                        Log.d("CreateHouseActivity", "${placeName.name} created ")
                        placeDoc1.update("placeId", placeDoc1.id).addOnSuccessListener {
                            Log.d(
                                "CreateHouseActivity",
                                "${placeName.name} ID: ${placeName.placeId} updated "
                            )

                            createWashMachineItem(placeName.placeId!!)

                            // Update name, address for the second document
                            mHouseRef.add(placeName).addOnSuccessListener { placeDoc2 ->

                                placeName.name = "Second Place"
                                placeName.local = "First floor area"
                                placeName.information = "at the second area of your home"
                                placeName.placeId = placeDoc2.id

                                placeDoc2.update(
                                    "name", placeName.name,
                                    "local", placeName.local, "information",
                                    placeName.information, "placeId", placeName.placeId
                                )
                                    .addOnCompleteListener {
                                        Log.d("CreateHouseActivity", "${placeName.name} created ")
                                        Log.d(
                                            "CreateHouseActivity",
                                            "${placeName.name} ID: ${placeName.placeId} updated "
                                        )

                                        createWashMachineItem(placeName.placeId!!)

                                    }
                            }
                        }
                    }
                }


            }
                .addOnFailureListener {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun createBathroomDocument() {
        //val resourceName = Resource("Bathroom")
        val resourceMap = hashMapOf<String, Any>()
        resourceMap["resourceName"] = "Washing Machine"

        // Save Resource with the same document ID
        val houseRef = FirebaseFirestore.getInstance()
            .collection("/houses")
            //.document(houseId)
            .document(houseId)
            .collection("/Resource")
            .document("Bathroom")

        houseRef.set(resourceMap).addOnSuccessListener {

            // Once the Resource is created, create place document into it
            val placeName = Place()
            placeName.name = "First Place"
            placeName.local = "Ground floor area"
            placeName.information = "at the main area of your home"


            val mHouseRef = FirebaseFirestore.getInstance()
                .collection("/houses")
                .document(houseId)
                .collection("/Resource")
                .document("Bathroom")
                .collection("/Place")

            mHouseRef.whereEqualTo("name", placeName.name)
            mHouseRef.get().addOnCompleteListener {
                if (it.result!!.size() < 2) { //create maximum 2 documents

                    mHouseRef.add(placeName).addOnSuccessListener { placeDoc1 ->
                        placeName.placeId = placeDoc1.id
                        Log.d("CreateHouseActivity", "${placeName.name} created ")
                        placeDoc1.update("placeId", placeDoc1.id).addOnSuccessListener {
                            Log.d(
                                "CreateHouseActivity",
                                "${placeName.name} ID: ${placeName.placeId} updated "
                            )

                            createBathroomItem(placeName.placeId!!)

                            // Update name, address for the second document
                            mHouseRef.add(placeName).addOnSuccessListener { placeDoc2 ->

                                placeName.name = "Second Place"
                                placeName.local = "First floor area"
                                placeName.information = "at the second area of your home"
                                placeName.placeId = placeDoc2.id

                                placeDoc2.update(
                                    "name", placeName.name,
                                    "local", placeName.local, "information",
                                    placeName.information, "placeId", placeName.placeId
                                )
                                    .addOnCompleteListener {
                                        Log.d("CreateHouseActivity", "${placeName.name} created ")
                                        Log.d(
                                            "CreateHouseActivity",
                                            "${placeName.name} ID: ${placeName.placeId} updated "
                                        )

                                        createBathroomItem(placeName.placeId!!)

                                    }
                            }
                        }
                    }
                }
            }


        }
            .addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
            }
    }


    private fun createBathroomItem(placeId: String) {

        val nWashingMachine = Item()
        nWashingMachine.name = "Bathroom #1"

        val nItemRef = FirebaseFirestore.getInstance()
            .collection("/houses")
            .document(houseId)
            .collection("/Resource")
            .document("Bathroom")
            .collection("/Place")
            .document(placeId)
            .collection("/Item")

        nItemRef.whereEqualTo("name", nWashingMachine.name)
        nItemRef.get().addOnCompleteListener {
            if (it.result!!.size() < 2) {
                nItemRef.add(nWashingMachine).addOnSuccessListener { washingMachineDoc1 ->
                    Log.d("CreateHouseActivity", "${nWashingMachine.name} created")
                    nWashingMachine.itemId = washingMachineDoc1.id

                    washingMachineDoc1.update("itemId", nWashingMachine.itemId)
                        .addOnSuccessListener {
                            Log.d(
                                "CreateHouseActivity",
                                "${nWashingMachine.name} ID: ${nWashingMachine.itemId} updated"
                            )

                            nItemRef.add(nWashingMachine)
                                .addOnSuccessListener { washingMachineDoc2 ->

                                    nWashingMachine.name =
                                        "Bathroom #2"
                                    nWashingMachine.itemId =
                                        washingMachineDoc2.id

                                    washingMachineDoc2.update(
                                        "name",
                                        nWashingMachine.name,
                                        "itemId",
                                        nWashingMachine.itemId
                                    )
                                        .addOnSuccessListener {
                                            Log.d(
                                                "CreateHouseActivity",
                                                "${nWashingMachine.name} created"
                                            )
                                            Log.d(
                                                "CreateHouseActivity",
                                                "${nWashingMachine.name} ID: ${nWashingMachine.itemId} updated"
                                            )

                                        }
                                }
                        }
                }
            }

        }
            .addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
            }

    }


    private fun createWashMachineItem(placeId: String) {

        val nWashingMachine = Item()
        nWashingMachine.name = "Washing Machine #1"

        val nItemRef = FirebaseFirestore.getInstance()
            .collection("/houses")
            .document(houseId)
            .collection("/Resource")
            .document("Washing Machine")
            .collection("/Place")
            .document(placeId)
            .collection("/Item")

        nItemRef.whereEqualTo("name", nWashingMachine.name)
        nItemRef.get().addOnCompleteListener {
            if (it.result!!.size() < 2) {
                nItemRef.add(nWashingMachine).addOnSuccessListener { washingMachineDoc1 ->
                    Log.d("CreateHouseActivity", "${nWashingMachine.name} created")
                    nWashingMachine.itemId = washingMachineDoc1.id

                    washingMachineDoc1.update("itemId", nWashingMachine.itemId)
                        .addOnSuccessListener {
                            Log.d(
                                "CreateHouseActivity",
                                "${nWashingMachine.name} ID: ${nWashingMachine.itemId} updated"
                            )

                            nItemRef.add(nWashingMachine)
                                .addOnSuccessListener { washingMachineDoc2 ->

                                    nWashingMachine.name =
                                        "Washing Machine #2"
                                    nWashingMachine.itemId =
                                        washingMachineDoc2.id

                                    washingMachineDoc2.update(
                                        "name",
                                        nWashingMachine.name,
                                        "itemId",
                                        nWashingMachine.itemId
                                    )
                                        .addOnSuccessListener {
                                            Log.d(
                                                "CreateHouseActivity",
                                                "${nWashingMachine.name} created"
                                            )
                                            Log.d(
                                                "CreateHouseActivity",
                                                "${nWashingMachine.name} ID: ${nWashingMachine.itemId} updated"
                                            )

                                        }
                                }
                        }
                }
            }

        }
            .addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
            }


    }

    private fun sendEmailVerification() {
        val user = mAuth.currentUser
        user?.sendEmailVerification()!!.addOnSuccessListener {
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

    private fun openMainActivity() {
        mAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}


