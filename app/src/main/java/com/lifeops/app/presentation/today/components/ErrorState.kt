package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifeops.app.ui.theme.LifeOpsTheme

/**
 * Error state component for Today Screen
 * 
 * Shows when:
 * - Database query fails
 * - Unexpected error occurs
 */
@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error emoji
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Error title
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        // Error message
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        // Retry button
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Retry")
        }
    }
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Error State - Database Error", showBackground = true)
@Composable
private fun PreviewErrorStateDatabase() {
    LifeOpsTheme {
        Surface {
            ErrorState(
                errorMessage = "Failed to load tasks from database",
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Error State - Generic Error", showBackground = true)
@Composable
private fun PreviewErrorStateGeneric() {
    LifeOpsTheme {
        Surface {
            ErrorState(
                errorMessage = "An unexpected error occurred. Please try again.",
                onRetry = {}
            )
        }
    }
}
