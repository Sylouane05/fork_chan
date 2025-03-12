package fr.fork_chan

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.fork_chan.activities.CreatePostPage
import fr.fork_chan.activities.EditProfilePage
import fr.fork_chan.activities.FeedPage
import fr.fork_chan.activities.LoginPage
import fr.fork_chan.activities.SignUp
import fr.fork_chan.activities.UserProfilePage
import fr.fork_chan.models.AuthViewModel
import fr.fork_chan.models.PostViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authViewModel = viewModel<AuthViewModel>() // Use viewModel() for proper lifecycle
    val postViewModel = viewModel<PostViewModel>() // Share a single PostViewModel instance

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(modifier, navController, authViewModel) }
        composable("signup") { SignUp(modifier, navController, authViewModel) }
        composable("feed") { FeedPage(navController, authViewModel, postViewModel) }
        composable("profile") { UserProfilePage(navController, authViewModel, postViewModel) }
        composable("edit_profile") { EditProfilePage(navController) }
        composable("create_post") { CreatePostPage(navController, postViewModel) } // Added CreatePostPage
    }
}