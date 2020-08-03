package com.chidozie.socket.util

import com.chidozie.socket.model.Game
import com.chidozie.socket.model.Player
import com.corundumstudio.socketio.SocketIOClient
import java.util.*

object Database {

    private val players: HashMap<String, Player> = hashMapOf()
    private val games: HashMap<String, Game> = hashMapOf()

    fun createPlayer(name: String, client: SocketIOClient): Player {
        val id = client.sessionId.toString()
        val player = Player(id, name)
        players[id] = player
        return player
    }

    fun removePlayer(client: SocketIOClient) {
        players.remove(client.sessionId.toString())
    }

    fun getPlayer(client: SocketIOClient): Player? {
        return players[client.sessionId.toString()]
    }

    fun getPlayer(id: String): Player? {
        return players[id]
    }

    fun getAvailablePlayers(): List<Player> {
        return players.values.toList().filterNot { player ->
            games.values.any { game ->
                game.players.any { id ->
                    id == player.id
                }
            }
        }
    }

    fun createGame(client: SocketIOClient, otherPlayerId: String): Game {
        val game = Game(listOf(client.sessionId.toString(), otherPlayerId))
        games[game.id] = game
        return game
    }

    fun getGame(id: String): Game? {
        return games[id]
    }

    fun removeGame(id: String): Game? {
        return games.remove(id)
    }

}
