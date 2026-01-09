package com.example.bloodbank.ui.map

import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.bloodbank.BloodBank
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarOverlay(
    userLocation: Location?,
    bloodBanks: List<BloodBank>
) {
    if (userLocation == null) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        listOf(1f, 3f, 5f).forEach { km ->
            drawCircle(
                color = Color.Green.copy(alpha = 0.2f),
                radius = km * 80f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        drawCircle(Color.Blue, 8f, center)

        bloodBanks.take(5).forEachIndexed { index, bank ->
            val angle = index * 0.8f
            val radius = (index + 1) * 60f

            val offset = Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            )

            drawCircle(Color.Red, 6f, offset)
        }

    }
}
