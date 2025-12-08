package com.example.gridmaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppDrawer(
    currentRoute: String,
    navigateTo: (String) -> Unit,
    closeDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "Guest"
    val name = email.split("@")[0].replaceFirstChar { it.uppercase() }
    val initial = if (name.isNotEmpty()) name.first().toString() else "U"

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(250.dp),
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // --- 1. PREMIUM HEADER (Gmail Style) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.BottomStart)
            ) {
                // Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(
                        text = initial,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // --- 2. NAVIGATION ITEMS ---
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            DrawerItem(
                label = "Dashboard",
                icon = Icons.Default.Home,
                selected = currentRoute == "dashboard",
                onClick = { navigateTo("dashboard"); closeDrawer() }
            )

            DrawerItem(
                label = "Fault Logger",
                icon = Icons.Default.Warning,
                selected = currentRoute == "faults",
                onClick = { navigateTo("faults"); closeDrawer() }
            )

            DrawerItem(
                label = "Maintenance",
                icon = Icons.Default.List,
                selected = currentRoute == "maintenance",
                onClick = { navigateTo("maintenance"); closeDrawer() }
            )

            DrawerItem(
                label = "Duty Roster",
                icon = Icons.Default.DateRange,
                selected = currentRoute == "duty",
                onClick = { navigateTo("duty"); closeDrawer() }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                "ENGINEERING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            DrawerItem(
                label = "Smart Tools",
                icon = Icons.Default.Build,
                selected = currentRoute == "diagnostics",
                onClick = { navigateTo("diagnostics"); closeDrawer() }
            )
            DrawerItem(
                label = "Store Inventory",
                icon = Icons.Default.ShoppingCart, // Or Icons.Default.List
                selected = currentRoute == "store",
                onClick = { navigateTo("store"); closeDrawer() }
            )

            // Future expansion placeholders (commented out)
            // DrawerItem("Safety Manual", Icons.Default.Lock, false) {}
            // DrawerItem("Assets", Icons.Default.ShoppingCart, false) {}

            Spacer(Modifier.weight(1f)) // Push footer down if needed

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- 3. FOOTER (LOGOUT) ---
            DrawerItem(
                label = "Log Out",
                icon = Icons.Default.ExitToApp,
                selected = false,
                onClick = { closeDrawer(); onLogout() },
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else color

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)) // Pill shape like Gmail
            .clickable { onClick() },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(Modifier.width(24.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}