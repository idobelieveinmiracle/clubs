package com.rose.clubs.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rose.clubs.models.firebase.FirebaseSearchClubModel
import com.rose.clubs.ui.screens.commons.ClubItem
import com.rose.clubs.viewmodels.searchclubs.SearchClubViewModel

@Composable
fun SearchClubScreen(
    navController: NavController?
) {
    val viewModel: SearchClubViewModel = viewModel(
        factory = SearchClubViewModel.Factory(FirebaseSearchClubModel())
    )

    val clubs by viewModel.clubs.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            Row(modifier = Modifier.padding(5.dp)) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable { navController?.navigateUp() }
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                )
                TextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.search(it)
                    },
                    label = {
                        Text("Search clubs by name or id...")
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    ) { scaffoldPadding ->
        Surface(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(clubs) { club ->
                        ClubItem(club = club) {
                            navController?.navigate("club_details/${club.clubId}")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchClubsPreview() {
    SearchClubScreen(
        navController = null
    )
}