package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifeops.app.ui.theme.LifeOpsTheme

/**
 * Loading state component for Today Screen
 * 
 * Shows when:
 * - Tasks are being fetched from the database
 * - Initial load of the screen
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress indicator
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        // Loading text
        Text(
            text = "Loading tasks...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun PreviewLoadingState() {
    LifeOpsTheme {
        Surface {
            LoadingState()
        }
    }
}
