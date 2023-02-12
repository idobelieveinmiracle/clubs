package com.rose.clubs.viewmodels.login

import android.util.Log
import androidx.lifecycle.*
import com.rose.clubs.data.User
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class LoginViewModel(private val loginModel: LoginModel) : ViewModel() {
    private val _loginStatus: MutableLiveData<LoginStatus> = MutableLiveData(LoginStatus.None)
    private val _loading: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _isLoginType: MutableLiveData<Boolean> = MutableLiveData(true)

    val loginStatus: LiveData<LoginStatus> get() = _loginStatus
    val loading: LiveData<Boolean> get() = _loading
    val isLoginType: LiveData<Boolean> get() = _isLoginType

    fun login(email: String, password: String) {
        if (email.isEmpty()) {
            _loginStatus.value = LoginStatus.Error("Invalid email!")
            return
        }

        _loading.value = true
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            _loginStatus.value = LoginStatus.Error("Login failed!")
        }) {
            val isSuccess = loginModel.login(email, password)
            _loginStatus.value = if (isSuccess){
                LoginStatus.LoginSuccess
            } else {
                LoginStatus.Error("Login failed!")

            }
        }.invokeOnCompletion {
            _loading.value = false
        }
    }

    fun register(user: User, password: String, password2: String, imageUri: String) {
        if (user.email.isEmpty()) {
            _loginStatus.value = LoginStatus.Error("Invalid email")
            return
        }

        if (user.displayName.isEmpty()) {
            _loginStatus.value = LoginStatus.Error("Display name must not be empty")
            return
        }

        if (password != password2) {
            _loginStatus.value = LoginStatus.Error("Passwords are not match")
            return
        }

        _loading.value = true
        viewModelScope.launch {
            Log.i(TAG, "register: $imageUri")

            val isSuccess = loginModel.register(user, password, imageUri)

            if (isSuccess) {
                _isLoginType.value = true
                _loginStatus.value = LoginStatus.Success("Register successfully")
            } else {
                _loginStatus.value = LoginStatus.Error("Register failed!")
            }


        }.invokeOnCompletion {
            _loading.value = false
        }
    }

    fun changeLoginType() {
        _isLoginType.value = isLoginType.value?.not()
            ?: throw IllegalArgumentException("Invalid previous login type")
    }

    class Factory(private val loginModel: LoginModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(loginModel) as T
        }
    }
}