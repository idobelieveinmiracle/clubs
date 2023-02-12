package com.rose.clubs.viewmodels.matchdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Match
import com.rose.clubs.data.Player
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchDetailsViewModel(
    private val matchId: String,
    private val model: MatchDetailsModel
) : ViewModel() {
    private val _message = MutableSharedFlow<String>()
    private val _match = MutableStateFlow<Match?>(null)
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    private val _joinEnabled = MutableStateFlow(false)

    val message: SharedFlow<String> get() = _message
    val match: StateFlow<Match?> get() = _match
    val players: StateFlow<List<Player>> get() = _players
    val joinEnabled: StateFlow<Boolean> get() = _joinEnabled

    init {
        viewModelScope.launch {
            launch {
                val matchInfo = model.loadMatchInfo(matchId)
                if (matchInfo == null) {
                    _message.emit("Load match info failed")
                }
                _match.value = matchInfo
            }
            launch {
                val players = model.loadPlayers(matchId)
                _players.value = players
                _joinEnabled.value = model.getJoinEnabled(matchId, players)
            }
        }
    }

    fun joinMatch() {
        viewModelScope.launch {
            _joinEnabled.value = false
            when (val joinResult = model.joinMatch(matchId)) {
                is JoinMatchResult.Success -> {
                    _players.value = model.loadPlayers(matchId)
                }
                is JoinMatchResult.Failed -> {
                    _message.emit(joinResult.message)
                }
            }
            _joinEnabled.value = model.getJoinEnabled(matchId, players.value)
        }
    }

    class Factory(private val matchId: String, private val model: MatchDetailsModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MatchDetailsViewModel(matchId, model) as T
        }
    }
}