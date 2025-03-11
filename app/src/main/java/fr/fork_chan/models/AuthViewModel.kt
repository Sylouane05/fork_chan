package fr.fork_chan.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest


class AuthViewModel : ViewModel() {
    // Firebase Authentication instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState: MutableLiveData<AuthState> =
        MutableLiveData(AuthState.Unauthenticated) // Initialization

    val authState: LiveData<AuthState> =
        _authState // Accessor (to read the authentication state)

    // Initialize authentication state
    init {
        checkStatus()
    }

    // Check authentication status
    fun checkStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Login function
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    // Sign up function
    fun signup(email: String, password: String, username: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Set the user's display name
                    updateUsername(username) {
                        _authState.value = AuthState.Authenticated
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "unknown error")
                }
            }
    }

    // Sign out function
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    // Update username function
    fun updateUsername(username: String, onComplete: () -> Unit = {}) {
        val user = auth.currentUser
        if (user != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete()
                    }
                }
        }
    }

    // Get current user's display name
    fun getCurrentUsername(): String {
        return auth.currentUser?.displayName ?: ""
    }

    // Get current user's email
    fun getCurrentEmail(): String {
        return auth.currentUser?.email ?: ""
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}