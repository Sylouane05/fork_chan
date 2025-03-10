package fr.fork_chan.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    // Appel du constructeur de Firebase pour pouvoir utiliser les méthodes d'authentification.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState: MutableLiveData<AuthState> =
        MutableLiveData(AuthState.Unauthenticated) // Initialisation

    val authState: LiveData<AuthState> =
        _authState // Accesseur (pour lire l'état de l'authentification)

    // Initialisation de l'état d'authentification (on lance checkStatus quoi)
    init {
        checkStatus()
    }
    // création de checkStatus pour vérifier l'état de l'authentification
    fun checkStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    //fonction pour ce login
    
    fun login(email : String, password : String) {
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
        //fonction pour créer un compte :
    }


    fun signup(email : String, password : String){
            if (email.isEmpty() || password.isEmpty()) {
                _authState.value = AuthState.Error("Email and password cannot be empty")
                return
            }

            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.message ?: "unknown error")
                    }
                }
        }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }








}
    sealed class AuthState {
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }