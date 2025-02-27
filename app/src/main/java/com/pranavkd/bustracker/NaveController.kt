package com.pranavkd.bustracker

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pranavkd.bustracker.ChatLogic.ChatViewModel

sealed class BootomNavItem(
    val route: String,
    // @DrawableRes val icon: Int,
    @DrawableRes val icon: Int,
    val title: String
){
    object TrackingScreen: BootomNavItem("TrackingScreen", R.drawable.location, "Tracking")
    object ChatScren: BootomNavItem("ChatScren", R.drawable.chat_round_svgrepo_com, "Chat")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NaveMain(viewmodel: ChatViewModel) {
    val navController = rememberNavController()
    Scaffold (
        //
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
            ),
        bottomBar = {
            BootomNav(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "TrackingScreen",
            modifier = Modifier.padding(innerPadding)
        ){
            composable("TrackingScreen"){
                MainScreen(navController)
            }
            composable("ChatScren"){
                ChatScren(viewmodel)
            }
        }
    }
}

@Composable
fun BootomNav(navController: NavController){
    val items = listOf(
        BootomNavItem.TrackingScreen,
        BootomNavItem.ChatScren
    )
    // BottomNavigation
    BottomNavigation (
        backgroundColor = colorResource(id = R.color.Light),
    ) {
        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = item.icon), contentDescription = null, modifier = Modifier.padding(5.dp).width(25.dp).height(25.dp)) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                selectedContentColor = colorResource(id = R.color.Mid),
                unselectedContentColor = colorResource(id = R.color.Mid).copy(alpha = 0.5f),
                onClick = {
                    if(currentRoute != item.route){
                        navController.navigate(item.route)
                    }
                }
            )
        }
    }
}