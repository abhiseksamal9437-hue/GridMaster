package com.example.gridmaster

import android.app.Application
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GridMasterApplication : Application() {

    companion object {
        const val PREFS_NAME = "gridmaster_prefs"
        const val KEY_INSTALL_DATE = "install_date"
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Setup Shared Prefs
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_INSTALL_DATE)) {
            prefs.edit().putLong(KEY_INSTALL_DATE, System.currentTimeMillis()).apply()
        }

        // 2. CONFIGURE FIREBASE OFFLINE CACHE (The "New" Database)
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
    }
}