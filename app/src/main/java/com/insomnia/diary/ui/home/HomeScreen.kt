package com.insomnia.diary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insomnia.diary.domain.EveningProtocol
import com.insomnia.diary.domain.MorningProtocol
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    onStartMorning: () -> Unit,
    onStartEvening: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val latestMorning by viewModel.latestMorning.collectAsStateWithLifecycle(null)
    val latestEvening by viewModel.latestEvening.collectAsStateWithLifecycle(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Insomnia", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = LocalDate.now().let { d ->
                "${d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                    "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProtocolCard(
            title = "Morning Protocol",
            latest = latestMorning,
            summary = { "Recovery ${it.recovery.value}% · ${formatSleep(it)}" },
            onStart = onStartMorning,
        )
        ProtocolCard(
            title = "Evening Protocol",
            latest = latestEvening,
            summary = { "Productivity ${it.productivity.value}% · ${it.events.size} events" },
            onStart = onStartEvening,
        )
    }
}

@Composable
private fun <T> ProtocolCard(
    title: String,
    latest: T?,
    summary: (T) -> String,
    onStart: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (latest != null) {
                    Text(
                        summary(latest),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text(if (latest == null) "Fill in" else "Fill in again")
            }
        }
    }
}

private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

private fun formatSleep(p: MorningProtocol): String {
    val h = p.sleepDuration.toHours()
    val m = p.sleepDuration.toMinutesPart()
    return "${p.wokeUp.format(TIME_FMT)} · ${h}h${m}m sleep"
}
