package com.arin.pocketcompanion.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.pocketcompanion.data.local.MemoryDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.arin.pocketcompanion.data.remote.CompanionApi
import com.arin.pocketcompanion.data.repository.IntentExecutor
import com.arin.pocketcompanion.domain.InterpretRequest
import com.arin.pocketcompanion.domain.MemoryContext
import com.arin.pocketcompanion.common.Format

data class MemoryUiState(
    val query: String = "",
    val filter: MemoryFilter = MemoryFilter.All,
    val sections: List<MemorySection> = emptyList(),
    val total: Int = 0,
    val loaded: Boolean = false,
    val isAdding: Boolean = false,
)

@OptIn(ExperimentalTime::class)
class MemoryViewModel(
    private val memoryDao: MemoryDao,
    private val api: CompanionApi,
    private val executor: IntentExecutor
) : ViewModel() {

    private val zone = TimeZone.currentSystemDefault()
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(MemoryFilter.All)

    // Re-evaluated every minute so temporary memories vanish and "forgets in" labels count down.
    private val clock = flow {
        while (true) {
            emit(Clock.System.now().toEpochMilliseconds())
            delay(60_000)
        }
    }

    val state: StateFlow<MemoryUiState> = combine(
        memoryDao.observeActive(Clock.System.now().toEpochMilliseconds()),
        query,
        filter,
        clock,
    ) { memories, q, f, now ->
        MemoryUiState(
            query = q,
            filter = f,
            sections = MemoryList.build(memories, q, f, now, zone),
            total = memories.count { it.expiresAt == null || it.expiresAt > now },
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemoryUiState())

    fun setQuery(value: String) { query.value = value }
    fun setFilter(value: MemoryFilter) { filter.value = value }

    fun addMemory(text: String) {
        val message = text.trim()
        if (message.isEmpty()) return
        
        viewModelScope.launch {
            val now = Clock.System.now()
            val memories = memoryDao.recent(now.toEpochMilliseconds(), limit = 30).map {
                MemoryContext(it.title, it.date?.let { d -> Format.dayLabel(d, zone) })
            }
            // For now, silently interpret and execute intents (like SaveMemoryIntent)
            api.interpret(InterpretRequest(message, now.toString(), zone.id, memories))
                .onSuccess { response ->
                    response.intents.forEach { executor.execute(it) }
                }
        }
    }

    /** Final by design; the screen confirms before calling this. */
    fun forget(id: String) {
        viewModelScope.launch { memoryDao.delete(id) }
    }
}
