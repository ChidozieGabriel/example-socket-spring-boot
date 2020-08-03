package com.chidozie.socket.model

import java.util.*

data class Game(
    val players: List<String> = emptyList(),
    val id: String = UUID.randomUUID().toString()
) {

    private val map = hashMapOf<String, Int>()
    private var firstToFinishId: String? = null
    private var firstToFinishScore: Int = -1

    fun onFinish(id: String, correctAnswers: Int) {
        if (firstToFinishId == null) {
            firstToFinishId = id
            firstToFinishScore = correctAnswers
        }

        map[id] = correctAnswers
    }

    fun isGameFinished(): Boolean {
        return map.size >= 2
    }

    fun getOpponent(userId: String): String? {
        return players.filterNot { id ->
            id == userId
        }.firstOrNull()
    }

    fun getWinner(): String {
        var winner = firstToFinishId ?: ""
        var max = firstToFinishScore

        map.forEach {
            if (it.value > max) {
                winner = it.key
                max = it.value
            }
        }

        return winner
    }
}
