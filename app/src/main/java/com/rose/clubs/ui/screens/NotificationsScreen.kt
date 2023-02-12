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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import com.rose.clubs.ui.screens.commons.AppTopBar
import com.rose.clubs.viewmodels.notifications.NotificationData
import com.rose.clubs.viewmodels.notifications.NotificationsModel
import com.rose.clubs.viewmodels.notifications.NotificationsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "NotificationsScreen"

@Composable
fun NotificationsScreen(
    navController: NavController?,
    model: NotificationsModel
) {
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.Factory(model)
    )

    val notifications by viewModel.notifications.collectAsState()
    val refreshing by viewModel.loading.collectAsState()
    val club by viewModel.club.collectAsState()

    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        launch {
            viewModel.message.collectLatest { message ->
                if (message.isNotEmpty()) {
                    hostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                text = club?.let { "${it.name}'s notifications" } ?: "Club notifications",
                icon = Icons.Filled.ArrowBack
            ) {
                Log.i(TAG, "NotificationsScreen: BackStack ${navController?.backQueue}")
                if (navController?.backQueue?.size == 1) {
                    navController.navigateUp()
                } else if (viewModel.isDataChanged) {
                    navController?.popBackStack()
                    navController?.popBackStack()
                    navController?.navigate("club_details/${club?.clubId ?: ""}")
                } else {
                    navController?.popBackStack()
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        }
    ) { scaffoldPadding ->
        NotificationsView(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            refreshing,
            notifications,
            onAgree = { id -> viewModel.agreeNotification(id) },
            onDisagree = { id -> viewModel.disagreeNotification(id) }
        ) {
            viewModel.loadNotifications()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotificationsView(
    modifier: Modifier = Modifier,
    refreshing: Boolean,
    notifications: List<NotificationData>,
    onAgree: (id: String) -> Unit,
    onDisagree: (id: String) -> Unit,
    onRefresh: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = onRefresh)

    Surface(
        modifier = modifier
    ) {
        Box(
            Modifier
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(notifications) {
                    NotificationItem(
                        data = it,
                        onAgree = { onAgree(it.id) },
                        onDisagree = { onDisagree(it.id) }
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }

    }
}

@Composable
fun NotificationItem(
    data: NotificationData,
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
    ) {
        Row {
            AsyncImage(
                model = data.imageUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .padding(10.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(data.text, style = MaterialTheme.typography.h6, maxLines = 1)
                Spacer(modifier = Modifier.height(3.dp))
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = data.subText,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.subtitle2,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    tint = Color.Green,
                    contentDescription = "Agree",
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable { onAgree() }
                        .padding(4.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    tint = Color.Red,
                    contentDescription = "Disagree",
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable { onDisagree() }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun NotificationsPreview() {
    NotificationsView(
        modifier = Modifier.fillMaxSize(),
        refreshing = false,
        notifications = listOf(
            NotificationData(
                "1",
                "Quoc Hung",
                "Quoc Hung want to join the club",
                "Image url"
            )
        ),
        onAgree = {},
        onDisagree = {}
    ) {

    }
}