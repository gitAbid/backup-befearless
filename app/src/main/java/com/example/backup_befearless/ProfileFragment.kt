package com.example.backup_befearless


import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_profile.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()


        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val docRef = db.collection("users").document(auth.currentUser?.uid.toString())
        docRef.get().addOnSuccessListener {
            val data = it.toObject(User::class.java)!!
            tvUserName.text = data.name

        }

        btnLogout.setOnClickListener {
            auth.signOut()
            stopService()
            val intent = Intent(activity, AuthActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }


        swShakerService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!isMyServiceRunning() && enableGPS()) {
                    startService()
                }

            } else if (isMyServiceRunning()) {
                stopService()
            }
        }


    }

    private fun startService() {
        val intent = Intent(activity, ShakerService::class.java)
        activity?.startService(intent)
    }

    private fun stopService() {
        val intent = Intent(activity, ShakerService::class.java)
        activity?.stopService(intent)
    }

    private fun enableGPS(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            val alertDialog = AlertDialog.Builder(context)
                .setTitle(R.string.gps_off)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(
                    R.string.open_location_settings
                ) { _, _ ->
                    startActivity(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        )
                    )

                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    stopService()
                    swShakerService.isChecked = false
                }
                .create()
            alertDialog.show()

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).background =
                ContextCompat.getDrawable(context!!, R.drawable.ripple_rectangle)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))
        }
        return gps_enabled
    }

    override fun onResume() {
        super.onResume()
        if (!enableGPS()) {
            stopService()
            swShakerService.setChecked(false)
        } else {
            if (isMyServiceRunning()) {
                swShakerService.setChecked(true)
            } else {
                swShakerService.setChecked(false)

            }
        }
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
            if (ShakerService::class.java.getName() == service.service.className) {
                return true
            }
        }
        return false
    }
}
