package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.HistoryRepository
import com.example.data.PreferencesManager
import com.example.ui.components.AppTopBar
import com.example.ui.components.MainTopNavHeader
import com.example.ui.screens.*
import com.example.ui.screens.tools.*
import com.example.ui.theme.SmartCalculatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val historyRepository = HistoryRepository(database.historyDao())
        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState()
            val amoledBlack by preferencesManager.amoledBlack.collectAsState()
            val userName by preferencesManager.userName.collectAsState()
            val avatarUri by preferencesManager.userAvatarUri.collectAsState()

            SmartCalculatorTheme(
                themeMode = themeMode,
                amoledMode = amoledBlack
            ) {
                val coroutineScope = rememberCoroutineScope()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "calculator"

                // Track expression to pass to calculator if user clicks "Use" in history
                var preloadedCalcExpression by remember { mutableStateOf<String?>(null) }

                // The 4 core navigation tabs: Calculator, Taraju, Tools, History
                val rootTabs = listOf("calculator", "taraju", "tools", "history")
                val isRootTab = currentRoute in rootTabs

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isRootTab) {
                            MainTopNavHeader(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                currentTheme = themeMode,
                                onThemeChange = { newMode ->
                                    coroutineScope.launch {
                                        preferencesManager.setThemeMode(newMode)
                                    }
                                },
                                onProfileClick = {
                                    navController.navigate("profile")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                },
                                avatarUri = avatarUri,
                                userName = userName
                            )
                        } else {
                            val childTitle = when (currentRoute) {
                                "settings" -> "Settings"
                                "profile" -> "Profile"
                                "gst_calculator" -> "GST Calculator"
                                "bmi_calculator" -> "BMI Calculator"
                                "sip_calculator" -> "SIP Calculator"
                                "emi_calculator" -> "Loan EMI Calculator"
                                "discount_calculator" -> "Discount Calculator"
                                "fuel_calculator" -> "Fuel Cost Calculator"
                                "unit_converter" -> "Unit Converter"
                                "currency_converter" -> "Currency Converter"
                                "tip_split" -> "Tip & Split Calculator"
                                "percentage" -> "Percentage Calculator"
                                "age_calculator" -> "Age Calculator"
                                "date_calculator" -> "Date Calculator"
                                "fd_calculator" -> "Fixed Deposit (FD)"
                                "rd_calculator" -> "Recurring Deposit (RD)"
                                "ppf_calculator" -> "PPF Calculator"
                                "cagr_calculator" -> "CAGR Calculator"
                                "inflation_calculator" -> "Inflation Calculator"
                                "interest_calculator" -> "Interest Calculator"
                                "margin_calculator" -> "Profit Margin Calculator"
                                "statistics" -> "Statistics Calculator"
                                "random_gen" -> "Random Generator"
                                else -> "Calculator"
                            }
                            AppTopBar(
                                title = childTitle,
                                subtitle = null,
                                showBack = true,
                                onBackClick = { navController.popBackStack() },
                                avatarUri = avatarUri,
                                userName = userName,
                                currentTheme = themeMode,
                                onThemeChange = { newMode ->
                                    coroutineScope.launch {
                                        preferencesManager.setThemeMode(newMode)
                                    }
                                },
                                onProfileClick = { navController.navigate("profile") },
                                onSettingsClick = { navController.navigate("settings") }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "calculator",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Root: Standard Calculator (Main Entry Screen)
                        composable("calculator") {
                            MainCalculatorScreen(
                                historyRepository = historyRepository,
                                preferencesManager = preferencesManager,
                                onNavigateToHistory = {
                                    navController.navigate("history") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateToTaraju = {
                                    navController.navigate("taraju") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                initialExpression = preloadedCalcExpression
                            )
                        }

                        // 2. Root: Taraju Page (Dedicated Rate & Weight Calculator)
                        composable("taraju") {
                            TarajuScreen(
                                historyRepository = historyRepository,
                                preferencesManager = preferencesManager
                            )
                        }

                        // 3. Root: Tools Hub
                        composable("tools") {
                            ToolsHubScreen(
                                preferencesManager = preferencesManager,
                                onToolSelected = { toolId ->
                                    navController.navigate(toolId)
                                }
                            )
                        }

                        // 4. Root: History
                        composable("history") {
                            HistoryScreen(
                                historyRepository = historyRepository,
                                onUseCalculation = { res ->
                                    preloadedCalcExpression = res
                                    navController.navigate("calculator") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        // Profile
                        composable("profile") {
                            ProfileScreen(
                                preferencesManager = preferencesManager,
                                historyRepository = historyRepository,
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        // Settings
                        composable("settings") {
                            SettingsScreen(
                                preferencesManager = preferencesManager,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: EMI
                        composable("emi_calculator") {
                            EmiCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: GST
                        composable("gst_calculator") {
                            GstCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Discount
                        composable("discount_calculator") {
                            DiscountCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Profit & Loss
                        composable("profit_loss") {
                            ProfitLossCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: SIP
                        composable("sip_calculator") {
                            SipCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: FD & RD
                        composable("fd_rd_calculator") {
                            FdRdCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Interest
                        composable("interest_calculator") {
                            InterestCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Age
                        composable("age_calculator") {
                            AgeCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Date
                        composable("date_calculator") {
                            DateCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Unit Converter
                        composable("unit_converter") {
                            UnitConverterScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Currency Converter
                        composable("currency_converter") {
                            CurrencyConverterScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Tip & Split
                        composable("tip_split") {
                            TipSplitCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Percentage
                        composable("percentage") {
                            PercentageCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Statistics
                        composable("statistics") {
                            StatisticsCalculatorScreen(
                                historyRepository = historyRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Tool: Random Generator
                        composable("random_gen") {
                            RandomGeneratorScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
