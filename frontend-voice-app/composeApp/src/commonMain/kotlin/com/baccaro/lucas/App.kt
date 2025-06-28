package com.baccaro.lucas

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baccaro.lucas.authentication.presentation.AuthViewModel
import com.baccaro.lucas.authentication.presentation.LoginScreen
import com.baccaro.lucas.authentication.presentation.SignUpScreen
import com.baccaro.lucas.conversation.presentation.ConversationScreen
import com.baccaro.lucas.di.appModule
import com.baccaro.lucas.home.presentation.HomeScreen
import com.baccaro.lucas.home.presentation.TopicsViewModel
import com.baccaro.lucas.profile.presentation.ProfileScreen
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

// --- 1. Sealed Class para Rutas (Type-Safe Navigation) ---
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Main : Screen("main_screen") // Contenedor para la navegación con BottomBar

    // Rutas de la BottomBar
    object Home : Screen("home")
    object Profile : Screen("profile")

    // Ruta de detalle con argumento
    object Conversation : Screen("conversation/{instructions}") {
        fun createRoute(instructions: String) = "conversation/$instructions"
    }
}

val bottomBarItems = listOf(
    Screen.Home,
    Screen.Profile,
)

// Helper para pasar el NavController principal a composables anidados
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            val navController = rememberNavController()
            // --- FIX: Proveer el NavController principal a través de CompositionLocal ---
            CompositionLocalProvider(LocalNavController provides navController) {
                NavHost(navController = navController, startDestination = Screen.Login.route) {
                    composable(Screen.Login.route) {
                        val authViewModel = koinInject<AuthViewModel>()
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                        )
                    }
                    composable(Screen.SignUp.route) {
                        val authViewModel = koinInject<AuthViewModel>()
                        SignUpScreen(
                            viewModel = authViewModel,
                            onSignUpSuccess = { navController.navigate(Screen.Login.route) }
                        )
                    }
                    composable(Screen.Main.route) {
                        MainScreenView()
                    }
                    composable(
                        route = Screen.Conversation.route,
                        arguments = listOf(navArgument("instructions") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val instructions = backStackEntry.arguments?.getString("instructions") ?: ""
                        ConversationScreen(instructions)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenView() {
    val nestedNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomBarItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            nestedNavController.navigate(screen.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(getLabelForScreen(screen)) },
                        icon = {
                            when (screen) {
                                Screen.Home -> Icon(Icons.Default.Home, contentDescription = null)
                                Screen.Profile -> Icon(Icons.Default.Person, contentDescription = null)
                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val topicsViewModel = koinInject<TopicsViewModel>()
                val mainNavController = LocalNavController.current
                HomeScreen(
                    viewModel = topicsViewModel,
                    onNavigateDetail = { prompt ->
                        mainNavController.navigate(Screen.Conversation.createRoute(prompt))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

private fun getLabelForScreen(screen: Screen): String {
    return when (screen) {
        Screen.Home -> "Home"
        Screen.Profile -> "Perfil"
        else -> ""
    }
}