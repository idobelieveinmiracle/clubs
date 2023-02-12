package com.rose.clubs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rose.clubs.data.*
import com.rose.clubs.models.firebase.FirebaseMatchDetailsModel
import com.rose.clubs.ui.screens.commons.AppTopBar
import com.rose.clubs.ui.screens.commons.PlayerCard
import com.rose.clubs.ui.screens.commons.asyncCollect
import com.rose.clubs.ui.screens.commons.getTimeFormatted
import com.rose.clubs.viewmodels.matchdetails.MatchDetailsViewModel
import kotlinx.coroutines.launch

@Composable
fun MatchDetails(
    navController: NavController?,
    matchId: String
) {
    val viewModel: MatchDetailsViewModel = viewModel(
        factory = MatchDetailsViewModel.Factory(matchId, FirebaseMatchDetailsModel())
    )

    val match by viewModel.match.collectAsState()
    val players by viewModel.players.collectAsState()
    val joinEnabled by viewModel.joinEnabled.collectAsState()

    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.message.asyncCollect(this) { message ->
            launch { hostState.showSnackbar(message) }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(text = "Match details", icon = Icons.Filled.ArrowBack) {
                navController?.navigateUp()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        },
        floatingActionButton = {
            if (joinEnabled) {
                FloatingActionButton(onClick = { viewModel.joinMatch() }) {
                    Text(
                        text = "Join",
                        modifier = Modifier.padding(horizontal = 17.dp),
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        MatchDetailView(
            modifier = Modifier.padding(scaffoldPadding),
            match = match,
            players = players
        )
    }
}

@Composable
private fun MatchDetailView(
    modifier: Modifier,
    match: Match?,
    players: List<Player>
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Location: ${match?.location ?: "No location"}",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Time: ${getTimeFormatted(match?.time ?: -1L)}",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cost: ${match?.cost ?: 0} K",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Attendees (${players.size})",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(players) {
                    PlayerCard(player = it)
                }
            }
        }
    }
}

@Composable
@Preview
fun MatchDetailsPreview() {
    MatchDetailView(
        modifier = Modifier.fillMaxSize(),
        match = Match("", "Dong Do 2", -1, 100, emptyList()),
        players = listOf(
            Player(
                "",
                User("", "hung@gmail.com", "quoc hung", ""),
                Club("", "", ""),
                10,
                Role.MEMBER,
                100
            )
        )
    )
}