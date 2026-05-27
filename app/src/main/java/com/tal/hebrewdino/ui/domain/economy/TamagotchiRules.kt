package com.tal.hebrewdino.ui.domain.economy

object TamagotchiRules {
    const val APPLES_TO_HATCH: Int = 3
    const val EGG_TAPS_TO_HATCH: Int = 3
    const val APPLES_TO_ADULT: Int = 11
    const val BABY_STAGE_FED_OFFSET: Int = 3
    const val BABY_STAGE_FED_SPAN: Int = 8

    fun fedTotal(
        totalEarned: Int,
        applesInInventory: Int,
    ): Int = (totalEarned - applesInInventory).coerceAtLeast(0)

    fun growthStageFromFed(
        fedTotal: Int,
        eggHatched: Boolean,
    ): GrowthStage =
        when {
            !eggHatched -> GrowthStage.EGG
            fedTotal < APPLES_TO_ADULT -> GrowthStage.BABY
            else -> GrowthStage.ADULT
        }

    fun resolveGrowthStage(
        stored: GrowthStage,
        computedFromFed: GrowthStage,
    ): GrowthStage =
        if (computedFromFed.ordinal >= stored.ordinal) {
            computedFromFed
        } else {
            stored
        }

    fun growthProgress01(
        fedTotal: Int,
        stage: GrowthStage,
        eggTapCount: Int = 0,
    ): Float =
        when (stage) {
            GrowthStage.EGG ->
                (eggTapCount / EGG_TAPS_TO_HATCH.toFloat()).coerceIn(0f, 1f)
            GrowthStage.BABY ->
                ((fedTotal - BABY_STAGE_FED_OFFSET) / BABY_STAGE_FED_SPAN.toFloat()).coerceIn(0f, 1f)
            GrowthStage.ADULT -> 1f
        }

    fun isHungry(
        stage: GrowthStage,
        fullUntilAtMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        if (stage == GrowthStage.EGG) return false
        val until = fullUntilAtMs.coerceAtLeast(0L)
        if (until <= 0L) return true
        if (until == Long.MAX_VALUE) return false
        return nowMs > until
    }

    fun buildWallet(
        applesCount: Int,
        totalEarned: Int,
        storedStageName: String,
        fullUntilAtMs: Long,
        eggHatched: Boolean,
        eggTapCount: Int = 0,
        nowMs: Long = System.currentTimeMillis(),
    ): PlayerWallet {
        val apples = applesCount.coerceAtLeast(0)
        val earned = totalEarned.coerceAtLeast(0)
        val stored = parseGrowthStage(storedStageName)
        val fed = fedTotal(earned, apples)
        val computed = growthStageFromFed(fed, eggHatched)
        val stage =
            if (!eggHatched) {
                GrowthStage.EGG
            } else {
                resolveGrowthStage(stored, computed)
            }
        return PlayerWallet(
            applesCount = apples,
            totalEarned = earned,
            fedTotal = fed,
            growthStage = stage,
            isHungry = isHungry(stage, fullUntilAtMs, nowMs),
            growthProgress01 = growthProgress01(fed, stage, eggTapCount),
        )
    }

    fun parseGrowthStage(name: String): GrowthStage =
        try {
            GrowthStage.valueOf(name)
        } catch (_: Throwable) {
            GrowthStage.EGG
        }
}
