package com.app.ezipaycoin.presentation.dashboard.earn

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.data.remote.dto.response.DailyTask
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground

@Composable
fun EarnTasks(
    tasks: Load<List<DailyTask>>,
    completingTaskId: Int?,
    onOpen: (DailyTask) -> Unit,
    onComplete: (DailyTask) -> Unit,
    onRetry: () -> Unit,
) {
    when (tasks) {
        Load.Loading -> EarnLoading()
        is Load.Failed -> EarnError(tasks.message, onRetry)
        is Load.Ready ->
            if (tasks.data.isEmpty()) EarnEmpty("No tasks today. Check back tomorrow.")
            else Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Daily tasks", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor))
                tasks.data.forEach { task ->
                    TaskCard(task, busy = completingTaskId == task.taskId, onOpen, onComplete)
                }
            }
    }
}

@Composable
private fun TaskCard(
    task: DailyTask,
    busy: Boolean,
    onOpen: (DailyTask) -> Unit,
    onComplete: (DailyTask) -> Unit,
) {
    val context = LocalContext.current
    val link = task.contentUrl?.takeIf { it.startsWith("https://") }

    OutlinedCard(
        border = BorderStroke(1.dp, greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    task.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                    modifier = Modifier.weight(1f)
                )
                task.reward?.amount?.let {
                    Text(
                        "+$it ${task.reward.tokenSymbol.orEmpty()}".trim(),
                        style = MaterialTheme.typography.titleSmall.copy(color = GoldAccentColor)
                    )
                }
            }
            task.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor))
            }
            if (task.isCompleted) {
                Text("Completed today", style = MaterialTheme.typography.labelLarge.copy(color = GoldAccentColor))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (link != null) {
                        OutlinedButton(
                            onClick = {
                                onOpen(task)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Open", color = TextPrimaryColor) }
                    }
                    GoldGradientButton(
                        label = if (busy) "Please wait..." else "Complete",
                        onClick = { if (!busy) onComplete(task) },
                        modifier = Modifier.weight(1f)
                    )
                }
                task.requiredSeconds?.takeIf { it > 0 }?.let {
                    Text(
                        "Spend at least $it seconds on the task first.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor)
                    )
                }
            }
        }
    }
}
