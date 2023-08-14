package com.messenger

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Messages : AppCompatActivity(),  MessageAdapter.DeleteClickListener, MessageAdapter.CopyClickListener {

    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedUser: User
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: MutableList<Message>
    private lateinit var messagesReference: DatabaseReference
    private val messageHandler = Handler()
    private val messageRunnable = object : Runnable {
        override fun run() {
            loadMessages()
            messageHandler.postDelayed(this, 5000) // Call loadMessages() every 1 second (1000 milliseconds)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)


        messageHandler.postDelayed(messageRunnable, 5000)

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)

        val database = FirebaseDatabase.getInstance()
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        messagesReference = database.reference.child("messages")

        messageList = mutableListOf()
        messageAdapter = MessageAdapter(messageList, this, this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Messages)
            adapter = messageAdapter
        }

        // Get the selected user data from the intent
        selectedUser = intent.getParcelableExtra("selectedUser") ?: return
        title = selectedUser.name

        val name = findViewById<TextView>(R.id.nameView)
        var phone = findViewById<TextView>(R.id.phoneView)

        name.text = selectedUser.name
        phone.text = selectedUser.phoneNumber



        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }
    }


    private fun loadMessages() {
        val selectedUserPhoneNumber = selectedUser.phoneNumber
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber

        // Create a query to fetch messages for the current user
        val query = messagesReference
            .orderByChild("timestamp")
            .limitToLast(50)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allMessages = mutableListOf<Message>()

                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null && ((message.senderPhoneNumber == currentUserPhoneNumber || message.receiverPhoneNumber == currentUserPhoneNumber) && (message.senderPhoneNumber == selectedUserPhoneNumber || message.receiverPhoneNumber == selectedUserPhoneNumber))) {
                        allMessages.add(message)
                    }
                }

                // Sort the messages by timestamp in ascending order
                allMessages.sortBy { it.timestamp }

                // Replace the messageList with all messages for the selected user
                messageList.clear()
                messageList.addAll(allMessages)

                // Notify the adapter that the data has changed
                messageAdapter.notifyDataSetChanged()

                // Scroll to the last item (newest message)
                //recyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun sendMessage(messageText: String) {
        val apiKey = "AIzaSyDIT4LqKbetvcZAn1S6LclvMqbQpdsxqGk"

        val messageId = messagesReference.push().key ?: return

        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        getCurrentUserName(currentUserPhoneNumber) { currentUserName ->
            GlobalScope.launch(Dispatchers.Main) {
                val translatedMessage = withContext(Dispatchers.IO) {
                    translateMessage(messageText, selectedUser.language, apiKey)
                }

                // The callback provides the user's name once it's retrieved
                val message = Message(
                    messageId,
                    messageText,
                    translatedMessage,
                    currentUserPhoneNumber,
                    selectedUser.phoneNumber,
                    currentUserName,
                    selectedUser.name,
                    System.currentTimeMillis()
                )

                messagesReference.child(messageId)
                    .setValue(message)
                    .addOnSuccessListener {
                        loadMessages()
                    }
                    .addOnFailureListener {
                        // Handle failure if message saving fails
                    }
                loadMessages()
            }
        }
        loadMessages()
    }

    private fun getCurrentUserName(currentUserPhoneNumber: String?, callback: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersReference = database.reference.child("users")

        usersReference.child(currentUserPhoneNumber ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java)
                callback(userName ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occurred during the retrieval
                callback("")
            }
        })
    }
    private fun translateMessage(
        messageText: String,
        targetLanguage: String,
        apiKey: String
    ): String {
        val translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().service

        val translation = translate.translate(
            messageText,
            Translate.TranslateOption.targetLanguage(targetLanguage)
        )

        return translation.translatedText
    }



    override fun onStop() {
        super.onStop()
        // Stop loading messages when the activity is stopped
        messageHandler.removeCallbacks(messageRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop loading messages when the activity is destroyed
        messageHandler.removeCallbacks(messageRunnable)
    }

    override fun onDeleteClicked(message: Message) {
        val messageId = message.id
        messagesReference.child(messageId).removeValue()
        loadMessages()
    }

    override fun onCopyClicked(message: Message) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Message Text", message.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
        loadMessages()
    }
}
