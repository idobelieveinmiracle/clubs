package com.rose.clubs.viewmodels.addclub

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "AddClubViewModel"

class AddClubViewModel(private val addClubModel: AddClubModel) : ViewModel() {
    private val _added: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _message: MutableSharedFlow<String> = MutableSharedFlow()

    val added: StateFlow<Boolean> get() = _added
    val loading: StateFlow<Boolean> get() = _loading
    val message: SharedFlow<String> get() = _message

    fun addClub(name: String, imageUri: String) {
        Log.i(TAG, "addClub: $name $imageUri")

        if (name.isEmpty()) {
            viewModelScope.launch {
                _message.emit("Club Name must not be empty")
            }
            return
        }

        _loading.value = true
        viewModelScope.launch {
            val result = addClubModel.addClub(name, imageUri)
            _loading.value = false

            _message.emit(if (result) "Add club success!" else "Add club failed!")
            _added.value = result
        }
    }

    class Factory(private val addClubModel: AddClubModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddClubViewModel(addClubModel) as T
        }
    }
}