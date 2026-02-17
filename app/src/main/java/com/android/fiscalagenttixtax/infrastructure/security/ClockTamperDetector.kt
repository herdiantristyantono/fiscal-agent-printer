package com.android.fiscalagenttixtax.infrastructure.security

import android.content.SharedPreferences
import android.os.SystemClock

class ClockTamperDetector(
    private val prefs: SharedPreferences
) {

    fun detect(): Boolean {
        val lastWall = prefs.getLong("wall", 0)
        val lastMono = prefs.getLong("mono", 0)

        if (lastWall == 0L || lastMono == 0L) {
            saveBaseline()
            return false
        }

        val deltaWall = System.currentTimeMillis() - lastWall
        val deltaMono = SystemClock.elapsedRealtime() - lastMono

        return kotlin.math.abs(deltaWall - deltaMono) > 5 * 60 * 1000
    }

    fun saveBaseline() {
        prefs.edit()
            .putLong("wall", System.currentTimeMillis())
            .putLong("mono", SystemClock.elapsedRealtime())
            .apply()
    }
}