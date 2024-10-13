package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.databinding.ItemMessageReceivedBinding
import com.example.mobappprototype.databinding.ItemMessageSentBinding
import com.example.mobappprototype.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val VIEW_TYPE_MESSAGE_SENT = 1
private const val VIEW_TYPE_MESSAGE_RECEIVED = 2

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        firestoreDb = FirebaseFirestore.getInstance()
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        auth = FirebaseAuth.getInstance()
        return if (message.senderUID == auth.currentUser?.uid) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.textMessageBody.text = message.content

            // Fetch sender's information
            firestoreDb.collection("users").document(message.senderUID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val profilePicUrl = document.getString("profilePic")
                        val senderFullName = document.getString("firstName") + " " + document.getString("lastName")
                        binding.textMessageName.text = senderFullName
                        Glide.with(itemView.context).load(profilePicUrl).into(binding.imageMessageProfile)
                    }
                }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.textMessageBody.text = message.content

            // Fetch receiver's information
            firestoreDb.collection("users").document(message.senderUID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val profilePicUrl = document.getString("profilePic")
                        val senderFullName = document.getString("firstName") + " " + document.getString("lastName")
                        binding.textMessageName.text = senderFullName
                        Glide.with(itemView.context).load(profilePicUrl).into(binding.imageMessageProfile)
                    }
                }
        }
    }
}