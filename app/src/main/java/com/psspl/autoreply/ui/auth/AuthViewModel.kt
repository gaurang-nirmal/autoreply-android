package com.psspl.autoreply.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity-scoped ViewModel that exposes the root authentication state.
 *
 * Observed in [com.psspl.autoreply.MainActivity] to decide whether to show
 * [com.psspl.autoreply.ui.screens.login.LoginScreen] or the main app shell.
 *
 * Also provides [signOut] so any screen can trigger a global logout without
 * needing its own repository reference.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /**
     * Starts as [AuthState.Loading] (avoids a flash of the login screen on
     * cold start when a valid session already exists).
     *
     * Firebase's [com.google.firebase.auth.FirebaseAuth.AuthStateListener] fires
     * synchronously upon registration if a current user already exists, so the
     * transition to [AuthState.Authenticated] or [AuthState.Unauthenticated]
     * typically happens within the same frame.
     */
    val authState: StateFlow<AuthState> = authRepository.currentUser
        .map { user ->
            if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading,
        )

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
