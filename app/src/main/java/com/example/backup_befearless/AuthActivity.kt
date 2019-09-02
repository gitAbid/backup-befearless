package com.example.backup_befearless

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    val notMatchError = "Password dosn't match"
    val error = "Provide a valid input"
    private var mAuth: FirebaseAuth? = null
    var dexter: DexterBuilder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        requestPermission()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            return
        }

        setContentView(R.layout.activity_auth)
        pbAuthProgress.isIndeterminate = true
        tvSignUp.setOnClickListener { showSignUpView() }
        btnSignIn.setOnClickListener {
            authenticate()
        }
    }

    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {

                    } else {
                        Snackbar.make(
                            container,
                            "All Permissions needed to work properly",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }


                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                }
            }).check()
    }


    private fun showSignUpView() {
        val visibility: Int
        if (tvSignUp.text.equals(getString(R.string.signup_text))) {
            visibility = View.VISIBLE
            tvSignUp.text = getString(R.string.sign_in_text)
            btnSignIn.text = getString(R.string.signup_text)
            tvAccountText.text=getString(R.string.already_have_account)
        } else {
            tvAccountText.text=getString(R.string.don_t_have_an_account)
            visibility = View.GONE
            tvSignUp.text = getString(R.string.signup_text)
            btnSignIn.text = getString(R.string.sign_in_text)
        }
        tilConfirmPassword.visibility = visibility
        tilName.visibility = visibility
    }

    private fun authenticate() {
        val intent = Intent(this, MainActivity::class.java)
        if (btnSignIn.text.equals(getString(R.string.signup_text))) {
            if (validSignUp()) {
                pbAuthProgress.visibility = View.VISIBLE
                mAuth?.createUserWithEmailAndPassword(
                    etEmailPhone.text.toString(),
                    etPassword.text.toString()
                )?.addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth?.currentUser
                        val userData = User(
                            uid = user?.uid,
                            name = etName.text.toString().trim(),
                            numbers = listOf(),
                            emergencyMessage = LocationUtilities.getMessage(
                                locationManager,
                                "",
                                etName.text.toString().trim()
                            )
                        )
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(user?.uid.toString()).set((userData))
                            .addOnSuccessListener {
                                startActivity(intent)
                            }
                            .addOnFailureListener {
                                pbAuthProgress.visibility = View.GONE
                                Snackbar.make(
                                    container,
                                    it.message.toString(),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        pbAuthProgress.visibility = View.GONE
                        val e = task.exception as FirebaseException
                        Snackbar.make(
                            container,
                            e.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else if (validSignIn()) {
            pbAuthProgress.visibility = View.VISIBLE
            mAuth?.signInWithEmailAndPassword(
                etEmailPhone.text.toString(),
                etPassword.text.toString()
            )?.addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    startActivity(intent)
                } else {
                    pbAuthProgress.visibility = View.GONE
                    val e = task.exception as FirebaseException
                    Snackbar.make(
                        container,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validSignIn(): Boolean {
        var valid = true
        etPassword.error = null
        etEmailPhone.error = null
        if (etEmailPhone.text.isNullOrEmpty()) {
            valid = false
            etEmailPhone.error = error
        }
        if (etPassword.text.isNullOrEmpty()) {
            valid = false
            etPassword.error = error
        }

        return valid
    }

    private fun validSignUp(): Boolean {
        etName.error = null
        etConfirmPassword.error = null
        var valid = validSignIn()
        if (etConfirmPassword.text.isNullOrEmpty()) {
            valid = false
            etConfirmPassword.error = error
        } else if (!etConfirmPassword.text?.trim()?.equals(etPassword.text?.trim())!!) {
            valid = false
            etConfirmPassword.error = notMatchError
        }
        if (etName.text.isNullOrEmpty()) {
            etName.error = error
            valid = false
        }
        return valid
    }

    override fun onResume() {
        super.onResume()
        requestPermission()
    }
}
