package com.arin.pocketcompanion.domain

/**
 * Decides whether Milo may interrupt, and how loudly.
 * Pure Kotlin, no Android, no AI: easy to unit test and easy to explain in an interview.
 */
class InterventionEngine(
    private val dismissalsBeforeGoingSilent: Int = 3,
) {
    data class Context(
        val requested: NudgeLevel,
        val isQuietTime: Boolean,          // sleep, class, meeting or focus session
        val interruptionsToday: Int,
        val dailyBudget: Int,
        val userSaidNotToday: Boolean,
        /** Most recent first: past responses to this kind of nudge in this context. */
        val recentResponses: List<NudgeResponse>,
    )

    data class Decision(val level: NudgeLevel?, val reason: String) {
        val shouldShow: Boolean get() = level != null
    }

    fun decide(ctx: Context): Decision {
        if (ctx.userSaidNotToday && ctx.requested != NudgeLevel.URGENT) {
            return Decision(null, "You asked me to stay quiet about this today.")
        }
        if (ctx.isQuietTime && ctx.requested != NudgeLevel.URGENT) {
            return Decision(null, "It's quiet time and this isn't urgent.")
        }
        if (ctx.requested == NudgeLevel.URGENT) {
            return Decision(NudgeLevel.URGENT, "Urgent things always get through.")
        }

        var level = ctx.requested
        var reason = "Nothing is blocking this."

        val recent = ctx.recentResponses.take(dismissalsBeforeGoingSilent)
        val keptDismissing = recent.size == dismissalsBeforeGoingSilent &&
            recent.all { it == NudgeResponse.DISMISSED || it == NudgeResponse.IGNORED }
        if (keptDismissing) {
            level = NudgeLevel.SILENT
            reason = "You've waved off the last $dismissalsBeforeGoingSilent of these, so I'll just glow."
        }

        if (ctx.interruptionsToday >= ctx.dailyBudget && level != NudgeLevel.SILENT) {
            level = NudgeLevel.SILENT
            reason = "Today's interruption budget is used up."
        }
        return Decision(level, reason)
    }
}
