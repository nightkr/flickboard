package se.nullable.flickboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.nullable.flickboard.ui.BetaMenu
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.ui.SettingsHomePage
import se.nullable.flickboard.ui.SettingsSectionPage
import se.nullable.flickboard.ui.TutorialPage

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        Column {
                            val appSettings = LocalAppSettings.current
                            NavHost(
                                navController = navController,
                                startDestination = "settings",
                            ) {
                                composable("tutorial") {
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
                                composable("settings") {
                                    val hasCompletedTutorial = appSettings.hasCompletedTutorial
                                    LaunchedEffect(hasCompletedTutorial.state.value) {
                                        if (!hasCompletedTutorial.currentValue) {
                                            hasCompletedTutorial.currentValue = true
                                            navController.navigate("tutorial") {
                                                anim {
                                                    enter = 0
                                                }
                                            }
                                        }
                                    }
                                    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(id = R.string.app_name)) }) }) { padding ->
                                        SettingsHomePage(
                                            onNavigateToSection = { section ->
                                                navController.navigate("settings/${section.key}")
                                            },
                                            onNavigateToTutorial = { navController.navigate("tutorial") },
                                            onNavigateToBetaMenu = { navController.navigate("beta-menu") },
                                            modifier = Modifier.padding(padding)
                                        )
                                    }
                                }
                                composable("beta-menu") {
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
                                appSettings.all.forEach { settingsSection ->
                                    composable("settings/${settingsSection.key}") {
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
}