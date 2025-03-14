package fr.fork_chan.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _followers = MutableLiveData<List<Follow>>(emptyList())
    val followers: LiveData<List<Follow>> = _followers

    private val _following = MutableLiveData<List<Follow>>(emptyList())
    val following: LiveData<List<Follow>> = _following

    fun followUser(targetUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val query = firestore.collection("follows")
                    .whereEqualTo("followerId", currentUser.uid)
                    .whereEqualTo("followingId", targetUserId)
                    .get().await()
                if (query.isEmpty) {
                    val follow = Follow(
                        followerId = currentUser.uid,
                        followingId = targetUserId,
                        createdAt = Timestamp.now()
                    )
                    firestore.collection("follows").add(follow).await()
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val query = firestore.collection("follows")
                    .whereEqualTo("followerId", currentUser.uid)
                    .whereEqualTo("followingId", targetUserId)
                    .get().await()
                if (!query.isEmpty) {
                    val docId = query.documents.first().id
                    firestore.collection("follows").document(docId).delete().await()
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun fetchFollowers(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = firestore.collection("follows")
                    .whereEqualTo("followingId", userId)
                    .get().await()
                val list = query.documents.mapNotNull {
                    it.toObject(Follow::class.java)
                }
                _followers.postValue(list)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun fetchFollowing(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = firestore.collection("follows")
                    .whereEqualTo("followerId", userId)
                    .get().await()
                val list = query.documents.mapNotNull {
                    it.toObject(Follow::class.java)
                }
                _following.postValue(list)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun checkIfFollowing(targetUserId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val query = firestore.collection("follows")
                    .whereEqualTo("followerId", currentUser.uid)
                    .whereEqualTo("followingId", targetUserId)
                    .get().await()
                callback(!query.isEmpty)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
}
