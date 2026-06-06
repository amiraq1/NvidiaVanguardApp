package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("chat") }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = VanguardSurface,
                            contentColor = VanguardAccent
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == "chat",
                                onClick = { currentScreen = "chat" },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Chat") },
                                label = { Text("CHAT") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VanguardAccent,
                                    selectedTextColor = VanguardAccent,
                                    unselectedIconColor = VanguardGray,
                                    unselectedTextColor = VanguardGray,
                                    indicatorColor = VanguardBorder
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == "profile",
                                onClick = { currentScreen = "profile" },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Dashboard") },
                                label = { Text("CORE") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VanguardAccent,
                                    selectedTextColor = VanguardAccent,
                                    unselectedIconColor = VanguardGray,
                                    unselectedTextColor = VanguardGray,
                                    indicatorColor = VanguardBorder
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    val viewModel: NvidiaViewModel = viewModel()
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "chat" -> NvidiaBentoScreen(viewModel)
                            "profile" -> NvidiaCoreScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
