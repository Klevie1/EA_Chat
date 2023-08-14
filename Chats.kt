package com.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Chats : AppCompatActivity(), UserAdapter.UserClickListener {

    private val database = FirebaseDatabase.getInstance()
    private val userReference: DatabaseReference = database.reference.child("users")
    private lateinit var currentUser: User
    private lateinit var userAdapter: UserAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        val recyclerViewUsers = findViewById<RecyclerView>(R.id.recyclerViewUsers)

        userAdapter = UserAdapter(emptyList(), this)
        recyclerViewUsers.adapter = userAdapter

//        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
//        val currentUser = findViewById<TextView>(R.id.usr)
//        currentUser.text = currentUserPhoneNumber

        val logoutButton = findViewById<ImageButton>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logout()
        }

//        val setting = findViewById<View>(R.id.settings)
//        setting.setOnClickListener {
//            navigateToSettings()
//        }
        val searchView = findViewById<SearchView>(R.id.search_bar)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform the search here (if needed)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform the search as the user types
                searchUsersByName(newText)
                return true
            }
        })


        loadCurrentUser()
        loadUsersFromFirebaseDatabase()
    }

    private fun searchUsersByName(nameQuery: String?) {
        if (nameQuery.isNullOrEmpty()) {
            loadUsersFromFirebaseDatabase()
        } else {
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.reference.child("users")
            usersRef.orderByChild("name")
                .startAt(nameQuery)
                .endAt(nameQuery + "\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val matchingUsers = mutableListOf<User>()
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            user?.let { matchingUsers.add(it) }
                        }
                        displayUsersInRecyclerView(matchingUsers)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }


    private fun loadCurrentUser() {
        // Get the current user from Firebase Authentication
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        currentUser = User(firebaseUser?.phoneNumber ?: "", firebaseUser?.displayName ?: "", "")
    }

    private fun loadUsersFromFirebaseDatabase() {
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val phone = userSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                    val language = userSnapshot.child("language").getValue(String::class.java) ?: ""
                    val user = User(phone, name, language)
                    if (user.phoneNumber != currentUser.phoneNumber) { // Exclude the current user from the list
                        userList.add(user)
                    }
                }
                Log.d("Chats", "UserList size: ${userList.size}") // Log the size of the userList
                displayUsersInRecyclerView(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Chats, "Error loading users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayUsersInRecyclerView(userList: List<User>) {
        val recyclerViewUsers = findViewById<RecyclerView>(R.id.recyclerViewUsers)
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        val userAdapter = UserAdapter(userList, this) // Pass 'this' as the UserClickListener
        recyclerViewUsers.adapter = userAdapter
        userAdapter.updateData(userList)
    }




    private fun logout() {
        // Check if the user is authenticated
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToSettings() {
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
        finish()
    }

    // Implement the UserAdapter.UserClickListener interface's method
    override fun onUserClicked(user: User) {
        // Handle user item click here
        val intent = Intent(this, Messages::class.java)
        intent.putExtra("selectedUser", user)
        startActivity(intent)
    }
}
