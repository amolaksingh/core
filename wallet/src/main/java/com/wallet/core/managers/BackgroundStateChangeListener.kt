package com.wallet.core.managers

import android.app.Activity
import com.core.BackgroundManager
import com.core.IPinComponent
import com.wallet.core.stats.StatsManager

class BackgroundStateChangeListener(
    private val pinComponent: IPinComponent,
    private val statsManager: StatsManager
) : BackgroundManager.Listener {

    override fun willEnterForeground(activity: Activity) {
        pinComponent.willEnterForeground(activity)

        statsManager.sendStats()
    }

    override fun didEnterBackground() {
        pinComponent.didEnterBackground()
    }

    override fun onAllActivitiesDestroyed() {
        pinComponent.lock()
    }

}
