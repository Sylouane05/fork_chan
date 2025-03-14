package fr.fork_chan.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fr.fork_chan.activities.ProfilePicture
import java.util.*

// ✅ Updated ChatMessage model to include a timestamp
data class ChatMessage(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis() // ✅ Store timestamp manually
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomPage(navController: NavController, roomId: String) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var messageText by remember { mutableStateOf("") }
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ✅ Fetch messages and sort by timestamp
    LaunchedEffect(roomId) {
        firestore.collection("chat_rooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // 🔥 Ensure ordering
            .addSnapshotListener { snapshot, _ ->
                val messageList = mutableListOf<ChatMessage>()

                snapshot?.documents?.forEach { doc ->
                    val senderId = doc.getString("senderId") ?: "Unknown"
                    val text = doc.getString("text") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0 // 🔥 Use stored timestamp

                    firestore.collection("users").document(senderId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val senderName = userDoc.getString("username") ?: "Unknown"
                            messageList.add(ChatMessage(senderId, senderName, text, timestamp))

                            // ✅ Always sort messages by timestamp before updating UI
                            messages.clear()
                            messages.addAll(messageList.sortedBy { it.timestamp })
                        }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Room: $roomId", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(messages) { message ->
                    ChatBubble(message, userId)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Message") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    if (messageText.isNotEmpty() && userId.isNotEmpty()) {
                        val newMessage = mapOf(
                            "senderId" to userId,
                            "text" to messageText,
                            "timestamp" to System.currentTimeMillis() // ✅ Store timestamp manually
                        )

                        firestore.collection("chat_rooms")
                            .document(roomId)
                            .collection("messages")
                            .add(newMessage)

                        messageText = ""
                    }
                }) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

// ✅ Use imported `ProfilePicture`
@Composable
fun ChatBubble(message: ChatMessage, currentUserId: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.senderId == currentUserId) Arrangement.End else Arrangement.Start
    ) {
        if (message.senderId != currentUserId) {
            ProfilePicture(userId = message.senderId, size = 40)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .background(
                    if (message.senderId == currentUserId) Color.Blue else Color.Gray,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.senderName,
                fontSize = 12.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
