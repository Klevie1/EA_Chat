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

class OTPVerification : AppCompatActivity() {
    private lateinit var verificationCodeEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendCodeButton: Button
    private lateinit var verificationId: String
    private lateinit var phoneNumber: String
    private lateinit var loader: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpverification)

        auth = FirebaseAuth.getInstance()

        verificationCodeEditText = findViewById(R.id.code1)
        verifyButton = findViewById(R.id.verifyButton)
        resendCodeButton = findViewById(R.id.resendCodeButton)
        loader = findViewById(R.id.loader)
        verificationId = intent.getStringExtra("verificationId") ?: ""
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        verifyButton.setOnClickListener {
            val verificationCode = verificationCodeEditText.text.toString().trim()
            if (verificationCode.isNotEmpty()) {
                verifyPhoneNumberWithCode(verificationCode)
            } else {
                Toast.makeText(this, "Enter verification code", Toast.LENGTH_SHORT).show()
            }
        }

        resendCodeButton.setOnClickListener {
            resendVerificationCode()
        }
    }

    private fun verifyPhoneNumberWithCode(verificationCode: String) {
        loader.visibility = View.VISIBLE
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                loader.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val phoneNumber = it.phoneNumber
                        if (phoneNumber != null) {
                            navigateToSettings(phoneNumber)
                        }
                    }
                } else {
                    Log.e(TAG, "Verification failed: ${task.exception?.message}")
                    Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resendVerificationCode() {
        loader.visibility = View.GONE
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval of the verification code succeeded
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Verification failed: ${e.message}")
                    loader.visibility = View.GONE
               }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // Code sent to the provided phone number
                    // Save the verificationId and resendToken if needed
                    this@OTPVerification.verificationId = verificationId

                    loader.visibility = View.GONE
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun navigateToSettings(phoneNumber: String) {
        val intent = Intent(this, Settings::class.java)
        intent.putExtra("phoneNumber", phoneNumber)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "OTPVerificationActivity"
    }
}
