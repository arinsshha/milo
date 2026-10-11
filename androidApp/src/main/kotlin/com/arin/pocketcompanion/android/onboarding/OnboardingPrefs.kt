package com.arin.pocketcompanion.android.onboarding

import android.content.Context

/** Simple on/off flag: has the user been through onboarding once? */
class OnboardingPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    var completed: Boolean
        get() = prefs.getBoolean("completed", false)
        set(value) = prefs.edit().putBoolean("completed", value).apply()
}