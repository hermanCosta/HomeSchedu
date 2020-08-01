//HomeScreenActivity
package com.herman.homeschedu.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.common.base.MoreObjects
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.herman.homeschedu.Adapter.ScheduleAdapter
import com.herman.homeschedu.Common.Common
import com.herman.homeschedu.Model.ScheduleInformation
import com.herman.homeschedu.Model.User
import com.herman.homeschedu.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_my_schedule.*
import kotlinx.android.synthetic.main.home_content_main.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.view.*
import java.time.format.DateTimeFormatter
import java.util.*


class HomeScreenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var adapter: ScheduleAdapter
    private lateinit var dialog: AlertDialog
    lateinit var houseId: String
    val uid = FirebaseAuth.getInstance().uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        centerTitle()

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        //Navigation Drawer
        toolbar = findViewById(R.id.home_screen_toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        navUserProfileInfo()
        setRecyclerView()

        fab_add_schedule.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(
                    android.Manifest.permission.READ_CALENDAR,
                    android.Manifest.permission.WRITE_CALENDAR
                )

                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                        openBookingActivity()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        list: List<PermissionRequest>,
                        permissionToken: PermissionToken
                    ) {
                    }
                }).check()
        }
    }// onCreate end

    override fun onResume() {
        super.onResume()
           setRecyclerView()
    }


    override fun onDestroy() {
       // if (mAuth.currentUser == null || houseId.isEmpty())
            super.onDestroy()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun navUserProfileInfo(): Boolean {
        val headerView = navView.getHeaderView(0)
        Log.d("HomeScreenActivity", "user id: ${Common.UID}")

        fStore
            .collection("/users")
            .document(uid)
            .addSnapshotListener { userDocument, _ ->

                headerView.tv_first_name_header.text = userDocument?.getString("firstName")
                headerView.tv_last_name_header.text = userDocument?.getString("lastName")
                headerView.tv_email_header.text = userDocument?.getString("email")

                Picasso.get().load(userDocument?.getString("profileImageUrl"))
                    .into(headerView.profile_circle_image_view_header)
                iv_profile_header.alpha = 0f
            }
        return true
    }

    private fun setRecyclerView() {
        dialog.show()

        // Get current userID
        val userRef = Common.fStore
            .collection("/users")
            .document(uid)

        // Get Current user houseID
        userRef.addSnapshotListener { documentSnapshot, _ ->
            houseId = documentSnapshot?.getString("houseId").toString()

            val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
            val dateFormat = Common.todayDate.format(formatter).toString()

            val houseRef = fStore
                .collection("/houses")
                .document(houseId)
                .collection("Booking")
                .document("AllBooking")
                .collection(dateFormat)

            houseRef.get().addOnCompleteListener {

                if (it.isSuccessful) {
                    Log.d("HomeScreenActivity", "Total Query is: ${it.result!!.size()}")
                    val query = houseRef.orderBy(
                        "timestamp",
                        Query.Direction.ASCENDING
                    )

                    val options = FirestoreRecyclerOptions.Builder<ScheduleInformation>()
                        .setQuery(query, ScheduleInformation::class.java)
                        .build()

                    adapter = ScheduleAdapter(options)

                    schedule_list_recycler_view.layoutManager = LinearLayoutManager(this)
                    schedule_list_recycler_view.setHasFixedSize(true)
                    schedule_list_recycler_view.adapter = adapter

                    adapter.startListening()
                    dialog.dismiss()
                }

                if (it.result!!.isEmpty) {
                    tv_no_schedules_to_display.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }

                .addOnFailureListener {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)

            }
            R.id.nav_my_schedules -> {
                val intent = Intent(this, MyScheduleActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_housemates -> {
                val intent = Intent(this, HousemateListActivity::class.java)
                startActivity(intent)

            }
            R.id.nav_update -> {
                val intent = Intent(this, UpdateProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_close_account -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setIcon(R.drawable.ic_logo_icon)
                alertDialogBuilder.setTitle("Are you sure?")
                alertDialogBuilder.setMessage(R.string.deleteAccount)
                alertDialogBuilder.setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                //    dialog.show()

                    deleteUserFromApp()
                    Common.HOUSE_TO_DELETE = houseId
                    val intent = Intent(this, CloseAccountActivity::class.java)
                    startActivity(intent)


                }

                alertDialogBuilder.setNegativeButton("Dismiss") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }

                alertDialogBuilder.show()

            }

            R.id.nav_logout -> {

                mAuth.signOut()
                onStop()
                Toast.makeText(this, "Signed out ", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    //Function to call in onResume() of your activity
    private fun centerTitle() {
        val textViews = ArrayList<View>()
        window.decorView.findViewsWithText(textViews, title, View.FIND_VIEWS_WITH_TEXT)
        if (textViews.size > 0) {
            var appCompatTextView: AppCompatTextView? = null
            if (textViews.size == 1)
                appCompatTextView = textViews[0] as AppCompatTextView
            else {
                for (v in textViews) {
                    if (v.parent is android.widget.Toolbar) {
                        appCompatTextView = v as AppCompatTextView
                        break
                    }
                }
            }
            if (appCompatTextView != null) {
                val params = appCompatTextView.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                appCompatTextView.layoutParams = params
                appCompatTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        }
    }

    private fun openBookingActivity() {
        val intent = Intent(this, ScheduleActivity::class.java)
        startActivity(intent)
    }

    private fun deleteUserFromApp() {
        mAuth.currentUser!!.delete().addOnCompleteListener() {
            Log.d("CloseAccountActivity", "User Deleted From Firebase")

            val intent = Intent(this, CloseAccountActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            dialog.dismiss()
        }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                Log.d("CloseAccountActivity", it.message!!)
            }
    }
}
