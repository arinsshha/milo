package com.arin.pocketcompanion.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.Palette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    LaunchedEffect(state.items.size) {
        if (state.items.isNotEmpty()) listState.animateScrollToItem(state.items.lastIndex)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().background(Palette.Surface).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Milo(size = 40.dp, talking = state.miloTalking)
            Column {
                Text("Milo", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                Text(
                    when {
                        state.sending -> "Thinking…"
                        state.miloTalking -> "Speaking…"
                        else -> "Here when you need me"
                    },
                    fontSize = 12.sp, color = Palette.Muted,
                )
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.items.isEmpty()) {
                item { Hint() }
            }
            items(state.items, key = { it.id }) { item ->
                when (item) {
                    is ChatItem.User -> UserBubble(item.text)
                    is ChatItem.Milo -> MiloBubble(item.text)
                    is ChatItem.Action -> ActionCard(item, onUndo = { viewModel.undo(item) })
                    is ChatItem.Confirm -> ConfirmCard(item, onYes = { viewModel.confirm(item) }, onNo = { viewModel.reject(item) })
                }
            }
        }

        Composer(sending = state.sending, onSend = viewModel::send)
    }
}

@Composable
private fun Hint() {
    MiloBubble("Hi! Try \"remind me to pay the EMI on the 5th every month\", \"remember my DBMS exam is on 14 October\" or \"spent 250 on lunch\".")
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(
            text,
            Modifier.widthIn(max = 300.dp)
                .background(Palette.Ink, RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .padding(horizontal = 15.dp, vertical = 11.dp),
            color = Palette.Surface, fontSize = 15.sp,
        )
    }
}

@Composable
private fun MiloBubble(text: String) {
    Text(
        text,
        Modifier.widthIn(max = 320.dp).bubble().padding(horizontal = 15.dp, vertical = 12.dp),
        color = Palette.Ink, fontSize = 15.sp, lineHeight = 21.sp,
    )
}

@Composable
private fun ActionCard(item: ChatItem.Action, onUndo: () -> Unit) {
    Column(Modifier.widthIn(max = 320.dp).bubble().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(item.result.label.uppercase(), color = Palette.Teal, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Text(
            item.result.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Ink,
            textDecoration = if (item.undone) TextDecoration.LineThrough else null,
        )
        Text(if (item.undone) "Undone" else item.result.detail, fontSize = 13.sp, color = Palette.Muted)
        if (item.result.undoId != null && !item.undone) {
            TextButton(onClick = onUndo, modifier = Modifier.heightIn(min = 44.dp)) { Text("Undo", color = Palette.Muted) }
        }
    }
}

@Composable
private fun ConfirmCard(item: ChatItem.Confirm, onYes: () -> Unit, onNo: () -> Unit) {
    Column(Modifier.widthIn(max = 320.dp).bubble().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(item.question, fontSize = 15.sp, color = Palette.Ink, lineHeight = 21.sp)
        if (!item.resolved) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onYes, colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                    modifier = Modifier.heightIn(min = 44.dp)) { Text("Yes, do it") }
                OutlinedButton(onClick = onNo, modifier = Modifier.heightIn(min = 44.dp)) { Text("No", color = Palette.Ink) }
            }
        }
    }
}

@Composable
private fun Composer(sending: Boolean, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        Modifier.fillMaxWidth().background(Palette.Surface).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Tell Milo anything…") },
            modifier = Modifier.weight(1f).semantics { contentDescription = "Message Milo" },
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 4,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Palette.Paper,
                unfocusedContainerColor = Palette.Paper,
                focusedIndicatorColor = Palette.Paper,
                unfocusedIndicatorColor = Palette.Paper,
            ),
        )
        // TODO: mic button → Android SpeechRecognizer (week 6).
        Button(
            onClick = { onSend(text); text = "" },
            enabled = !sending && text.isNotBlank(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(0.dp),
        ) { Text("↑", fontSize = 20.sp) }
    }
}

private fun Modifier.bubble(): Modifier =
    this.background(Palette.Surface, RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
        .border(1.dp, Palette.Line, RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
