package fr.fork_chan.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp


data class Comment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicUrl: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Post(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicUrl: String = "",
    val description: String = "",
    val imageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0
)

data class Like(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = ""
)