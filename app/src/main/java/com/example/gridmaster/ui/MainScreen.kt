package com.example.gridmaster.ui

import androidx.compose.foundation.layout.padding
// REMOVED: Bottom Bar Icons imports (Build, DateRange, List, Warning) are no longer needed here
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Import your tools & components
import com.example.gridmaster.ui.tools.*
import com.example.gridmaster.ui.components.AppDrawer // <--- IMPORT THE DRAWER

@Composable
fun MainScreen(
    maintenanceViewModel: MaintenanceViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    // --- NEW: DRAWER STATE & SCOPE ---
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun performLogout() {
        auth.signOut()
        navController.navigate("login") { popUpTo(0) { inclusive = true } }
    }

    // --- NEW: WRAP EVERYTHING IN NAVIGATION DRAWER ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Get current route for highlighting
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

            AppDrawer(
                currentRoute = currentRoute,
                navigateTo = { route ->
                    navController.navigate(route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                closeDrawer = { scope.launch { drawerState.close() } },
                onLogout = { performLogout() }
            )
        }
    ) {
        // --- SCAFFOLD CONTENT (No Bottom Bar anymore) ---
        Scaffold(
            // bottomBar = { ... }  <--- DELETED BOTTOM BAR CODE
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            ) {
                // LOGIN & SPLASH (No Drawer needed here)
                composable("login") {
                    LoginScreen(onLoginSuccess = {
                        navController.navigate("splash") { popUpTo("login") { inclusive = true } }
                    })
                }
                composable("splash") {
                    SplashScreen {
                        navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
                    }
                }

                // --- MAIN SCREENS (Now receiving onOpenDrawer) ---

                composable("dashboard") {
                    DashboardScreen(
                        navController,
                        maintenanceViewModel,
                        isDarkTheme,
                        onToggleTheme,
                        onLogout = { performLogout() },
                        onOpenDrawer = { scope.launch { drawerState.open() } } // <--- NEW PARAMETER
                    )
                }

                composable("faults") {
                    FaultScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } } // <--- NEW PARAMETER
                    )
                }

                composable("maintenance") {
                    MaintenanceScreen(
                        viewModel = maintenanceViewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onOpenDrawer = { scope.launch { drawerState.open() } } // <--- NEW PARAMETER
                    )
                }

                composable("duty") {
                    DutyScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } } // <--- NEW PARAMETER
                    )
                }

                composable("diagnostics") {
                    DiagnosticsScreen(
                        navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } } // <--- NEW PARAMETER
                    )
                }

                // --- TOOLS (These have Back buttons, so no Drawer needed) ---
                composable("tool_battery") { ToolBattery(onBack = { navController.popBackStack() }) }
                composable("tool_distance") { ToolDistance(onBack = { navController.popBackStack() }) }
                composable("tool_ct") { ToolCT(onBack = { navController.popBackStack() }) }
                composable("tool_safety") { ToolSafety(onBack = { navController.popBackStack() }) }
                composable("tool_sf6") { ToolSF6(onBack = { navController.popBackStack() }) }
                composable("tool_isolator") { ToolIsolator(onBack = { navController.popBackStack() }) }
                composable("tool_earth") { ToolEarth(onBack = { navController.popBackStack() }) }
                composable("tool_metering") { ToolMetering(onBack = { navController.popBackStack() }) }
                composable("tool_moisture") { ToolMoisture(onBack = { navController.popBackStack() }) }
                composable("tool_cap") { ToolCapacitor(onBack = { navController.popBackStack() }) }
                composable("tool_steptouch") { ToolStepTouch(onBack = { navController.popBackStack() }) }
                composable("tool_dc") { ToolDC(onBack = { navController.popBackStack() }) }
                composable("tool_oil") { ToolOil(onBack = { navController.popBackStack() }) }

                // NEW WIZARDS
                composable("tool_breaker") { com.example.gridmaster.ui.tools.ToolBreakerWizard(onBack = { navController.popBackStack() }) }
                composable("tool_annun") { com.example.gridmaster.ui.tools.ToolAnnunciation(onBack = { navController.popBackStack() }) }
                composable("tool_meter_wiz") { com.example.gridmaster.ui.tools.ToolMeteringWizard(onBack = { navController.popBackStack() }) }
                composable("tool_ptw") { com.example.gridmaster.ui.tools.ToolPTW(onBack = { navController.popBackStack() }) }
                composable("tool_clearance") { com.example.gridmaster.ui.tools.ToolClearance(onBack = { navController.popBackStack() }) }
                composable("tool_ppe") { com.example.gridmaster.ui.tools.ToolPPE(onBack = { navController.popBackStack() }) }
                composable("tool_firstaid") { com.example.gridmaster.ui.tools.ToolFirstAid(onBack = { navController.popBackStack() }) }
                composable("tool_ref") { com.example.gridmaster.ui.tools.ToolREF(onBack = { navController.popBackStack() }) }
                composable("tool_flux") { com.example.gridmaster.ui.tools.ToolFlux(onBack = { navController.popBackStack() }) }
                composable("tool_lbb") { com.example.gridmaster.ui.tools.ToolLBB(onBack = { navController.popBackStack() }) }
                composable("tool_thermo") { com.example.gridmaster.ui.tools.ToolThermo(onBack = { navController.popBackStack() }) }
                composable("tool_ir") { com.example.gridmaster.ui.tools.ToolIRCorr(onBack = { navController.popBackStack() }) }
            }
        }
    }
}