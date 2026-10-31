package com.app.ezipaycoin.presentation.dashboard.earn.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.app.ezipaycoin.data.remote.dto.response.CheckInStatusResponse
import com.app.ezipaycoin.presentation.dashboard.earn.EarnError
import com.app.ezipaycoin.presentation.dashboard.earn.EarnLoading
import com.app.ezipaycoin.presentation.dashboard.earn.Load
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun Checkin(
    status: Load<CheckInStatusResponse>,
    busy: Boolean,
    onCheckIn: () -> Unit,
    onRetry: () -> Unit,
) {
    when (status) {
        Load.Loading -> EarnLoading()
        is Load.Failed -> EarnError(status.message, onRetry)
        is Load.Ready -> CheckinContent(status.data, busy, onCheckIn)
    }
}

@Composable
private fun CheckinContent(
    status: CheckInStatusResponse,
    busy: Boolean,
    onCheckIn: () -> Unit,
) {
    val month = remember(status.month) {
        runCatching { YearMonth.parse(status.month) }.getOrElse { YearMonth.now() }
    }
    val checkedIn = remember(status.checkedInDates) {
        status.checkedInDates.orEmpty().mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
    }
    val today = remember(status.today) { runCatching { LocalDate.parse(status.today) }.getOrNull() }
    val calendar = rememberCalendarState(startMonth = month, endMonth = month, firstVisibleMonth = month)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(8.dp)) {
        Text("Daily Rewards", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor))

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
        ) {
            Text(
                text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            WeekDaysHeader()
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6 * 45.dp)) {
                HorizontalCalendar(
                    state = calendar,
                    dayContent = { day ->
                        val done = day.date in checkedIn
                        val isToday = day.date == today
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(if (done) GoldAccentColor else Color.Transparent)
                                .then(if (isToday && !done) Modifier.border(1.dp, GoldAccentColor, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                color = when {
                                    done -> Color.Black
                                    day.position.name == "MonthDate" -> Color.White
                                    else -> Color.DarkGray
                                }
                            )
                        }
                    }
                )
            }
        }

        Text(
            "Current streak: ${status.currentStreak} day${if (status.currentStreak == 1) "" else "s"}",
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
        )
        status.nextReward?.takeIf { !it.amount.isNullOrBlank() }?.let { next ->
            Text(
                "Next check-in earns ${next.amount} ${next.tokenSymbol.orEmpty()}" +
                    if (next.lockDays > 0) ", locked for ${next.lockDays} days" else "",
                style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor)
            )
        }

        if (status.canCheckIn) {
            GoldGradientButton(
                label = if (busy) "Checking in..." else "Check in",
                onClick = { if (!busy) onCheckIn() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
        } else {
            Text(
                "You have checked in today. Come back tomorrow.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextHintColor),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WeekDaysHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        daysOfWeek().forEach { day ->
            Text(
                text = day.name.take(3),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}
