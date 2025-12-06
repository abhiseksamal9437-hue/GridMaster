package com.example.gridmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit // <--- NEW
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Engineering Hub", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    // --- DRAWER ICON ---
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { padding ->

        // --- 2. THE GRID ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {

            // --- SECTION 1: CRITICAL ---
            item(span = { GridItemSpan(2) }) { SectionTitle("Critical Diagnostics") }

            item {
                ModernToolCard("Battery Doctor", "SG Analysis", Icons.Default.Info, Color(0xFF2196F3)) { navController.navigate("tool_battery") }
            }
            item {
                ModernToolCard("SF6 Analyzer", "Dew Point", Icons.Default.Notifications, Color(0xFF00BCD4)) { navController.navigate("tool_sf6") }
            }

            // --- SECTION 2: PROTECTION ---
            item(span = { GridItemSpan(2) }) { SectionTitle("Protection & Relays") }

            item { ModernToolCard("Distance Relay", "Zone Reach", Icons.Default.PlayArrow, Color(0xFF673AB7)) { navController.navigate("tool_distance") } }
            item { ModernToolCard("CT Validator", "Knee Point", Icons.Default.Settings, Color(0xFF3F51B5)) { navController.navigate("tool_ct") } }
            item { ModernToolCard("Prot. Safety", "Pre-Work Check", Icons.Default.Warning, Color(0xFFD32F2F)) { navController.navigate("tool_safety") } }
            item { ModernToolCard("Smart Metering", "Vector Faults", Icons.Default.Search, Color(0xFF9C27B0)) { navController.navigate("tool_metering") } }

            // --- SECTION 3: ASSETS ---
            item(span = { GridItemSpan(2) }) { SectionTitle("Switchyard Assets") }

            item { ModernToolCard("Isolator Health", "Contact Res.", Icons.Default.Menu, Color(0xFFFF5722)) { navController.navigate("tool_isolator") } }
            item { ModernToolCard("Earth Pit", "Resistance", Icons.Default.Home, Color(0xFF4CAF50)) { navController.navigate("tool_earth") } }
            item { ModernToolCard("Transformer", "Moisture Calc", Icons.Default.Build, Color(0xFFFFC107)) { navController.navigate("tool_moisture") } }
            item { ModernToolCard("Oil Quality", "IS 1866 Check", Icons.Default.CheckCircle, Color(0xFFFF9800)) { navController.navigate("tool_oil") } }
            item { ModernToolCard("Capacitor", "Rise & Inrush", Icons.Default.Add, Color(0xFF03A9F4)) { navController.navigate("tool_cap") } }

            // --- SECTION 4: SAFETY ---
            item(span = { GridItemSpan(2) }) { SectionTitle("Site Safety") }

            item { ModernToolCard("Step & Touch", "Safe Limits", Icons.Default.Warning, Color(0xFFE91E63)) { navController.navigate("tool_steptouch") } }
            item { ModernToolCard("DC Ground", "Fault Finder", Icons.Default.Notifications, Color(0xFF607D8B)) { navController.navigate("tool_dc") } }

            // --- SECTION 5: OPERATIONAL SAFETY (NEW) ---
            item(span = { GridItemSpan(2) }) { SectionTitle("Operational Safety") }

            item { ModernToolCard("PTW Validator", "Permit Check", Icons.Default.Lock, Color(0xFFD32F2F)) { navController.navigate("tool_ptw") } }
            item { ModernToolCard("Clearance", "Safety Dist", Icons.Default.Info, Color(0xFFE64A19)) { navController.navigate("tool_clearance") } }
            item { ModernToolCard("PPE Guide", "Safety Gear", Icons.Default.Face, Color(0xFF1976D2)) { navController.navigate("tool_ppe") } }
            item { ModernToolCard("First Aid", "Shock Protocol", Icons.Default.Favorite, Color(0xFFC2185B)) { navController.navigate("tool_firstaid") } }

            // --- SECTION 6: ADVANCED ENGINEERING (NEW) ---
            item(span = { GridItemSpan(2) }) { SectionTitle("Advanced Engineering") }

            item { ModernToolCard("REF Stab.", "Voltage Calc", Icons.Default.Settings, Color(0xFF512DA8)) { navController.navigate("tool_ref") } }
            item { ModernToolCard("Over-Fluxing", "V/f Monitor", Icons.Default.Warning, Color(0xFFFFC107)) { navController.navigate("tool_flux") } }
            item { ModernToolCard("LBB Logic", "Breaker Fail", Icons.Default.Build, Color(0xFF607D8B)) { navController.navigate("tool_lbb") } }
            item { ModernToolCard("Thermo-Vis", "Severity Grader", Icons.Default.ThumbUp, Color(0xFFFF5722)) { navController.navigate("tool_thermo") } }
            item { ModernToolCard("IR Correct", "Temp Norm.", Icons.Default.Refresh, Color(0xFF009688)) { navController.navigate("tool_ir") } }

            // Spacer at bottom
            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// --- MODERN COMPONENTS ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun ModernToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom Row: Text
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}