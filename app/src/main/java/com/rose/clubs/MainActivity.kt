package com.rose.clubs

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rose.clubs.models.firebase.FirebaseClubNotificationsModel
import com.rose.clubs.models.firebase.FirebaseClubsListModel
import com.rose.clubs.models.firebase.FirebaseUserModel
import com.rose.clubs.ui.screens.*
import com.rose.clubs.ui.theme.ClubsTheme
import com.rose.clubs.viewmodels.clublist.ClubsListViewModel
import com.rose.clubs.viewmodels.main.MainViewModel

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClubsTheme {
                ClubApplication()
            }
        }
    }
}

@Composable
fun ClubApplication() {
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(FirebaseUserModel()))
    val clubsViewModel: ClubsListViewModel = viewModel(
        factory = ClubsListViewModel.Factory(FirebaseClubsListModel())
    )

    val user by viewModel.user.collectAsState(null)
    val loadingUserInfo by viewModel.loadingUserInfo.collectAsState(true)

    val navController = rememberNavController()
    Log.i(TAG, "ClubApplication: NavController@${navController.hashCode()}")

    NavHost(
        navController = navController,
        startDestination = if (loadingUserInfo) "loading" else if (user == null) "login" else "clubs"
    ) {
        composable("login") {
            LoginScreen(navController) {
                Log.i(TAG, "ClubApplication: navigated up from login")
                viewModel.reloadUser()
                clubsViewModel.reloadMyClubs()
            }
        }
        composable("profile") {
            ProfileScreen(user = user, navController) {
                viewModel.logout()
            }
        }
        composable("clubs") {
            ClubsListScreen(navController, clubsViewModel)
        }
        composable("add_club") {
            AddClubScreen(navController) {
                clubsViewModel.reloadMyClubs()
            }
        }
        composable("search_club") {
            SearchClubScreen(navController = navController)
        }
        composable("club_details/{club_id}", listOf(navArgument("club_id") {
            type = NavType.StringType
        })) { navEntry ->
            val clubId = navEntry.arguments?.getString("club_id") ?: ""
            Log.i(TAG, "ClubApplication: navigated to club details $clubId")
            ClubDetailsScreen(navController, clubId, clubsViewModel, viewModel)
        }
        composable("add_match/{club_id}", listOf(navArgument("club_id") {
            type = NavType.StringType
        })) { navEntry ->
            val clubId = navEntry.arguments?.getString("club_id") ?: ""
            AddMatchScreen(navController = navController, clubId = clubId, mainViewModel = viewModel)
        }
        composable("notifications/club/{club_id}", listOf(navArgument("club_id") {
            type = NavType.StringType
        })) { navEntry ->
            val clubId = navEntry.arguments?.getString("club_id") ?: ""
            NotificationsScreen(
                navController = navController,
                model = FirebaseClubNotificationsModel(clubId)
            )
        }
        composable("match_details/{match_id}", listOf(navArgument("match_id") {
            type = NavType.StringType
        })) { navEntry ->
            val matchId = navEntry.arguments?.getString("match_id") ?: ""
            MatchDetails(navController = navController, matchId = matchId)
        }
        composable("loading") {
            LoadingUserInfoScreen()
        }
    }
}
