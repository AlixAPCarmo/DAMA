package pt.ipt.DAMA.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

class lightSensor(context: Context) : SensorEventListener{

    private var lightIntensity: Float = -1.0f
    private var lightSensor: Sensor? = null
    private var sensorManager: SensorManager = getSystemService(context, SensorManager::class.java) as SensorManager

    init {
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            Toast.makeText(context, "Sensor de luminosidade não disponível", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lightIntensity = event.values[0]
            this.lightIntensity = lightIntensity
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    fun getLightIntensity(): Float {
        return lightIntensity
    }


}