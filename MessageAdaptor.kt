package com.messenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MessageAdapter(private val messages: List<Message>, private val deleteClickListener: DeleteClickListener, private val copyClickListener: CopyClickListener) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    interface DeleteClickListener {
        fun onDeleteClicked(message: Message)
    }

    interface CopyClickListener {
        fun onCopyClicked(message: Message)
    }

    // ViewHolder for both sender and receiver messages
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val usr: TextView = itemView.findViewById(R.id.user)
        val time: TextView = itemView.findViewById(R.id.time)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val copyButton: ImageView = itemView.findViewById(R.id.copyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(itemView)
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.deleteButton.visibility = View.GONE
        holder.copyButton.visibility = View.GONE
        val message = messages[position]
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber

        holder.messageTextView.text = if (message.senderPhoneNumber == currentUserPhoneNumber) {
            message.text
        } else {
            message.receiverText
        }

        if (message.senderPhoneNumber == currentUserPhoneNumber) {
            holder.usr.text = "Me"
            holder.time.text = formatDate(message.timestamp)
            holder.usr.setTextColor(holder.itemView.resources.getColor(android.R.color.holo_blue_bright))
            holder.messageTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.black))
        } else if (message.receiverPhoneNumber == currentUserPhoneNumber) {
            holder.usr.text = message.senderName
            holder.time.text = formatDate(message.timestamp)
            holder.usr.setTextColor(holder.itemView.resources.getColor(android.R.color.holo_green_light))
            holder.messageTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.black))
        }

        // Set long-press listener to show delete button
        holder.itemView.setOnLongClickListener {
            holder.deleteButton.visibility = View.VISIBLE
            holder.copyButton.visibility = View.VISIBLE

            true
        }

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener {
            val messageToDelete = messages[holder.adapterPosition]
            deleteClickListener.onDeleteClicked(messageToDelete)
            holder.deleteButton.visibility = View.GONE
        }
        holder.copyButton.setOnClickListener {
            val messageToCopy = messages[holder.adapterPosition]
            copyClickListener.onCopyClicked(messageToCopy)
            holder.copyButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
