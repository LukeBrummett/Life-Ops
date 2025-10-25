package com.lifeops.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.lifeops.app.domain.usecase.CreateBackupUseCase
import com.lifeops.app.navigation.LifeOpsNavGraph
import com.lifeops.app.ui.theme.LifeOpsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var createBackupUseCase: CreateBackupUseCase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Trigger automatic backup on app launch (once per day)
        triggerAutomaticBackup()
        
        setContent {
            LifeOpsTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LifeOpsNavGraph(navController = navController)
                }
            }
        }
    }
    
    private fun triggerAutomaticBackup() {
        lifecycleScope.launch {
            val prefs = getSharedPreferences("lifeops_backups", MODE_PRIVATE)
            val lastBackupDate = prefs.getString("last_auto_backup_date", null)
            val today = LocalDate.now().toString()
            
            // Only backup once per day
            if (lastBackupDate != today) {
                val result = createBackupUseCase.execute(isAutomatic = true)
                if (result.isSuccess) {
                    prefs.edit()
                        .putString("last_auto_backup_date", today)
                        .apply()
                }
            }
        }
    }
}
