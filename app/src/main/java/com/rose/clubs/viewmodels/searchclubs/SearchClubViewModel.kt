package com.rose.clubs.viewmodels.searchclubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Club
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchClubViewModel(private val model: SearchClubModel) : ViewModel() {
    private val _clubs: MutableStateFlow<List<Club>> = MutableStateFlow(emptyList())
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val clubs: StateFlow<List<Club>> = _clubs
    val loading: StateFlow<Boolean> = _loading

    private var job: Job? = null

    fun search(searchText: String) {
        _loading.value = true

        if (job?.isActive == true) {
            job?.cancel()
        }

        job = viewModelScope.launch {
            if (searchText.isEmpty()) {
                _clubs.value = emptyList()
            } else {
                _clubs.value = model.search(searchText)
            }
            _loading.value = false
        }
    }

    class Factory(private val model: SearchClubModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchClubViewModel(model) as T
        }
    }
}