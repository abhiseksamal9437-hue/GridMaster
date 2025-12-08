package com.example.gridmaster.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class StoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- 1. LIVE INVENTORY STREAM ---
    fun getStoreItems(): Flow<List<StoreItem>> = callbackFlow {
        // Order by "Master Serial Number" so it matches your Excel sheet
        val ref = db.collection("store_inventory").orderBy("sortIndex", Query.Direction.ASCENDING)

        val sub = ref.addSnapshotListener { s, _ ->
            if (s != null) {
                val list = s.documents.mapNotNull { doc ->
                    doc.toObject(StoreItem::class.java)?.copy(id = doc.id)
                }
                trySend(list)
            }
        }
        awaitClose { sub.remove() }
    }

    // --- 2. EXECUTE TRANSACTION (The Accountant) ---
    suspend fun executeTransaction(item: StoreItem, type: TransactionType, qty: Double, ref: String, remarks: String) {
        val (uid, name) = getCurrentUser()

        // Use a "Batch" or "Transaction" to ensure safety
        db.runTransaction { transaction ->
            val itemRef = db.collection("store_inventory").document(item.id)
            val snapshot = transaction.get(itemRef)

            // 1. Calculate New Quantity
            val currentQty = snapshot.getDouble("quantity") ?: 0.0
            val newQty = if (type == TransactionType.RECEIVE) {
                currentQty + qty
            } else {
                if (currentQty < qty) throw Exception("Insufficient Stock! Available: $currentQty")
                currentQty - qty
            }

            // 2. Update the Item Stock
            transaction.update(itemRef, "quantity", newQty)
            transaction.update(itemRef, "lastUpdated", System.currentTimeMillis())

            // 3. Create the History Log
            val logRef = db.collection("store_transactions").document()
            val log = StoreTransaction(
                id = logRef.id,
                itemId = item.id,
                itemName = item.legacyName, // We log the Legacy Name for history
                type = type,
                quantity = qty,
                date = System.currentTimeMillis(),
                reference = ref,
                remarks = remarks,
                userId = uid,
                userName = name
            )
            transaction.set(logRef, log)
        }.await()
    }

    // --- 3. MASTER EDIT (Fixing Typos / Updating SAP Name) ---
    suspend fun updateItemDetails(item: StoreItem) {
        db.collection("store_inventory").document(item.id).set(item).await()
    }
    suspend fun addNewItem(item: StoreItem) {
        // [FIX] Use Auto-ID instead of "item_{SN}"
        // This prevents "S.N. 1" (VCB) from being overwritten by "S.N. 1" (Conductor)
        val docRef = db.collection("store_inventory").document()

        // Save the item with the generated ID
        docRef.set(item.copy(id = docRef.id)).await()
    }

    // --- 4. INITIAL SETUP (Add New Item) ---


    // --- HELPER ---
    private fun getCurrentUser(): Pair<String, String> {
        val user = auth.currentUser
        val uid = user?.uid ?: ""
        val name = user?.email?.substringBefore("@")
            ?.replace(".", " ")
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: "Unknown"
        return Pair(uid, name)
    }
}