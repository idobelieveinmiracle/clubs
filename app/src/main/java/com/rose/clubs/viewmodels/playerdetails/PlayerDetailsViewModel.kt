package com.rose.clubs.viewmodels.playerdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Player
import com.rose.clubs.data.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerDetailsViewModel(
    private val playerId: String,
    private val model: PlayerDetailModel
) : ViewModel() {
    private val _player = MutableStateFlow<Player?>(null)
    private val _viewerRole = MutableStateFlow(Role.MEMBER)
    private val _balanceUpdated = MutableStateFlow(-1L)

    val player: StateFlow<Player?> get() = _player
    val viewerRole: StateFlow<Role> get() = _viewerRole
    val balanceUpdated: SharedFlow<Long> get() = _balanceUpdated

    init {
        viewModelScope.launch {
            val player = model.getPlayerInfo(playerId)
            _player.value = player
            _viewerRole.value = model.getViewerRole(player?.club?.clubId ?: "")
        }
    }

    fun addBalance(deltaBalance: Int) {
        viewModelScope.launch {
            val result = model.addBalance(playerId, deltaBalance)
            if (result) {
                _balanceUpdated.value = System.currentTimeMillis()
                _player.value = model.getPlayerInfo(playerId)
            }
        }
    }

    class Factory(
        private val playerId: String,
        private val model: PlayerDetailModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerDetailsViewModel(playerId, model) as T
        }
    }
}