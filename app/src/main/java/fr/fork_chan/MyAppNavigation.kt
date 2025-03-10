package fr.fork_chan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.fork_chan.activities.LoginPage
import fr.fork_chan.activities.SignUp
import fr.fork_chan.activities.feedPage
import fr.fork_chan.models.AuthViewModel


@Composable
fun Navigation(modifier : Modifier = Modifier, AuthViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(modifier, navController, AuthViewModel()) }
        composable("signup") { SignUp(modifier, navController, AuthViewModel()) }
        composable("feed") { feedPage(modifier, navController, AuthViewModel()) }
    }
}