package fr.fork_chan.activities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomsPage(navController: NavController) {
    val chatRooms = remember { mutableStateListOf<String>() }

    // Fetch chat rooms from Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("chat_rooms")
            .get()
            .addOnSuccessListener { result ->
                val rooms = result.documents.mapNotNull { it.id }
                chatRooms.clear()
                chatRooms.addAll(rooms)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Rooms", fontSize = 20.sp) },
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
            Button(
                onClick = { navController.navigate("create_chat_room") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Chat Room")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(chatRooms) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { navController.navigate("chat/$room") },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = room,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
