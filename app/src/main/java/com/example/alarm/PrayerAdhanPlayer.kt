package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// ponytail: lightweight reactive audio player supporting authentic Adhan and system fallback with 90s timeout
object PrayerAdhanPlayer {
    val isPlaying = MutableStateFlow(false)
    val currentAlarmPrayer = MutableStateFlow<String?>(null)

    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var timeoutJob: Job? = null
    private var volumeRampJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Synchronized
    fun play(context: Context?, prayerName: String = "الصلاة", timeoutMs: Long = 90_000L) {
        stop()
        isPlaying.value = true
        currentAlarmPrayer.value = prayerName

        // ponytail: automatically terminate playback after timeout (default: 90s / 1.5 minutes)
        timeoutJob = scope.launch {
            delay(timeoutMs)
            stop()
        }

        if (context == null) return

        try {
            // First attempt: authentic public domain human Adhan recording
            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            // ponytail: resolve adhan tone from user preference
            val toneResId = resolveAdhanResource(context)
            mediaPlayer = MediaPlayer.create(context.applicationContext, toneResId, audioAttr, 0)?.apply {
                isLooping = true
                setVolume(0.15f, 0.15f) // Soft start at 15% volume
                start()
            }

            // Smooth volume ramp: 15% -> 100% over 5 seconds
            volumeRampJob?.cancel()
            volumeRampJob = scope.launch {
                val steps = 20
                val stepDelayMs = 250L
                val startVolume = 0.15f
                val targetVolume = 1.0f
                for (i in 1..steps) {
                    delay(stepDelayMs)
                    val vol = startVolume + (targetVolume - startVolume) * (i.toFloat() / steps)
                    try {
                        mediaPlayer?.setVolume(vol, vol)
                    } catch (_: Exception) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            mediaPlayer = null
        }

        // Fallback: standard system alarm / ringtone if MediaPlayer could not initialize
        if (mediaPlayer == null) {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                ringtone = RingtoneManager.getRingtone(context.applicationContext, alarmUri)?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        streamType = AudioManager.STREAM_ALARM
                    }
                    play()
                }
            } catch (e: Exception) {
                // Ignore if audio hardware unavailable
            }
        }
    }

    @Synchronized
    fun stop() {
        timeoutJob?.cancel()
        timeoutJob = null
        volumeRampJob?.cancel()
        volumeRampJob = null

        try {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                } catch (e: Exception) {
                    // Ignore state exception during stop
                }
                // ponytail: ensure native resources are always freed even if stop() threw
                it.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null

        try {
            ringtone?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        ringtone = null

        isPlaying.value = false
        currentAlarmPrayer.value = null
    }

    // ponytail: resolve Sunni adhan raw resource from user preference, defaulting to Abdul-Basit
    private fun resolveAdhanResource(context: Context): Int {
        val toneId = runCatching {
            com.example.util.prayer.PrayerPreferences.getSettings(context).adhanToneId
        }.getOrDefault("abdulbasit")
        return when (toneId) {
            "abdulbasit" -> R.raw.adhan_abdulbasit
            "makkah" -> R.raw.adhan_makkah
            "madinah" -> R.raw.adhan_madinah
            "alaqsa" -> R.raw.adhan_alaqsa
            "egypt" -> R.raw.adhan_egypt
            else -> R.raw.adhan_abdulbasit
        }
    }
}
