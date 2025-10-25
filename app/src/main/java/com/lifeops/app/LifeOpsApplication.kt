package com.lifeops.app

import android.app.Application
import com.lifeops.app.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Life-Ops Application class
 * 
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 * As per Technical Architecture: Hilt for DI across the application
 */
@HiltAndroidApp
class LifeOpsApplication : Application() {
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with sample data
        applicationScope.launch {
            databaseInitializer.initializeWithSampleData()
        }
    }
}
