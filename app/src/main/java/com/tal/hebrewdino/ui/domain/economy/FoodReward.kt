package com.tal.hebrewdino.ui.domain.economy

import kotlin.math.abs

data class FoodRewardKind(
    val nameSingularHe: String,
    val namePluralHe: String,
    val emoji: String,
)

/** Varied food rewards (inventory is still counted as generic "food"). */
object FoodRewards {
    val kinds: List<FoodRewardKind> =
        listOf(
            FoodRewardKind("תפוח", "תפוחים", "🍎"),
            FoodRewardKind("בננה", "בננות", "🍌"),
            FoodRewardKind("אפרסק", "אפרסקים", "🍑"),
            FoodRewardKind("אגס", "אגסים", "🍐"),
            FoodRewardKind("אבטיח", "אבטיחים", "🍉"),
            FoodRewardKind("תות", "תותים", "🍓"),
        )

    fun pickForSeed(seed: String): FoodRewardKind {
        val index = abs(seed.hashCode()) % kinds.size
        return kinds[index]
    }
}

private fun foodCountPhrase(
    count: Int,
    food: FoodRewardKind,
): String =
    when (count) {
        1 -> "${food.nameSingularHe} אחד"
        2 -> "שני ${food.namePluralHe}"
        3 ->
            when (food.namePluralHe) {
                "בננות" -> "שלוש בננות"
                "תפוחים" -> "שלושה תפוחים"
                "אפרסקים" -> "שלושה אפרסקים"
                "אגסים" -> "שלושה אגסים"
                "אבטיחים" -> "שלושה אבטיחים"
                "תותים" -> "שלושה תותים"
                else -> "שלושה ${food.namePluralHe}"
            }
        else -> "$count ${food.namePluralHe}"
    }

fun fanfareTextForFood(
    count: Int,
    food: FoodRewardKind,
): String = "הצלחתם! קיבלתם ${foodCountPhrase(count, food)}"

fun fanfareDisplayTextForFood(
    count: Int,
    food: FoodRewardKind,
): String = "${fanfareTextForFood(count, food)}! ${food.emoji}"
