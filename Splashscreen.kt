package com.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class Splashscreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            navigateToChats()
            return
        }

        val background = object : Thread() {
            override fun run() {
                try {
                    // Display the splash screen for 3 seconds
                    Thread.sleep(3000)

                    // Start the main activity
                    val intent = Intent(baseContext, MainActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }

    private fun navigateToChats() {
        val intent = Intent(this, Chats::class.java)
        startActivity(intent)
        finish()
    }
}