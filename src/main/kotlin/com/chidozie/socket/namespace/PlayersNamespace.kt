package com.chidozie.socket.namespace

import com.chidozie.socket.model.StartGame
import com.chidozie.socket.util.Database
import com.chidozie.socket.util.toJson
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.ConnectListener
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class PlayersNamespace @Autowired constructor(server: SocketIOServer) {

    private val namespace = server.addNamespace("/players")

    init {
        namespace.addConnectListener(onConnected())
        namespace.addDisconnectListener(onDisconnected())
        namespace.addEventListener(ClientEvent.CREATE_PLAYER, String::class.java, onCreatePlayer())
        namespace.addEventListener(ClientEvent.CHALLENGE_PLAYER, String::class.java, onChallengePlayer())
        namespace.addEventListener(ClientEvent.ACCEPT_REQUEST, String::class.java, onAcceptRequest())
    }

    private fun onCreatePlayer(): DataListener<String> {
        return DataListener { client, data, _ ->
            val player = Database.createPlayer(data, client)
            log.info("onCreatePlayer $player")
            client.sendEvent(ServerEvent.PLAYER_CREATED, player)
            broadcastAvailablePlayers()
        }
    }

    private fun onChallengePlayer(): DataListener<String> {
        return DataListener { client, data, _ ->
            val player = Database.getPlayer(client)

            if (player != null) {
                val otherPlayerId = UUID.fromString(data)
                namespace.getClient(otherPlayerId).sendEvent(ServerEvent.PLAY_REQUEST, player)
            }
        }
    }

    private fun onAcceptRequest(): DataListener<String> {
        return DataListener { client, data, _ ->
            val game = Database.createGame(client, data)
            val player = Database.getPlayer(client)
            val otherPlayer = Database.getPlayer(data)

            if (player != null && otherPlayer != null) {
                client.sendEvent(ServerEvent.START_GAME, StartGame(game.id, otherPlayer))

                namespace.getClient(UUID.fromString(otherPlayer.id))
                    .sendEvent(ServerEvent.START_GAME, StartGame(game.id, player))

                broadcastAvailablePlayers()
            }

        }
    }

    private fun onConnected(): ConnectListener {
        return ConnectListener {
            log.info("onConnected")
//            broadcastAvailablePlayers()
        }
    }

    private fun onDisconnected(): DisconnectListener {
        return DisconnectListener { client ->
            log.info("onDisconnected")
            Database.removePlayer(client)
            broadcastAvailablePlayers()
        }
    }

    fun broadcastAvailablePlayers() {
        val players = Database.getAvailablePlayers()
        log.info("Available: $players")
        namespace.broadcastOperations.sendEvent(ServerEvent.AVAILABLE_PLAYERS, players.toJson())
    }

    private object ClientEvent {
        const val CREATE_PLAYER = "CREATE_PLAYER"
        const val CHALLENGE_PLAYER = "CHALLENGE_PLAYER"
        const val ACCEPT_REQUEST = "ACCEPT_REQUEST"
    }

    private object ServerEvent {
        const val PLAYER_CREATED = "PLAYER_CREATED"
        const val AVAILABLE_PLAYERS = "AVAILABLE_PLAYERS"
        const val PLAY_REQUEST = "PLAY_REQUEST"
        const val START_GAME = "START_GAME"
    }


    companion object {
        private val log = LoggerFactory.getLogger(PlayersNamespace::class.java)
    }

}
