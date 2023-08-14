package com.messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var phoneNumberEditText: EditText
    private lateinit var sendCodeButton: Button
    private var verificationId: String? = null
    private lateinit var loader: ProgressBar


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already authenticated

        phoneNumberEditText = findViewById(R.id.PhoneNumber)
        sendCodeButton = findViewById(R.id.loginButton)
        loader = findViewById(R.id.loader)


        sendCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                sendVerificationCode(phoneNumber)
            } else {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        loader.visibility = View.VISIBLE

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    loader.visibility = View.GONE
                    Log.e(TAG, "Verification failed: ${e.message}")

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@Login.verificationId = verificationId
                    val phone = phoneNumberEditText.text.toString().trim()
                    val intent = Intent(applicationContext, OTPVerification::class.java)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("phoneNumber", phone)
                    startActivity(intent)


                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                loader.visibility = View.GONE

                if (task.isSuccessful) {
                    navigateToChats()
                } else {
                    Log.e(TAG, "Sign-in failed: ${task.exception?.message}")

                }
            }
    }


    private fun navigateToChats() {
        val intent = Intent(this, Chats::class.java)
        startActivity(intent)
        finish()
    }


    companion object {
        private const val TAG = "LoginActivity"
    }



}
