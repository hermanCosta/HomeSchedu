package com.herman.homeschedu.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.herman.homeschedu.Adapter.PlaceAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Common.SpacesItemDecoration
import com.herman.homeschedu.Interface.IAllResourceLoadListener
import com.herman.homeschedu.Interface.IPlaceLoadListener
import com.herman.homeschedu.Model.Place
import com.herman.homeschedu.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_booking_step_one.*
import kotlinx.android.synthetic.main.fragment_booking_step_one.view.*
import kotlin.collections.ArrayList

class BookingStep1Fragment : Fragment(), IAllResourceLoadListener, IPlaceLoadListener {

    //Variables
    private lateinit var allResourceRef: CollectionReference
    private lateinit var placeRef: CollectionReference
    lateinit var mAuth: FirebaseAuth
    private lateinit var iAllResourceLoadListener: IAllResourceLoadListener
    private lateinit var iPlaceLoadListener: IPlaceLoadListener
    lateinit var dialog: AlertDialog
    lateinit var houseId: String

    val uid = FirebaseAuth.getInstance().uid ?: ""

    companion object {
        private var instance: BookingStep1Fragment? = null
        fun getInstance(): BookingStep1Fragment? {
            if (instance == null) {
                instance = BookingStep1Fragment()
            }
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iAllResourceLoadListener = this
        iPlaceLoadListener = this

        dialog = SpotsDialog.Builder().setContext(activity).setCancelable(false).build()
    } //End of onCreate


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val itemView = inflater.inflate(R.layout.fragment_booking_step_one, container, false)
        val recyclerSalon = itemView.recycler_place

        init(recyclerSalon)
        loadAllResource()

        return itemView
    }

    private fun init(recyclerSalon: RecyclerView?) {
        recyclerSalon!!.setHasFixedSize(true)
        recyclerSalon.layoutManager = GridLayoutManager(activity, 2)
        recyclerSalon.addItemDecoration(SpacesItemDecoration(4))

    }

    //List all Resources of the house
    private fun loadAllResource() {
        dialog.show()


        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        userRef.addSnapshotListener { documentSnapshot, _ ->
            houseId = documentSnapshot?.getString("houseId").toString()

            allResourceRef = FirebaseFirestore.getInstance().collection("/houses")
                .document(houseId)
                .collection("/Resource")


            allResourceRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialog.dismiss()

                        val list: ArrayList<String> = ArrayList()
                        list.add("Please choose resource")
                        for (queryDocumentSnapshot in task.result!!)
                            list.add(queryDocumentSnapshot.id)

                        iAllResourceLoadListener.onAllResourceLoadSuccess(list)

                    }
                }
                .addOnFailureListener {
                    iAllResourceLoadListener.onAllResourceLoadFailed(it.message!!)
                }
        }
    }


    override fun onAllResourceLoadSuccess(resourceNameList: List<String>) {
        resource_spinner.setItems(resourceNameList)
        resource_spinner.setOnItemSelectedListener { _, position, _, item ->
            if (position > 0) {
                loadPlaceOfHouse(item.toString())

            } else  {
                recycler_place.visibility = View.GONE

            }
        }
    }

    // Load all Places of chosen resource
    private fun loadPlaceOfHouse(placeName: String) {
        dialog.show()

            Common.place = placeName
            placeRef = FirebaseFirestore.getInstance()
                .collection("/houses")
                .document(houseId)
                .collection("/Resource")
                .document(placeName)
                .collection("/Place")

            placeRef.get()
                .addOnCompleteListener {
                    val list: ArrayList<Place> = ArrayList()
                    if (it.isSuccessful) {
                        for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result!!) {
                            val place = queryDocumentSnapshot.toObject(Place::class.java)
                            place.placeId = queryDocumentSnapshot.id
                            list.add(place)
                        }
                    }
                    iPlaceLoadListener.onPlaceLoadSuccess(list)
                }
                .addOnFailureListener {
                    iPlaceLoadListener.onPlaceLoadFailed(it.message!!)
                }
        }

    override fun onAllResourceLoadFailed(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPlaceLoadSuccess(placeList: List<Place>) {
        val salonAdapter = PlaceAdapter(activity!!, placeList)
        recycler_place.adapter = salonAdapter

        recycler_place.visibility = View.VISIBLE
        dialog.dismiss()

    }

    override fun onPlaceLoadFailed(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
}