package com.arin.pocketcompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arin.pocketcompanion.ui.chat.ChatScreen
import com.arin.pocketcompanion.ui.components.Glyph
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.memory.MemoryScreen
import com.arin.pocketcompanion.ui.components.TabIcon
import com.arin.pocketcompanion.ui.theme.CompanionTheme
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import com.arin.pocketcompanion.ui.today.TodayScreen

enum class Tab(val label: String, val glyph: Glyph) {
    Today("Today", Glyph.TODAY),
    Chat("Chat", Glyph.CHAT),
    Memory("Memory", Glyph.MEMORY),
    Focus("Focus", Glyph.FOCUS),
    Money("Money", Glyph.MONEY),
}

private val BottomBarHeight = 72.dp

@Composable
fun App(userName: String = "Arin") {
    CompanionTheme {
        var tab by rememberSaveable { mutableStateOf(Tab.Today) }
        Box(Modifier.fillMaxSize().background(Palette.Paper).statusBarsPadding()) {
            Column(Modifier.fillMaxSize()) {
                // The bottom bar already occupies this much of the screen, so imePadding() below must not re-add it.
                Box(
                    Modifier.weight(1f).fillMaxWidth()
                        .consumeWindowInsets(WindowInsets.navigationBars)
                        .consumeWindowInsets(PaddingValues(bottom = BottomBarHeight)),
                ) {
                    when (tab) {
                        Tab.Today -> TodayScreen(userName, onOpenMemory = { tab = Tab.Memory })
                        Tab.Chat -> ChatScreen()
                        Tab.Memory -> MemoryScreen()
                        Tab.Focus -> ComingSoon("Focus", "Usage tracking and focus mode land in week 7–8.")
                        Tab.Money -> ComingSoon("Money", "Monthly totals and categories land in week 9–10.")
                    }
                }
                BottomBar(current = tab, onSelect = { tab = it })
            }
            if (tab == Tab.Today) {
                Box(
                    Modifier.align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 18.dp, bottom = 100.dp)
                        .size(68.dp)
                        .shadow(10.dp, CircleShape)
                        .background(Palette.Surface, CircleShape)
                        .clickable(role = Role.Button) { tab = Tab.Chat }
                        .semantics { contentDescription = "Talk to Milo" },
                    contentAlignment = Alignment.Center,
                ) { Milo(size = 56.dp) }
            }
        }
    }
}

@Composable
private fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(Palette.Surface)
            .border(1.dp, Palette.Line)
            .navigationBarsPadding()
            .height(BottomBarHeight),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { t ->
            val selected = t == current
            val color = if (selected) Palette.Teal else Palette.Muted
            Column(
                Modifier.widthIn(min = 60.dp).height(56.dp)
                    .clickable(role = Role.Tab) { onSelect(t) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    Modifier.background(if (selected) Palette.TealSoft else Palette.Surface, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) { TabIcon(t.glyph, color) }
                Text(t.label, fontSize = 11.sp, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ComingSoon(title: String, note: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontFamily = DisplayFont, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Milo(size = 44.dp)
            Text(note, color = Palette.Muted, fontSize = 15.sp)
        }
    }
}
