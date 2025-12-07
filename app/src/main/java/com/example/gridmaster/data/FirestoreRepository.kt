package com.example.gridmaster.data
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage // Import this
import kotlinx.coroutines.tasks.await // Import this
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val storage = FirebaseStorage.getInstance().reference

    // --- HELPER: GET CURRENT ENGINEER INFO ---
    // Extracts name from email (e.g., "b.sahoo@optcl.in" -> "B Sahoo")
    private fun getCurrentUser(): Pair<String, String> {
        val user = auth.currentUser
        val uid = user?.uid ?: ""
        val name = user?.email?.substringBefore("@")
            ?.replace(".", " ")
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: "Unknown Engineer"
        return Pair(uid, name)
    }

    // ==========================================
    // 1. FAULT LOGS (Cloud Sync + Audit Trail)
    // ==========================================
    fun getFaults(): Flow<List<FaultLog>> = callbackFlow {
        // Order by Trip Time (Newest First)
        val ref = db.collection("fault_logs")
            .orderBy("tripTime", Query.Direction.DESCENDING)

        val subscription = ref.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FaultLog::class.java)?.copy(id = doc.id)
                }
                trySend(list)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addFault(fault: FaultLog) {
        val (uid, name) = getCurrentUser()
        val newDoc = db.collection("fault_logs").document()

        // AUTO-STAMP USER INFO
        val finalFault = fault.copy(
            id = newDoc.id,
            userId = uid,
            userName = name,
            timestamp = System.currentTimeMillis()
        )
        newDoc.set(finalFault).await()
    }

    suspend fun updateFault(fault: FaultLog) {
        // We don't overwrite the original author, just update the status
        if (fault.id.isNotEmpty()) {
            db.collection("fault_logs").document(fault.id).set(fault).await()
        }
    }

    suspend fun deleteFault(id: String) {
        db.collection("fault_logs").document(id).delete().await()
    }

    // ==========================================
    // 2. WORK ORDERS (Maintenance Notes)
    // ==========================================
    fun getNotes(): Flow<List<PlannedWork>> = callbackFlow {
        val ref = db.collection("work_orders").orderBy("scheduledDate")
        val sub = ref.addSnapshotListener { s, _ ->
            if (s != null) {
                val list = s.documents.mapNotNull {
                    it.toObject(PlannedWork::class.java)?.copy(id = it.id)
                }
                trySend(list)
            }
        }
        awaitClose { sub.remove() }
    }

    suspend fun saveNote(note: PlannedWork) {
        val (uid, name) = getCurrentUser()

        if (note.id.isEmpty()) {
            // CREATE NEW: Stamp Creator
            val newDoc = db.collection("work_orders").document()
            val finalNote = note.copy(
                id = newDoc.id,
                createdById = uid,
                createdBy = name
            )
            newDoc.set(finalNote).await()
        } else {
            // UPDATE EXISTING
            db.collection("work_orders").document(note.id).set(note).await()
        }
    }

    suspend fun deleteNote(id: String) {
        db.collection("work_orders").document(id).delete().await()
    }

    // ==========================================
    // 3. MAINTENANCE TASKS (Shared Daily Checklist)
    // ==========================================
    fun getTasks(): Flow<List<MaintenanceTask>> = callbackFlow {
        val ref = db.collection("daily_tasks")
        val sub = ref.addSnapshotListener { s, _ ->
            if (s != null) {
                val list = s.documents.mapNotNull {
                    it.toObject(MaintenanceTask::class.java)?.copy(id = it.id)
                }
                trySend(list)
            }
        }
        awaitClose { sub.remove() }
    }

    suspend fun updateTask(task: MaintenanceTask) {
        val (uid, name) = getCurrentUser()
        if (task.id.isNotEmpty()) {
            // When checking a box, stamp WHO did it
            val finalTask = task.copy(
                completedBy = if (task.isCompleted) name else "",
                completedById = if (task.isCompleted) uid else ""
            )
            db.collection("daily_tasks").document(task.id).set(finalTask).await()
        }
    }

    // Uploads the Manual to Cloud (Runs once)
    suspend fun seedInitialTasks(tasks: List<MaintenanceTask>) {
        val batch = db.batch()
        val col = db.collection("daily_tasks")
        tasks.forEach { task ->
            val doc = col.document()
            batch.set(doc, task.copy(id = doc.id))
        }
        batch.commit().await()
    }

    // ==========================================
    // 4. DUTY ROSTER OVERRIDES
    // ==========================================
    fun getDutyOverrides(): Flow<List<DutyOverride>> = callbackFlow {
        val ref = db.collection("duty_overrides")
        val sub = ref.addSnapshotListener { s, _ ->
            if (s != null) {
                val list = s.documents.mapNotNull { it.toObject(DutyOverride::class.java) }
                trySend(list)
            }
        }
        awaitClose { sub.remove() }
    }

    suspend fun addDutyOverride(override: DutyOverride) {
        // ID = "Name_Date" to prevent duplicates
        val key = "${override.staffName}_${override.dateEpoch}"
        db.collection("duty_overrides").document(key).set(override).await()
    }

    suspend fun deleteDutyOverride(staffName: String, dateEpoch: Long) {
        val key = "${staffName}_${dateEpoch}"
        db.collection("duty_overrides").document(key).delete().await()
    }
    suspend fun uploadFaultImage(uri: Uri): String? {
        return try {
            val filename = "faults/${System.currentTimeMillis()}.jpg"
            val ref = storage.child(filename)
            ref.putFile(uri).await() // Upload
            ref.downloadUrl.await().toString() // Get Link
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
