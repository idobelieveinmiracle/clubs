package com.rose.clubs.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rose.clubs.data.User
import com.rose.clubs.models.firebase.FirebaseLoginModel
import com.rose.clubs.ui.screens.commons.ImageSelector
import com.rose.clubs.viewmodels.login.LoginStatus
import com.rose.clubs.viewmodels.login.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun LoginScreen(navController: NavController?, onReloadUser: () -> Unit) {
    val activity = LocalContext.current as? Activity
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(
            FirebaseLoginModel(context = LocalContext.current.applicationContext)
        )
    )

    val loading by viewModel.loading.observeAsState(false)
    val status by viewModel.loginStatus.observeAsState(LoginStatus.None)
    val isLoginType by viewModel.isLoginType.observeAsState(true)

    val snackbarHostState = remember { SnackbarHostState() }
    val composeScope = rememberCoroutineScope()

    if (status == LoginStatus.LoginSuccess) {
        composeScope.launch {
            launch {
                delay(500)
                navController?.navigateUp()
                onReloadUser.invoke()
            }
            snackbarHostState.showSnackbar("Login successfully!")
        }
    }

    Scaffold(
        topBar = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(10.dp)
                    .clickable { activity?.finish() }
                    .padding(4.dp)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { scaffoldPadding ->
        LoginView(
            modifier = Modifier.padding(scaffoldPadding),
            loading = loading,
            status = status,
            isLoginType = isLoginType,
            onLogIn = { email, password -> viewModel.login(email, password) },
            onRegister = { user, password, password2, imageUri ->
                viewModel.register(
                    user,
                    password,
                    password2,
                    imageUri
                )
            },
            onLoginTypeChanged = { viewModel.changeLoginType() }
        )
    }
}

@Composable
private fun LoginView(
    modifier: Modifier = Modifier,
    loading: Boolean,
    status: LoginStatus,
    isLoginType: Boolean,
    onLogIn: (email: String, password: String) -> Unit,
    onRegister: (user: User, password: String, password2: String, imageUri: String) -> Unit,
    onLoginTypeChanged: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf("") }


    Surface(
        modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isLoginType) "Login" else "Register",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(text = "Email")
                },
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(text = "Password")
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!isLoginType) {
                OutlinedTextField(
                    value = password2,
                    onValueChange = { password2 = it },
                    label = {
                        Text(text = "Password 2")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = {
                        Text(text = "Display name")
                    },
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ImageSelector(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    imageUri = imageUri
                ) { selectedUri ->
                    imageUri = selectedUri
                }

                Spacer(modifier = Modifier.height(15.dp))
            }

            if (status != LoginStatus.None) {
                Text(
                    text = when (status) {
                        is LoginStatus.Error -> status.message
                        is LoginStatus.Success -> status.message
                        else -> ""
                    },
                    style = MaterialTheme.typography.subtitle2.copy(
                        when (status) {
                            is LoginStatus.Error -> Color.Red
                            is LoginStatus.Success -> Color.Green
                            else -> Color.Black
                        }
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (loading) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    visible = true
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = {
                        if (isLoginType) {
                            onLogIn(email, password)
                        } else {
                            onRegister(
                                User("", email, displayName, ""),
                                password,
                                password2,
                                imageUri
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(text = if (isLoginType) "Login" else "Register")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isLoginType) "Register new account" else "Back to login",
                style = MaterialTheme.typography.subtitle2.copy(
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        onLoginTypeChanged()
                    }
            )
        }
    }

}

@Preview
@Composable
fun LoginPreview() {
    LoginView(
        loading = false,
        status = LoginStatus.Success("Success"),
        isLoginType = true,
        onLogIn = { _, _ -> },
        onRegister = { _, _, _, _ -> },
        onLoginTypeChanged = { }
    )
}

@Preview
@Composable
fun RegisterPreview() {
    LoginView(
        loading = true,
        status = LoginStatus.Error("Success"),
        isLoginType = false,
        onLogIn = { _, _ -> },
        onRegister = { _, _, _, _ -> },
        onLoginTypeChanged = { }
    )
}
