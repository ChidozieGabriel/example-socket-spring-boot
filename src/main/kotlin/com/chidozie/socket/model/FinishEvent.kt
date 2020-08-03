package com.chidozie.socket.model

data class FinishEvent(
    val id: String = "",
    val gameId: String = "",
    val correctAnswers: Int = -1
)
