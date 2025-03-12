package fr.fork_chan.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class PostState {
    data object Idle : PostState()
    data object Loading : PostState()
    data object Success : PostState()
    data class Error(val message: String) : PostState()
}

class PostViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _postState = MutableLiveData<PostState>(PostState.Idle)

    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val posts: LiveData<List<Post>> = _posts

    private val _userPosts = MutableLiveData<List<Post>>(emptyList())
    val userPosts: LiveData<List<Post>> = _userPosts

    private val _comments = MutableLiveData<Map<String, List<Comment>>>(emptyMap())
    val comments: LiveData<Map<String, List<Comment>>> = _comments

    fun createPost(description: String, imageBase64: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _postState.postValue(PostState.Loading)
                val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

                // Create the post using the base64 image string
                // (You could rename the Post field from imageUrl to imageBase64 if you prefer.)
                val post = Post(
                    userId = currentUser.uid,
                    username = currentUser.displayName ?: "Anonymous",
                    userProfilePicUrl = currentUser.photoUrl?.toString() ?: "",
                    description = description,
                    imageUrl = imageBase64 ?: "" // storing base64 string here
                )

                // Save to Firestore
                firestore.collection("posts").add(post).await()

                _postState.postValue(PostState.Success)

                // Refresh posts
                fetchPosts()
            } catch (e: Exception) {
                _postState.postValue(PostState.Error(e.message ?: "Unknown error"))
            }
        }
    }


    fun fetchPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val postsSnapshot = firestore.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val postsList = postsSnapshot.documents.mapNotNull {
                    it.toObject(Post::class.java)
                }

                _posts.postValue(postsList)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchUserPosts(userId: String = auth.currentUser?.uid ?: "") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val postsSnapshot = firestore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val postsList = postsSnapshot.documents.mapNotNull {
                    it.toObject(Post::class.java)
                }

                _userPosts.postValue(postsList)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

                // Check if already liked
                val likeDoc = firestore.collection("likes")
                    .whereEqualTo("postId", postId)
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                if (likeDoc.isEmpty) {
                    // Add like
                    val like = Like(
                        postId = postId,
                        userId = currentUser.uid
                    )

                    firestore.collection("likes").add(like).await()

                    // Update post like count
                    val postRef = firestore.collection("posts").document(postId)
                    firestore.runTransaction { transaction ->
                        val post = transaction.get(postRef).toObject(Post::class.java)
                        post?.let {
                            transaction.update(postRef, "likeCount", it.likeCount + 1)
                        }
                    }.await()
                } else {
                    // Remove like
                    val likeId = likeDoc.documents.first().id
                    firestore.collection("likes").document(likeId).delete().await()

                    // Update post like count
                    val postRef = firestore.collection("posts").document(postId)
                    firestore.runTransaction { transaction ->
                        val post = transaction.get(postRef).toObject(Post::class.java)
                        post?.let {
                            transaction.update(postRef, "likeCount", it.likeCount - 1)
                        }
                    }.await()
                }

                // Refresh posts
                fetchPosts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

                val comment = Comment(
                    postId = postId,
                    userId = currentUser.uid,
                    username = currentUser.displayName ?: "Anonymous",
                    userProfilePicUrl = currentUser.photoUrl?.toString() ?: "",
                    text = commentText
                )

                // Add comment
                firestore.collection("comments").add(comment).await()

                // Update post comment count
                val postRef = firestore.collection("posts").document(postId)
                firestore.runTransaction { transaction ->
                    val post = transaction.get(postRef).toObject(Post::class.java)
                    post?.let {
                        transaction.update(postRef, "commentCount", it.commentCount + 1)
                    }
                }.await()

                // Refresh comments for the post
                fetchComments(postId)
                // Also refresh posts to update the comment count
                fetchPosts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchComments(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val commentsSnapshot = firestore.collection("comments")
                    .whereEqualTo("postId", postId)
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val commentsList = commentsSnapshot.documents.mapNotNull {
                    it.toObject(Comment::class.java)
                }

                _comments.postValue(_comments.value?.toMutableMap().apply {
                    this?.put(postId, commentsList)
                })
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun checkIfUserLikedPost(postId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

                val likeDoc = firestore.collection("likes")
                    .whereEqualTo("postId", postId)
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                callback(!likeDoc.isEmpty)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
}