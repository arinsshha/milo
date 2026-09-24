package com.arin.pocketcompanion.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.pocketcompanion.data.local.MemoryDao
import com.arin.pocketcompanion.data.remote.CompanionApi
import com.arin.pocketcompanion.data.repository.ActionResult
import com.arin.pocketcompanion.data.repository.IntentExecutor
import com.arin.pocketcompanion.data.repository.Outcome
import com.arin.pocketcompanion.domain.IntentDto
import com.arin.pocketcompanion.domain.InterpretRequest
import com.arin.pocketcompanion.domain.MemoryContext
import com.arin.pocketcompanion.common.Format
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface ChatItem {
    val id: Long
    data class User(override val id: Long, val text: String) : ChatItem
    data class Milo(override val id: Long, val text: String) : ChatItem
    data class Action(override val id: Long, val result: ActionResult, val undone: Boolean = false) : ChatItem
    data class Confirm(override val id: Long, val intent: IntentDto, val question: String, val resolved: Boolean = false) : ChatItem
}

data class ChatUiState(
    val items: List<ChatItem> = emptyList(),
    val sending: Boolean = false,
    val miloTalking: Boolean = false,
)

@OptIn(ExperimentalTime::class)
class ChatViewModel(
    private val api: CompanionApi,
    private val executor: IntentExecutor,
    private val memoryDao: MemoryDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()
    private var nextId = 0L
    private var talkJob: Job? = null

    fun send(text: String) {
        val message = text.trim()
        if (message.isEmpty() || _state.value.sending) return
        add(ChatItem.User(nextId++, message))
        _state.update { it.copy(sending = true) }

        viewModelScope.launch {
            val zone = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val memories = memoryDao.recent(now.toEpochMilliseconds(), limit = 30).map {
                MemoryContext(it.title, it.date?.let { d -> Format.dayLabel(d, zone) })
            }
            api.interpret(InterpretRequest(message, now.toString(), zone.id, memories))
                .onSuccess { response ->
                    add(ChatItem.Milo(nextId++, response.reply))
                    speak(response.reply)
                    response.intents.forEach { handle(executor.execute(it)) }
                }
                .onFailure {
                    add(ChatItem.Milo(nextId++, "I couldn't reach my brain just now. Try again in a moment?"))
                }
            _state.update { it.copy(sending = false) }
        }
    }

    fun confirm(item: ChatItem.Confirm) = viewModelScope.launch {
        resolve(item.id)
        handle(executor.execute(item.intent, confirmed = true))
    }

    fun reject(item: ChatItem.Confirm) {
        resolve(item.id)
        add(ChatItem.Milo(nextId++, "Okay, I'll leave it."))
    }

    fun undo(item: ChatItem.Action) = viewModelScope.launch {
        executor.undo(item.result)
        _state.update { s -> s.copy(items = s.items.map { if (it.id == item.id) item.copy(undone = true) else it }) }
    }

    private fun handle(outcome: Outcome) {
        when (outcome) {
            is Outcome.Done -> add(ChatItem.Action(nextId++, outcome.result))
            is Outcome.NeedsConfirmation -> add(ChatItem.Confirm(nextId++, outcome.intent, outcome.question))
            is Outcome.Skipped -> Unit
        }
    }

    /** Placeholder until TextToSpeech is wired in: talk for roughly as long as the reply would take to say. */
    private fun speak(text: String) {
        talkJob?.cancel()
        talkJob = viewModelScope.launch {
            _state.update { it.copy(miloTalking = true) }
            delay((text.length * 55L).coerceIn(800L, 4_000L))
            _state.update { it.copy(miloTalking = false) }
        }
    }

    private fun resolve(id: Long) = _state.update { s ->
        s.copy(items = s.items.map { if (it is ChatItem.Confirm && it.id == id) it.copy(resolved = true) else it })
    }

    private fun add(item: ChatItem) = _state.update { it.copy(items = it.items + item) }
}
