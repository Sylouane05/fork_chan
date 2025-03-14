package fr.fork_chan.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatRoom(navController: NavController) {
    var roomName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Chat Room") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern styled text field
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Chat Room Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )

            // Error message if applicable
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button with loading indicator
            Button(
                onClick = {
                    if (roomName.isNotEmpty()) {
                        isLoading = true
                        errorMessage = null
                        FirebaseFirestore.getInstance().collection("chat_rooms")
                            .document(roomName)
                            .set(mapOf("created" to System.currentTimeMillis()))
                            .addOnSuccessListener {
                                isLoading = false
                                navController.navigate("chat/$roomName")
                            }
                            .addOnFailureListener {
                                isLoading = false
                                errorMessage = "Failed to create room. Try again."
                            }
                    } else {
                        errorMessage = "Room name cannot be empty"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Room")
                }
            }
        }
    }
}