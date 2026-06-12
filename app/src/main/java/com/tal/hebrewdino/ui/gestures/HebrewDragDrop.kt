package com.tal.hebrewdino.ui.gestures

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

/**
 * Tracks picture drop zones in root coordinates for drag hit-testing.
 * Works with RTL layouts — bounds are in screen space.
 */
@Stable
class DropTargetRegistry {
    private val targets = mutableMapOf<String, Rect>()

    fun update(id: String, rect: Rect) {
        targets[id] = rect
    }

    fun remove(id: String) {
        targets.remove(id)
    }

    fun clear() {
        targets.clear()
    }

    fun findTarget(at: Offset, paddingPx: Float = 12f): String? {
        val padded =
            targets.entries.firstOrNull { (_, rect) ->
                rect.inflate(paddingPx).contains(at)
            }
        return padded?.key
    }
}

@Composable
fun rememberDropTargetRegistry(): DropTargetRegistry = remember { DropTargetRegistry() }

fun Modifier.registerDropTarget(
    id: String,
    registry: DropTargetRegistry,
    enabled: Boolean = true,
): Modifier =
    this.then(
        Modifier.onGloballyPositioned { coordinates ->
            if (!enabled) {
                registry.remove(id)
                return@onGloballyPositioned
            }
            val topLeft = coordinates.positionInRoot()
            val size = coordinates.size
            registry.update(
                id = id,
                rect =
                    Rect(
                        left = topLeft.x,
                        top = topLeft.y,
                        right = topLeft.x + size.width,
                        bottom = topLeft.y + size.height,
                    ),
            )
        },
    )

@Composable
fun DropTargetDisposable(
    id: String,
    registry: DropTargetRegistry,
) {
    DisposableEffect(id, registry) {
        onDispose { registry.remove(id) }
    }
}

/**
 * Primary drag interaction for Hebrew word cards.
 * Reports cumulative drag offset from the pointer-down position in the parent box.
 */
fun Modifier.hebrewDraggable(
    enabled: Boolean,
    onDragStart: (localStart: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
): Modifier =
    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        var started = false
        detectDragGestures(
            onDragStart = { localStart ->
                started = true
                onDragStart(localStart)
            },
            onDragEnd = {
                if (started) {
                    onDragEnd()
                }
                started = false
            },
            onDragCancel = {
                if (started) {
                    onDragCancel()
                }
                started = false
            },
        ) { change, dragAmount ->
            change.consume()
            onDrag(dragAmount)
        }
    }
