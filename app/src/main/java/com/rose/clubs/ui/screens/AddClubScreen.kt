package com.rose.clubs.ui.screens


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rose.clubs.models.firebase.FirebaseAddClubModel
import com.rose.clubs.ui.screens.commons.AppTopBar
import com.rose.clubs.ui.screens.commons.ImageSelector
import com.rose.clubs.viewmodels.addclub.AddClubViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "AddClubScreen"

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AddClubScreen(navController: NavController?, onClubAdded: () -> Unit) {
    val viewModel: AddClubViewModel = viewModel(
        factory = AddClubViewModel.Factory(FirebaseAddClubModel(
            context = LocalContext.current.applicationContext
        ))
    )

    val hostState = remember { SnackbarHostState() }
    val composeScope = rememberCoroutineScope()

    val added by viewModel.added.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Log.i(TAG, "AddClubScreen: HostState@${hostState.hashCode()}")
    LaunchedEffect(key1 = Unit) {
        viewModel.message.collectLatest {
            hostState.showSnackbar(it)
        }
    }

    if (added) {
        composeScope.launch {
            delay(500)
            onClubAdded()
            navController?.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(text = "New Club", icon = Icons.Filled.ArrowBack, iconDescription = "Back") {
                navController?.navigateUp()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        }
    ) { scaffoldPadding ->
        AddClubView(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            loading = loading,
            onSendAdd = { name, imageUri ->
                viewModel.addClub(name, imageUri)
            }
        )
    }
}

@Composable
private fun AddClubView(
    modifier: Modifier,
    loading: Boolean,
    onSendAdd: (name: String, imageUri: String) -> Unit
) {
    Surface(
        modifier = modifier
    ) {
        var clubName: String by rememberSaveable { mutableStateOf("") }
        var clubImageUri: String by rememberSaveable { mutableStateOf("") }

        Column(Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = clubName,
                onValueChange = { clubName = it },
                label = {
                    Text("Club name")
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ImageSelector(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                imageUri = clubImageUri
            ) { selectedUri ->
                clubImageUri = selectedUri
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (loading) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    visible = true
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = { onSendAdd(clubName, clubImageUri) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(text = "Add club")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
@Preview
fun AddClubPreview() {
    AddClubView(
        modifier = Modifier.fillMaxSize(),
        loading = false,
        onSendAdd = { _, _ -> }
    )
}