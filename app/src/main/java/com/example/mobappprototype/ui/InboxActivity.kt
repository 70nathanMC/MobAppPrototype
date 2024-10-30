package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.InboxAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityInboxBinding
import com.example.mobappprototype.model.ChatRoom
import com.example.mobappprototype.model.LastMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging

import java.util.Date

private const val TAG = "InboxActivity"

class InboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInboxBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var inboxAdapter: InboxAdapter
    private val chatRooms = mutableListOf<ChatRoom>()
    private val messageListeners = mutableMapOf<String, ListenerRegistration>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: started") // Add this log

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        inboxAdapter = InboxAdapter(chatRooms)

        binding.rvInbox.layoutManager = LinearLayoutManager(this)
        binding.rvInbox.adapter = inboxAdapter

        binding.linearLayout6.post {
            val rect = Rect()
            binding.btnHome.getHitRect(rect)
            rect.inset(-50, -50) // Expand the touch area by 50 pixels on each side
            binding.linearLayout6.touchDelegate = TouchDelegate(rect, binding.btnHome)
        }

        fetchChatRooms()
        listenForNewMessages()
        updateFcmToken()

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            listenForUnreadCountChanges(currentUserId) // Start listening for unread count changes
        }

        binding.bottomNavigationBar.selectedItemId = R.id.messages

        binding.btnHome.setOnClickListener{
            val currentUserId = auth.currentUser?.uid ?: return@setOnClickListener
            firestoreDb.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        val intent = if (role == "Student") {
                            Intent(this, TutorListActivity::class.java)
                        } else {
                            Intent(this, TutorMainActivity::class.java)
                        }
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting user role: ", exception)
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    getCurrentUserRole { userRole ->
                        val intent = if (userRole == "Student") {
                            Intent(this, StudentMainActivity::class.java)
                        } else {
                            Intent(this, TutorMainActivity::class.java)
                        }
                        startActivity(intent)
                    }
                    true
                }
                R.id.messages -> {
                    true
                }
                R.id.profile -> {
                    getCurrentUserRole { userRole ->
                        val intent = if (userRole == "Student") {
                            Intent(this, StudentMainProfileActivity::class.java)
                        } else {
                            Intent(this, TutorMainProfileActivity::class.java)
                        }
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }

    }

    private fun getCurrentUserRole(callback: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        firestoreDb.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.getString("role") ?: "")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user role", exception)
                callback("")
            }
    }


    private fun fetchChatRooms() {
        Log.d(TAG, "fetchChatRooms: started")
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG, "fetchChatRooms: ${documents.size()} chat rooms found")
                    chatRooms.clear()
                    documents.forEach { document ->
                        val chatRoom = document.toObject(ChatRoom::class.java)
                        if (chatRoom != null) {
                            chatRooms.add(chatRoom)

                            // Add SnapshotListener for real-time last message updates
                            val listener = firestoreDb.collection("chats")
                                .document(chatRoom.meetingID)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener { snapshot, exception ->
                                    if (exception != null) {
                                        Log.e(TAG, "Error listening for new messages", exception)
                                        return@addSnapshotListener
                                    }
                                    if (snapshot != null && !snapshot.isEmpty) {
                                        val lastMessageContent = snapshot.documents[0].getString("content") ?: ""
                                        val lastMessage = LastMessage(content = lastMessageContent)
                                        val chatRoomIndex = chatRooms.indexOfFirst { it.meetingID == chatRoom.meetingID }
                                        if (chatRoomIndex != -1) {
                                            chatRooms[chatRoomIndex] = chatRooms[chatRoomIndex].copy(lastMessage = lastMessage)
                                            runOnUiThread {
                                                inboxAdapter.notifyItemChanged(chatRoomIndex)
                                            }
                                        }
                                    }
                                }
                            messageListeners[chatRoom.meetingID] = listener // Store the listener
                        }
                    }
                    runOnUiThread {
                        inboxAdapter.notifyDataSetChanged()
                    }
                    listenForUnreadCountChanges(currentUserId)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching chat rooms", exception)
                }
        }
    }

    private fun listenForUnreadCountChanges(userId: String) {
        firestoreDb.collection("users").document(userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening for unread count changes", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val lastSeenChats = snapshot.get("lastSeenChats") as? Map<String, Any>
                    if (lastSeenChats != null) {
                        for (chatRoom in chatRooms) {
                            val unreadCount = (lastSeenChats[chatRoom.meetingID] as? Map<String, Any>)?.get("unreadCount") as? Long ?: 0
                            chatRoom.unreadCount = unreadCount
                        }
                        runOnUiThread {
                            inboxAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private fun listenForNewMessages() {
        Log.d(TAG, "listenForNewMessages: started")
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e(TAG, "Error listening for new messages", exception)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        for (documentChange in snapshot.documentChanges) {
                            if (documentChange.type == DocumentChange.Type.MODIFIED)
                            {
                                val updatedChatRoom = documentChange.document.toObject(ChatRoom::class.java)
                                val chatRoomIndex = chatRooms.indexOfFirst { it.meetingID == updatedChatRoom.meetingID }
                                if (chatRoomIndex != -1) {
                                    // Update the chat room in the list and notify the adapter
                                    chatRooms[chatRoomIndex] = updatedChatRoom
                                    runOnUiThread {
                                        inboxAdapter.notifyItemChanged(chatRoomIndex)
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            val userId = auth.currentUser?.uid
            if (userId != null && token != null) {
                firestoreDb.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token updated successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error updating FCM token", exception)
                    }
            }
        }
    }
}