package com.tal.hebrewdino.ui.domain.economy

/**
 * Durable reward presentation payload. Persisted until [com.tal.hebrewdino.ui.economy.RewardEngine.markPresented].
 */
data class PendingRewardEvent(
    val eventId: String,
    val applesCount: Int,
    val fanfareText: String,
    val visualCue: ParticleCue? = null,
    /** Milestone accessory unlocked this round — only when Dino is adult. */
    val accessoryUnlockId: String? = null,
    val foodEmoji: String = "🍎",
    val foodNameSingularHe: String = "תפוח",
    val foodNamePluralHe: String = "תפוחים",
)
