package fr.fork_chan.models

import com.google.firebase.Timestamp

data class Follow(
    val followerId: String = "",
    val followingId: String = "",
    val createdAt: Timestamp? = null
)
