package com.tal.hebrewdino.ui.domain.economy

/**
 * Unified tamagotchi economy snapshot — single source of truth for home UI.
 */
data class PlayerWallet(
    val applesCount: Int,
    val totalEarned: Int,
    val fedTotal: Int,
    val growthStage: GrowthStage,
    val isHungry: Boolean,
    val growthProgress01: Float,
)
