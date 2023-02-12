package com.rose.clubs.viewmodels.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Club
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "NotificationsViewModel"

class NotificationsViewModel(
    private val model: NotificationsModel
) : ViewModel() {
    private val _notifications: MutableStateFlow<List<NotificationData>> = MutableStateFlow(
        emptyList()
    )
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _club: MutableStateFlow<Club?> = MutableStateFlow(null)
    private val _message: MutableSharedFlow<String> = MutableSharedFlow()
    private var _isDataChanged: Boolean = false

    val notifications: StateFlow<List<NotificationData>> get() = _notifications
    val loading: StateFlow<Boolean> get() = _loading
    val club: StateFlow<Club?> get() = _club
    val message: SharedFlow<String> get() = _message
    val isDataChanged: Boolean get() = _isDataChanged

    init {
        loadNotifications()
        viewModelScope.launch {
            _club.value = model.getClubInfo()
        }
    }

    fun loadNotifications() {
        if (_loading.value) {
            return
        }

        Log.i(TAG, "loadNotifications: start")
        _loading.value = true
        viewModelScope.launch {
            _notifications.value = model.fetchNotifications()
            delay(100)
            _loading.value = false
            Log.i(TAG, "loadNotifications: done")
        }
    }

    fun agreeNotification(notificationId: String) {
        if (_loading.value) {
            viewModelScope.launch {
                _message.emit("Can not agree while loading")
            }
            return
        }
        _loading.value = true
        viewModelScope.launch {
            model.agreeNotification(notificationId)
            _isDataChanged = true
            _notifications.value = model.fetchNotifications()
            _loading.value = false
        }
    }

    fun disagreeNotification(notificationId: String) {
        if (_loading.value) {
            viewModelScope.launch {
                _message.emit("Can not disagree while loading")
            }
            return
        }
        _loading.value = true
        viewModelScope.launch {
            model.disagreeNotification(notificationId)
            _notifications.value = model.fetchNotifications()
            _loading.value = false
        }
    }

    class Factory(private val model: NotificationsModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsViewModel(model) as T
        }
    }
}