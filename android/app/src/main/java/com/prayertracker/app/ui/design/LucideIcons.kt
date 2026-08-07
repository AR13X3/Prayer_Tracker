package com.prayertracker.app.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lucide icons (https://lucide.dev) transcribed to Compose ImageVectors — no dependency.
 * Lucide is a stroke-based 24×24 set (stroke-width 2, round caps/joins, no fill). `<circle>`
 * elements are converted to equivalent arc path data. Icons are drawn black and recolored by
 * the `Icon(tint = …)` composable, the same way Material icons work.
 */
object LucideIcons {

    val Sun: ImageVector = build(
        "Sun",
        "M8 12 a4 4 0 1 0 8 0 a4 4 0 1 0 -8 0",        // circle cx12 cy12 r4
        "M12 2v2", "M12 20v2",
        "m4.93 4.93 1.41 1.41", "m17.66 17.66 1.41 1.41",
        "M2 12h2", "M20 12h2",
        "m6.34 17.66-1.41 1.41", "m19.07 4.93-1.41 1.41",
    )

    val Flame: ImageVector = build(
        "Flame",
        "M12 3q1 4 4 6.5t3 5.5a1 1 0 0 1-14 0 5 5 0 0 1 1-3 1 1 0 0 0 5 0c0-2-1.5-3-1.5-5q0-2 2.5-4",
    )

    val Users: ImageVector = build(
        "Users",
        "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
        "M16 3.128a4 4 0 0 1 0 7.744",
        "M22 21v-2a4 4 0 0 0-3-3.87",
        "M5 7 a4 4 0 1 0 8 0 a4 4 0 1 0 -8 0",         // circle cx9 cy7 r4
    )

    val Settings: ImageVector = build(
        "Settings",
        "M14 17H5", "M19 7h-9",
        "M14 17 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0",       // circle cx17 cy17 r3
        "M4 7 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0",         // circle cx7 cy7 r3
    )

    val ArrowLeft: ImageVector = build(
        "ArrowLeft",
        "m12 19-7-7 7-7", "M19 12H5",
    )

    val Compass: ImageVector = build(
        "Compass",
        "M2 12 a10 10 0 1 0 20 0 a10 10 0 1 0 -20 0",  // circle cx12 cy12 r10
        "m16.24 7.76-1.804 5.411a2 2 0 0 1-1.265 1.265L7.76 16.24l1.804-5.411a2 2 0 0 1 1.265-1.265z",
    )

    private fun build(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(
            name = "lucide_$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        for (d in paths) {
            b.addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }
}
