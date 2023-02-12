package com.rose.clubs.ui.screens.commons

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> SharedFlow<T>.asyncCollect(scope: CoroutineScope, onCollected: (T) -> Unit) {
    scope.launch {
        collectLatest {
            onCollected(it)
        }
    }
}
