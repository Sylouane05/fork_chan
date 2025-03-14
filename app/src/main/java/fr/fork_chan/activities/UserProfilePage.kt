package fr.fork_chan.activities

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.fork_chan.models.AuthState
import fr.fork_chan.models.AuthViewModel
import fr.fork_chan.models.FollowViewModel
import fr.fork_chan.models.PostViewModel
import fr.fork_chan.models.Post
import fr.fork_chan.models.Comment

enum class ProfileTab {
    POSTS, FOLLOWERS, FOLLOWING
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

    // Initialize FollowViewModel for follow functionality
    val followViewModel: FollowViewModel = viewModel()

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

    // State for follow button
    var isFollowing by remember { mutableStateOf(false) }

    // Load user posts, profile image, and follow status
    LaunchedEffect(profileUserId) {
        if (profileUserId.isNotEmpty()) {
            postViewModel.fetchUserPosts(profileUserId)
            followViewModel.fetchFollowers(profileUserId)
            followViewModel.fetchFollowing(profileUserId)
            if (!isOwnProfile) {
                followViewModel.checkIfFollowing(profileUserId) { following ->
                    isFollowing = following
                }
            }
            isLoading.value = true
            FirebaseFirestore.getInstance().collection("users")
                .document(profileUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.getString("username")?.let { username.value = it }
                        document.getString("email")?.let { email.value = it }
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

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isOwnProfile) "Mon Profil" else "Profil du fourcheur") },
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
        ) {
            // Profile Header
            ProfileHeader(
                isLoading = isLoading.value,
                profileImageBitmap = profileImageBitmap.value,
                username = username.value,
                email = email.value,
                isOwnProfile = isOwnProfile,
                isFollowing = isFollowing,
                onFollowClick = {
                    if (isFollowing) {
                        followViewModel.unfollowUser(profileUserId)
                        isFollowing = false
                    } else {
                        followViewModel.followUser(profileUserId)
                        isFollowing = true
                    }
                },
                onEditProfileClick = { navController.navigate("edit_profile") },
                onCreatePostClick = { navController.navigate("create_post") },
                onSignOutClick = { authViewModel.signout() },
                errorMessage = errorMessage.value
            )

            // Tabs
            ProfileTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    ProfileTab.POSTS -> PostsList(
                        userPosts = userPosts,
                        comments = comments,
                        postViewModel = postViewModel,
                        navController = navController
                    )
                    ProfileTab.FOLLOWERS -> FollowersList(
                        followViewModel = followViewModel,
                        navController = navController
                    )
                    ProfileTab.FOLLOWING -> FollowingList(
                        followViewModel = followViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    isLoading: Boolean,
    profileImageBitmap: ImageBitmap?,
    username: String,
    email: String,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCreatePostClick: () -> Unit,
    onSignOutClick: () -> Unit,
    errorMessage: String?
) {
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (profileImageBitmap != null) {
                Image(
                    bitmap = profileImageBitmap,
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

        Text(
            text = username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (isOwnProfile) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = email,
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Own profile actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditProfileClick,
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
                    onClick = onCreatePostClick,
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
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Déconnexion")
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onFollowClick) {
                Text(if (isFollowing) "Se défourcher" else "Enfourcher")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color.Red
            )
        }
    }
}

@Composable
fun ProfileTabs(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == ProfileTab.POSTS,
            onClick = { onTabSelected(ProfileTab.POSTS) },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Posts") },
            text = { Text("Forks") }
        )
        Tab(
            selected = selectedTab == ProfileTab.FOLLOWERS,
            onClick = { onTabSelected(ProfileTab.FOLLOWERS) },
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Followers") },
            text = { Text("Forketteurs") }
        )
        Tab(
            selected = selectedTab == ProfileTab.FOLLOWING,
            onClick = { onTabSelected(ProfileTab.FOLLOWING) },
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Following") },
            text = { Text("Forkettement") }
        )
    }
}

@Composable
fun PostsList(
    userPosts: List<Post>,
    comments: Map<String, List<Comment>>,
    postViewModel: PostViewModel,
    navController: NavHostController
) {
    LazyColumn {
        if (userPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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
                            text = "Pas encore de Fork!",
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
                    onUnlikeClick = { postViewModel.unlikePost(post.id) },
                    onCommentClick = { postViewModel.fetchComments(post.id) },
                    onProfileClick = { navController.navigate("profile/$it") },
                    onAddComment = { comment -> postViewModel.addComment(post.id, comment) },
                    postViewModel = postViewModel
                )
            }
        }
    }
}

@Composable
fun FollowersList(
    followViewModel: FollowViewModel,
    navController: NavHostController
) {
    val followersList by followViewModel.followers.observeAsState(emptyList())
    LazyColumn {
        if (followersList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun Forketteurs trouvé", color = Color.Gray, fontSize = 18.sp)
                }
            }
        } else {
            items(followersList) { follow ->
                FollowListItem(userId = follow.followerId) { selectedUserId ->
                    navController.navigate("profile/$selectedUserId")
                }
            }
        }
    }
}

@Composable
fun FollowingList(
    followViewModel: FollowViewModel,
    navController: NavHostController
) {
    val followingList by followViewModel.following.observeAsState(emptyList())
    LazyColumn {
        if (followingList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun forkettement trouvé", color = Color.Gray, fontSize = 18.sp)
                }
            }
        } else {
            items(followingList) { follow ->
                FollowListItem(userId = follow.followingId) { selectedUserId ->
                    navController.navigate("profile/$selectedUserId")
                }
            }
        }
    }
}

@Composable
fun FollowListItem(userId: String, onClick: (String) -> Unit) {
    var username by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch the username from Firestore when userId changes
    LaunchedEffect(userId) {
        isLoading = true
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fetchedUsername = document.getString("username")
                    username = fetchedUsername ?: "Utilisateur inconnu"
                } else {
                    errorMessage = "Utilisateur pas trouvé"
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to load user: ${e.message}"
                isLoading = false
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(userId) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfilePicture(userId = userId, size = 40)
        Spacer(modifier = Modifier.width(8.dp))
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
            else -> {
                Text(
                    text = username ?: "Unknown User",
                    fontSize = 16.sp
                )
            }
        }
    }
}