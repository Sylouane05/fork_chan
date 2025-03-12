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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import fr.fork_chan.models.AuthState
import fr.fork_chan.models.AuthViewModel
import fr.fork_chan.models.PostViewModel

// --- New Composable to display a user's profile picture ---
@Composable
fun ProfilePicture(userId: String, size: Int = 40) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch profile image from Firestore (using similar logic as in UserProfilePage)
    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val imageBase64 = document.getString("profileImage")
                    if (!imageBase64.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            imageBitmap = bitmap.asImageBitmap()
                        } catch (e: Exception) {
                            errorMessage = "Failed to load profile image"
                        }
                    }
                }
                isLoading = false
            }
            .addOnFailureListener {
                errorMessage = "Failed to load user data"
                isLoading = false
            }
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size((size / 2).dp), strokeWidth = 2.dp)
        } else if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback icon if there is no profile image
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                modifier = Modifier.size((size * 0.8).dp),
                tint = Color.White
            )
        }
    }
}

// --- Modified FeedPage.kt ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPage(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel
) {
    val authState by authViewModel.authState.observeAsState()
    val posts by postViewModel.posts.observeAsState(emptyList())
    val comments by postViewModel.comments.observeAsState(emptyMap())

    // Track which posts have their comments section expanded
    val expandedCommentSections = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        postViewModel.fetchPosts()
    }

    // Fetch comments for all posts initially
    LaunchedEffect(posts) {
        posts.forEach { post ->
            postViewModel.fetchComments(post.id)
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ForkChan",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = { /* Navigate to messages */ }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Messages"
                        )
                    }
                    IconButton(onClick = { authViewModel.signout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_post") }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Post"
                )
            }
        }
    ) { paddingValues ->
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No posts yet",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your first post!",
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(all = 16.dp)
            ) {
                items(posts) { post ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Row for the author's profile picture and username
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Assumes that each post has a "userId" field.
                            ProfilePicture(userId = post.userId, size = 40)
                            Spacer(modifier = Modifier.width(8.dp))
                            // Display username (if available in your post model)
                            Text(
                                text = post.username,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Get expanded state for this post or default to false
                        val isExpanded = expandedCommentSections[post.id] ?: false

                        // Pass the comments for this post
                        val postComments = comments[post.id] ?: emptyList()

                        // Post item with comments
                        PostItem(
                            post = post,
                            comments = postComments,
                            onLikeClick = { postViewModel.likePost(post.id) },
                            onCommentClick = {
                                // Toggle comment section and fetch comments if expanding
                                val newExpandedState = !(expandedCommentSections[post.id] ?: false)
                                expandedCommentSections[post.id] = newExpandedState
                                if (newExpandedState) {
                                    postViewModel.fetchComments(post.id)
                                }
                            },
                            onProfileClick = { userId ->
                                navController.navigate("profile/$userId")
                            },
                            onAddComment = { comment ->
                                postViewModel.addComment(post.id, comment)
                                // Fetch updated comments immediately after adding
                                postViewModel.fetchComments(post.id)
                            },
                            postViewModel = postViewModel
                        )
                    }
                }
            }
        }
    }
}