package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.tal.hebrewdino.ui.components.learning.CaveHomeMark
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
    chapter4ComingSoon: Boolean = false,
    onOpenSettings: () -> Unit,
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
                title = "פרק 2 - חוזרים הביתה",
                subtitle = "הדרך חזרה לקן — אותיות בדרך",
            ),
            ChapterCard(
                id = 3,
                title = "פרק 3 - מצא את החבר",
                subtitle = "מי קרא? אותיות כרמזים",
            ),
            ChapterCard(id = 4, title = "פרק 4", subtitle = ""),
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
                    .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 20.dp),
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
                chapter4ComingSoon = chapter4ComingSoon,
                onOpenChapter = onOpenChapter,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Above the scroll column so touches reach the button (column uses fillMaxSize).
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 4.dp, end = 8.dp)
                    .zIndex(1f),
        ) {
            OutlinedButton(
                onClick = onOpenSettings,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.90f),
                        contentColor = Color(0xFF0B2B3D),
                    ),
            ) {
                Text("הגדרות", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun ChapterVerticalPath(
    chapters: List<ChapterCard>,
    unlockedChapter: Int,
    chapter4ComingSoon: Boolean,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tall canvas to show an “old medieval road” between villages; user scrolls the screen.
    BoxWithConstraints(modifier = modifier.height(1160.dp)) {
        val w = maxWidth
        val h = maxHeight

        // Draw a vertical “medieval road” (top -> bottom), snake-like and irregular.
        Box(modifier = Modifier.fillMaxSize()) {
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
            // Soften the road segment leading toward “פרק 4 — בקרוב” so it does not read like a hot goal.
            if (chapter4ComingSoon) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val yMid = size.height * 0.50f
                    val half = size.height * 0.12f
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFFECEFF1).copy(alpha = 0.30f),
                                        Color(0xFFB0BEC5).copy(alpha = 0.14f),
                                        Color.Transparent,
                                    ),
                                startY = yMid - half,
                                endY = yMid + half,
                            ),
                        topLeft = Offset(0f, yMid - half),
                        size = Size(size.width, half * 2f),
                    )
                }
            }
        }

        chapters.take(6).forEachIndexed { idx, ch ->
            val comingSoon = chapter4ComingSoon && ch.id == 4
            val locked = ch.id > unlockedChapter && !comingSoon
            val t = (idx + 1) / 7f
            val pt = pointOnChaptersRoad(t)

            ChapterNode(
                chapter = ch,
                locked = locked,
                comingSoon = comingSoon,
                isCurrent = ch.id == unlockedChapter,
                isPast = !locked && ch.id < unlockedChapter && ch.title.isNotBlank(),
                onClick = { if (!locked && !comingSoon) onOpenChapter(ch.id) },
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = w * pt.x - 90.dp, y = h * pt.y - 28.dp),
            )
        }

        val dinoT = ((unlockedChapter.coerceIn(1, 6) / 7f) - 0.08f).coerceIn(0.04f, 0.92f)
        val dinoPt = pointOnChaptersRoad(dinoT)
        Image(
            painter = painterResource(id = R.drawable.dino_idle),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = w * dinoPt.x - 160.dp, y = h * dinoPt.y - 32.dp)
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
            CaveHomeMark(modifier = Modifier.size(width = 120.dp, height = 88.dp))
            Text(
                text = "הקן — הבית",
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
    comingSoon: Boolean,
    isCurrent: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse by rememberInfiniteTransition(label = "chNode").animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent && !comingSoon) 1.06f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(950), repeatMode = RepeatMode.Reverse),
        label = "chNodePulse",
    )
    val chipScale = if (comingSoon) 1f else pulse
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .scale(chipScale)
                    .size(
                        when {
                            comingSoon -> 70.dp
                            isCurrent -> 86.dp
                            else -> 76.dp
                        },
                    )
                    .background(
                        when {
                            comingSoon -> Color(0xFF90A4AE).copy(alpha = 0.44f)
                            locked -> Color(0xFF5D6A73).copy(alpha = 0.42f)
                            isCurrent -> Color(0xFFFFC400).copy(alpha = 0.98f)
                            isPast -> Color(0xFF2E7D32).copy(alpha = 0.55f)
                            else -> Color(0xFF2AA6C9).copy(alpha = 0.78f)
                        },
                        shape = RoundedCornerShape(28.dp),
                    )
                    .clickable(enabled = !locked && !comingSoon, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    when {
                        comingSoon -> "בקרוב"
                        locked -> "🔒"
                        else -> chapter.id.toString()
                    },
                style =
                    if (comingSoon) {
                        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                    } else {
                        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    },
                color = if (comingSoon) Color.White.copy(alpha = 0.82f) else Color.White,
            )
        }

        if (chapter.title.isNotBlank()) {
            Box(
                modifier =
                    Modifier
                        .width(240.dp)
                        .background(
                            if (comingSoon) Color.White.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.88f),
                            RoundedCornerShape(18.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = chapter.title,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (comingSoon) FontWeight.SemiBold else FontWeight.Black,
                            ),
                        color = Color(0xFF0B2B3D).copy(alpha = if (comingSoon) 0.72f else 1f),
                        textAlign = TextAlign.End,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text =
                            when {
                                comingSoon -> "עדיין לא זמין — אפשר לשחק בפרקים שמוכנים"
                                locked -> "נעול עד שמסיימים את הפרק הקודם"
                                else -> chapter.subtitle
                            },
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (comingSoon) FontWeight.Medium else FontWeight.Bold,
                            ),
                        color = Color(0xFF0B2B3D).copy(alpha = if (comingSoon) 0.60f else 0.85f),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private data class ChapPt(val x: Float, val y: Float)

private fun chapCubic(t: Float, p0: ChapPt, p1: ChapPt, p2: ChapPt, p3: ChapPt): ChapPt {
    val u = 1f - t
    val uu = u * u
    val tt = t * t
    val x = u * uu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + t * tt * p3.x
    val y = u * uu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + t * tt * p3.y
    return ChapPt(x, y)
}

/** Same centerline as the road drawn in [ChapterVerticalPath] (normalized 0–1 of width/height). */
private fun pointOnChaptersRoad(t: Float): ChapPt {
    val p0 = ChapPt(0.66f, 0.06f)
    val p1 = ChapPt(0.44f, 0.18f)
    val p2 = ChapPt(0.70f, 0.34f)
    val p3 = ChapPt(0.38f, 0.46f)
    val p4 = ChapPt(0.72f, 0.62f)
    val p5 = ChapPt(0.40f, 0.78f)
    val p6 = ChapPt(0.64f, 0.94f)
    val tt = t.coerceIn(0f, 1f)
    return if (tt <= 0.5f) {
        chapCubic(tt * 2f, p0, p1, p2, p3)
    } else {
        chapCubic((tt - 0.5f) * 2f, p3, p4, p5, p6)
    }
}

