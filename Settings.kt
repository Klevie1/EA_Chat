package com.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class Settings : AppCompatActivity() {
    private lateinit var phoneNumber: String
    private lateinit var loader: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        val phone = findViewById<TextView>(R.id.phones)
        phone.text = phoneNumber

        val nameEditText = findViewById<EditText>(R.id.name)
        val languageSpinner = findViewById<Spinner>(R.id.language)

        loader = findViewById(R.id.loader)
        val languages = resources.getStringArray(R.array.language_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val languageMap = mapOf(
            "English" to "en",
            "Spanish" to "es",
            "French" to "fr",
            "German" to "de",
            "Chinese (Simplified)" to "zh-CN",
            "Chinese (Traditional)" to "zh-TW",
            "Japanese" to "ja",
            "Korean" to "ko",
            "Italian" to "it",
            "Portuguese" to "pt",
            "Russian" to "ru",
            "Arabic" to "ar",
            "Hindi" to "hi",
            "Bengali" to "bn",
            "Urdu" to "ur",
            "Turkish" to "tr",
            "Dutch" to "nl",
            "Swedish" to "sv",
            "Polish" to "pl",
            "Indonesian" to "id",
            "Thai" to "th",
            "Greek" to "el",
            "Czech" to "cs",
            "Hebrew" to "he",
            "Vietnamese" to "vi",
            "Romanian" to "ro",
            "Hungarian" to "hu",
            "Swahili" to "sw",
            "Malay" to "ms",
            "Filipino" to "fil",
            "Danish" to "da",
            "Finnish" to "fi",
            "Norwegian" to "no",
            "Ukrainian" to "uk",
            "Slovak" to "sk",
            "Slovenian" to "sl",
            "Croatian" to "hr",
            "Lithuanian" to "lt",
            "Latvian" to "lv",
            "Estonian" to "et",
            "Albanian" to "sq",
            "Serbian" to "sr",
            "Macedonian" to "mk",
            "Bulgarian" to "bg",
            "Bosnian" to "bs",
            "Montenegrin" to "me",
            "Icelandic" to "is",
            "Georgian" to "ka",
            "Armenian" to "hy",
            "Kazakh" to "kk",
            "Azerbaijani" to "az",
            "Uzbek" to "uz",
            "Kyrgyz" to "ky",
            "Tajik" to "tg",
            "Turkmen" to "tk",
            "Assamese" to "as",
            "Kannada" to "kn",
            "Malayalam" to "ml",
            "Odia (Oriya)" to "or",
            "Punjabi" to "pa",
            "Tamil" to "ta",
            "Telugu" to "te",
            "Maori" to "mi",
            "Yiddish" to "yi",
            "Mongolian" to "mn",
            "Khmer" to "km",
            "Sinhala" to "si",
            "Tibetan" to "bo",
            "Nepali" to "ne",
            "Lao" to "lo",
            "Pashto" to "ps",
            "Kurdish" to "ku",
            "Dari" to "prs",
            "Afrikaans" to "af",
            "Zulu" to "zu",
            "Sotho" to "st",
            "Xhosa" to "xh",
            "Tswana" to "tn",
            "Sesotho" to "st",
            "Swazi" to "ss",
            "Chichewa" to "ny",
            "Shona" to "sn",
            "Hausa" to "ha",
            "Igbo" to "ig",
            "Yoruba" to "yo",
            "Somali" to "so",
            "Kinyarwanda" to "rw",
            "Amharic" to "am",
            "Tigrinya" to "ti",
            "Oromo" to "om",
            "Akan" to "ak",
            "Ibibio" to "ibb",
            "Fulfulde" to "ff",
            "Wolof" to "wo",
            "Kinyamulenge" to "rw",
            "Malagasy" to "mg"
        )



        val profileButton = findViewById<Button>(R.id.profileButton)
        profileButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val selectedLanguageName = languageSpinner.selectedItem.toString().trim()
            val selectedLanguageKey = languageMap[selectedLanguageName] ?: ""


            if (name.isNotEmpty() && selectedLanguageKey.isNotEmpty()) {
                // Save the user's profile data to the database
                saveUserProfileData(name, selectedLanguageKey)
            } else {
                Toast.makeText(this, "Please fill in all the details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserProfileData(name: String, language: String) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number not provided", Toast.LENGTH_SHORT).show()
            return
        }

        loader.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance()
        val userProfileRef = database.reference.child("users").child(phoneNumber)

        // Create a map of user profile data
        val userProfileData = mapOf(
            "phoneNumber" to phoneNumber,
            "name" to name,
            "language" to language
        )

        // Save the userProfileData to the Realtime Database
        userProfileRef.setValue(userProfileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile data saved successfully", Toast.LENGTH_SHORT).show()
                // Navigate to the next activity or perform any other actions here
                navigateToChats()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                loader.visibility = View.GONE
            }
    }




    private fun navigateToChats() {
        val intent = Intent(this, Chats::class.java)
        startActivity(intent)
        finish()
    }
}
