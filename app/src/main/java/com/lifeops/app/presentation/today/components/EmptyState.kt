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
import kotlin.random.Random

/**
 * Empty state component for Today Screen
 * 
 * Shows when:
 * - No tasks exist for the selected date
 * - All tasks are complete for the day
 */
@Composable
fun EmptyState(
    isAllComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val (emoji, message) = if (isAllComplete) {
        getCompletionMessage()
    } else {
        getNoTasksMessage()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Message
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        // Subtitle
        if (!isAllComplete) {
            Text(
                text = "Add tasks from the inventory or create new ones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Get random completion celebration message
 */
private fun getCompletionMessage(): Pair<String, String> {
    val messages = listOf(
        "🎉" to "All Done!",
        "✨" to "You Crushed It!",
        "🌟" to "Perfect Day!",
        "💪" to "Nothing Left!",
        "🎯" to "Mission Complete!",
        "🏆" to "Champion!",
        "⭐" to "Stellar Work!",
        "🔥" to "On Fire!",
        "🎊" to "Fantastic!",
        "👏" to "Well Done!"
    )
    return messages[Random.nextInt(messages.size)]
}

/**
 * Get random no tasks message
 */
private fun getNoTasksMessage(): Pair<String, String> {
    val messages = listOf(
        "📝" to "No Tasks Today",
        "🌴" to "Free Day Ahead",
        "☀️" to "Clear Schedule",
        "🎈" to "Nothing Planned",
        "🌈" to "Open Calendar",
        "🦋" to "Day Off",
        "🌺" to "Relaxation Time",
        "🎨" to "Free to Create",
        "📚" to "Time to Plan",
        "☕" to "Enjoy the Day"
    )
    return messages[Random.nextInt(messages.size)]
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Empty State - No Tasks", showBackground = true)
@Composable
private fun PreviewEmptyStateNoTasks() {
    LifeOpsTheme {
        Surface {
            EmptyState(
                isAllComplete = false
            )
        }
    }
}

@Preview(name = "Empty State - All Complete", showBackground = true)
@Composable
private fun PreviewEmptyStateAllComplete() {
    LifeOpsTheme {
        Surface {
            EmptyState(
                isAllComplete = true
            )
        }
    }
}
