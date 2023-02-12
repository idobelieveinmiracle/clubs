package com.rose.clubs.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rose.clubs.data.Club
import com.rose.clubs.ui.screens.commons.ClubItem
import com.rose.clubs.viewmodels.clublist.ClubsListViewModel

private const val TAG = "ClubsListScreen"

@Composable
fun ClubsListScreen(
    navController: NavController?,
    viewModel: ClubsListViewModel?
) {

    val clubs by viewModel!!.clubs.collectAsState()

    Scaffold(
        topBar = {
            Row(Modifier.padding(5.dp)) {
                Text(
                    text = "Clubs",
                    style = MaterialTheme.typography.h5.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .align(Alignment.CenterVertically)
                )

                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .clickable { navController?.navigate("search_club") }
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                )

                Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                    var menuExpanded by remember { mutableStateOf(false) }

                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 10.dp)
                            .clickable { menuExpanded = !menuExpanded }
                            .padding(4.dp)
                    )

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            navController?.navigate("profile")
                        }) {
                            Text("Profile")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController?.navigate("add_club") },
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add club")
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
