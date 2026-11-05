package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistence for the first-launch onboarding experience of Al-Muhasib.
 * Supports both [KEY_IS_FIRST_LAUNCH_COMPLETED] and [KEY_HAS_COMPLETED_ONBOARDING]
 * for full consistency across repositories and settings.
 */
object OnboardingPreferences {

    private const val PREFS_NAME = "al_muhasib_app_prefs"
    // ponytail: support all 3 keys (has_seen_onboarding, has_completed_onboarding, isFirstLaunchCompleted)
    const val KEY_IS_FIRST_LAUNCH_COMPLETED = "isFirstLaunchCompleted"
    const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Returns true if the user has already completed the first-time onboarding flow.
     */
    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_IS_FIRST_LAUNCH_COMPLETED, false) ||
                prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false) ||
                prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    }

    /**
     * Persists the completion status of the onboarding flow.
     * Updates all keys to ensure backward and forward compatibility.
     */
    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_FIRST_LAUNCH_COMPLETED, completed)
            .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed)
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, completed)
            .apply()
    }

    /**
     * Resets the onboarding flag, allowing the tour to be replayed.
     */
    fun resetOnboarding(context: Context) {
        setOnboardingCompleted(context, false)
    }
}
