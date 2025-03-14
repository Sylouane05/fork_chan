package fr.fork_chan.activities

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

/**
 * Fonction qui utilise FirebaseAuth pour envoyer l'email de réinitialisation.
 */
fun resetPassword(
    email: String,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        .addOnSuccessListener {
            Log.d("ResetPassword", "Email de réinitialisation envoyé avec succès")
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e("ResetPassword", "Erreur lors de l'envoi de l'email de réinitialisation", exception)
            onFailure(exception)
        }
}

@Composable
fun ForgotPasswordScreen(
    onEmailSent: () -> Unit,
    onError: (String) -> Unit,
    navController: NavHostController
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Contenu de la page
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Réinitialiser le mot de passe",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isLoading = true
                resetPassword(email,
                    onSuccess = {
                        isLoading = false
                        message = "Email envoyé ! Veuillez vérifier votre boîte mail."
                        onEmailSent()
                        // Par exemple, naviguer vers la page de connexion après succès :
                        navController.navigate("login")
                    },
                    onFailure = { exception ->
                        isLoading = false
                        message = exception.message ?: "Erreur inconnue"
                        onError(message)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank()
        ) {
            Text(text = if (isLoading) "Envoi en cours..." else "Envoyer l'email")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }
}
