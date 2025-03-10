package fr.fork_chan.activities

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fr.fork_chan.models.AuthState
import fr.fork_chan.models.AuthViewModel


@Composable
fun SignUp(modifier : Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel) {
    //Initialisation des variables :
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    val  context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value) {
            AuthState.Authenticated -> navController.navigate("feed")
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    //Affichage de la page :
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Sign up Page")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email,
            label = { Text("Email") },
            onValueChange = { email = it })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = password,
            label = { Text("Password") },
            onValueChange = { password = it })

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { authViewModel.signup(email,password) },){
            Text(text = "sign up")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("login") },){
            Text(text = "Already have an account ?")
        }

    }


}