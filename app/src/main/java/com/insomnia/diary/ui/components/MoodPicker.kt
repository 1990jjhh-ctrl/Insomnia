package com.insomnia.diary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.insomnia.diary.domain.FeelingPreset
import com.insomnia.diary.domain.Mood

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodPicker(
    selected: List<Mood>,
    onToggle: (Mood) -> Unit,
    modifier: Modifier = Modifier,
    extraMoods: List<Mood> = emptyList(),
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FeelingPreset.entries.forEach { preset ->
            val mood = Mood.from(preset)
            FilterChip(
                selected = selected.any { it.label == mood.label },
                onClick = { onToggle(mood) },
                label = { Text(preset.label) },
            )
        }
        extraMoods.forEach { mood ->
            FilterChip(
                selected = selected.any { it.label == mood.label },
                onClick = { onToggle(mood) },
                label = { Text(mood.label) },
            )
        }
    }
}
