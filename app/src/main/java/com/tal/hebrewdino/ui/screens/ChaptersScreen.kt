package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R

data class ChapterCard(
    val id: Int,
    val title: String,
    val subtitle: String,
)

@Composable
fun ChaptersScreen(
    unlockedChapter: Int,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val chapters =
        listOf(
            ChapterCard(
                id = 1,
                title = "פרק 1 - מצא את הביצה",
                subtitle = "היער: תחנות בדרך + אותיות",
            ),
            ChapterCard(
                id = 2,
                title = "פרק 2 - חוזרים למערה",
                subtitle = "שבילים לפי אות — הבית במערה",
            ),
            ChapterCard(
                id = 3,
                title = "פרק 3 - מצא את החבר",
                subtitle = "מי קרא? אותיות כרמזים",
            ),
            // Placeholders (no titles yet) to reserve space in the journey.
            ChapterCard(id = 4, title = "", subtitle = ""),
            ChapterCard(id = 5, title = "", subtitle = ""),
            ChapterCard(id = 6, title = "", subtitle = ""),
        )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "כל הפרקים",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(14.dp))

            ChapterVerticalPath(
                chapters = chapters,
                unlockedChapter = unlockedChapter,
                onOpenChapter = onOpenChapter,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ChapterVerticalPath(
    chapters: List<ChapterCard>,
    unlockedChapter: Int,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tall canvas to show an “old medieval road” between villages; user scrolls the screen.
    BoxWithConstraints(modifier = modifier.height(1160.dp)) {
        val w = maxWidth
        val h = maxHeight

        // Draw a vertical “medieval road” (top -> bottom), snake-like and irregular.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            val start = Offset(size.width * 0.66f, size.height * 0.06f)
            path.moveTo(start.x, start.y)
            val p1 = Offset(size.width * 0.44f, size.height * 0.18f)
            val p2 = Offset(size.width * 0.70f, size.height * 0.34f)
            val p3 = Offset(size.width * 0.38f, size.height * 0.46f)
            val p4 = Offset(size.width * 0.72f, size.height * 0.62f)
            val p5 = Offset(size.width * 0.40f, size.height * 0.78f)
            val end = Offset(size.width * 0.64f, size.height * 0.94f)
            path.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
            path.cubicTo(p4.x, p4.y, p5.x, p5.y, end.x, end.y)

            drawPath(
                path = path,
                color = Color(0xFFE2C999).copy(alpha = 0.92f),
                style = Stroke(width = 56f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = path,
                color = Color(0xFF6B4A2A).copy(alpha = 0.26f),
                style = Stroke(width = 62f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.18f),
                style = Stroke(width = 26f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            // Pebble texture along the road to feel more “real”.
            val dots = 95
            for (i in 0 until dots) {
                val t = i.toFloat() / (dots - 1).toFloat()
                // sample along the vertical axis and place stones around the road centerline
                val x = lerp(
                    lerp(start.x, p3.x, t),
                    lerp(p3.x, end.x, t),
                    t,
                ) + (if (i % 2 == 0) 16f else -12f)
                val y = start.y + (end.y - start.y) * t + (if (i % 3 == 0) 10f else -8f)
                drawCircle(
                    color = Color(0xFF6B4A2A).copy(alpha = 0.12f),
                    radius = if (i % 5 == 0) 6.5f else 4.5f,
                    center = Offset(x, y),
                )
            }
        }

        val slots = listOf(
            0.60f to 0.10f,
            0.38f to 0.26f,
            0.64f to 0.42f,
            0.40f to 0.58f,
            0.66f to 0.74f,
            0.44f to 0.86f,
        )

        chapters.take(6).forEachIndexed { idx, ch ->
            val locked = ch.id > unlockedChapter
            val (xF, yF) = slots.getOrElse(idx) { 0.55f to (0.12f + idx * 0.22f) }

            ChapterNode(
                chapter = ch,
                locked = locked,
                isCurrent = ch.id == unlockedChapter,
                onClick = { if (!locked) onOpenChapter(ch.id) },
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = w * xF - 90.dp, y = h * yF - 24.dp),
            )
        }

        // Dino stands near the next locked chapter (the “next goal”).
        val nextLocked = (1..6).firstOrNull { it > unlockedChapter } ?: 6
        val (dxF, dyF) = slots.getOrElse(nextLocked - 1) { 0.55f to 0.90f }
        Image(
            painter = painterResource(id = R.drawable.dino_idle),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = w * dxF - 170.dp, y = h * dyF - 36.dp)
                    .size(88.dp),
            contentScale = ContentScale.Fit,
        )

        // Goal at the bottom: home cave for the dinosaurs.
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-10).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(modifier = Modifier.size(width = 120.dp, height = 88.dp)) {
                val cx = size.width * 0.52f
                val cy = size.height * 0.62f
                drawOval(
                    color = Color(0xFF3E2723),
                    topLeft = Offset(cx - size.width * 0.42f, cy - size.height * 0.18f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.84f, size.height * 0.50f),
                    style = Fill,
                )
                drawArc(
                    color = Color(0xFF1B120E),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - size.width * 0.38f, cy - size.height * 0.55f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.72f),
                    style = Fill,
                )
                drawOval(
                    color = Color(0xFF0D0705).copy(alpha = 0.35f),
                    topLeft = Offset(cx - size.width * 0.22f, cy + size.height * 0.02f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.22f),
                    style = Fill,
                )
            }
            Text(
                text = "המערה — הבית",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
        }
    }
}

@Composable
private fun ChapterNode(
    chapter: ChapterCard,
    locked: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(if (isCurrent) 84.dp else 76.dp)
                    .background(
                        when {
                            locked -> Color(0xFF7E8A93).copy(alpha = 0.35f)
                            isCurrent -> Color(0xFFFFC400).copy(alpha = 0.95f)
                            else -> Color(0xFF2AA6C9).copy(alpha = 0.90f)
                        },
                        shape = RoundedCornerShape(28.dp),
                    )
                    .clickable(enabled = !locked, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (locked) "🔒" else chapter.id.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
            )
        }

        if (chapter.title.isNotBlank()) {
            Box(
                modifier =
                    Modifier
                        .width(240.dp)
                        .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.End,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (locked) "נעול עד שמסיימים את הפרק הקודם" else chapter.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0B2B3D).copy(alpha = 0.85f),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

