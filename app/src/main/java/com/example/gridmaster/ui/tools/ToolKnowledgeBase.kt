package com.example.gridmaster.ui.tools

data class ToolInfo(
    val title: String,
    val description: String,
    val source: String,
    val explanation: String
)

val toolKnowledgeMap = mapOf(

    // ==========================================
    // 1. BATTERY HEALTH DOCTOR
    // Source: Vol-05 Battery Manual
    // ==========================================
    "battery" to ToolInfo(
        "Battery Health Doctor",
        "Specific Gravity Variation Analysis.",
        "Vol-05 Battery Charging Systems, Page 22",
        "THE SCIENCE:\n" +
                "In a Lead-Acid cell, Specific Gravity (SG) is the direct measure of the state of charge. " +
                "During discharge, Sulphuric Acid (H2SO4) is consumed to form Lead Sulphate (PbSO4) on the plates, lowering the SG of the electrolyte.\n\n" +
                "THE RULE:\n" +
                "If the SG of any individual pilot cell deviates by more than 0.030 (30 points) from the average of the set, it indicates 'Cell Lagging' or 'Stratification'.\n\n" +
                "RISK IF IGNORED:\n" +
                "1. Sulfation: Hard crystals form on plates, permanently reducing capacity.\n" +
                "2. Cell Reversal: The weak cell may reverse polarity during load, causing battery bank failure during a blackout.\n\n" +
                "CORRECTION:\n" +
                "Apply an 'Equalizing Charge' (High Voltage, Low Current) to break down the sulphate crystals and mix the electrolyte."
    ),

    // ==========================================
    // 2. DISTANCE RELAY ZONES
    // Source: OPTCL Protection Philosophy
    // ==========================================
    "distance" to ToolInfo(
        "Distance Relay Zones",
        "Impedance Reach Calculation (Mho/Quad).",
        "OPTCL Protection Philosophy, Page 4",
        "THE CONCEPT:\n" +
                "Distance relays measure impedance (V/I). Since line impedance is proportional to length, the relay can calculate the exact distance to the fault.\n\n" +
                "ZONE LOGIC:\n" +
                "• Zone 1 (80% of Line): Instantaneous Trip (0s). We leave a 20% safety margin to prevent 'Over-reaching' into the next line due to CT errors or transients.\n" +
                "• Zone 2 (100% + 50% of Shortest Next Line): Time Delayed (0.35s). Covers the remaining 20% of the main line plus backup for the busbar and the start of the next line.\n" +
                "• Zone 3 (100% + 100% of Longest Next Line): Backup Protection (1.0s). The last resort if the primary protection of the adjacent line fails.\n\n" +
                "CRITICAL NOTE:\n" +
                "If Zone 1 is set >85%, a fault on the next line might trip YOUR breaker instantly (Maloperation)."
    ),

    // ==========================================
    // 3. CT KNEE POINT VALIDATOR
    // Source: OPTCL Protection Philosophy
    // ==========================================
    "ct_knee" to ToolInfo(
        "CT Knee Point Check",
        "Saturation Voltage Verification.",
        "OPTCL Protection Philosophy, Page 12",
        "THE PHYSICS:\n" +
                "A Current Transformer (CT) pushes current through the relay circuit. To do this, it generates a voltage (V = I × R). " +
                "However, the iron core can only support a certain magnetic flux density before it 'Saturates'. " +
                "The voltage at which saturation occurs is the 'Knee Point Voltage' (Vk).\n\n" +
                "THE FORMULA:\n" +
                "Vk_required ≥ K × If_max × (R_ct + 2 × R_lead)\n" +
                "• K: Transient Safety Factor (usually 2.0 for stability).\n" +
                "• If_max: Maximum Through Fault Current (Secondary).\n\n" +
                "DANGER:\n" +
                "If the required voltage > nameplate Vk, the CT saturates. The secondary output becomes distorted or zero. " +
                "Result: Differential Relay will fail to trip during an internal fault (Transformer Explosion) or trip wrongly for an external fault."
    ),

    // ==========================================
    // 4. PROTECTION SAFETY CHECKLIST
    // Source: Protection Philosophy
    // ==========================================
    "safety" to ToolInfo(
        "Protection Safety Permit",
        "Critical Pre-Work Interlocks.",
        "OPTCL Protection Philosophy, Page 80",
        "LIFE SAVING RULES:\n\n" +
                "1. CT SECONDARY MUST BE SHORTED:\n" +
                "Why? A CT acts like a constant current source. If the secondary is OPEN, the current tries to force its way through infinite resistance (Air). " +
                "V = I × R. If R is infinite, Voltage becomes infinite (kV spikes). This causes flashover, panel explosion, and electrocution.\n\n" +
                "2. PT SECONDARY MUST BE OPEN:\n" +
                "Why? A PT is a constant voltage source. If shorted, I = V / R. Since R is near zero, Current becomes massive, melting the windings instantly.\n\n" +
                "3. DC POLARITY:\n" +
                "Why? Digital relays and coil diodes are polarity sensitive. Reversing +/- can blow the relay card power supply."
    ),

    // ==========================================
    // 5. SF6 DEW POINT ANALYZER
    // Source: Vol-09 Switchyard Manual
    // ==========================================
    "sf6" to ToolInfo(
        "SF6 Dew Point Analyzer",
        "Gas Moisture & Acid Formation Risk.",
        "Vol-09 Switchyard Manual, Page 39 (Table 3)",
        "THE CHEMISTRY:\n" +
                "Pure SF6 is inert. However, under the high heat of an arc (during breaker operation), SF6 decomposes. " +
                "If Moisture (H2O) is present, the decomposed products react to form Hydrofluoric Acid (HF) and SO2.\n" +
                "• Formula: SF4 + H2O → SOF2 + 2HF (Acid)\n\n" +
                "THE DAMAGE:\n" +
                "HF Acid eats away the nozzle (Teflon) and corrodes the metal contacts, leading to breaker explosion.\n\n" +
                "THE LIMITS:\n" +
                "• Measured Dew Point must be better than -27°C (at Atmospheric Pressure).\n" +
                "• If worse (e.g., -15°C), the gas is 'Wet' and must be reclaimed/filtered immediately."
    ),

    // ==========================================
    // 6. ISOLATOR CONTACT MONITOR
    // Source: Vol-09 Switchyard Manual
    // ==========================================
    "isolator" to ToolInfo(
        "Isolator Contact Health",
        "Micro-Ohm Contact Resistance Check.",
        "Vol-09 Switchyard Manual, Page 57",
        "THE PHYSICS:\n" +
                "Isolator contacts carry thousands of Amperes. Even a tiny resistance creates massive heat.\n" +
                "• Heat Generated = I² × R\n" +
                "• Example: 2000A current through a bad contact (500 µΩ) generates 2000 Watts of heat! This acts like a heater inside the clamp.\n\n" +
                "THE LIMIT:\n" +
                "Contact Resistance (CRM) must be < 300 µΩ (Micro-ohms). Higher values indicate:\n" +
                "1. Oxide film formation.\n" +
                "2. Loss of spring pressure.\n" +
                "3. Misalignment of jaws.\n\n" +
                "ACTION: Clean contacts and apply conductive grease."
    ),

    // ==========================================
    // 7. EARTH RESISTANCE EVALUATOR
    // Source: Vol-06 Earthing Manual
    // ==========================================
    "earth" to ToolInfo(
        "Earth Resistance Evaluator",
        "Grid Integrity & Safety Limits.",
        "Vol-06 Earthing Manual, Page 7 (Table 2)",
        "THE PURPOSE:\n" +
                "The Earth Mat provides a low-impedance path for fault currents to dissipate into the ground. " +
                "If resistance is high, the fault current creates a high 'Ground Potential Rise' (GPR).\n\n" +
                "THE DANGER:\n" +
                "High GPR creates lethal voltage differences on the surface:\n" +
                "• Step Potential: Voltage between your two feet.\n" +
                "• Touch Potential: Voltage between your hand (touching a structure) and your feet.\n\n" +
                "LIMITS:\n" +
                "• EHV Station (>132kV): Combined Grid R < 1.0 Ω.\n" +
                "• HV Station (33kV): Combined Grid R < 5.0 Ω.\n" +
                "• If High: Treat pits with Bentonite/Salt/Charcoal and water immediately."
    ),

    // ==========================================
    // 8. SMART METERING FAULT DETECTOR
    // Source: Circle Wiring Manual
    // ==========================================
    "metering" to ToolInfo(
        "Smart Metering Diagnosis",
        "Phasor Analysis for CT/PT Errors.",
        "Circle Wiring Manual, Page 66",
        "THE LOGIC:\n" +
                "In a 3-Phase system, we expect balanced currents and specific phase angles. Wiring errors create predictable signatures:\n\n" +
                "1. Negative Power (Watts): Indicates CT Polarity is reversed (P1/P2 swapped).\n" +
                "2. Zero Current: Indicates CT Open Circuit or Shorting Link left closed.\n" +
                "3. Leading PF on Inductive Load: Indicates Voltage (PT) and Current (CT) phases are swapped (e.g., R-Volt with Y-Current).\n\n" +
                "This tool analyzes the magnitudes and signs (-ve/+ve) to pinpoint the exact wiring mistake."
    ),

    // ==========================================
    // 9. TRANSFORMER MOISTURE ANALYST
    // Source: Vol-01 Transformer Manual
    // ==========================================
    "moisture" to ToolInfo(
        "Moisture in Paper Analyst",
        "Equilibrium Chart (Piper's Method).",
        "Vol-01 Transformer Manual, Page 77",
        "THE PROBLEM:\n" +
                "99% of moisture in a transformer resides in the Paper Insulation, not the Oil. We cannot measure paper moisture directly without opening the tank.\n\n" +
                "THE SOLUTION:\n" +
                "Paper and Oil exist in equilibrium. By measuring Moisture in Oil (PPM) and Temperature, we can use Isotherms (Piper's Chart) to calculate the % Moisture in Paper.\n\n" +
                "INTERPRETATION:\n" +
                "• < 1.5%: Dry / New Transformer.\n" +
                "• 2.0% - 3.0%: Wet. Insulation aging accelerates by 4x.\n" +
                "• > 4.0%: Critical. Risk of bubble formation and flashover. Dry-out required."
    ),

    // ==========================================
    // 10. CAPACITOR BANK CALCULATOR
    // Source: Vol-09 Switchyard Manual
    // ==========================================
    "cap_bank" to ToolInfo(
        "Capacitor Bank Calculator",
        "Voltage Rise & Inrush Current.",
        "Vol-09 Switchyard Manual, Page 64",
        "VOLTAGE RISE:\n" +
                "Switching ON a capacitor bank raises the bus voltage. This rise must not exceed 3% to protect other equipment.\n" +
                "• Formula: % Rise = (MVAR Rating × % Impedance) / MVA Fault Level.\n\n" +
                "INRUSH CURRENT:\n" +
                "When closing the breaker, the capacitor acts like a momentary short circuit. The high frequency inrush current can damage breaker contacts or CTs.\n" +
                "• Formula: Peak = 1.414 × I_rated × Sqrt(MVA_sc / MVAR).\n" +
                "• Mitigation: Series Reactors are used to dampen this inrush."
    ),

    // ==========================================
    // 11. STEP & TOUCH POTENTIAL
    // Source: Vol-06 Earthing Manual
    // ==========================================
    "step_touch" to ToolInfo(
        "Step & Touch Potential Calc",
        "IEEE 80 Safety Limits.",
        "Vol-06 Earthing Manual, Page 17",
        "DEFINITIONS:\n" +
                "• Step Potential: The voltage difference between two feet spaced 1m apart during a fault.\n" +
                "• Touch Potential: The voltage difference between a hand touching a grounded structure and the feet.\n\n" +
                "THE CALCULATION:\n" +
                "Safety depends on the 'Surface Layer' (Crushed Rock). The high resistivity of rock (3000 Ωm) acts as an insulator for the human body.\n" +
                "This tool calculates the Maximum Safe Voltage limits based on your soil data and fault clearing time (t). If the grid's actual GPR exceeds this, you need thicker rock or a better earth mat."
    ),

    // ==========================================
    // 12. DC GROUND FAULT DETECTOR
    // Source: Vol-05 Battery Manual
    // ==========================================
    "dc_ground" to ToolInfo(
        "DC Earth Fault Finder",
        "Two-Bulb Method Logic.",
        "Vol-05 Battery Manual, Page 46",
        "THE METHOD:\n" +
                "Two identical bulbs are connected in series across the DC Bus (+ to -), with the center point grounded. Under normal conditions, both glow equally (110V each).\n\n" +
                "DIAGNOSIS:\n" +
                "1. Positive (+Ve) Ground Fault: The +Ve bulb gets shorted out (Voltage -> 0). It becomes DIM or OFF. The -Ve bulb gets full 220V and becomes BRIGHT.\n" +
                "2. Negative (-Ve) Ground Fault: The -Ve bulb becomes DIM/OFF. The +Ve bulb becomes BRIGHT.\n\n" +
                "This is the quickest way to identify which pole has faulted."
    ),

    // ==========================================
    // 13. OIL QUALITY INDEX
    // Source: Vol-01 Transformer Manual
    // ==========================================
    "oil_quality" to ToolInfo(
        "Oil Quality Validator",
        "IS 1866 Service Limits.",
        "Vol-01 Transformer Manual, Page 62 (Table 19)",
        "WHY TEST OIL?\n" +
                "Insulating oil performs two jobs: Cooling and Insulation. Over time, it oxidizes (sludge), absorbs moisture, and loses dielectric strength.\n\n" +
                "CRITICAL LIMITS (For > 145 kV):\n" +
                "• BDV (Breakdown Voltage): Must be > 50 kV (rms). If low, it will spark over internally.\n" +
                "• Moisture: Must be < 25 PPM. Water reduces BDV drastically.\n" +
                "• Tan Delta: Dielectric Dissipation Factor. High values indicate polar contaminants/sludge.\n\n" +
                "ACTION: If any parameter fails, perform Filtration (Dehydration) immediately."
    ),
    // ... (Keep existing 13 tools) ...

    // ==========================================
    // 14. BREAKER TROUBLESHOOTER (Source: Wiring Manual)
    // ==========================================
    "breaker_local_close" to ToolInfo(
        "Breaker Not Closing (Local)",
        "Mechanical vs Electrical check.",
        "Wiring Manual, Page 39",
        "THEORY:\nIf the breaker closes mechanically (by hand), the mechanism is fine. The issue is in the Closing Circuit (52C).\n\nCHECKLIST:\n1. Control Fuses (DC Supply).\n2. Closing Coil Resistance (Open circuit?).\n3. Interlocks: Gas Pressure, Spring Charge, Local/Remote Switch.\n4. Anti-Pumping Relay (86) status."
    ),
    "breaker_remote_close" to ToolInfo(
        "Breaker Not Closing (Remote)",
        "SCADA/Remote Panel check.",
        "Wiring Manual, Page 40",
        "THEORY:\nIf Local Close works, the breaker is healthy. The break is in the remote command path.\n\nCHECKLIST:\n1. Local/Remote Switch position.\n2. Sync Check Relay (25) contact.\n3. Master Trip Relay (86) Reset status.\n4. Inter-panel wiring continuity."
    ),
    "breaker_trip_fail" to ToolInfo(
        "Breaker Fails to Trip",
        "CRITICAL SAFETY FAILURE.",
        "Wiring Manual, Page 40",
        "THEORY:\nTripping must happen regardless of interlocks. If it fails, the coil or DC supply is compromised.\n\nCHECKLIST:\n1. Trip Coil Resistance (Burned out?).\n2. DC Supply at Trip Coil terminals.\n3. Trip Circuit Supervision (TCS) Relay healthy?"
    ),
    "breaker_ghost_trip" to ToolInfo(
        "Tripping Without Fault",
        "DC Leakage / Ghost Tripping.",
        "Wiring Manual, Page 42",
        "THEORY:\nA 'Double Ground Fault' in the DC system can bypass the relay contact, sending +ve directly to the trip coil.\n\nCHECKLIST:\n1. Check DC System for Earth Faults (+ve or -ve).\n2. Check TCS Relay (95) for shorting.\n3. Check if Indication Lamps are shorting into the trip circuit."
    ),
    "breaker_coil_burn" to ToolInfo(
        "Trip/Close Coil Burning",
        "Continuous Supply Issue.",
        "Wiring Manual, Page 43",
        "THEORY:\nCoils are 'Short Time' rated (momentary). If the Breaker Aux Contact (52a/b) fails to cut off supply after operation, the coil burns.\n\nCHECKLIST:\n1. Breaker Auxiliary Contact (sticky/misaligned?).\n2. DC Leakage extending supply.\n3. Anti-Pumping Relay failure (for Closing Coil)."
    ),
    "breaker_immediate_trip" to ToolInfo(
        "Trips Immediately on Close",
        "Trip-on-Close logic.",
        "Wiring Manual, Page 44",
        "THEORY:\nUsually caused by a 'Master Trip Relay (86)' that hasn't been reset, or a permanent fault on the line.\n\nCHECKLIST:\n1. Check Master Trip (86) status.\n2. Check DC Leakage (Potential extension).\n3. Check for 'LBB' or 'Busbar Protection' operation."
    ),
    "breaker_fuse_blow" to ToolInfo(
        "Fuse Blown After Trip",
        "Coil Short Circuit.",
        "Wiring Manual, Page 45",
        "THEORY:\nThe Trip Coil has likely failed internally (shorted turns), drawing massive current when energized.\n\nCHECKLIST:\n1. Measure Trip Coil Resistance (compare with manual).\n2. Check for wiring short to earth."
    ),
    "breaker_pd" to ToolInfo(
        "Pole Discrepancy Trip",
        "Poles not moving together.",
        "Wiring Manual, Page 45",
        "THEORY:\nIf one pole closes but others don't (within 2.5s), the PD relay trips all poles to prevent unbalance.\n\nCHECKLIST:\n1. Check individual pole mechanisms.\n2. Check 'Close Command' wiring to each pole.\n3. Measure Pole discrepancy timer setting."
    ),
// ... (Keep all previous tools) ...

    // ==========================================
    // 15. ANNUNCIATION TROUBLESHOOTER (Source: Wiring Manual Pg 30-31)
    // ==========================================
    "annun_fuse" to ToolInfo(
        "Annunciation Fuse Blown",
        "Power Supply Failure.",
        "Wiring Manual, Page 30 (Sl. 1)",
        "CHECKLIST:\n1. Check for Loose or Wrong connections in the supply line.\n2. Check for defects in the Internal Circuit of the window.\n3. Check for defects in the Bulb/LED itself.\n4. Inspect Auxiliary Contacts (shorting?)."
    ),
    "annun_no_flash" to ToolInfo(
        "Facia Not Flashing",
        "Logic/Wiring Error.",
        "Wiring Manual, Page 30 (Sl. 2)",
        "CHECKLIST:\n1. Wrong selection of DIP switch contacts (Check Manual/Auto settings).\n2. Wrong connection of 'TRIP' and 'NON-TRIP' jumpers.\n3. Wrong selection of transition contacts.\n4. Leakage in the Facia circuit."
    ),
    "annun_wrong_glow" to ToolInfo(
        "Wrong Window Glowing",
        "Inter-mixing of signals.",
        "Wiring Manual, Page 30 (Sl. 3)",
        "THEORY:\nOften caused by 'Back-feed' or potential mixing.\n\nCHECKLIST:\n1. Check if Annunciation contacts are connected to a live potential supply elsewhere.\n2. Check for DC Leakage in the system.\n3. Verify common return wire isolation."
    ),
    "annun_intermittent" to ToolInfo(
        "Intermittent Glowing",
        "Unstable Signal.",
        "Wiring Manual, Page 31 (Sl. 4)",
        "CHECKLIST:\n1. Check for DC Leakage in the facia circuit.\n2. Check for 'Abnormal Make' of contacts due to vibration.\n3. Check for external touch (e.g., water drop leakage or lizard entry)."
    ),
    "annun_buzzer_fuse" to ToolInfo(
        "Buzzer Fuse Blown",
        "Audio Circuit Short.",
        "Wiring Manual, Page 31 (Sl. 5)",
        "CHECKLIST:\n1. Check for short circuit in the Bell/Hooter wiring.\n2. Inspect the Bell Coil resistance.\n3. Check for loose connections touching the body."
    ),
    "annun_bell_fail" to ToolInfo(
        "Bell/Hooter Not Working",
        "Silent Alarm.",
        "Wiring Manual, Page 31 (Sl. 6)",
        "CHECKLIST:\n1. Check 'Accept', 'Test', 'Reset' push buttons (Are contacts stuck?).\n2. Check extension of Common Negative/Positive to the Push Button.\n3. Check Facia Relay output contact."
    ),

    // ==========================================
    // 16. METERING ANALYST (Source: Wiring Manual Pg 66-67)
    // ==========================================
    "meter_r_rev" to ToolInfo(
        "R-Phase CT Reverse",
        "Negative Power in R-Phase.",
        "Wiring Manual, Page 66 (Case 2)",
        "SYMPTOM:\n• V1, V2, V3 are OK (+ve).\n• Current I1 is NEGATIVE (or Power Factor is reversed).\n\nDIAGNOSIS:\nR-Phase Current Coil is connected in reverse (S1/S2 swapped).\n\nACTION: Swap terminal connections S1 and S2 for R-Phase."
    ),
    "meter_y_rev" to ToolInfo(
        "Y-Phase CT Reverse",
        "Negative Power in Y-Phase.",
        "Wiring Manual, Page 66 (Case 3)",
        "SYMPTOM:\n• V1, V2, V3 are OK.\n• Current I2 is NEGATIVE.\n\nDIAGNOSIS:\nY-Phase Current Coil is connected in reverse.\n\nACTION: Swap S1 and S2 for Y-Phase."
    ),
    "meter_b_rev" to ToolInfo(
        "B-Phase CT Reverse",
        "Negative Power in B-Phase.",
        "Wiring Manual, Page 66 (Case 4)",
        "SYMPTOM:\n• V1, V2, V3 are OK.\n• Current I3 is NEGATIVE.\n\nDIAGNOSIS:\nB-Phase Current Coil is connected in reverse.\n\nACTION: Swap S1 and S2 for B-Phase."
    ),
    "meter_ry_swap" to ToolInfo(
        "R & Y CTs Swapped",
        "Phase Association Error.",
        "Wiring Manual, Page 67 (Case 5)",
        "SYMPTOM:\n• Q1 (PF) is Leading (0.44 to 0.66).\n• Q2 (PF) is Lagging (0.22 to 0.44).\n\nDIAGNOSIS:\nR-Phase voltage is interacting with Y-Phase current (and vice versa).\n\nACTION: Check wiring block. R and Y Current wires are interchanged."
    ),
    "meter_rb_swap" to ToolInfo(
        "R & B CTs Swapped",
        "Phase Association Error.",
        "Wiring Manual, Page 67 (Case 6)",
        "SYMPTOM:\n• I1 is Negative.\n• I3 is Negative.\n• I1 > I3 magnitude mismatch.\n\nDIAGNOSIS:\nR-Phase and B-Phase Current coils are interchanged.\n\nACTION: Trace R and B wires from CT box to Meter."
    ),
    "meter_yb_swap" to ToolInfo(
        "Y & B CTs Swapped",
        "Phase Association Error.",
        "Wiring Manual, Page 67 (Case 7)",
        "SYMPTOM:\n• I2 is Negative.\n• Q2 is Leading.\n• Q3 is Lagging.\n\nDIAGNOSIS:\nY-Phase and B-Phase Current coils are interchanged."
    ),
    "meter_normal" to ToolInfo(
        "Normal Condition",
        "Balanced Star Connection.",
        "Wiring Manual, Page 66 (Case 1)",
        "REFERENCE VALUES:\n• Voltages: V1 ≈ V2 ≈ V3\n• Currents: I1 ≈ I2 ≈ I3\n• Power Factor: Q1 ≈ Q2 ≈ Q3 (Lagging 0.9 to 0.99)\n\nThis is the standard for a healthy Import Mode connection."
    ),
    // --- NEW SAFETY TOOLS (FROM SAFETY MANUAL) ---
    "ptw" to ToolInfo("PTW Validator", "Permit To Work Issuer", "Safety Manual, 'Permit System'", "INTERLOCKS:\n1. Line Isolator OPEN?\n2. Bus Isolator OPEN?\n3. Earth Switch CLOSED?\n4. 'Men at Work' Board Hung?\nOnly if ALL are true -> Issue Permit."),
    "clearance" to ToolInfo("Safety Clearance Calc", "Statutory Distance", "Safety Manual, Reg-73", "MANDATORY CLEARANCE:\n• 11kV: 2.6m\n• 33kV: 2.8m\n• 132kV: 4.0m\n• 220kV: 5.0m\n• 400kV: 7.0m\nViolation = Electrocution."),
    "ppe" to ToolInfo("PPE Selector", "Safety Gear Guide", "Safety Manual, 'Safety Equip'", "REQUIREMENTS:\n• Line Work: Helmet, Safety Belt, Gum Boots.\n• Battery Room: Apron, Goggles, Rubber Gloves.\n• Welding: Face Shield, Leather Gloves."),
    "first_aid" to ToolInfo("First Aid Assistant", "Electric Shock Protocol", "Safety Manual, 'Resuscitation'", "PROTOCOL:\n1. Switch OFF Supply.\n2. Insulate yourself.\n3. Check Breathing.\n4. CPR (30 compressions : 2 breaths)."),

    // --- NEW ADVANCED TOOLS (FROM PROTECTION/O&M) ---
    "ref_stab" to ToolInfo("REF Stabilizing Voltage", "Relay Setting Calc", "Protection Philosophy, Page 18", "FORMULA:\nVs >= If_max * (Rct + 2*Rl)\nEnsures relay remains stable (doesn't trip) during external faults even if CT saturates."),
    "flux" to ToolInfo("Over-Fluxing (V/f)", "Core Saturation Monitor", "Protection Philosophy, Page 25", "LOGIC:\nFlux is proportional to Voltage/Frequency.\n• > 110%: Alarm\n• > 125%: Trip (1 min)\n• > 150%: Instant Trip\nEffect: Core heating & degradation."),
    "lbb" to ToolInfo("LBB Logic Simulator", "Breaker Failure Check", "Protection Philosophy, Page 35", "LOGIC:\nIF (Main Trip Issued) AND (Current > 200mA) AND (Time > 200ms) -> THEN (Trip All Bus Breakers)."),
    "thermo" to ToolInfo("Thermo-Vision Grader", "Hotspot Severity", "Master O&M Manual", "GRADING (Temp Rise above Ambient):\n• < 10°C: Normal\n• 10-35°C: Defect (Plan Repair)\n• > 35°C: CRITICAL (Emergency Isolation)."),
    "ir_corr" to ToolInfo("IR Temp Corrector", "Insulation Normalizer", "Vol-01 Transformer, Page 80", "PHYSICS:\nInsulation Resistance drops by half for every 10°C rise.\nThis tool corrects your reading to 30°C standard for valid comparison.")

)