package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.game.FinaleGame

@Composable
internal fun FinaleSlotQuestionRenderer(
    current: Question.FinaleSlotQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakeEpoch: Int,
    onWrongPlacement: () -> Unit,
    onSolved: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    FinaleGame(
        question = current,
        contentKey = contentKey,
        enabled = enabled,
        shakeEpoch = shakeEpoch,
        onWrongPlacement = onWrongPlacement,
        onSolved = onSolved,
        modifier = modifier,
    )
}
