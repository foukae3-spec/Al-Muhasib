package com.example.util.qibla

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class CompassReading(
    val azimuthDegrees: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isSupported: Boolean = true
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassFlow = MutableStateFlow(CompassReading())
    val compassFlow: StateFlow<CompassReading> = _compassFlow.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var isAccelerometerSet = false
    private var isMagnetometerSet = false

    private var geomagneticField: GeomagneticField? = null
    private var smoothedAzimuth = 0f

    fun updateLocation(latitude: Double, longitude: Double, altitudeMeters: Double = 0.0) {
        try {
            geomagneticField = GeomagneticField(
                latitude.toFloat(),
                longitude.toFloat(),
                altitudeMeters.toFloat(),
                System.currentTimeMillis()
            )
        } catch (_: Exception) {}
    }

    fun startListening() {
        val sm = sensorManager ?: run {
            _compassFlow.value = CompassReading(isSupported = false)
            return
        }

        if (rotationSensor != null) {
            sm.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null && magnetometer != null) {
            sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        } else {
            _compassFlow.value = CompassReading(isSupported = false)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val ev = event ?: return

        var azimuthDeg = 0f

        if (ev.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, ev.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
        } else if (ev.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            lowPassFilter(ev.values, lastAccelerometer)
            isAccelerometerSet = true
        } else if (ev.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lowPassFilter(ev.values, lastMagnetometer)
            isMagnetometerSet = true
        }

        if (rotationSensor == null && isAccelerometerSet && isMagnetometerSet) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            }
        }

        // Normalize azimuth to 0..360
        azimuthDeg = (azimuthDeg + 360f) % 360f

        // Correct for True North if magnetic declination is known
        geomagneticField?.let {
            azimuthDeg = (azimuthDeg + it.declination + 360f) % 360f
        }

        // Circular smooth lerp to eliminate sensor jitter
        smoothedAzimuth = smoothAngle(smoothedAzimuth, azimuthDeg, 0.15f)

        _compassFlow.value = CompassReading(
            azimuthDegrees = smoothedAzimuth,
            accuracy = ev.accuracy,
            isSupported = true
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _compassFlow.value = _compassFlow.value.copy(accuracy = accuracy)
    }

    private fun lowPassFilter(input: FloatArray, output: FloatArray, alpha: Float = 0.2f) {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return (current + diff * factor + 360f) % 360f
    }
}
