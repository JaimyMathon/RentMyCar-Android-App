package com.example.rentmycar_android_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.AuthRepository
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        loginSuccess = result.data.username
                    )
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        error = result.message ?: "E-mail of wachtwoord onjuist"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
