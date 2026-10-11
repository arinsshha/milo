package com.arin.pocketcompanion.android.floating

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.arin.pocketcompanion.domain.ReelsCounterManager

class ReelsAccessibilityService : AccessibilityService() {

    private var lastScrollTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return

        val isReelsApp = pkgName == "com.instagram.android" ||
                pkgName == "com.google.android.youtube" ||
                pkgName == "com.zhiliaoapp.musically"

        // Always update foreground state whenever Instagram/YouTube sends any event
        ReelsCounterManager.setReelsAppForeground(isReelsApp)

        if (isReelsApp && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val now = System.currentTimeMillis()
            // Throttle scroll events (min 1.5 seconds between reels)
            if (now - lastScrollTime >= 1500) {
                lastScrollTime = now
                ReelsCounterManager.incrementScroll()
            }
        }
    }

    override fun onInterrupt() {}
}
