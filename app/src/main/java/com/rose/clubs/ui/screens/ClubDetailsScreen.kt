package com.rose.clubs.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rose.clubs.data.Club
import com.rose.clubs.data.Match
import com.rose.clubs.data.Player
import com.rose.clubs.models.firebase.FirebaseClubDetailsModel
import com.rose.clubs.ui.screens.commons.AppTopBar
import com.rose.clubs.ui.screens.commons.PlayerCard
import com.rose.clubs.ui.screens.commons.asyncCollect
import com.rose.clubs.ui.screens.commons.getTimeFormatted
import com.rose.clubs.viewmodels.clubdetails.ActionType
import com.rose.clubs.viewmodels.clubdetails.ClubDetailsViewModel
import com.rose.clubs.viewmodels.clublist.ClubsListViewModel
import com.rose.clubs.viewmodels.main.MainViewModel
import kotlinx.coroutines.launch

private const val TAG = "ClubDetailsScreen"

@Composable
fun ClubDetailsScreen(
    navController: NavController?,
    clubId: String,
    clubsListViewModel: ClubsListViewModel,
    mainViewModel: MainViewModel
) {
    val viewModel: ClubDetailsViewModel = viewModel(
        factory = ClubDetailsViewModel.Factory(
            FirebaseClubDetailsModel(),
            clubId
        )
    )

    val club by viewModel.club.collectAsState()
    val players by viewModel.players.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val actionType by viewModel.actionType.collectAsState()

    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.errorMessage.asyncCollect(this) { error ->
            if (error.isNotEmpty()) {
                launch {
                    hostState.showSnackbar(error)
                }
            }
        }

        viewModel.reloadClubs.asyncCollect(this) { shouldReload ->
            Log.i(TAG, "ClubDetailsScreen: reload clubs $shouldReload")
            if (shouldReload) {
                clubsListViewModel.reloadMyClubs()
            }
        }

        Log.i(TAG, "ClubDetailsScreen: init reload state ${mainViewModel.reloadMap}")
        mainViewModel.reloadMap.asyncCollect(this) { map ->
            Log.i(TAG, "ClubDetailsScreen: reload state changed $map")
            if (map.getOrDefault("club_details:matches", false)) {
                viewModel.reloadMatches()
                mainViewModel.markReloaded("club_details:matches")
            }
            if (map.getOrDefault("club_details:players", false)) {
                viewModel.reloadPlayers()
                mainViewModel.markReloaded("club_details:players")
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                text = "Club details",
                icon = Icons.Filled.ArrowBack,
                secondaryIcon = if (actionType == ActionType.ADD_MATCH) Icons.Filled.Email else null,
                onSecondaryIconClick = {
                    navController?.navigate("notifications/club/$clubId")
                }
            ) {
                navController?.navigateUp()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        },
        floatingActionButton = {
            if (actionType == ActionType.LOADING) {
                CircularProgressIndicator()
            } else if (actionType != ActionType.NONE) {
                FloatingActionButton(
                    onClick = {
                        when (actionType) {
                            ActionType.ASK_TO_JOIN -> viewModel.askToJoin()
                            ActionType.CANCEL_ASK -> viewModel.cancelAsk()
                            ActionType.ADD_MATCH -> navController?.navigate("add_match/$clubId")
                            else -> Unit
                        }
                    },
                    backgroundColor = Color.White
                ) {
                    Text(
                        text = when (actionType) {
                            ActionType.ASK_TO_JOIN -> "Ask to join"
                            ActionType.ADD_MATCH -> "Add match"
                            ActionType.CANCEL_ASK -> "Cancel ask"
                            else -> ""
                        },
                        modifier = Modifier.padding(horizontal = 17.dp),
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        ClubDetailsView(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            club = club,
            players = players,
            matches = matches,
            onPlayerSelected = { playerId -> navController?.navigate("player_details/$playerId") },
            onMatchSelected = { matchId -> navController?.navigate("match_details/$matchId") }
        )
    }
}

@Composable
private fun ClubDetailsView(
    modifier: Modifier = Modifier,
    club: Club?,
    players: List<Player>,
    matches: List<Match>,
    onPlayerSelected: (playerId: String) -> Unit,
    onMatchSelected: (matchId: String) -> Unit
) {
    Surface(
        modifier = modifier
    ) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = club?.avatarUrl ?: "",
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(102.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .align(Alignment.Center)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(7.dp))
            }
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = club?.name ?: "No name",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.h5
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Text(
                    text = "Players (${players.size})",
                    modifier = Modifier.padding(horizontal = 14.dp),
                    style = MaterialTheme.typography.subtitle2
                )
            }
            item { Spacer(modifier = Modifier.height(7.dp)) }
            items(players) { player ->
                PlayerCard(player = player) {
                    onPlayerSelected(player.playerId)
                }
            }

            item {
                Spacer(modifier = Modifier.height(5.dp))
            }

            if (matches.isNotEmpty()) {
                item {
                    Text(
                        text = "Matches (${matches.size})",
                        modifier = Modifier.padding(horizontal = 14.dp),
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                item { Spacer(modifier = Modifier.height(7.dp)) }
                items(matches) { match ->
                    MatchCard(match = match) {
                        onMatchSelected(match.matchId)
                    }
                }
            }

        }
    }
}

@Composable
private fun MatchCard(
    match: Match,
    onSelected: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .clickable { onSelected() }
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(match.location, style = MaterialTheme.typography.h6, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "Time: ${getTimeFormatted(match.time)}",
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.subtitle2,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "Cost: ${match.cost}",
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.subtitle2,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
@Preview
fun ClubDetailsPreview() {
    ClubDetailsView(
        modifier = Modifier.fillMaxSize(),
        club = Club("", "Miami heat", ""),
        players = emptyList(),
        matches = emptyList(),
        onPlayerSelected = { },
        onMatchSelected = { }
    )
}
