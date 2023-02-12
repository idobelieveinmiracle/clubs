package com.rose.clubs.viewmodels.addmatch

sealed class SaveMatchResult {
    object Success: SaveMatchResult()
    class Failed(val message: String): SaveMatchResult()
}
