package com.example.app

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationTests {
    init {
        System.setProperty("config.file", "frpc.ini")
    }

    @Autowired
    lateinit var frpConfigManager: FrpConfigManager


    @Test
    fun testRestartFrpc() {
        frpConfigManager.restartFrpc("/home/wc/Projects/frp-client-manager/frp_0.29.0_linux_amd64/frpc")
    }

    @Test
    fun editServerBean() {
        var newCommonBean = CommonBean().apply {
            this.serverAddr = "4r2412341234"
            this.serverPort = 7000
            this.token = "12345678"
        }

        frpConfigManager.updateCommonBean(newCommonBean)
    }

    @Test
    fun addClientBean() {
        frpConfigManager.addClientBean(ClientBean().apply {
            this.localPort = 22
            this.remotePort = 40000
            this.localIp = "127.0.0.1"
            this.type = "tcp"
            this.name = "ssh"
        })
    }

    @Test
    fun removeClientBean() {
        frpConfigManager.removeClientBean("ssh")
    }

    @Test
    fun updateClientBean() {
        var newClientBean = ClientBean().apply {
            this.name = "ssh"
            this.type = "fasdfasdf"
            this.localPort = 234234
            this.remotePort = 40000
            this.localIp = "127.0.0.1"
        }

        frpConfigManager.updateClientBean(newClientBean)
    }

}
