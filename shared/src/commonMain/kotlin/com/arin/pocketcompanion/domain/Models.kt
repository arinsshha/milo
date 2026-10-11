package com.arin.pocketcompanion.domain

enum class MemoryKind { DATE, FACT, PREFERENCE, TEMPORARY, FINANCIAL, ROUTINE }
enum class Source { USER_SAID, LEARNED }
enum class Priority { LOW, NORMAL, IMPORTANT }
enum class ReminderStatus { ACTIVE, DONE, CANCELLED }
enum class AppCategory { PRODUCTIVE, DISTRACTING }

/** How loudly Milo is allowed to speak up. */
enum class NudgeLevel { SILENT, GENTLE, URGENT }

/** What the user did with a nudge. This is what Milo learns from. */
enum class NudgeResponse { ACCEPTED, SNOOZED, DISMISSED, IGNORED }
