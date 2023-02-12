package com.rose.clubs.viewmodels.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(private val userModel: UserModel) : ViewModel() {
    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _loadingUserInfo: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val _reloadMap: MutableStateFlow<MutableMap<String, Boolean>> = MutableStateFlow(hashMapOf())

    val user: StateFlow<User?> get() = _user
    val loadingUserInfo: StateFlow<Boolean> get() = _loadingUserInfo
    val reloadMap: StateFlow<MutableMap<String, Boolean>> = _reloadMap

    init {
        reloadUser()
    }

    fun reloadUser() {
        Log.i(TAG, "reloadUser: ")
        _loadingUserInfo.value = true
        viewModelScope.launch {
            _user.value = userModel.getUser()
            _loadingUserInfo.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            userModel.logout()
            _user.value = null
        }
    }

    fun triggerReload(key: String) {
        _reloadMap.value = reloadMap.value.apply {
            put(key, true)
        }
    }

    fun markReloaded(key: String) {
        _reloadMap.value = reloadMap.value.apply {
            put(key, false)
        }
    }

    class Factory(private val userModel: UserModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(userModel) as T
        }
    }
}