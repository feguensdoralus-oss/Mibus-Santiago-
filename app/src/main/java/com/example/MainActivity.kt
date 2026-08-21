package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.TransitRepository
import com.example.firebase.FirebaseManager
import com.example.ui.components.TransitTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RedPrimary

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home)
    object Map : Screen("map", "Buses", Icons.Filled.DirectionsBus, Icons.Outlined.DirectionsBus)
    object Bip : Screen("bip", "Saldo BIP", Icons.Filled.CreditCard, Icons.Outlined.CreditCard)
    object Metro : Screen("metro", "Metro", Icons.Filled.Subway, Icons.Outlined.Subway)
    object Favorites : Screen("favorites", "Favoritos", Icons.Filled.Star, Icons.Outlined.StarOutline)
    object Alerts : Screen("alerts", "Alertas", Icons.Filled.Notifications, Icons.Outlined.Notifications)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TransitRepository(applicationContext)
        val firebaseManager = FirebaseManager()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

                val screens = listOf(
                    Screen.Home,
                    Screen.Map,
                    Screen.Bip,
                    Screen.Metro,
                    Screen.Favorites
                )

                val currentScreenTitle = when (currentRoute) {
                    Screen.Home.route -> "MiBus Santiago"
                    Screen.Map.route -> "Bus en Vivo y Radar"
                    Screen.Bip.route -> "Tarjeta BIP!"
                    Screen.Metro.route -> "Metro de Santiago"
                    Screen.Favorites.route -> "Mis Favoritos"
                    Screen.Alerts.route -> "Alertas del Servicio"
                    else -> "MiBus Santiago"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TransitTopBar(
                            title = currentScreenTitle,
                            canNavigateBack = currentRoute == Screen.Alerts.route,
                            onNavigateBack = { navController.popBackStack() },
                            actions = {
                                if (currentRoute != Screen.Alerts.route) {
                                    IconButton(
                                        onClick = { navController.navigate(Screen.Alerts.route) },
                                        modifier = Modifier.testTag("action_alerts_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = "Alertas del Servicio",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        Surface(
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 10.dp,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(76.dp)
                            ) {
                                screens.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_${screen.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                navController = navController,
                                repository = repository
                            )
                        }
                        composable(Screen.Map.route) {
                            MapScreen(
                                repository = repository,
                                firebaseManager = firebaseManager
                            )
                        }
                        composable(Screen.Bip.route) {
                            BipScreen(repository = repository)
                        }
                        composable(Screen.Metro.route) {
                            MetroScreen(repository = repository)
                        }
                        composable(Screen.Favorites.route) {
                            FavoritesScreen(
                                repository = repository,
                                firebaseManager = firebaseManager,
                                onNavigateToMap = {
                                    navController.navigate(Screen.Map.route) {
                                        popUpTo(navController.graph.findStartDestination().id)
                                    }
                                }
                            )
                        }
                        composable(Screen.Alerts.route) {
                            AlertsScreen(repository = repository)
                        }
                    }
                }
            }
        }
    }
}
