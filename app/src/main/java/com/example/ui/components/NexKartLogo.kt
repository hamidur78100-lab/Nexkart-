package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Distinct brand colors matching the uploaded logo exactly
val BrandTeal = Color(0xFF007A87)   // Deep cyan "NEX" and cart
val BrandOrange = Color(0xFFFA821E) // Vibrant "KART" and highlight arrow

@Composable
fun NexKartLogoIcon(
    modifier: Modifier = Modifier
) {
    // Draws the stylized shopping cart speed-arrow logo dynamically
    Canvas(
        modifier = modifier
            .width(80.dp)
            .height(50.dp)
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Speed lines on the far left (three horizontal streaks)
        // Streak 1 (Top teal)
        drawLine(
            color = BrandTeal,
            start = Offset(w * 0.05f, h * 0.35f),
            end = Offset(w * 0.38f, h * 0.35f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Streak 2 (Middle orange)
        drawLine(
            color = BrandOrange,
            start = Offset(w * 0.12f, h * 0.47f),
            end = Offset(w * 0.40f, h * 0.47f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Streak 3 (Bottom teal)
        drawLine(
            color = BrandTeal,
            start = Offset(w * 0.20f, h * 0.58f),
            end = Offset(w * 0.42f, h * 0.58f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. Cart Handle (Teal line on the left)
        val handlePath = Path().apply {
            moveTo(w * 0.40f, h * 0.25f)
            lineTo(w * 0.48f, h * 0.25f)
            lineTo(w * 0.56f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.65f)
        }
        drawPath(
            path = handlePath,
            color = BrandTeal,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Wheels at the bottom (Teal wheels with dark center)
        drawCircle(
            color = BrandTeal,
            radius = 4.5.dp.toPx(),
            center = Offset(w * 0.58f, h * 0.78f)
        )
        drawCircle(
            color = Color(0xFF0F0F0F), // Background color
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.58f, h * 0.78f)
        )

        drawCircle(
            color = BrandTeal,
            radius = 4.5.dp.toPx(),
            center = Offset(w * 0.76f, h * 0.78f)
        )
        drawCircle(
            color = Color(0xFF0F0F0F),
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.76f, h * 0.78f)
        )

        // 4. Cart Basket / Teal Solid Shape
        val basketPath = Path().apply {
            moveTo(w * 0.52f, h * 0.35f) // Upper left
            lineTo(w * 0.78f, h * 0.35f) // Upper right
            lineTo(w * 0.72f, h * 0.60f) // Lower right
            lineTo(w * 0.58f, h * 0.60f) // Lower left
            close()
        }
        drawPath(
            path = basketPath,
            color = BrandTeal
        )

        // 5. Arrow Tip (Orange Solid Shape) on the right side
        val arrowPath = Path().apply {
            moveTo(w * 0.76f, h * 0.35f) // Left corner of arrow
            lineTo(w * 0.95f, h * 0.15f) // Arrow Top-Right Tip (Arrow Head)
            lineTo(w * 0.88f, h * 0.48f) // Right corner of arrow
            lineTo(w * 0.80f, h * 0.38f) // Inward corner
            close()
        }
        drawPath(
            path = arrowPath,
            color = BrandOrange
        )

        // 6. Draw miniature stylized icons inside basket area for incredible fidelity
        // A mini phone (rectangle)
        drawRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(w * 0.68f, h * 0.40f),
            size = Size(w * 0.05f, h * 0.12f)
        )
        // A mini hanger hook (line & curve)
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.60f, h * 0.42f),
            style = Stroke(width = 1.dp.toPx())
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.58f, h * 0.46f),
            end = Offset(w * 0.62f, h * 0.46f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun NexKartTextLogo(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "NEXKART" Text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEX",
                color = BrandTeal,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "KART",
                color = BrandOrange,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Tagline: "FASHION • ELECTRONICS • SHOES • BEAUTY"
        Text(
            text = "FASHION • ELECTRONICS • SHOES • BEAUTY",
            color = Color(0xFF938F99), // SlateGrey
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun NexKartFullLogo(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.Transparent)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        NexKartLogoIcon()
        Spacer(modifier = Modifier.width(4.dp))
        NexKartTextLogo()
    }
}
