package fr.fork_chan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import fr.fork_chan.ui.theme.Fork_chanTheme

// Data classes
data class User(val id: String, val name: String, val pseudo: String, val profileUrl: String)
data class Post(val id: String, val user: User, val imageUrl: String?, val description: String, var likes: Int, var comments: List<String>)

class FeedScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Fork_chanTheme {
                FeedContent()
            }
        }
    }
}

@Composable
fun FeedContent() {
    val sampleUser = User("1", "John Doe", "johnd", "https://example.com/profile.jpg")
    val posts = listOf(
        Post("1", sampleUser, "https://example.com/image1.jpg", "Beautiful sunset!", 12, listOf("Nice!", "Wow!")),
        Post("2", sampleUser, null, "No image, just thoughts!", 5, listOf("Great post!"))
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(posts.size) { index ->
            PostItem(post = posts[index])
        }
    }
}

@Composable
fun PostItem(post: Post) {
    var likes by remember { mutableStateOf(post.likes) }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(post.user.profileUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = post.user.pseudo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image (if available)
            post.imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description
            Text(text = post.description, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Like and Comment Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "❤ $likes",
                    modifier = Modifier.clickable { likes++ },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "💬 ${post.comments.size}", fontSize = 14.sp)
            }
        }
    }
}
