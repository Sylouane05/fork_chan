package fr.fork_chan.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // State for user information
    val username = remember { mutableStateOf(currentUser?.displayName ?: "") }
    val email = remember { mutableStateOf(currentUser?.email ?: "") }
    val photoUrl = remember { mutableStateOf("") }
    val profileImageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    // State for loading and errors
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // State for showing dialogs
    val showChangeEmailDialog = remember { mutableStateOf(false) }
    val showChangePasswordDialog = remember { mutableStateOf(false) }

    // Coroutine scope for asynchronous operations
    val scope = rememberCoroutineScope()

    // Load existing profile image if available
    LaunchedEffect(Unit) {
        isLoading.value = true
        currentUser?.uid?.let { uid ->
            // Check if the user document exists, create if it doesn't
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Create user document if it doesn't exist
                        val initialUserData = hashMapOf(
                            "username" to (currentUser.displayName ?: ""),
                            "email" to (currentUser.email ?: "")
                        )
                        firestore.collection("users")
                            .document(uid)
                            .set(initialUserData)
                    }
                    // Continue with loading profile image if document exists
                    val imageBase64 = document.getString("profileImage")
                    if (!imageBase64.isNullOrEmpty()) {
                        try {
                            val bitmap = base64ToBitmap(imageBase64)
                            profileImageBitmap.value = bitmap.asImageBitmap()
                            photoUrl.value = "base64_image"
                        } catch (e: Exception) {
                            errorMessage.value = "Failed to load profile image: ${e.message}"
                        }
                    }
                    isLoading.value = false
                }
                .addOnFailureListener { e ->
                    errorMessage.value = "Failed to load user data: ${e.message}"
                    isLoading.value = false
                }
        }
    }

    // Launcher for image picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    isLoading.value = true
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                        inputStream?.use { stream ->
                            // Convert to bitmap
                            val originalBitmap = BitmapFactory.decodeStream(stream)

                            // Resize bitmap to reduce size
                            val resizedBitmap = resizeBitmap(originalBitmap, 500)

                            // Convert to base64
                            val base64Image = bitmapToBase64(resizedBitmap)

                            // Save to Firestore
                            currentUser?.uid?.let { uid ->
                                firestore.collection("users")
                                    .document(uid)
                                    .update("profileImage", base64Image)
                                    .addOnSuccessListener {
                                        profileImageBitmap.value = resizedBitmap.asImageBitmap()
                                        photoUrl.value = "base64_image" // Just a placeholder
                                        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                        isLoading.value = false
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage.value = "Failed to update profile: ${e.message}"
                                        isLoading.value = false
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage.value = "Error: ${e.message}"
                        isLoading.value = false
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil") },
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
            // Profile picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable(enabled = !isLoading.value) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageBitmap.value != null) {
                    androidx.compose.foundation.Image(
                        bitmap = profileImageBitmap.value!!,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (photoUrl.value.isNotEmpty() && photoUrl.value != "base64_image") {
                    // For backward compatibility if you still have URLs stored
                    AsyncImage(
                        model = photoUrl.value,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Username field
            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Pseudo") },
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
                onClick = {
                    updateUsername(username.value, context)
                    // Also update username in Firestore
                    currentUser?.uid?.let { uid ->
                        firestore.collection("users")
                            .document(uid)
                            .update("username", username.value)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading.value && username.value.isNotEmpty()
            ) {
                Text("Mettre à jour le pseudo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Change email button
            Button(
                onClick = { showChangeEmailDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Modifier l'email")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Change password button
            Button(
                onClick = { showChangePasswordDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Modifier le mot de passe")
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
            title = { Text("Modifier votre email") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Nouveau email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe actuel") },
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

                            // Update email in Firestore
                            currentUser?.uid?.let { uid ->
                                firestore.collection("users")
                                    .document(uid)
                                    .update("email", newEmail)
                            }
                        }
                    }
                ) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog.value = false }) {
                    Text("Annuler")
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
            title = { Text("Modifier le mot de passe") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("Mot de passe actuel") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("Nouveau mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("Confirmer le nouveau mot de passe") },
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
                ) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog.value = false }) {
                    Text("Annuler")
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
                Toast.makeText(context, "Pseudo mis à jour avec succès", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Echec de la mise à jour du pseudo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Function to update email
private fun updateEmail(newEmail: String, password: String, context: android.content.Context, onSuccess: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null && user.email != null) {
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Email de vérification envoyé à $newEmail. Veuillez confirmer le nouveau email.",
                            Toast.LENGTH_LONG
                        ).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Echec de la mise à jour de l'email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Authentication echoué: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Function to update password
private fun updatePassword(currentPassword: String, newPassword: String, context: android.content.Context, onSuccess: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null && user.email != null) {
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show()
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

// Helper function to resize bitmap
private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val ratio = maxDimension.toFloat() / maxOf(width, height)

    return if (ratio < 1) {
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
        bitmap
    }
}

// Helper function to convert bitmap to Base64 string
private fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    // Compress with quality 70% to reduce size
    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Helper function to convert Base64 string to bitmap
private fun base64ToBitmap(base64String: String): Bitmap {
    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}