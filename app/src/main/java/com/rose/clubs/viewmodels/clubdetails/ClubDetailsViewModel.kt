package com.rose.clubs.viewmodels.clubdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Club
import com.rose.clubs.data.Match
import com.rose.clubs.data.Player
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "ClubDetailsViewModel"

class ClubDetailsViewModel(
    private val clubDetailsModel: ClubDetailsModel,
    clubId: String
) : ViewModel() {
    private val _club: MutableStateFlow<Club?> = MutableStateFlow(null)
    private val _players: MutableStateFlow<List<Player>> = MutableStateFlow(listOf())
    private val _matches: MutableStateFlow<List<Match>> = MutableStateFlow(listOf())
    private val _errorMessage: MutableSharedFlow<String> = MutableSharedFlow()
    private val _actionType: MutableStateFlow<ActionType> = MutableStateFlow(ActionType.NONE)
    private val _reloadClubs: MutableSharedFlow<Boolean> = MutableSharedFlow()

    val club: StateFlow<Club?> = _club
    val players: StateFlow<List<Player>> = _players
    val matches: StateFlow<List<Match>> = _matches
    val errorMessage: SharedFlow<String> = _errorMessage
    val actionType: StateFlow<ActionType> = _actionType
    val reloadClubs: SharedFlow<Boolean> = _reloadClubs

    init {
        viewModelScope.launch {
            launch {
                val club = clubDetailsModel.loadClubData(clubId)
                if (club == null) {
                    _errorMessage.emit("Error when load club")
                }
                _club.value = club
                Log.i(TAG, "init: club id = ${club?.clubId}")
            }
            launch {
                val loadedPlayers = clubDetailsModel.loadPlayers(clubId)
                _players.value = loadedPlayers
                _actionType.value = clubDetailsModel.getActionType(clubId, loadedPlayers)
            }
            launch {
                _matches.value = clubDetailsModel.loadMatches(clubId)
            }
        }
    }

    private fun reloadPlayers() {
        viewModelScope.launch {
            val loadedPlayers = clubDetailsModel.loadPlayers(club.value!!.clubId)
            _players.value = loadedPlayers
            _actionType.value = clubDetailsModel.getActionType(club.value!!.clubId, loadedPlayers)
        }
    }

    fun reloadMatches() {
        viewModelScope.launch {
            _matches.value = clubDetailsModel.loadMatches(club.value!!.clubId)
        }
    }

    fun askToJoin() {
        _actionType.value = ActionType.LOADING
        viewModelScope.launch {
            val result = clubDetailsModel.askToJoin(club.value!!.clubId)
            Log.i(TAG, "askToJoin: $result")
            if (result) {
                reloadPlayers()
                Log.i(TAG, "askToJoin: emitting reload")
                _reloadClubs.emit(true)
            }
        }
    }

    fun cancelAsk() {
        _actionType.value = ActionType.LOADING
        viewModelScope.launch {
            val result = clubDetailsModel.cancelAsk(club.value!!.clubId)
            Log.i(TAG, "cancelAsk: $result")
            if (result) {
                reloadPlayers()
                Log.i(TAG, "askToJoin: emitting reload")
                _reloadClubs.emit(true)
            }
        }
    }

    class Factory(
        private val clubDetailsModel: ClubDetailsModel,
        private val clubId: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClubDetailsViewModel(clubDetailsModel, clubId) as T
        }
    }
}