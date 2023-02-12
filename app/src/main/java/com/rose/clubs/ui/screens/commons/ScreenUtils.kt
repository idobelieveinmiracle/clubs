package com.rose.clubs.ui.screens.commons

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun <T> SharedFlow<T>.asyncCollect(scope: CoroutineScope, onCollected: (T) -> Unit) {
    scope.launch {
        collectLatest {
            onCollected(it)
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun getTimeFormatted(time: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
    return formatter.format(Date(time))
}
