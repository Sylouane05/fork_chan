package fr.fork_chan.activities

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
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
    val allPosts by postViewModel.posts.observeAsState(emptyList())
    val comments by postViewModel.comments.observeAsState(emptyMap())

    // Determine if viewing own profile or someone else's
    val isOwnProfile = userId == null || userId == currentUser?.uid
    val profileUserId = userId ?: currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }

    // Username and email for display
    val username = remember { mutableStateOf(currentUser?.displayName ?: "Username") }
    val email = remember { mutableStateOf(currentUser?.email ?: "email@example.com") }

    // Stats for display
    val postCount = userPosts.size
    val followerCount = 0 // Placeholder for future implementation
    val followingCount = 0 // Placeholder for future implementation

    // Load user posts
    LaunchedEffect(profileUserId) {
        if (profileUserId.isNotEmpty()) {
            postViewModel.fetchUserPosts(profileUserId)
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
                title = { Text(if (isOwnProfile) "My Profile" else "User Profile") },
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
                        val profilePic = currentUser?.photoUrl?.toString()
                        if (!profilePic.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(profilePic),
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats row
                    //Row(
                        //modifier = Modifier.fillMaxWidth(),
                        //horizontalArrangement = Arrangement.SpaceEvenly
/*) {
    ProfileStat(label = "Posts", count = postCount.toString())
    ProfileStat(label = "Followers", count = followerCount.toString())
    ProfileStat(label = "Following", count = followingCount.toString())
}*/

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
            Text("Edit Profile")
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
            Text("New Post")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = { authViewModel.signout() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign Out")
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
    text = { Text("Posts") }
)
Tab(
    selected = selectedTab == ProfileTab.LIKES,
    onClick = { selectedTab = ProfileTab.LIKES },
    icon = {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Likes"
        )
    },
    text = { Text("Likes") }
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