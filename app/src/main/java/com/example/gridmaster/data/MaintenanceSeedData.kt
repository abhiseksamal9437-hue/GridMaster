package com.example.gridmaster.data

// ==========================================
// THE "GRIDMASTER" ULTIMATE O&M MANUAL
// Sources: Vol-01, 05, 06, 07, 09, Master Manual, Wiring Manual
// ==========================================

val detailedMaintenanceList = listOf(

    // ---------------------------------------------------------
    // A. STATION BATTERY (Source: Vol-05)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.DAILY, "Pilot Cell: Measure Voltage, Specific Gravity, & Temp"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.DAILY, "Battery Set: Switch OFF Charger & Measure Total Terminal Voltage"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.DAILY, "Charger Status: Verify Output Current, DC Voltage, & Trickle Current"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.DAILY, "Emergency Light: Check Auto-Changeover functionality on AC failure"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.WEEKLY, "Terminals: Clean oxidation & apply thin layer of Petroleum Jelly"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.WEEKLY, "Electrolyte: Top-up with Distilled Water (Max Level). Never use Acid"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.WEEKLY, "Ventilation: Check Exhaust Fans & Room Temp (< 27 C preferred)"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.MONTHLY, "Full Set: Measure Voltage, SG, & Temp of EVERY cell (1 to 55)"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.MONTHLY, "Connectors: Check tightness of inter-cell connectors (Torque wrench)"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.MONTHLY, "Load Test: Switch OFF Charger. Trip/Close one feeder using Battery only"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.MONTHLY, "SG Analysis: If variation > 0.030 points, start Equalizing Charge"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.MONTHLY, "DC Leakage: Perform Two Bulb Test (Equal Glow = Healthy)"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.MONTHLY, "Charger Cleaning: Blow dust from SCR heatsinks & Control Cards"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.YEARLY, "Torque Check: Re-torque all bolted connections"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.YEARLY, "Capacity Test: Discharge @ 10% AH rating until voltage drops to 1.85V/cell"),
    MaintenanceTask("", EquipmentType.BATTERY, MaintenanceFreq.YEARLY, "Cell Replacement: Identify and replace Dead cells (< 1.8V during load)"),

    // ---------------------------------------------------------
    // B. TRANSFORMER (Source: Vol-01)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.DAILY, "MOG (Conservator): Check Oil Level. Must be steady at 35 C mark"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.DAILY, "POG (Air Cell): Check bladder health. Oil level in breather cup normal"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.DAILY, "OTI (Oil Temp): Ensure reading is < 85 C"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.DAILY, "WTI (Winding Temp): Ensure reading is < 95 C"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.DAILY, "Silica Gel: Check Color. Blue/Pink = Good. White = Replace"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.DAILY, "Leakage Hunt: Inspect Bushings, Radiator Flanges, & OLTC Tank"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.WEEKLY, "Cooling System: Manually run Fans & Pumps for 15 mins"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.WEEKLY, "Pump Motor: Check Current Imbalance (>20% indicates bearing failure)"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.WEEKLY, "Buchholz Relay: Check Gas Collection Window. Must be empty"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.WEEKLY, "Bushings: Wipe porcelain clean. Look for flashover marks"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.MONTHLY, "Fan Maintenance: Grease bearings if running hours > 1000"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.MONTHLY, "Protection Functional Test: Manually rotate OTI/WTI pointers to trip"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.MONTHLY, "Oil Sampling: Take sample for BDV & Moisture Analysis"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.MONTHLY, "Radiators: Clean fins with water jet/air blower"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.MONTHLY, "Marshalling Box: Check Heater operation & Vermin proofing"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.YEARLY, "IR Test (Megger): Measure HV-LV, HV-E, LV-E. Calculate PI"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.YEARLY, "Tan Delta: Measure Capacitance & Tan Delta of Windings & Bushings"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.YEARLY, "Winding Resistance: Measure all taps. Correct to 75 C"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.YEARLY, "Oil Lab Test: Full DGA, Acidity, Interfacial Tension, & Flash Point"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.SPECIAL, "FRA (Frequency Response Analysis): Check for winding deformation"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.SPECIAL, "OLTC Overhaul: Filter Diverter Switch oil. Inspect contacts"),
    MaintenanceTask("", EquipmentType.TRANSFORMER, MaintenanceFreq.SPECIAL, "Painting: Anti-corrosive paint on Main Tank & Conservator"),

    // ---------------------------------------------------------
    // C. SWITCHYARD EQUIPMENT (Source: Vol-09)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.DAILY, "CB Pressure: Check SF6/Air/Hydraulic gauge. Must be in GREEN zone"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.DAILY, "CB Heaters: Touch Control Cubicle. Should be warm"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.DAILY, "LA Counter: Record Surge Counter reading"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.WEEKLY, "CB Operation: Check Pole Discrepancy. Operate Breaker Local/Remote"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.WEEKLY, "Drive Mechanism: Check Hydraulic Oil Level / Compressor Run Time"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.MONTHLY, "Isolator CRM: Measure Contact Resistance. Limit < 300 micro-ohm"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.MONTHLY, "Isolator Maint: Clean contacts & apply conducting grease"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.MONTHLY, "LA Health: LCM (Leakage Current Monitor) Test"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.MONTHLY, "Thermo-vision: Scan all Clamps/Jumpers for Hot Spots (>90 C critical)"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.YEARLY, "CB Timing: Measure Close, Open, C-O times"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.YEARLY, "CB DCRM: Dynamic Contact Resistance Measurement"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.YEARLY, "SF6 Quality: Test Dew Point & Purity. Limit -35 C"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.YEARLY, "CT/PT Condition: Tan Delta, Ratio, & Polarity Test"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.YEARLY, "LA Harmonic Test: Measure 3rd Harmonic Resistive Current"),
    MaintenanceTask("", EquipmentType.SWITCHYARD, MaintenanceFreq.YEARLY, "Wave Traps: Visual check & Tuning Unit connection tightness"),

    // ---------------------------------------------------------
    // D. FIRE SYSTEM (Source: Vol-07)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.FIRE_SYSTEM, MaintenanceFreq.DAILY, "Fire Panel: Verify System Healthy LED is ON"),
    MaintenanceTask("", EquipmentType.FIRE_SYSTEM, MaintenanceFreq.DAILY, "Valve Status: Ensure all butterfly valves in Hydrant line are OPEN"),
    MaintenanceTask("", EquipmentType.FIRE_SYSTEM, MaintenanceFreq.WEEKLY, "Diesel Engine: Test run for 15 mins. Check Battery Voltage & Fuel"),
    MaintenanceTask("", EquipmentType.FIRE_SYSTEM, MaintenanceFreq.MONTHLY, "Extinguishers: Check Pressure Gauge (Green Zone) & Weight"),
    MaintenanceTask("", EquipmentType.FIRE_SYSTEM, MaintenanceFreq.YEARLY, "Detectors: Functional test of Smoke & Heat Detectors"),

    // ---------------------------------------------------------
    // E. EARTHING SYSTEM (Source: Vol-06)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.EARTHING, MaintenanceFreq.YEARLY, "Earth Grid: Measure combined Grid Resistance. Limit < 1.0 Ohm"),
    MaintenanceTask("", EquipmentType.EARTHING, MaintenanceFreq.MONTHLY, "Earth Pits: Check watering status (if dry season)"),
    MaintenanceTask("", EquipmentType.EARTHING, MaintenanceFreq.YEARLY, "Risers: Check continuity of equipment earth risers"),

    // ---------------------------------------------------------
    // F. TRANSMISSION LINES (Source: Master O&M)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.TRANSMISSION_LINE, MaintenanceFreq.YEARLY, "PID Scanning: Conduct Punctured Insulator Detection on Tension Towers"),
    MaintenanceTask("", EquipmentType.TRANSMISSION_LINE, MaintenanceFreq.YEARLY, "Coronography: UV Camera inspection for Corona Rings"),
    MaintenanceTask("", EquipmentType.TRANSMISSION_LINE, MaintenanceFreq.YEARLY, "LSA: Line Signature Analysis for conductor health check"),
    MaintenanceTask("", EquipmentType.TRANSMISSION_LINE, MaintenanceFreq.QUARTERLY, "Thermo-vision: Scan Jumpers & Mid-span joints"),
    MaintenanceTask("", EquipmentType.TRANSMISSION_LINE, MaintenanceFreq.MONTHLY, "Patrolling: Check for vegetation growth near ROW"),

    // ---------------------------------------------------------
    // G. SHUNT REACTORS (Source: Master O&M)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.REACTOR, MaintenanceFreq.DAILY, "MOG & OTI: Check Oil Level and Oil Temp"),
    MaintenanceTask("", EquipmentType.REACTOR, MaintenanceFreq.MONTHLY, "Oil Surge Relay (OSR): Verify valve is locked in OPEN position"),
    MaintenanceTask("", EquipmentType.REACTOR, MaintenanceFreq.MONTHLY, "Air Gap: Inspect magnetic circuit air gaps"),
    MaintenanceTask("", EquipmentType.REACTOR, MaintenanceFreq.YEARLY, "NGR Check: Inspect Neutral Grounding Reactor earth connection"),

    // ---------------------------------------------------------
    // H. CIVIL & GENERAL (Source: Wiring Manual & Master O&M)
    // ---------------------------------------------------------
    MaintenanceTask("", EquipmentType.CIVIL_WORKS, MaintenanceFreq.YEARLY, "Gravelling: Check resistivity of crushed rock layer"),
    MaintenanceTask("", EquipmentType.CIVIL_WORKS, MaintenanceFreq.QUARTERLY, "Tower Footing: Check Coping & Protection for rust/damage"),
    MaintenanceTask("", EquipmentType.CIVIL_WORKS, MaintenanceFreq.SPECIAL, "Drainage: Pre-Monsoon check of Cable Trenches & Oil Soak Pits"),
    MaintenanceTask("", EquipmentType.GENERAL, MaintenanceFreq.DAILY, "Yard Lighting: Check High Mast & Street Lights"),
    MaintenanceTask("", EquipmentType.GENERAL, MaintenanceFreq.MONTHLY, "C&R Panels: Internal cleaning & Terminal tightness check")
)