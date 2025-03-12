package fr.fork_chan.activities

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import fr.fork_chan.models.Comment
import fr.fork_chan.models.Post
import fr.fork_chan.models.PostViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostItem(
    post: Post,
    comments: List<Comment>,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAddComment: (String) -> Unit,
    postViewModel: PostViewModel
) {
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }

    // Check if user has liked this post
    LaunchedEffect(post.id) {
        postViewModel.checkIfUserLikedPost(post.id) { liked ->
            isLiked = liked
        }
    }

    // Update showComments if there are new comments
    LaunchedEffect(comments) {
        if (comments.isNotEmpty()) {
            showComments = true
        }
    }

    // Create a remembered state for the decoded bitmap
    val decodedBitmap = remember(post.imageUrl) {
        if (!post.imageUrl.startsWith("http", ignoreCase = true) && post.imageUrl.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(post.imageUrl, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null // Return null if decoding fails
            }
        } else {
            null // Return null for remote URLs or empty strings
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // -- Post header with user info --
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { onProfileClick(post.userId) },
                    contentAlignment = Alignment.Center
                ) {
                    if (post.userProfilePicUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(post.userProfilePicUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    post.createdAt?.let { timestamp ->
                        val date = timestamp.toDate()
                        val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                        Text(
                            text = formatter.format(date),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = { /* Show options menu */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -- Post description --
            if (post.description.isNotEmpty()) {
                Text(
                    text = post.description,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // -- Post image --
            if (post.imageUrl.isNotEmpty()) {
                // Handle remote URL
                if (post.imageUrl.startsWith("http", ignoreCase = true)) {
                    // Use Coil to load remote URL
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(data = post.imageUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (decodedBitmap != null) {
                    // Handle base64 image if successfully decoded
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // -- Post stats (likes & comments) --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Likes",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${post.likeCount} likes",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${comments.size} comments", // Use actual comment count from the list
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // -- Action buttons --
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like button
                TextButton(
                    onClick = { onLikeClick() },
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Like",
                        color = if (isLiked) Color.Red else Color.Gray
                    )
                }

                // Comment button
                TextButton(
                    onClick = {
                        showComments = !showComments
                        if (showComments) {
                            onCommentClick()
                        }
                    },
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Comment",
                        color = Color.Gray
                    )
                }

                // Share button
                TextButton(
                    onClick = { /* Implement share if needed */ },
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        color = Color.Gray
                    )
                }
            }

            // -- Comments section --
            AnimatedVisibility(visible = showComments) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Display existing comments (if any)
                    if (comments.isEmpty()) {
                        Text(
                            text = "No comments yet. Be the first to comment!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // Show all comments
                        comments.forEach { comment ->
                            CommentItem(comment = comment)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add comment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Add a comment...") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onAddComment(commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Comment"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (comment.userProfilePicUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(comment.userProfilePicUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Visible
                )
            }

            comment.createdAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
                Text(
                    text = formatter.format(date),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}