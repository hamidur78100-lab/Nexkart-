package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonMint
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.BlackPure
import com.example.ui.theme.SlateGrey
import com.example.ui.viewmodel.MarketplaceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MarketplaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContainer(viewModel)
            }
        }
    }
}

@Composable
fun MainContainer(viewModel: MarketplaceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Primary bottom bar destinations
    val bottomTabs = listOf(
        NavigationTabItem("home", "Browse", Icons.Filled.Store),
        NavigationTabItem("reels", "Reels", Icons.Filled.PlayCircle),
        NavigationTabItem("post_ad", "Post Ad", Icons.Filled.AddCircle),
        NavigationTabItem("profile", "Profile", Icons.Filled.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        bottomBar = {
            // Only show bottom navigation on primary screens
            val currentRoute = currentDestination?.route
            val shouldShowBottomBar = bottomTabs.any { it.route == currentRoute }
            
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = NeonMint,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    bottomTabs.forEach { tab ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = isSelected,
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BlackPure,
                                selectedTextColor = NeonMint,
                                unselectedIconColor = SlateGrey,
                                unselectedTextColor = SlateGrey,
                                indicatorColor = CyberCyan
                            ),
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> navController.navigate("detail/$id") },
                    onNavigateToPost = { navController.navigate("post_ad") },
                    onNavigateToAdmin = { navController.navigate("admin") }
                )
            }
            
            composable("detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 1
                ProductDetailScreen(
                    listingId = id,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { chatId -> navController.navigate("chat/$chatId") },
                    onNavigateToDetails = { detailId -> navController.navigate("detail/$detailId") }
                )
            }
            
            composable("reels") {
                ReelsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> navController.navigate("detail/$id") },
                    onNavigateToChat = { chatId -> navController.navigate("chat/$chatId") }
                )
            }
            
            composable("post_ad") {
                PostAdScreen(
                    viewModel = viewModel,
                    onNavigateHome = { navController.navigate("home") }
                )
            }
            
            composable("chat/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 1
                ChatScreen(
                    listingId = id,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("admin") {
                AdminScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> navController.navigate("detail/$id") },
                    onNavigateToPost = { navController.navigate("post_ad") }
                )
            }
            
            composable("developer") {
                LaunchedEffect(Unit) {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
                Box(modifier = Modifier.fillMaxSize().background(DarkBackground))
            }
            
        }
    }
}

data class NavigationTabItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
