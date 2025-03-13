package fr.fork_chan.activities

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.fork_chan.models.AuthState
import fr.fork_chan.models.AuthViewModel
import fr.fork_chan.models.PostViewModel

enum class ProfileTab {
    POSTS, LIKES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePage(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    userId: String? = null
) {
    val authState = authViewModel.authState.observeAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userPosts by postViewModel.userPosts.observeAsState(emptyList())
    val comments by postViewModel.comments.observeAsState(emptyMap())

    // Determine if viewing own profile or someone else's
    val isOwnProfile = userId == null || userId == currentUser?.uid
    val profileUserId = userId ?: currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }

    // Username and email for display
    val username = remember { mutableStateOf(currentUser?.displayName ?: "Username") }
    val email = remember { mutableStateOf(currentUser?.email ?: "email@example.com") }

    // State for profile image
    val profileImageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Stats for display
    val postCount = userPosts.size
    val followerCount = 0 // Placeholder for future implementation
    val followingCount = 0 // Placeholder for future implementation

    // Load user posts
    LaunchedEffect(profileUserId) {
        if (profileUserId.isNotEmpty()) {
            postViewModel.fetchUserPosts(profileUserId)

            // Load profile image from Firestore
            isLoading.value = true
            FirebaseFirestore.getInstance().collection("users")
                .document(profileUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Update username and email if available
                        document.getString("username")?.let {
                            username.value = it
                        }
                        document.getString("email")?.let {
                            email.value = it
                        }

                        // Get profile image from base64
                        val imageBase64 = document.getString("profileImage")
                        if (!imageBase64.isNullOrEmpty()) {
                            try {
                                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                profileImageBitmap.value = bitmap.asImageBitmap()
                            } catch (e: Exception) {
                                errorMessage.value = "Failed to load profile image: ${e.message}"
                            }
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

    // Check authentication state
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isOwnProfile) "Mon Profil" else "User Profile") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Profile header section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (profileImageBitmap.value != null) {
                            // Display the base64 image
                            Image(
                                bitmap = profileImageBitmap.value!!,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Default icon if no image is available
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username
                    Text(
                        text = username.value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isOwnProfile) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Email
                        Text(
                            text = email.value,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }

                    // Display error message if any
                    if (errorMessage.value != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage.value!!,
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (isOwnProfile) {
                        // Own profile actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate("edit_profile") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Modifier le profil")
                            }

                            Button(
                                onClick = { navController.navigate("create_post") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Créer un Fork")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { authViewModel.signout() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Déconnexion")
                        }
                    } else {
                        // Other user profile actions
                        Button(
                            onClick = { /* Implement follow functionality */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Follow")
                        }
                    }
                }

                // Tabs for Posts and Likes
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == ProfileTab.POSTS,
                        onClick = { selectedTab = ProfileTab.POSTS },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Posts"
                            )
                        },
                        text = { Text("Mes Forks") }
                    )

                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            when (selectedTab) {
                ProfileTab.POSTS -> {
                    if (userPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No posts yet",
                                        color = Color.Gray,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(userPosts) { post ->
                            PostItem(
                                post = post,
                                comments = comments[post.id] ?: emptyList(),
                                onLikeClick = { postViewModel.likePost(post.id) },
                                onCommentClick = { postViewModel.fetchComments(post.id) },
                                onProfileClick = { navController.navigate("profile/$it") },
                                onAddComment = { comment ->
                                    postViewModel.addComment(post.id, comment)
                                },
                                postViewModel = postViewModel
                            )
                        }
                    }
                }
                ProfileTab.LIKES -> {
                    // Show liked posts (future implementation)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No liked posts yet",
                                    color = Color.Gray,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}