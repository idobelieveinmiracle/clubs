package com.rose.clubs.viewmodels.addmatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Match
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddMatchViewModel(
    private val clubId: String,
    private val addMatchModel: AddMatchModel
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    private val _message = MutableSharedFlow<String>()
    private val _saved = MutableStateFlow(false)

    val loading: StateFlow<Boolean> get() = _loading
    val message: SharedFlow<String> get() = _message
    val saved: StateFlow<Boolean> get() = _saved

    fun save(location: String, time: Long, cost: Int) {
        _loading.value = true
        viewModelScope.launch {
            val result = addMatchModel.saveMatch(
                clubId,
                Match(
                    matchId = "",
                    location,
                    time,
                    cost,
                    players = emptyList()
                )
            )

            when (result) {
                is SaveMatchResult.Success -> {
                    _message.emit("Match saved")
                    delay(300)
                    _saved.value = true
                }
                is SaveMatchResult.Failed -> _message.emit(result.message)
            }
            _loading.value = false
        }
    }

    class Factory(
        private val clubId: String,
        private val model: AddMatchModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddMatchViewModel(clubId, model) as T
        }
    }
}