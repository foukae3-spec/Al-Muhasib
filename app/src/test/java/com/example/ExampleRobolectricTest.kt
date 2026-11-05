package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Al-Muhasib", appName)
  }

  @Test
  fun `verify PrayerAdhanPlayer initializes and plays with real Android context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.alarm.PrayerAdhanPlayer.stop()
    org.junit.Assert.assertFalse(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)

    // Play with real context - must not crash with IllegalStateException
    com.example.alarm.PrayerAdhanPlayer.play(context, "الفجر")
    org.junit.Assert.assertTrue(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
    assertEquals("الفجر", com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)

    com.example.alarm.PrayerAdhanPlayer.stop()
    org.junit.Assert.assertFalse(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
    org.junit.Assert.assertNull(com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)
  }
}
