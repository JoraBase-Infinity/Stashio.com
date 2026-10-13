package com.aistudio.poysha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.screens.*
import com.aistudio.poysha.ui.theme.PoyshaPrimary
import com.aistudio.poysha.ui.theme.PoyshaSecondary
import com.aistudio.poysha.ui.theme.PoyshaTextLight
import com.aistudio.poysha.ui.theme.PoyshaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoyshaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PoyshaAppNav()
                }
            }
        }
    }
}

@Composable
fun PoyshaAppNav() {
    val activeShop by PosRepository.activeShop.collectAsState()
    val currentUser by PosRepository.currentUser.collectAsState()

    var currentScreen by remember {
        mutableStateOf(
            if (currentUser == null) "AUTH"
            else if (activeShop == null) "DASHBOARD"
            else "HOME"
        )
    }

    // Handle Android system back button
    BackHandler(enabled = currentScreen != "AUTH") {
        when (currentScreen) {
            "HOME" -> {
                currentScreen = "DASHBOARD"
            }
            "DASHBOARD" -> {
                currentScreen = "AUTH"
            }
            else -> {
                currentScreen = "HOME"
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen == "HOME") {
                Surface(
                    color = Color.White,
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = true,
                            onClick = { currentScreen = "HOME" },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PoyshaPrimary,
                                selectedTextColor = PoyshaPrimary,
                                indicatorColor = PoyshaSecondary.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { currentScreen = "REPORTS" },
                            icon = { Icon(Icons.Default.PieChart, contentDescription = "Reports") },
                            label = { Text("Reports", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = PoyshaTextLight,
                                unselectedTextColor = PoyshaTextLight
                            )
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { currentScreen = "SETTINGS" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = PoyshaTextLight,
                                unselectedTextColor = PoyshaTextLight
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    "AUTH" -> AuthScreen(
                        onLoginSuccess = {
                            currentScreen = if (PosRepository.activeShop.value != null) "HOME" else "DASHBOARD"
                        }
                    )

                    "DASHBOARD" -> ShopDashboardScreen(
                        onShopSelected = {
                            currentScreen = "HOME"
                        },
                        onLogout = {
                            currentScreen = "AUTH"
                        }
                    )

                    "HOME" -> HomeScreen(
                        onNavigate = { route ->
                            currentScreen = route
                        }
                    )

                    "SELL", "ESTIMATE" -> SellScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    "PURCHASE" -> PurchaseScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    "PRODUCTS", "STOCK_BOOK" -> ProductListScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    "SALES_BOOK" -> LedgerBooksScreen(
                        initialTab = "SALES",
                        onBack = { currentScreen = "HOME" }
                    )

                    "PURCHASE_BOOK" -> LedgerBooksScreen(
                        initialTab = "PURCHASES",
                        onBack = { currentScreen = "HOME" }
                    )

                    "DUE_BOOK" -> LedgerBooksScreen(
                        initialTab = "DUES",
                        onBack = { currentScreen = "HOME" }
                    )

                    "EXPENSES_BOOK" -> LedgerBooksScreen(
                        initialTab = "EXPENSES",
                        onBack = { currentScreen = "HOME" }
                    )

                    "CASHBOX" -> CashboxScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    "CONTACTS" -> ContactsScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    "REPORTS" -> ReportsScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    "SETTINGS" -> SettingsScreen(
                        onBack = { currentScreen = "HOME" },
                        onSwitchShop = { currentScreen = "DASHBOARD" },
                        onLogout = { currentScreen = "AUTH" }
                    )

                    "NOTES" -> NotesScreen(
                        onBack = { currentScreen = "HOME" }
                    )

                    else -> HomeScreen(
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        }
    }
}
