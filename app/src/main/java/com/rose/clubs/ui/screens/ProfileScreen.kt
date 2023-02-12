package com.rose.clubs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rose.clubs.R
import com.rose.clubs.data.User
import com.rose.clubs.ui.screens.commons.AppTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    user: User?,
    navController: NavController?,
    onLogout: () -> Unit,
) {
    val hostState: SnackbarHostState = remember { SnackbarHostState() }
    val composeScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(text = "User profile", icon = Icons.Filled.ArrowBack) {
                navController?.navigateUp()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        }
    ) { scaffoldPadding ->
        Surface(
            Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                AsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = "",
                    placeholder = painterResource(id = R.drawable.profile_picture),
                    error = painterResource(id = R.drawable.profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(88.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = user?.displayName ?: "Unknown",
                    style = MaterialTheme.typography.h5,
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = user?.email ?: "Unknown"
                )

                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    onClick = {
                        composeScope.launch {
                            launch {
                                delay(500)
                                onLogout()
                            }
                            hostState.showSnackbar("Logged out!")
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Log out")
                }
            }
        }
    }
}

@Composable
@Preview
fun ProfilePreview() {
    ProfileScreen(
        user = User("1", "hung@gmail.com", "Quoc Hung", ""),
        navController = null
    ) { }
}
