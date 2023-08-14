package com.messenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UserAdapter(private var userList: List<User>, private val clickListener: UserClickListener) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        val newchat: View = itemView.findViewById(R.id.newchat)
    }

    interface UserClickListener {
        fun onUserClicked(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.nameTextView.text = currentUser.name
        holder.phoneTextView.text = currentUser.phoneNumber

        // Set click listener for each user item
        holder.itemView.setOnClickListener {
            clickListener.onUserClicked(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateData(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }

}
