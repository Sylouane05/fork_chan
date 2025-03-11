package fr.fork_chan

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.fork_chan.activities.EditProfilePage
import fr.fork_chan.activities.FeedPage
import fr.fork_chan.activities.LoginPage
import fr.fork_chan.activities.SignUp
import fr.fork_chan.activities.UserProfilePage
import fr.fork_chan.models.AuthViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun Navigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authViewModel = AuthViewModel() // Create a single instance to share across screens

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(modifier, navController, authViewModel) }
        composable("signup") { SignUp(modifier, navController, authViewModel) }
        composable("feed") { FeedPage(navController, authViewModel) }
        composable("profile") { UserProfilePage(navController, authViewModel) }
        composable("edit_profile") { EditProfilePage(navController, authViewModel) }
    }
}