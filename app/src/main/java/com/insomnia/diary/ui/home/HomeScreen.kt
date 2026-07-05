package com.insomnia.diary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val HISTORY_DATE_FMT = DateTimeFormatter.ofPattern("EEE d MMM")

@Composable
fun HomeScreen(
    onStartMorning: () -> Unit,
    onStartEvening: () -> Unit,
    onEditMorning: (Long) -> Unit,
    onEditEvening: (Long) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val latestMorning by viewModel.latestMorning.collectAsStateWithLifecycle(null)
    val latestEvening by viewModel.latestEvening.collectAsStateWithLifecycle(null)
    val history by viewModel.history.collectAsStateWithLifecycle(emptyList())

    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Insomnia", style = MaterialTheme.typography.headlineLarge)
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
            Text(
                text =
                    LocalDate.now().let { d ->
                        "${d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                            "${d.dayOfMonth} " +
                            "${d.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}"
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))
            ProtocolCard(
                title = "Morning Protocol",
                latest = latestMorning,
                summary = { "Recovery ${it.recovery.value}% · ${formatDuration(it)}" },
                onStart = onStartMorning,
            )
            ProtocolCard(
                title = "Evening Protocol",
                latest = latestEvening,
                summary = { "Productivity ${it.productivity.value}% · ${it.events.size} events" },
                onStart = onStartEvening,
            )

            if (history.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "History",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    history.forEach { entry ->
                        HistoryRow(
                            entry = entry,
                            onEdit = {
                                if (entry.isMorning) {
                                    onEditMorning(
                                        entry.id,
                                    )
                                } else {
                                    onEditEvening(entry.id)
                                }
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    } // Scaffold
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

@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    onEdit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(entry.label, style = MaterialTheme.typography.titleSmall)
                    Text(
                        entry.recordedAt.format(HISTORY_DATE_FMT),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(entry.summary, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onEdit) { Text("Edit") }
        }
    }
}
