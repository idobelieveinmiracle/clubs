package com.rose.clubs.viewmodels.login

sealed class LoginStatus {
    object None: LoginStatus()
    class Error(val message: String): LoginStatus()
    class Success(val message: String): LoginStatus()
    object LoginSuccess: LoginStatus()
}