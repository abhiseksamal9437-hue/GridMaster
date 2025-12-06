package com.example.gridmaster.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

// ==========================================
// MODERN TOOL WRAPPER (Dark Mode Ready)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(toolId: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    val info = toolKnowledgeMap[toolId] ?: return
    var showInfo by remember { mutableStateOf(false) }

    // INFO POPUP
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            icon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(info.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            "Source: ${info.source}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(info.description, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text(info.explanation, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = { showInfo = false }) { Text("Got It") } }
        )
    }

    // MAIN LAYOUT
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showInfo = true }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))) {
                    Icon(Icons.Default.Info, "Info", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(info.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 32.sp)
            Text(info.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
            }
        }
    }
}

// --- COMPONENTS ---
@Composable
fun ToolInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ResultCard(text: String, isGood: Boolean) {
    val bgColor = if (isGood) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    Card(colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(if (isGood) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(if (isGood) "Status: Healthy" else "Status: Alert", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(text, color = Color.White)
            }
        }
    }
}

// ==========================================
// 13 ORIGINAL TOOLS
// ==========================================

// 1. BATTERY DOCTOR
@Composable
fun ToolBattery(onBack: () -> Unit) {
    val cells = remember { mutableStateListOf("", "", "", "", "", "", "", "", "", "") }
    var avg by remember { mutableStateOf("1200") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("battery", onBack) {
        Text("Target SG:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        ToolInput("Average SG (1200)", avg) { avg = it }
        Text("Readings (10 Cells):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until 5) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val i1 = i * 2; val i2 = i * 2 + 1
                    OutlinedTextField(value = cells[i1], onValueChange = { cells[i1] = it }, label = { Text("${i1+1}") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = cells[i2], onValueChange = { cells[i2] = it }, label = { Text("${i2+1}") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                }
            }
        }
        Button(onClick = {
            val t = avg.toIntOrNull() ?: 1200
            val maxDev = cells.mapNotNull { it.toIntOrNull() }.maxOfOrNull { abs(it - t) } ?: 0
            if (maxDev > 30) { isGood = false; res = "Deviation: $maxDev (>30)\nStart Equalizing Charge!" }
            else { isGood = true; res = "Deviation: $maxDev\nSystem Healthy." }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("ANALYZE") }
        if (res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 2. DISTANCE RELAY
@Composable
fun ToolDistance(onBack: () -> Unit) {
    var len by remember { mutableStateOf("") }
    var lShort by remember { mutableStateOf("") }
    var lLong by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    ToolScreen("distance", onBack) {
        ToolInput("Line Length (km)", len) { len = it }
        ToolInput("Next Shortest Line (km)", lShort) { lShort = it }
        ToolInput("Next Longest Line (km)", lLong) { lLong = it }
        Button(onClick = {
            val l = len.toDoubleOrNull() ?: 0.0; val s = lShort.toDoubleOrNull() ?: 0.0; val lg = lLong.toDoubleOrNull() ?: 0.0
            res = "Z1 (80%): ${String.format("%.2f", l*0.8)} km\nZ2 (100%+50%): ${String.format("%.2f", l+s*0.5)} km\nZ3 (100%+100%): ${String.format("%.2f", l+lg)} km"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CALCULATE") }
        if (res.isNotEmpty()) ResultCard(res, true)
    }
}

// 3. CT VALIDATOR
@Composable
fun ToolCT(onBack: () -> Unit) {
    var i by remember { mutableStateOf("") }
    var rct by remember { mutableStateOf("") }
    var rl by remember { mutableStateOf("") }
    var vk by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("ct_knee", onBack) {
        ToolInput("Max Fault Current (kA)", i) { i = it }
        ToolInput("CT Resistance (Ω)", rct) { rct = it }
        ToolInput("Lead Resistance (Ω)", rl) { rl = it }
        ToolInput("Nameplate Vk (V)", vk) { vk = it }
        Button(onClick = {
            val ifault = (i.toDoubleOrNull() ?: 0.0) * 1000
            val r = (rct.toDoubleOrNull() ?: 0.0) + 2 * (rl.toDoubleOrNull() ?: 0.0)
            val req = 2.0 * (ifault/1000) * r // Simplified K*If*R
            val act = vk.toDoubleOrNull() ?: 0.0
            isGood = act >= req
            res = "Required Vk: ${String.format("%.1f", req)} V\nNameplate: $act V"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("VALIDATE") }
        if (res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 4. SAFETY CHECKLIST
@Composable
fun ToolSafety(onBack: () -> Unit) {
    val checks = remember { mutableStateListOf(false, false, false) }
    val allSafe = checks.all { it }
    ToolScreen("safety", onBack) {
        Text("Pre-Work Verification:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[0], { checks[0] = it }); Text("CT Secondary Shorted?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[1], { checks[1] = it }); Text("PT Secondary Open?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[2], { checks[2] = it }); Text("DC Polarity Checked?", color = MaterialTheme.colorScheme.onSurface) }
        ResultCard(if(allSafe) "SAFE TO PROCEED" else "UNSAFE CONDITIONS", allSafe)
    }
}

// 5. SF6 DEW POINT
@Composable
fun ToolSF6(onBack: () -> Unit) {
    var dew by remember { mutableStateOf("") }
    var pres by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("sf6", onBack) {
        ToolInput("Measured Dew Point (°C)", dew) { dew = it }
        ToolInput("Pressure (Bar)", pres) { pres = it }
        Button(onClick = {
            val d = dew.toDoubleOrNull() ?: 0.0
            val corr = d - (2 * ((pres.toDoubleOrNull()?:1.0) - 1))
            isGood = corr < -27
            res = "Atm. Pressure: ${String.format("%.1f", corr)}°C"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("ANALYZE") }
        if (res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 6. ISOLATOR
@Composable
fun ToolIsolator(onBack: () -> Unit) {
    var r by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("isolator", onBack) {
        ToolInput("Contact Resistance (µΩ)", r) { r = it }
        Button(onClick = {
            val rv = r.toDoubleOrNull() ?: 0.0
            isGood = rv < 300
            res = if(isGood) "Value: $rv µΩ (Healthy)" else "Value: $rv µΩ (Hotspot Risk!)"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CHECK") }
        if (res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 7. EARTH RESISTANCE
@Composable
fun ToolEarth(onBack: () -> Unit) {
    var r by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(0) }
    var isGood by remember { mutableStateOf(true) }
    var res by remember { mutableStateOf("") }
    ToolScreen("earth", onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { type = 0 }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(type==0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) { Text("EHV") }
            Button(onClick = { type = 1 }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(type==1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) { Text("HV") }
        }
        ToolInput("Resistance (Ω)", r) { r = it }
        Button(onClick = {
            val limit = if(type==0) 1.0 else 5.0
            val rv = r.toDoubleOrNull() ?: 0.0
            isGood = rv < limit
            res = "Limit: $limit Ω\nStatus: ${if(isGood) "Pass" else "Fail"}"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("EVALUATE") }
        if (res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 8. METERING
@Composable
fun ToolMetering(onBack: () -> Unit) {
    var i1 by remember { mutableStateOf("") }
    var i2 by remember { mutableStateOf("") }
    var i3 by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    ToolScreen("metering", onBack) {
        Text("Active Current (- for Reverse):", color = MaterialTheme.colorScheme.secondary)
        ToolInput("R-Phase", i1) { i1 = it }
        ToolInput("Y-Phase", i2) { i2 = it }
        ToolInput("B-Phase", i3) { i3 = it }
        Button(onClick = {
            val r = i1.toDoubleOrNull() ?: 0.0
            val y = i2.toDoubleOrNull() ?: 0.0
            val b = i3.toDoubleOrNull() ?: 0.0
            res = when {
                r < 0 && y > 0 && b > 0 -> "R-Phase CT Reversed"
                r > 0 && y < 0 && b > 0 -> "Y-Phase CT Reversed"
                r > 0 && y > 0 && b < 0 -> "B-Phase CT Reversed"
                r < 0 && y < 0 -> "R & Y CTs Swapped/Reversed"
                else -> "Polarity Likely Correct"
            }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("DIAGNOSE") }
        if (res.isNotEmpty()) ResultCard(res, !res.contains("Reversed"))
    }
}

// 9. MOISTURE
@Composable
fun ToolMoisture(onBack: () -> Unit) {
    var ppm by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("moisture", onBack) {
        ToolInput("Oil Moisture (PPM)", ppm) { ppm = it }
        ToolInput("Oil Temp (°C)", temp) { temp = it }
        Button(onClick = {
            val p = ppm.toDoubleOrNull() ?: 10.0
            val t = temp.toDoubleOrNull() ?: 40.0
            val paperM = 2.24 * exp(-0.04 * t) * p.pow(0.7)
            isGood = paperM < 2.0
            res = "Paper Moisture: ${String.format("%.2f", paperM)}%"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CALCULATE") }
        if (res.isNotEmpty()) ResultCard(res + if(isGood) "\nDry/Healthy" else "\nWet (Dryout Req)", isGood)
    }
}

// 10. CAPACITOR
@Composable
fun ToolCapacitor(onBack: () -> Unit) {
    var mvar by remember { mutableStateOf("") }
    var mvasc by remember { mutableStateOf("3000") }
    var res by remember { mutableStateOf("") }
    ToolScreen("cap_bank", onBack) {
        ToolInput("Bank Rating (MVAR)", mvar) { mvar = it }
        ToolInput("Fault Level (MVA)", mvasc) { mvasc = it }
        Button(onClick = {
            val q = mvar.toDoubleOrNull() ?: 0.0
            val sc = mvasc.toDoubleOrNull() ?: 3000.0
            val rise = (q / sc) * 100
            val inrush = 1.414 * (q / (1.732 * 33)) * sqrt(sc/q)
            res = "Voltage Rise: ${String.format("%.2f", rise)}%\nInrush: ${String.format("%.0f", inrush)} A"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CALCULATE") }
        if (res.isNotEmpty()) ResultCard(res, true)
    }
}

// 11. STEP TOUCH
@Composable
fun ToolStepTouch(onBack: () -> Unit) {
    var rho by remember { mutableStateOf("100") }
    var t by remember { mutableStateOf("0.5") }
    var res by remember { mutableStateOf("") }
    ToolScreen("step_touch", onBack) {
        ToolInput("Soil Resistivity (Ωm)", rho) { rho = it }
        ToolInput("Fault Duration (sec)", t) { t = it }
        Button(onClick = {
            val r = rho.toDoubleOrNull() ?: 100.0
            val time = t.toDoubleOrNull() ?: 0.5
            val step = (1000 + 6 * r) * 0.157 / sqrt(time)
            val touch = (1000 + 1.5 * r) * 0.157 / sqrt(time)
            res = "Max Step: ${String.format("%.0f", step)} V\nMax Touch: ${String.format("%.0f", touch)} V"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CALCULATE") }
        if (res.isNotEmpty()) ResultCard(res, true)
    }
}

// 12. DC GROUND
@Composable
fun ToolDC(onBack: () -> Unit) {
    var state by remember { mutableStateOf(0) }
    ToolScreen("dc_ground", onBack) {
        Text("Bulb Condition:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(state == 0, { state = 0 }); Text("Both Equal", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(state == 1, { state = 1 }); Text("+Ve Dim / -Ve Bright", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(state == 2, { state = 2 }); Text("-Ve Dim / +Ve Bright", color = MaterialTheme.colorScheme.onSurface) }
        val msg = when(state) {
            0 -> "System Healthy."
            1 -> "FAULT: Positive (+Ve) Earth."
            2 -> "FAULT: Negative (-Ve) Earth."
            else -> ""
        }
        ResultCard(msg, state == 0)
    }
}

// 13. OIL QUALITY
@Composable
fun ToolOil(onBack: () -> Unit) {
    var bdv by remember { mutableStateOf("") }
    var ppm by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("oil_quality", onBack) {
        ToolInput("BDV (kV)", bdv) { bdv = it }
        ToolInput("Moisture (PPM)", ppm) { ppm = it }
        Button(onClick = {
            val b = bdv.toDoubleOrNull() ?: 0.0
            val p = ppm.toDoubleOrNull() ?: 100.0
            isGood = b >= 50 && p <= 25
            res = "BDV: ${if(b>=50) "OK" else "LOW"}\nMoisture: ${if(p<=25) "OK" else "HIGH"}"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CHECK") }
        if (res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// ==========================================
// 9 NEW TOOLS (SAFETY & ADVANCED)
// ==========================================

// 14. PTW VALIDATOR
@Composable
fun ToolPTW(onBack: () -> Unit) {
    val checks = remember { mutableStateListOf(false, false, false, false) }
    val safe = checks.all { it }
    ToolScreen("ptw", onBack) {
        Text("Isolation Checklist:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[0], { checks[0] = it }); Text("Line Isolator OPEN?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[1], { checks[1] = it }); Text("Bus Isolator OPEN?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[2], { checks[2] = it }); Text("Earth Switch CLOSED?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[3], { checks[3] = it }); Text("'Men at Work' Board Hung?", color = MaterialTheme.colorScheme.onSurface) }
        ResultCard(if(safe) "SAFE TO ISSUE PERMIT" else "DO NOT ISSUE PERMIT", safe)
    }
}

// 15. SAFETY CLEARANCE
@Composable
fun ToolClearance(onBack: () -> Unit) {
    var kv by remember { mutableStateOf(132) }
    ToolScreen("clearance", onBack) {
        Text("Select Voltage Level:", color = MaterialTheme.colorScheme.onSurface)
        Row {
            Button(onClick = { kv = 33 }) { Text("33kV") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { kv = 132 }) { Text("132kV") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { kv = 220 }) { Text("220kV") }
        }
        val (sec, gnd) = when(kv) {
            33 -> "2.8 m" to "3.7 m"
            132 -> "4.0 m" to "4.6 m"
            220 -> "5.0 m" to "5.5 m"
            400 -> "7.0 m" to "8.0 m"
            else -> "2.6 m" to "3.7 m"
        }
        Text("Selected: $kv kV", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary)
        ResultCard("Section Clearance: $sec\nGround Clearance: $gnd", true)
    }
}

// 16. PPE SELECTOR
@Composable
fun ToolPPE(onBack: () -> Unit) {
    var job by remember { mutableStateOf(0) }
    ToolScreen("ppe", onBack) {
        Text("Select Job Type:", color = MaterialTheme.colorScheme.onSurface)
        Button(onClick = { job = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Line Maintenance") }
        Button(onClick = { job = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Battery Room") }
        Button(onClick = { job = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Welding / Cutting") }
        val res = when(job) {
            0 -> "Helmet, Safety Belt, Gum Boots, Hand Gloves"
            1 -> "Acid-Proof Apron, Goggles, Rubber Gloves"
            2 -> "Face Shield, Leather Gloves, Leather Apron"
            else -> ""
        }
        ResultCard("Required PPE:\n$res", true)
    }
}

// 17. FIRST AID
@Composable
fun ToolFirstAid(onBack: () -> Unit) {
    ToolScreen("first_aid", onBack) {
        Text("EMERGENCY PROTOCOL", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C), fontSize = 20.sp)
        Text("1. Switch OFF Supply immediately.\n2. Do NOT touch victim with bare hands.\n3. Use insulated stick to separate victim.\n4. Check Breathing.\n5. If no breath, start CPR (30 push : 2 breath).", color = MaterialTheme.colorScheme.onSurface)
        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)), modifier = Modifier.fillMaxWidth()) {
            Text("CALL EMERGENCY")
        }
    }
}

// 18. REF CALCULATOR
@Composable
fun ToolREF(onBack: () -> Unit) {
    var ifault by remember { mutableStateOf("") }
    var rct by remember { mutableStateOf("") }
    var rl by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    ToolScreen("ref_stab", onBack) {
        ToolInput("Max Fault Current (kA)", ifault) { ifault = it }
        ToolInput("CT Resistance (Ω)", rct) { rct = it }
        ToolInput("Lead Resistance (Ω)", rl) { rl = it }
        Button(onClick = {
            val i = (ifault.toDoubleOrNull() ?: 0.0) * 1000
            val r = (rct.toDoubleOrNull() ?: 0.0) + 2 * (rl.toDoubleOrNull() ?: 0.0)
            res = "Stabilizing Voltage Vs >= ${String.format("%.1f", i * r)} Volts"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CALCULATE") }
        if(res.isNotEmpty()) ResultCard(res, true)
    }
}

// 19. FLUX MONITOR
@Composable
fun ToolFlux(onBack: () -> Unit) {
    var v by remember { mutableStateOf("132") }
    var f by remember { mutableStateOf("50") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("flux", onBack) {
        ToolInput("Voltage (kV)", v) { v = it }
        ToolInput("Frequency (Hz)", f) { f = it }
        Button(onClick = {
            val volt = v.toDoubleOrNull() ?: 132.0
            val freq = f.toDoubleOrNull() ?: 50.0
            val ratio = (volt / 132.0) / (freq / 50.0) * 100
            isGood = ratio < 110
            res = "Flux Level: ${String.format("%.1f", ratio)}%" + if(isGood) " (Normal)" else " (ALARM!)"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CHECK") }
        if(res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 20. LBB SIMULATOR
@Composable
fun ToolLBB(onBack: () -> Unit) {
    val checks = remember { mutableStateListOf(false, false, false) }
    ToolScreen("lbb", onBack) {
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[0], { checks[0] = it }); Text("Main Trip Contact?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[1], { checks[1] = it }); Text("Current > 200mA?", color = MaterialTheme.colorScheme.onSurface) }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[2], { checks[2] = it }); Text("Timer > 200ms?", color = MaterialTheme.colorScheme.onSurface) }
        val trip = checks.all { it }
        ResultCard(if(trip) "LBB OPERATED: BUS BAR TRIP" else "LBB NORMAL", !trip)
    }
}

// 21. THERMOVISION
@Composable
fun ToolThermo(onBack: () -> Unit) {
    var rise by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    var isGood by remember { mutableStateOf(true) }
    ToolScreen("thermo", onBack) {
        ToolInput("Temp Rise > Ambient (°C)", rise) { rise = it }
        Button(onClick = {
            val r = rise.toDoubleOrNull() ?: 0.0
            isGood = r < 10
            res = when {
                r < 10 -> "Condition: Normal"
                r < 35 -> "Condition: Defect (Plan Repair)"
                else -> "Condition: CRITICAL (Isolate Now)"
            }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("ANALYZE") }
        if(res.isNotEmpty()) ResultCard(res, isGood)
    }
}

// 22. IR CORRECTION
@Composable
fun ToolIRCorr(onBack: () -> Unit) {
    var ir by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var res by remember { mutableStateOf("") }
    ToolScreen("ir_corr", onBack) {
        ToolInput("Measured IR (MΩ)", ir) { ir = it }
        ToolInput("Oil Temp (°C)", temp) { temp = it }
        Button(onClick = {
            val r = ir.toDoubleOrNull() ?: 0.0
            val t = temp.toDoubleOrNull() ?: 30.0
            val r30 = r * 2.0.pow((t - 30) / 10.0)
            res = "Corrected IR at 30°C: ${String.format("%.0f", r30)} MΩ"
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("CORRECT") }
        if(res.isNotEmpty()) ResultCard(res, true)
    }
}