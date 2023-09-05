package com.example.app.frpcParser

import com.example.app.frpcProcessManager.FrpcProfileManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

interface FrpcConfigParser {
    fun parse(file: File): FrpcConfig
    fun writeToFile(profileName: String, config: FrpcConfig, file: File)
}

@Component
class FrpcConfigParserImpl : FrpcConfigParser {
    @Autowired
    lateinit var frpcProcessManager: FrpcProfileManager
    override fun parse(file: File): FrpcConfig {
        val lines = file.readLines()
        var common: CommonBean? = null
        val clients = mutableListOf<ClientBean>()

        var currentSection: String? = null

        for (line in lines) {
            when {
                line.startsWith("[") -> {
                    currentSection = line.trim('[', ']')
                }

                currentSection == "common" -> {
                    if(line.trim() == "") continue
                    val (key, value) = line.split("=", limit = 2).map { it.trim() }
                    common = when (key) {
                        "server_addr" -> common?.copy(server_addr = value) ?: CommonBean(value, 0, "")
                        "server_port" -> common?.copy(server_port = value.toInt()) ?: CommonBean("", value.toInt(), "")
                        "token" -> common?.copy(token = value) ?: CommonBean("", 0, value)
                        else -> common
                    }
                }

                currentSection != null -> {
                    if(line.trim() == "") continue
                    val (key, value) = line.split("=", limit = 2).map { it.trim() }
                    val currentClientIndex = clients.size - 1
                    val currentClient = if (clients.isNotEmpty()) clients[currentClientIndex] else null
                    when (key) {
                        "type" -> clients += ClientBean(currentSection, value, "", 0, 0)
                        "local_ip" -> clients[currentClientIndex] =
                            currentClient?.copy(local_ip = value) ?: ClientBean(currentSection, "", value, 0, 0)

                        "local_port" -> clients[currentClientIndex] =
                            currentClient?.copy(local_port = value.toInt()) ?: ClientBean(
                                currentSection,
                                "",
                                "",
                                value.toInt(),
                                0
                            )

                        "remote_port" -> clients[currentClientIndex] =
                            currentClient?.copy(remote_port = value.toInt()) ?: ClientBean(
                                currentSection,
                                "",
                                "",
                                0,
                                value.toInt()
                            )
                    }
                }
            }
        }

        return FrpcConfig(common ?: throw IllegalArgumentException("Common section not found!"), clients)
    }

    override fun writeToFile(profileName: String, config: FrpcConfig, file: File) {
        file.bufferedWriter().use { writer ->
            writer.write("[common]\n")
            writer.write("server_addr = ${config.common.server_addr}\n")
            writer.write("server_port = ${config.common.server_port}\n")
            writer.write("token = ${config.common.token}\n\n")

            for (client in config.clients) {
                writer.write("[${client.name}]\n")
                writer.write("type = ${client.type}\n")
                writer.write("local_ip = ${client.local_ip}\n")
                writer.write("local_port = ${client.local_port}\n")
                writer.write("remote_port = ${client.remote_port}\n\n")
            }
        }
        frpcProcessManager.reloadProfile(profileName)
    }
}