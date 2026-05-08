package com.psspl.autoreply.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.auth.AuthRepository
import com.psspl.autoreply.data.auth.model.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for [LoginScreen]. */
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()

    /** Sign-in succeeded — [AuthViewModel] will observe the Firebase state change
     *  and automatically switch to the main app shell. */
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()

    /**
     * Google Play Services is missing or outdated.
     * The UI should surface an actionable "Update Google Play Services" prompt.
     */
    data class PlayServicesError(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Launches the Google sign-in flow.
     *
     * @param activityContext Required by Credential Manager to display the
     *   account-picker bottom-sheet. Pass [LocalContext.current] from the composable.
     */
    fun signIn(activityContext: Context) {
        if (_uiState.value is LoginUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            _uiState.value = when (val result = authRepository.signInWithGoogle(activityContext)) {
                is AuthResult.Success -> LoginUiState.Success
                is AuthResult.Cancelled -> LoginUiState.Idle
                is AuthResult.Error -> LoginUiState.Error(result.message)
                is AuthResult.PlayServicesError -> LoginUiState.PlayServicesError(result.message)
            }
        }
    }

    fun clearError() {
        when (_uiState.value) {
            is LoginUiState.Error, is LoginUiState.PlayServicesError ->
                _uiState.value = LoginUiState.Idle

            else -> Unit
        }
    }
}
