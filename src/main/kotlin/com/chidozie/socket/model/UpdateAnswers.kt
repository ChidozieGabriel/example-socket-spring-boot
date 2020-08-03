package com.chidozie.socket.model

data class UpdateAnswers(
    val id: String = "",
    val gameId: String = "",
    val answers: List<String?> = emptyList()
)
