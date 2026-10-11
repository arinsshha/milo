package com.arin.pocketcompanion.ui.memory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.imePadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import com.arin.pocketcompanion.ui.today.card
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MemoryScreen(viewModel: MemoryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingForget by remember { mutableStateOf<MemoryRow?>(null) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Remembered", fontFamily = DisplayFont, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                Text(
                    when (state.total) { 0 -> "Nothing yet"; 1 -> "1 thing Milo knows"; else -> "${state.total} things Milo knows" },
                    color = Palette.Muted, fontSize = 14.sp,
                )
            }
        }

        if (state.total > 0) {
            item { SearchField(state.query, viewModel::setQuery) }
            item { FilterRow(state.filter, viewModel::setFilter) }
        }

        when {
            !state.loaded -> Unit
            state.total == 0 -> item { EmptyState() }
            state.sections.isEmpty() -> item {
                Text("Nothing matches. Try another word or filter.", color = Palette.Muted, fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp))
            }
            else -> items(state.sections, key = { it.title }) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(section.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                    Column(Modifier.card()) {
                        section.rows.forEachIndexed { i, row ->
                            if (i > 0) HorizontalDivider(color = Palette.Line, modifier = Modifier.padding(horizontal = 16.dp))
                            MemoryItem(row, onForget = { pendingForget = row })
                        }
                    }
                }
            }
        }
    }
    
    // Manual Memory Entry field
    var newMemoryText by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.BottomCenter) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.Surface)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newMemoryText,
                onValueChange = { newMemoryText = it },
                placeholder = { Text("Tell Milo to remember...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Palette.Paper,
                    unfocusedContainerColor = Palette.Paper,
                    focusedIndicatorColor = Palette.Paper,
                    unfocusedIndicatorColor = Palette.Paper,
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newMemoryText.isNotBlank()) {
                        viewModel.addMemory(newMemoryText)
                        newMemoryText = ""
                    }
                },
                modifier = Modifier.background(Palette.Teal, CircleShape)
            ) {
                Text("↑", color = Palette.Surface, fontSize = 20.sp)
            }
        }
    }

    pendingForget?.let { row ->
        AlertDialog(
            onDismissRequest = { pendingForget = null },
            title = { Text("Forget \"${row.title}\"?") },
            text = { Text("Milo won't remember this anymore. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.forget(row.id); pendingForget = null }) { Text("Forget", color = Palette.Teal) }
            },
            dismissButton = {
                TextButton(onClick = { pendingForget = null }) { Text("Keep it", color = Palette.Muted) }
            },
            containerColor = Palette.Surface,
        )
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search memories", color = Palette.Muted) },
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Palette.Surface,
            unfocusedContainerColor = Palette.Surface,
            focusedIndicatorColor = Palette.Surface,
            unfocusedIndicatorColor = Palette.Surface,
        ),
    )
}

@Composable
private fun FilterRow(current: MemoryFilter, onSelect: (MemoryFilter) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MemoryFilter.entries.forEach { f ->
            FilterChip(
                selected = f == current,
                onClick = { onSelect(f) },
                label = { Text(f.label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Palette.Surface,
                    labelColor = Palette.Muted,
                    selectedContainerColor = Palette.TealSoft,
                    selectedLabelColor = Palette.Teal,
                ),
            )
        }
    }
}

@Composable
private fun MemoryItem(row: MemoryRow, onForget: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(row.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
            row.detail?.let { Text(it, fontSize = 14.sp, color = Palette.Ink, lineHeight = 20.sp) }
            Text(row.meta, fontSize = 13.sp, color = Palette.Muted)
        }
        TextButton(onClick = onForget, modifier = Modifier.heightIn(min = 44.dp)) { Text("Forget", color = Palette.Muted) }
    }
}

@Composable
private fun EmptyState() {
    Row(
        Modifier.card().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Milo(size = 44.dp)
        Text(
            "Tell Milo \"remember my DBMS exam is on 14 October\" and it'll show up here.",
            color = Palette.Muted, fontSize = 14.sp, lineHeight = 20.sp,
        )
    }
}
