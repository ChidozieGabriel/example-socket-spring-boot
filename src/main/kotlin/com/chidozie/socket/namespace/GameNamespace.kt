package com.chidozie.socket.namespace

import com.chidozie.socket.model.AbortEvent
import com.chidozie.socket.model.FinishEvent
import com.chidozie.socket.model.UpdateAnswers
import com.chidozie.socket.util.Database
import com.chidozie.socket.util.toObjectNonNull
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.DataListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class GameNamespace @Autowired constructor(server: SocketIOServer) {

    private val namespace = server.addNamespace("/game")

    @Autowired
    private lateinit var playersNamespace: PlayersNamespace

    init {
        namespace.addEventListener(ClientEvent.UPDATE_MY_ANSWERS, String::class.java, onUpdateMyAnswers())
        namespace.addEventListener(ClientEvent.FINISH, String::class.java, onFinish())
        namespace.addEventListener(ClientEvent.ABORT_GAME, String::class.java, onAbortGame())
    }

    private fun onUpdateMyAnswers(): DataListener<String> {
        return DataListener { _, string, _ ->
            val data: UpdateAnswers = string.toObjectNonNull()
            val opponent = Database.getGame(data.gameId)?.getOpponent(data.id)
            namespace.getClient(UUID.fromString(opponent))?.sendEvent(ServerEvent.UPDATE_OPPONENT_ANSWERS, data)
        }
    }

    private fun onFinish(): DataListener<String> {
        return DataListener { _, string, _ ->
            val data: FinishEvent = string.toObjectNonNull()
            val game = Database.getGame(data.gameId) ?: return@DataListener
            game.onFinish(data.id, data.correctAnswers)

            if (game.isGameFinished()) {
                val winner = game.getWinner()

                game.players.forEach { id ->
                    val won = id == winner
                    namespace.getClient(UUID.fromString(id))?.sendEvent(ServerEvent.SHOW_RESULT, won)
                }

                Database.removeGame(game.id)

                playersNamespace.broadcastAvailablePlayers()
            }
        }
    }

    private fun onAbortGame(): DataListener<String> {
        return DataListener { _, string, _ ->
            val data: AbortEvent = string.toObjectNonNull()

            val game = Database.getGame(data.gameId) ?: return@DataListener

            val opponent = game.getOpponent(data.id) ?: return@DataListener

            Database.removeGame(game.id)

            namespace.getClient(UUID.fromString(opponent))?.sendEvent(ServerEvent.ABORT_GAME, data)

            playersNamespace.broadcastAvailablePlayers()
        }
    }

    private object ClientEvent {
        const val UPDATE_MY_ANSWERS = "UPDATE_MY_ANSWERS"
        const val FINISH = "FINISH"
        const val ABORT_GAME = "ABORT_GAME"
    }

    private object ServerEvent {
        const val ABORT_GAME = "ABORT_GAME"
        const val UPDATE_OPPONENT_ANSWERS = "UPDATE_OPPONENT_ANSWERS"
        const val SHOW_RESULT = "SHOW_RESULT"
    }

}
