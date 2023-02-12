package com.rose.clubs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rose.clubs.R
import com.rose.clubs.data.Player
import com.rose.clubs.data.Role
import com.rose.clubs.models.firebase.FirebasePlayerDetailsModel
import com.rose.clubs.ui.screens.commons.AppTopBar
import com.rose.clubs.viewmodels.playerdetails.PlayerDetailsViewModel

@Composable
fun PlayerDetailsScreen(
    navController: NavController?,
    playerId: String
) {
    val viewModel: PlayerDetailsViewModel = viewModel(
        factory = PlayerDetailsViewModel.Factory(playerId, FirebasePlayerDetailsModel())
    )

    val player by viewModel.player.collectAsState()
    val viewerRole by viewModel.viewerRole.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(text = "Player details", icon = Icons.Filled.ArrowBack) {
                navController?.navigateUp()
            }
        },
    ) { scaffoldPadding ->
        PlayerDetailsView(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            player = player,
            viewerRole = viewerRole
        )
    }
}

@Composable
fun PlayerDetailsView(
    modifier: Modifier,
    player: Player?,
    viewerRole: Role
) {
    Surface(modifier) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            AsyncImage(
                model = player?.user?.avatarUrl,
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
                text = player?.user?.displayName ?: "Unknown",
                style = MaterialTheme.typography.h5,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Number: ${player?.number ?: "Unknown"}"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Balance: ${player?.balance ?: "Unknown"}"
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (viewerRole in arrayOf(Role.CAPTAIN, Role.SUB_CAPTAIN)) {
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF913175)
                        )
                    ) {
                        Row {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Kick")
                        }
                    }
                    Spacer(modifier = Modifier.width(30.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary
                        )
                    ) {
                        Row {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Balance")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
@Preview
fun PlayerDetailsPreview() {
    PlayerDetailsView(modifier = Modifier.fillMaxSize(), player = null, Role.CAPTAIN)
}