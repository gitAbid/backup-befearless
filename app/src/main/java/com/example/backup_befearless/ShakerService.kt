package com.example.backup_befearless

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager

import androidx.core.app.NotificationCompat

import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

import java.util.ArrayList


class ShakerService : Service() {
    private lateinit var locationManager: LocationManager
    private var shakeListener: ShakeDetector.ShakeListener? = null
    private val threshold = 18F
    private val timeBeforeDeclaringShakeStopped = 1000L
    private var sms: SmsManager? = null
    private val CHANNEL_RT_TRACKER = "BackupShakeDetector"


    private var data: User? = null
    lateinit var contactAdapter: ContactAdapter
    var db: FirebaseFirestore? = null
    lateinit var numbers: ArrayList<ContactModel>
    var docRef: DocumentReference? = null


    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Sensey.getInstance().init(this)


        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        db = FirebaseFirestore.getInstance()

        numbers = arrayListOf()
        data = User()
        docRef = db?.collection("users")?.document(currentUser?.uid.toString())


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        if (intent != null) {
            startForegroundService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundService() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_RT_TRACKER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.backup_shaker))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        shakeListener = object : ShakeDetector.ShakeListener {
            override fun onShakeDetected() {
                // Shake detected, do something
            }

            override fun onShakeStopped() {
                docRef?.get()
                    ?.addOnSuccessListener {
                        data = it.toObject(User::class.java)
                        if (data != null && data?.numbers != null) {
                            numbers = data!!.numbers as ArrayList<ContactModel>
                            for (numberModel in numbers) {
                                sendMessages(
                                    numberModel.number,
                                    LocationUtilities.getMessage(
                                        locationManager,
                                        numberModel.contactName,
                                        data?.name!!
                                    )
                                )
                            }
                        }

                    }
            }

        }

        Sensey.getInstance()
            .startShakeDetection(threshold, timeBeforeDeclaringShakeStopped, shakeListener)

        startForeground(2, notification)
    }

    fun sendMessages(number: String, message: String) {
        try {
            sms = SmsManager.getDefault()
            val messageParts = sms!!.divideMessage(message)
            sms!!.sendMultipartTextMessage(number, null, messageParts, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Sensey.getInstance().stopShakeDetection(shakeListener)
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val description = getString(R.string.backup_shaker)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_RT_TRACKER, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}
