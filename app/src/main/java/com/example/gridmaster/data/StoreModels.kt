package com.example.gridmaster.data

import com.google.firebase.firestore.PropertyName

// 1. THE ASSET (Matches your Excel Row)
data class StoreItem(
    // Unique ID (e.g. "item_61") - We will generate this from the Master SN
    val id: String = "",

    // IDENTITY
    val legacyName: String = "", // "EHV grade new Transformer oil" (Your Old Name)
    val sapName: String = "",    // "OIL,TRANSFORMER..." (New SAP Standard)
    val nickname: String = "",   // "Used Oil" (Your Daily Alias)

    // AUDIT
    val masterSn: Int = 0,       // Matches "S.N." in your Excel (CRITICAL)
    // [NEW] To force the exact Excel sequence
    val sortIndex: Int = 0,

    // INVENTORY
    val unit: String = "No.",    // No., Ltr, Mtr, Set
    val unitRate: Double = 0.0,  // Price per unit

    @get:PropertyName("quantity")
    val quantity: Double = 0.0,  // Current Stock (Closing Balance)

    val minStock: Double = 0.0,  // Low stock alert threshold (Optional)
    val location: String = "",   // e.g. "Rack 4, Shelf B" (Optional)

    val lastUpdated: Long = System.currentTimeMillis()
)

// 2. THE LEDGER (Tracks every movement)
data class StoreTransaction(
    val id: String = "",
    val itemId: String = "",     // Links back to the Item (e.g., "item_61")
    val itemName: String = "",   // Snapshot of name at time of issue

    val type: TransactionType = TransactionType.ISSUE,
    val quantity: Double = 0.0,

    val date: Long = System.currentTimeMillis(),

    // PROOF
    val reference: String = "",  // "SRIV-102" or "Site Indent #5"
    val remarks: String = "",

    // AUDIT TRAIL
    val userId: String = "",
    val userName: String = ""
)

enum class TransactionType {
    RECEIVE, // Adds to Stock
    ISSUE    // Subtracts from Stock
}