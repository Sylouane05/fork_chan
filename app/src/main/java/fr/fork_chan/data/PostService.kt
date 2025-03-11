package fr.fork_chan.data

// Par exemple, si tu as une data class Post :
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.fork_chan.models.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PostService {

    private val postsRef = FirebaseDatabase.getInstance().getReference("posts")

    fun createPost(userId: String, description: String, imageUrls: List<String>?) {
        val postId = postsRef.push().key ?: return
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = sdf.format(Date(currentTime))

        val newPost = Post(postId, userId, description, formattedDate, 0, imageUrls)

        postsRef.child(postId).setValue(newPost)
            .addOnSuccessListener {
                Log.d("PostService", "Post créé avec succès")
            }
            .addOnFailureListener { exception ->
                Log.e("PostService", "Erreur lors de la création du post", exception)
            }
    }

    fun fetchPosts(onPostsFetched: (List<Post>) -> Unit) {
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postsList = mutableListOf<Post>()
                for (child in snapshot.children) {
                    val post = child.getValue(Post::class.java)
                    if (post != null) {
                        postsList.add(post)
                    }
                }
                onPostsFetched(postsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostService", "Erreur lors de la récupération des posts", error.toException())
                onPostsFetched(emptyList())
            }
        })
    }

    // Ajoute d'autres fonctions (update, delete, etc.) si nécessaire
}
