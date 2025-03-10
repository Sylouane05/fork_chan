package fr.fork_chan.activities


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fr.fork_chan.models.AuthViewModel
import fr.fork_chan.models.AuthState

@Composable
fun feedPage(modifier : Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel){
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    TextButton(onClick = { authViewModel.signout() }, enabled = true) {
        Text(modifier = Modifier.padding(10.dp), text = "Signout", color = Color.Red)
    }
}