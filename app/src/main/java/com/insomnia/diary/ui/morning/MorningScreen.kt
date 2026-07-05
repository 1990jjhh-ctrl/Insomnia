package com.insomnia.diary.ui.morning

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insomnia.diary.domain.Dream
import com.insomnia.diary.domain.Substance
import com.insomnia.diary.ui.components.DateField
import com.insomnia.diary.ui.components.MoodPicker
import com.insomnia.diary.ui.components.PercentageSlider
import com.insomnia.diary.ui.components.TimeField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningScreen(
    onDone: () -> Unit,
    viewModel: MorningViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Morning" else "Morning Protocol") },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            SectionLabel("How are you feeling?")
            MoodPicker(
                selected = state.moods,
                onToggle = viewModel::toggleMood,
                extraMoods = state.customMoods,
            )

            HorizontalDivider()
            PercentageSlider("Recovery", state.recovery, viewModel::setRecovery)

            HorizontalDivider()
            SectionLabel("Sleep times")
            DateField("Date", state.date, viewModel::setDate, Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Went to bed after midnight", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = state.wentToBedAfterMidnight,
                    onCheckedChange = { viewModel.toggleAfterMidnight() },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeField("In bed", state.inBed, viewModel::setInBed, Modifier.weight(1f))
                TimeField("Asleep", state.asleep, viewModel::setAsleep, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeField("Woke up", state.wokeUp, viewModel::setWokeUp, Modifier.weight(1f))
                TimeField("Out of bed", state.outOfBed, viewModel::setOutOfBed, Modifier.weight(1f))
            }

            HorizontalDivider()
            SectionLabel("Sleep quality")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "Times awake",
                    value = state.awakeCount,
                    onValueChange = viewModel::setAwakeCount,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    label = "Total awake (min)",
                    value = state.totalAwakeMin,
                    onValueChange = viewModel::setTotalAwake,
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider()
            DreamSection(
                dreamType = state.dreamType,
                dreamNote = state.dreamNote,
                onTypeChange = viewModel::setDreamType,
                onNoteChange = viewModel::setDreamNote,
            )

            HorizontalDivider()
            MedicationSection(
                medication = state.medication,
                onAdd = viewModel::addMedication,
                onRemove = viewModel::removeMedication,
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

@Composable
private fun NumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun DreamSection(
    dreamType: Dream?,
    dreamNote: String,
    onTypeChange: (Dream?) -> Unit,
    onNoteChange: (String) -> Unit,
) {
    SectionLabel("Dream recall")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Dream.entries.forEach { dream ->
            FilterChip(
                selected = dreamType == dream,
                onClick = { onTypeChange(if (dreamType == dream) null else dream) },
                label = { Text(dream.label) },
            )
        }
    }
    if (dreamType != null && dreamType != Dream.NO_MEMORY) {
        OutlinedTextField(
            value = dreamNote,
            onValueChange = onNoteChange,
            label = { Text("Dream note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }
}

@Composable
private fun MedicationSection(
    medication: List<Substance>,
    onAdd: (Substance) -> Unit,
    onRemove: (Int) -> Unit,
) {
    SectionLabel("Medication before bed")
    medication.forEachIndexed { i, sub ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${sub.name}  ${sub.amount} ${sub.unit}",
                style = MaterialTheme.typography.bodyMedium,
            )
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            name,
            { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.weight(2f),
        )
        OutlinedTextField(
            amount,
            { amount = it },
            label = { Text("Amount") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            unit,
            { unit = it },
            label = { Text("Unit") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
    val amountDouble = amount.toDoubleOrNull()
    if (name.isNotBlank() && amountDouble != null && unit.isNotBlank()) {
        TextButton(onClick = {
            onAdd(Substance(name.trim(), amountDouble, unit.trim()))
            name = ""
            amount = ""
            unit = ""
        }) { Text("+ Add") }
    }
}
