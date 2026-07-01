package com.insomnia.diary.ui.evening

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insomnia.diary.domain.Substance
import com.insomnia.diary.ui.components.MoodPicker
import com.insomnia.diary.ui.components.PercentageSlider
import com.insomnia.diary.ui.components.TimeField
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EveningScreen(
    onDone: () -> Unit,
    viewModel: EveningViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onDone() }

    if (state.showEventSheet) {
        EventEditorSheet(
            draft = state.eventDraft,
            onDraftChange = viewModel::updateEventDraft,
            onSave = viewModel::saveEventDraft,
            onDismiss = viewModel::closeEventSheet,
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Evening Protocol") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            SectionLabel("How are you feeling?")
            MoodPicker(selected = state.moods, onToggle = viewModel::toggleMood)

            HorizontalDivider()
            PercentageSlider("Productivity", state.productivity, viewModel::setProductivity)

            HorizontalDivider()
            EventsSection(
                events = state.events,
                onAdd = viewModel::openNewEvent,
                onEdit = viewModel::openEditEvent,
                onRemove = viewModel::removeEvent,
            )

            HorizontalDivider()
            BatterySection(
                battery = state.battery,
                onAdd = viewModel::addBattery,
                onUpdate = viewModel::updateBattery,
                onRemove = viewModel::removeBattery,
            )

            HorizontalDivider()
            AlcoholSection(
                alcohol = state.alcohol,
                onAdd = viewModel::addAlcohol,
                onRemove = viewModel::removeAlcohol,
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.isSaving) "Saving…" else "Save") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall)
}

private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

@Composable
private fun EventsSection(
    events: List<EventDraft>,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    SectionLabel("Day timeline")
    events.forEachIndexed { i, e ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(e.typeLabel, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${e.start.format(TIME_FMT)}–${e.end.format(TIME_FMT)} · stress ${e.stressMin}–${e.stressMax}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                TextButton(onClick = { onEdit(i) }) { Text("Edit") }
                TextButton(onClick = { onRemove(i) }) { Text("Remove") }
            }
        }
    }
    TextButton(onClick = onAdd) { Text("+ Add event") }
}

@Composable
private fun BatterySection(
    battery: List<BatteryDraft>,
    onAdd: () -> Unit,
    onUpdate: (Int, BatteryDraft) -> Unit,
    onRemove: (Int) -> Unit,
) {
    SectionLabel("Battery / energy checkpoints")
    battery.forEachIndexed { i, b ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TimeField(
                label = "Time",
                value = b.at,
                onValueChange = { onUpdate(i, b.copy(at = it)) },
                modifier = Modifier.weight(1f),
            )
            Column(modifier = Modifier.weight(2f)) {
                PercentageSlider(
                    label = "Level",
                    value = b.level,
                    onValueChange = { onUpdate(i, b.copy(level = it)) },
                )
            }
            TextButton(onClick = { onRemove(i) }) { Text("×") }
        }
    }
    TextButton(onClick = onAdd) { Text("+ Add checkpoint") }
}

@Composable
private fun AlcoholSection(
    alcohol: List<Substance>,
    onAdd: (Substance) -> Unit,
    onRemove: (Int) -> Unit,
) {
    SectionLabel("Alcohol")
    alcohol.forEachIndexed { i, sub ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("${sub.name}  ${sub.amount} ${sub.unit}", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { onRemove(i) }) { Text("Remove") }
        }
    }
    AddSubstanceRow(onAdd = onAdd)
}

@Composable
private fun AddSubstanceRow(onAdd: (Substance) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.weight(2f))
        OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, singleLine = true,
            modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        OutlinedTextField(unit, { unit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
    }
    val amountDouble = amount.toDoubleOrNull()
    if (name.isNotBlank() && amountDouble != null && unit.isNotBlank()) {
        TextButton(onClick = {
            onAdd(Substance(name.trim(), amountDouble, unit.trim()))
            name = ""; amount = ""; unit = ""
        }) { Text("+ Add") }
    }
}
