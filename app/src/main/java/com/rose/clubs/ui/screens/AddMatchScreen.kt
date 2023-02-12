package com.rose.clubs.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rose.clubs.ui.screens.commons.AppTopBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rose.clubs.models.firebase.FirebaseAddMatchModel
import com.rose.clubs.ui.screens.commons.getTimeFormatted
import com.rose.clubs.viewmodels.addmatch.AddMatchViewModel
import com.rose.clubs.viewmodels.main.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AddMatchScreen"

@Composable
fun AddMatchScreen(
    navController: NavController?,
    clubId: String,
    mainViewModel: MainViewModel
) {
    val viewModel: AddMatchViewModel = viewModel(
        factory = AddMatchViewModel.Factory(
            clubId,
            FirebaseAddMatchModel()
        )
    )

    val loading by viewModel.loading.collectAsState()

    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        launch {
            viewModel.message.collectLatest {
                if (it.isNotEmpty()) {
                    hostState.showSnackbar(it)
                }
            }
        }
        launch {
            viewModel.saved.collectLatest {
                if (it) {
                    mainViewModel.triggerReload("club_details:matches")
                    Log.i(TAG, "AddMatchScreen: saved")
                    navController?.navigateUp()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(text = "Add match", icon = Icons.Filled.ArrowBack) {
                navController?.navigateUp()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        }
    ) { scaffoldPadding ->
        AddMatchView(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            loading = loading
        ) { location, time, cost ->
            viewModel.save(location, time, cost)
        }
    }
}

@Composable
private fun AddMatchView(
    modifier: Modifier,
    loading: Boolean,
    onSave: (location: String, time: Long, cost: Int) -> Unit
) {
    var location by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var cost by rememberSaveable { mutableStateOf(0) }

    val calendar = Calendar.getInstance()
    calendar.time = Date(time)
    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hour, minute ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = Date(time)
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
            selectedCalendar.set(Calendar.MINUTE, minute)
            time = selectedCalendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val datePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = Date(time)
            selectedCalendar.set(year, month, day)
            time = selectedCalendar.timeInMillis
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Surface(modifier = modifier) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = {
                    Text("Location")
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = cost.toString(),
                onValueChange = { cost = it.toInt() },
                label = {
                    Text("Cost (K)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Time: ${getTimeFormatted(time)}",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        datePickerDialog.show()
                    }
                    .padding(5.dp),
                style = MaterialTheme.typography.subtitle2
            )

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
                    onClick = { onSave(location, time, cost) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(text = "Add match")
                }
            }
        }
    }
}

@Composable
@Preview
fun AddMatchPreview() {
    AddMatchView(
        modifier = Modifier.fillMaxSize(),
        loading = false,
        onSave = { _, _, _ -> }
    )
}