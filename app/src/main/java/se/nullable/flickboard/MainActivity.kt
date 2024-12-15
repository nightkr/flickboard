package se.nullable.flickboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import se.nullable.flickboard.ui.BetaMenu
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.ui.SettingsHomePage
import se.nullable.flickboard.ui.SettingsSectionPage
import se.nullable.flickboard.ui.TutorialPage
import se.nullable.flickboard.ui.consumeExcludedInsets
import se.nullable.flickboard.ui.help.KeyboardDescriber
import se.nullable.flickboard.ui.theme.Transition

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Composable
        fun NavigateUpIcon(navController: NavController) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    "Up"
                )
            }
        }
        setContent {
            FlickBoardParent {
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .consumeExcludedInsets()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        Column {
                            val appSettings = LocalAppSettings.current
                            NavHost(
                                navController = navController,
                                startDestination = MainActivityNav.SettingsMain,
                                enterTransition = { Transition.pushEnter },
                                exitTransition = { Transition.pushExit },
                                popEnterTransition = { Transition.popEnter },
                                popExitTransition = { Transition.popExit },
                            ) {
                                composable<MainActivityNav.Tutorial> {
                                    Scaffold(topBar = {
                                        TopAppBar(
                                            title = {},
                                            actions = {
                                                Box(Modifier.clickable { navController.navigateUp() }) {
                                                    Text("SKIP", Modifier.padding(8.dp))
                                                }
                                            })
                                    }) { padding ->
                                        TutorialPage(
                                            onFinish = { navController.navigateUp() },
                                            modifier = Modifier.padding(padding)
                                        )
                                    }
                                }
                                composable<MainActivityNav.SettingsMain> {
                                    val hasCompletedTutorial = appSettings.hasCompletedTutorial
                                    LaunchedEffect(hasCompletedTutorial.state.value) {
                                        if (!hasCompletedTutorial.currentValue) {
                                            hasCompletedTutorial.currentValue = true
                                            navController.navigate(MainActivityNav.Tutorial) {
                                                anim {
                                                    enter = 0
                                                    exit = 0
                                                }
                                            }
                                        }
                                    }
                                    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(id = R.string.app_name)) }) }) { padding ->
                                        SettingsHomePage(
                                            onNavigateToSection = { section ->
                                                navController.navigate(
                                                    MainActivityNav.SettingsSection(
                                                        section.key
                                                    )
                                                )
                                            },
                                            onNavigateToTutorial = {
                                                navController.navigate(MainActivityNav.Tutorial)
                                            },
                                            onNavigateToBetaMenu = {
                                                navController.navigate(MainActivityNav.BetaMenu)
                                            },
                                            onNavigateToDescriber = {
                                                navController.navigate(MainActivityNav.KeyboardDescriber)
                                            },
                                            modifier = Modifier.padding(padding)
                                        )
                                    }
                                }
                                composable<MainActivityNav.KeyboardDescriber> {
                                    Scaffold(topBar = {
                                        TopAppBar(
                                            title = { Text("What Does This Do?") },
                                            navigationIcon = { NavigateUpIcon(navController) })
                                    }) { padding ->
                                        KeyboardDescriber(
                                            modifier = Modifier.padding(padding)
                                        )
                                    }
                                }
                                composable<MainActivityNav.BetaMenu> {
                                    Scaffold(topBar = {
                                        TopAppBar(
                                            title = { Text("Beta Options") },
                                            navigationIcon = { NavigateUpIcon(navController) })
                                    }) { padding ->
                                        BetaMenu(
                                            modifier = Modifier.padding(padding)
                                        )
                                    }
                                }
                                composable<MainActivityNav.SettingsSection> { backStackEntry ->
                                    val route =
                                        backStackEntry.toRoute<MainActivityNav.SettingsSection>()
                                    val settingsSection =
                                        appSettings.all.find { it.key == route.sectionKey }
                                            ?: throw Exception("No settings section with key ${route.sectionKey}")
                                    Scaffold(topBar = {
                                        TopAppBar(
                                            title = { Text(settingsSection.label) },
                                            navigationIcon = { NavigateUpIcon(navController) })
                                    }) { padding ->
                                        SettingsSectionPage(
                                            section = settingsSection,
                                            modifier = Modifier.padding(padding)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class MainActivityNav {
    @Serializable
    data object SettingsMain : MainActivityNav()

    @Serializable
    data class SettingsSection(val sectionKey: String) : MainActivityNav()

    @Serializable
    data object Tutorial : MainActivityNav()

    @Serializable
    data object KeyboardDescriber : MainActivityNav()

    @Serializable
    data object BetaMenu : MainActivityNav()
}