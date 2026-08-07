package com.prayertracker.app.qibla

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayertracker.app.ui.design.OutlinePill
import com.prayertracker.app.ui.design.ScreenHeader
import com.prayertracker.app.ui.design.SectionLabel
import com.prayertracker.app.ui.design.SoftCard
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(modifier: Modifier = Modifier) {
    val vm: QiblaViewModel = viewModel()
    val s by vm.ui.collectAsStateWithLifecycle()
    val magneticAzimuth = rememberMagneticAzimuth()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader("Qibla", subtitle = "Direction of prayer")

        when {
            s.loading -> Box(Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            s.error != null -> SoftCard(Modifier.fillMaxWidth()) {
                Text("Couldn't load: ${s.error}")
                Spacer(Modifier.height(10.dp))
                OutlinePill("Retry", onClick = vm::load)
            }

            !s.hasLocation -> SoftCard(Modifier.fillMaxWidth()) {
                SectionLabel("No location set")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Set your location in Settings to find the Qibla direction.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                val trueAzimuth = magneticAzimuth + s.declination
                val needle = s.bearing - trueAzimuth
                val normalized = (((needle % 360f) + 540f) % 360f) - 180f  // [-180, 180]
                val aligned = abs(normalized) < 6f
                val heading = ((trueAzimuth % 360f) + 360f) % 360f

                CompassDial(needleAngle = needle, aligned = aligned)

                SoftCard(Modifier.fillMaxWidth()) {
                    SectionLabel("Details")
                    Spacer(Modifier.height(10.dp))
                    InfoRow("Qibla bearing", "${s.bearing.roundToInt()}° ${QiblaMath.cardinal(s.bearing.toDouble())}")
                    InfoRow("Your heading", "${heading.roundToInt()}°")
                    InfoRow("Location", s.locationLabel)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        when {
                            aligned -> "You're facing the Qibla."
                            normalized > 0 -> "Turn right toward the arrow."
                            else -> "Turn left toward the arrow."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (aligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    "Hold the phone flat. If the needle drifts, move it in a figure-8 to recalibrate the compass.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CompassDial(needleAngle: Float, aligned: Boolean) {
    val cs = MaterialTheme.colorScheme
    val needleColor = if (aligned) Color(0xFF2E9E6B) else cs.primary
    val track = cs.surfaceVariant
    val marker = cs.onSurface
    val hub = cs.secondary

    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(280.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.minDimension / 2f - 6.dp.toPx()

            // Outer ring.
            drawCircle(track, radius = r, center = Offset(cx, cy), style = Stroke(width = 3.dp.toPx()))

            // Fixed reference marker at the top (where the phone points): small triangle.
            val m = 9.dp.toPx()
            val marker2 = Path().apply {
                moveTo(cx, cy - r + m * 2)
                lineTo(cx - m, cy - r)
                lineTo(cx + m, cy - r)
                close()
            }
            drawPath(marker2, marker)

            // Qibla needle, rotated relative to the device heading.
            rotate(needleAngle) {
                val tip = Offset(cx, cy - r * 0.80f)
                val baseL = Offset(cx - 20.dp.toPx(), cy - r * 0.12f)
                val baseR = Offset(cx + 20.dp.toPx(), cy - r * 0.12f)
                val needlePath = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(baseL.x, baseL.y)
                    lineTo(baseR.x, baseR.y)
                    close()
                }
                drawPath(needlePath, needleColor)
                drawLine(track, Offset(cx, cy), Offset(cx, cy + r * 0.55f), strokeWidth = 4.dp.toPx())
            }

            drawCircle(hub, radius = 7.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

/** Live device heading (degrees clockwise from magnetic north) from the rotation-vector sensor. */
@Composable
private fun rememberMagneticAzimuth(): Float {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) {
        val sm = context.getSystemService(SensorManager::class.java)
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            private val rotation = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                var deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (deg < 0f) deg += 360f
                azimuth = deg
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm?.unregisterListener(listener) }
    }
    return azimuth
}
