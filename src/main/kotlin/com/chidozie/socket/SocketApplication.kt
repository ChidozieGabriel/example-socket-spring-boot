package com.chidozie.socket

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.Transport
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SocketApplication {

    @Bean
    fun socketIOServer(): SocketIOServer {
        val config = Configuration()
        config.hostname = HOST
        config.port = PORT
        config.setTransports(Transport.WEBSOCKET)
        return SocketIOServer(config)
    }

    companion object {
        private const val HOST = "192.168.1.100"
        private const val PORT = 9092
    }

}

fun main(args: Array<String>) {
    runApplication<SocketApplication>(*args)
}
