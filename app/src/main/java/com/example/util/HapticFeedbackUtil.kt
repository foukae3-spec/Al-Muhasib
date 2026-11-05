package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * High-Precision Tactile Haptic Feedback Utility.
 * 
 * Engineered specifically for modern Linear Resonant Actuators (LRA / X-axis linear motors
 * such as Snapdragon 870 / Xiaomi Poco F4 and modern Pixel/Samsung flagships).
 *
 * Utilizes hardware-accelerated DSP haptic primitives:
 * - API 30+ (Android 11+): [VibrationEffect.Composition] with [PRIMITIVE_CLICK], [PRIMITIVE_TICK], [PRIMITIVE_THUD]
 * - API 29+ (Android 10+): Predefined [VibrationEffect.EFFECT_CLICK], [VibrationEffect.EFFECT_HEAVY_CLICK]
 * - Fallback: Ultra-short 5-8ms square wave pulses to eliminate generic motor resonance buzz.
 */
object HapticFeedbackUtil {

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Authentic crisp mechanical button click sensation.
     * Mimics an instant physical spring switch / tally counter depression with zero decay buzz.
     */
    fun performMechanicalMisbahaTick(context: Context) = performMechanicalClick(context)

    fun performMechanicalClick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (_: Exception) {
                // Fall through to predefined effect
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                return
            } catch (_: Exception) {
                // Fall through to waveform
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Ultra-short 7ms impulse: halts motor before resonance drone begins
            vibrator.vibrate(VibrationEffect.createOneShot(7, 255))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(7)
        }
    }

    /**
     * Subtle, gentle bead slide tick (حبة مسبحة ناعمة).
     */
    fun performDhikrTick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                return
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5, 180))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(5)
        }
    }

    /**
     * Light subtle micro-tick for UI button taps, page turns, and tabs.
     */
    fun performLightTick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.55f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                } else if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.4f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                return
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(4, 120))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(4)
        }
    }

    /**
     * Double mechanical click for 33 or 66 bead milestones.
     */
    fun performMilestoneDoublePulse(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.75f)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 65)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                return
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 8, 45, 10)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 10, 40, 12), -1)
        }
    }

    /**
     * Satisfying mechanical completion thud for 100 complete cycle.
     */
    fun performCompletionRumble(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (vibrator.areAllPrimitivesSupported(
                        VibrationEffect.Composition.PRIMITIVE_CLICK,
                        VibrationEffect.Composition.PRIMITIVE_THUD
                    )
                ) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 35)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                } else if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f, 50)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 50)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                return
            } catch (_: Exception) {
                // Fall through
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 12, 35, 20)
            val amplitudes = intArrayOf(0, 220, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 20, 30, 25), -1)
        }
    }

    /**
     * Unified Misbaha feedback dispatcher according to user's selected profile and bead count.
     * Profile constants: 0 = MECHANICAL, 1 = TICK, 2 = SILENT.
     */
    fun performMisbahaFeedback(context: Context, profile: Int, nextCount: Int, cycleTarget: Int = 100) {
        if (profile == 2) {
            // Silent
            return
        }

        if (nextCount > 0 && nextCount % cycleTarget == 0) {
            performCompletionRumble(context)
        } else if (nextCount > 0 && nextCount % 33 == 0) {
            performMilestoneDoublePulse(context)
        } else {
            if (profile == 1) {
                performDhikrTick(context)
            } else {
                performMechanicalClick(context)
            }
        }
    }

    /** Emergency high-intensity rumble for Salah Warning / Alarm */
    fun performSevereAlarmVibration(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 400, 150, 400, 150, 700)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }
}
