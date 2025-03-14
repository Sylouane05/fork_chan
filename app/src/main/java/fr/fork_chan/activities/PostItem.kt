package fr.fork_chan.activities

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import fr.fork_chan.models.Comment
import fr.fork_chan.models.Post
import fr.fork_chan.models.PostViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PostItem(
    post: Post,
    comments: List<Comment>,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAddComment: (String) -> Unit,
    postViewModel: PostViewModel
) {
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    // Flag to prevent the asynchronous like check from overriding the optimistic update.
    var likeClicked by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Check if the user has liked this post initially.
    LaunchedEffect(post.id) {
        postViewModel.checkIfUserLikedPost(post.id) { liked ->
            if (!likeClicked) {
                isLiked = liked
            }
        }
    }

    LaunchedEffect(comments) {
        if (comments.isNotEmpty()) {
            showComments = true
        }
    }

    val decodedBitmap = remember(post.imageUrl) {
        if (!post.imageUrl.startsWith("http", ignoreCase = true) && post.imageUrl.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(post.imageUrl, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // -- Post header --
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.clickable { onProfileClick(post.userId) }) {
                    ProfilePicture(userId = post.userId, size = 40)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
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
                if (post.userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer le post"
                        )
                    }
                }
                IconButton(onClick = { /* Options menu can be added here */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -- Post description --
            if (post.description.isNotEmpty()) {
                Text(text = post.description, modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // -- Post image --
            if (post.imageUrl.isNotEmpty()) {
                if (post.imageUrl.startsWith("http", ignoreCase = true)) {
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
                    text = "${comments.size} commentaires",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // -- Action buttons --
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like button with optimistic UI update.
                TextButton(
                    onClick = {
                        if (!isLiked) {
                            isLiked = true
                            likeClicked = true
                            onLikeClick()
                        } else {
                            isLiked = false
                            likeClicked = true
                            onUnlikeClick()
                        }
                    },
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

                // Comment button.
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
                        text = "Commentaires",
                        color = Color.Gray
                    )
                }
            }

            // -- Comments section --
            AnimatedVisibility(visible = showComments) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (comments.isEmpty()) {
                        Text(
                            text = "Pas de commentaires pour l'instant! Soyez le premier à commenter!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        comments.forEach { comment ->
                            CommentItem(comment = comment)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Ajouter un commentaire...") },
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
                                contentDescription = "Envoyer"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le post") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce post ?") },
            confirmButton = {
                Button(
                    onClick = {
                        postViewModel.deletePost(post.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annuler")
                }
            }
        )
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
        ProfilePicture(userId = comment.userId, size = 32)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(comment.username)
                        append(": ")
                    }
                    append(comment.text)
                },
                fontSize = 14.sp
            )
            comment.createdAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = SimpleDateFormat("dd MMM • HH:mm", Locale.FRENCH)
                Text(
                    text = formatter.format(date),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}