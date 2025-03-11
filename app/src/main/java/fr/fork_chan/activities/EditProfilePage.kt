package fr.fork_chan.activities

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import fr.fork_chan.models.AuthViewModel
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(navController: NavHostController, authViewModel: AuthViewModel) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    // State for user information
    val username = remember { mutableStateOf(currentUser?.displayName ?: "") }
    val email = remember { mutableStateOf(currentUser?.email ?: "") }
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    // State for loading and errors
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // State for showing dialogs
    val showChangeEmailDialog = remember { mutableStateOf(false) }
    val showChangePasswordDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Username field
            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current email display
            OutlinedTextField(
                value = email.value,
                onValueChange = { },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Update username button
            Button(
                onClick = { updateUsername(username.value, context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading.value && username.value.isNotEmpty()
            ) {
                Text("Update Username")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Change email button
            Button(
                onClick = { showChangeEmailDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Email")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Change password button
            Button(
                onClick = { showChangePasswordDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Password")
            }

            if (errorMessage.value != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage.value!!,
                    color = Color.Red
                )
            }

            if (isLoading.value) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }

    // Change Email Dialog
    if (showChangeEmailDialog.value) {
        var newEmail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangeEmailDialog.value = false },
            title = { Text("Change Email") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("New Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateEmail(newEmail, password, context) {
                            email.value = newEmail
                            showChangeEmailDialog.value = false
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog.value) {
        var currentPwd by remember { mutableStateOf("") }
        var newPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog.value = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPwd == confirmPwd) {
                            updatePassword(currentPwd, newPwd, context) {
                                showChangePasswordDialog.value = false
                            }
                        } else {
                            Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Function to update username
private fun updateUsername(newUsername: String, context: android.content.Context) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null && newUsername.isNotEmpty()) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Function to update email
private fun updateEmail(newEmail: String, password: String, context: android.content.Context, onSuccess: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null && user.email != null) {
        // Re-authenticate user before changing email
        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Email update
                user.updateEmail(newEmail)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Email updated successfully", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Function to update password
private fun updatePassword(currentPassword: String, newPassword: String, context: android.content.Context, onSuccess: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null && user.email != null) {
        // Re-authenticate user before changing password
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Password update
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}