package com.rose.clubs.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.rose.clubs.ui.screens.commons.AppTopBar

@Composable
fun AddMatchScreen(
    navController: NavController?,
    clubId: String
) {

    Scaffold(
        topBar = {
            AppTopBar(text = "Add match", icon = Icons.Filled.ArrowBack) {
                navController?.navigateUp()
            }
        }
    ) { scaffoldPadding ->
        AddMatchView(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) { location, time, cost ->

        }
    }
}

@Composable
fun AddMatchView(
    modifier: Modifier,
    onSave: (location: String, time: Long, cost: Int) -> Unit
) {

}

@Composable
@Preview
fun AddMatchPreview() {
    AddMatchView(
        modifier = Modifier.fillMaxSize(),
        onSave = { _, _, _ -> }
    )
}