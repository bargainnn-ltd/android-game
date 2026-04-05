package com.application.eatbts.viewmodel

/**
 * Two-dice couples edition: die 1 = body, die 2 = action (faces 1–8 each, d8).
 */
object CouplesDiceRules {
    const val SIDES = 8
    const val TURNS_PER_PLAYER = 5

    fun bodyPartIndex(roll: Int): Int = (roll - 1).coerceIn(0, SIDES - 1)
    fun actionIndex(roll: Int): Int = (roll - 1).coerceIn(0, SIDES - 1)

    fun isDoubleRoll(bodyRoll: Int, actionRoll: Int): Boolean = bodyRoll == actionRoll

    fun maxTurns(playerCount: Int): Int = TURNS_PER_PLAYER * playerCount.coerceAtLeast(1)
}
