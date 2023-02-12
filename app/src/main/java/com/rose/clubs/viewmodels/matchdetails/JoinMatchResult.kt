package com.rose.clubs.viewmodels.matchdetails

sealed class JoinMatchResult {
    object Success : JoinMatchResult()
    class Failed(val message: String) : JoinMatchResult()
}
