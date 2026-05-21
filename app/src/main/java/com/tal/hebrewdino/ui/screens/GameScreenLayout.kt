package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding

internal fun gameScreenContentTopInset(
    isCompactLandscapePhone: Boolean,
    stationUiSpec: StationUiSpec,
): Dp {
    val base = (stationUiSpec.contentTopInsetDp?.dp ?: 40.dp)
    return if (isCompactLandscapePhone) {
        base.coerceAtMost(28.dp)
    } else {
        base
    }
}

@Composable
internal fun Modifier.gameScreenStationTopChrome(
    isCompactLandscapePhone: Boolean,
    chapterId: Int,
): Modifier {
    val sagaYOffset =
        if (isSagaEpisode(chapterId)) {
            -SixStationArcHalfCmNudge + GameBackButtonExtraTopInset
        } else {
            0.dp
        }
    return this
        .topChromeInsetsPadding()
        .padding(
            start = if (isCompactLandscapePhone) 6.dp else 8.dp,
            end = if (isCompactLandscapePhone) 6.dp else 8.dp,
            top = (if (isCompactLandscapePhone) 8.dp else 10.dp) + GameBackButtonExtraTopInset,
            bottom = 0.dp,
        )
        .offset(y = sagaYOffset)
}
