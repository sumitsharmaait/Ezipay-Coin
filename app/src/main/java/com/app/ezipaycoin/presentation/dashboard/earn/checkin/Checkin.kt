package com.app.ezipaycoin.presentation.dashboard.earn.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun Checkin(
) {

    val currentMonth = remember { YearMonth.now() }
    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth.plusMonths(12),
        firstVisibleMonth = currentMonth
    )

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                "Daily Rewards",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(16.dp)
            ) {
                Text(
                    text = "${
                        state.firstVisibleMonth.yearMonth.month.name.lowercase()
                            .replaceFirstChar { it.uppercase() }
                    } ${state.firstVisibleMonth.yearMonth.year}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(8.dp))

                WeekDaysHeader()
                Spacer(Modifier.height(8.dp))
                val calendarHeight = 6 * 45.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calendarHeight)
                ) {
                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            val isSelected = selectedDate == day.date

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFFD4A24C) else Color.Transparent
                                    )
                                    .clickable {
                                        selectedDate = day.date
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.date.dayOfMonth.toString(),
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    )
                }
            }
        }
        item{
            GoldGradientButton(
                "Check - IN",
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
            )
        }

    }

}

@Composable
private fun WeekDaysHeader() {
    val daysOfWeek = daysOfWeek()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day.name.take(3), // MON, TUE...
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview
@Composable
fun StakingPreview() {
    EzipayCoinTheme {
        Checkin()
    }
}