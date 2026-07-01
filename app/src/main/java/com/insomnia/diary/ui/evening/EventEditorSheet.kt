package com.insomnia.diary.ui.evening

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.insomnia.diary.domain.EventTypePreset
import com.insomnia.diary.ui.components.PercentageSlider
import com.insomnia.diary.ui.components.TimeField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventEditorSheet(
    draft: EventDraft,
    onDraftChange: (EventDraft) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Event", style = MaterialTheme.typography.titleMedium)

            Text("Type", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EventTypePreset.entries.forEach { preset ->
                    FilterChip(
                        selected = draft.typeLabel == preset.label && !draft.isCustom,
                        onClick = { onDraftChange(draft.copy(typeLabel = preset.label, isCustom = false)) },
                        label = { Text(preset.label) },
                    )
                }
                FilterChip(
                    selected = draft.isCustom,
                    onClick = { onDraftChange(draft.copy(isCustom = true, typeLabel = "")) },
                    label = { Text("Custom…") },
                )
            }
            if (draft.isCustom) {
                OutlinedTextField(
                    value = draft.typeLabel,
                    onValueChange = { onDraftChange(draft.copy(typeLabel = it)) },
                    label = { Text("Custom type name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeField("Start", draft.start, { onDraftChange(draft.copy(start = it)) }, Modifier.weight(1f))
                TimeField("End", draft.end, { onDraftChange(draft.copy(end = it)) }, Modifier.weight(1f))
            }

            Text("Stress range", style = MaterialTheme.typography.labelMedium)
            PercentageSlider("Min", draft.stressMin, { onDraftChange(draft.copy(stressMin = it.coerceAtMost(draft.stressMax))) })
            PercentageSlider("Max", draft.stressMax, { onDraftChange(draft.copy(stressMax = it.coerceAtLeast(draft.stressMin))) })

            OutlinedTextField(
                value = draft.attendeesText,
                onValueChange = { onDraftChange(draft.copy(attendeesText = it)) },
                label = { Text("Attendees (comma-separated)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft.note,
                onValueChange = { onDraftChange(draft.copy(note = it)) },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            val canSave = draft.typeLabel.isNotBlank() && !draft.end.isBefore(draft.start)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = onSave, enabled = canSave) { Text("Save event") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
