package com.prayertracker.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prayertracker.app.ui.design.LucideIcons
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prayertracker.app.qibla.QiblaScreen
import com.prayertracker.app.reminders.ReminderBootstrap
import com.prayertracker.app.ui.friends.FriendDetailScreen
import com.prayertracker.app.ui.friends.FriendsScreen
import com.prayertracker.app.ui.settings.SettingsScreen
import com.prayertracker.app.ui.streaks.StreaksScreen
import com.prayertracker.app.ui.today.TodayScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    TODAY("today", "Today", LucideIcons.Sun),
    STREAKS("streaks", "Streaks", LucideIcons.Flame),
    QIBLA("qibla", "Qibla", LucideIcons.Compass),
    FRIENDS("friends", "Friends", LucideIcons.Users),
    SETTINGS("settings", "Settings", LucideIcons.Settings),
}

private const val FRIEND_DETAIL = "friend/{ownerId}"

@Composable
fun MainScreen(onSignOut: () -> Unit) {
    val nav = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) { ReminderBootstrap.sync(context) }

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val onTab = Tab.entries.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { if (onTab) BottomBar(nav = nav, backStack = backStack) },
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = Tab.TODAY.route,
            modifier = Modifier.fillMaxSize().padding(pad),
        ) {
            composable(Tab.TODAY.route) { TodayScreen() }
            composable(Tab.STREAKS.route) { StreaksScreen() }
            composable(Tab.QIBLA.route) { QiblaScreen() }
            composable(Tab.FRIENDS.route) {
                FriendsScreen(onOpenFriend = { ownerId -> nav.navigate("friend/$ownerId") })
            }
            composable(Tab.SETTINGS.route) { SettingsScreen(onSignOut = onSignOut) }
            composable(FRIEND_DETAIL) { entry ->
                FriendDetailScreen(
                    ownerId = entry.arguments?.getString("ownerId").orEmpty(),
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    nav: NavHostController,
    backStack: androidx.navigation.NavBackStackEntry?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .shadow(12.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { tab ->
            val selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true
            BottomItem(
                icon = tab.icon,
                label = tab.label,
                selected = selected,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(Tab.TODAY.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent,
        label = "navitem",
    )
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navtint",
    )
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
    }
}
