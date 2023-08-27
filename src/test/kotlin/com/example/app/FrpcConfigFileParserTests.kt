package com.example.app

import com.example.app.frpcParser.ClientBean
import com.example.app.frpcParser.CommonBean
import com.example.app.frpcParser.FrpConfigManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FrpcConfigFileParserTests {
    init {
        // -Dfrpc.exec=/home/wc/apps/frp_0.29.0_linux_amd64/frpc
        // -Dconfig.dir=/home/wc/apps/frp_0.29.0_linux_amd64/profiles
        System.setProperty("frpc.exec", "/home/wc/apps/frp_0.29.0_linux_amd64/frpc")
        System.setProperty("config.dir", "./fordebug")
    }

    @Autowired
    lateinit var frpConfigManager: FrpConfigManager


    @Test
    fun listClientBeans() {
        val listClientBeans = frpConfigManager.listClientBeans("frpc.ini")
        listClientBeans.forEach{
            println(it)
        }
    }

    @Test
    fun addClientBean() {
        frpConfigManager.addClientBean(
            "frpc.ini", ClientBean("ssh", "tcp", "127.0.0.1", 22, 40000)
        )
    }

    @Test
    fun updateClientBean() {
        frpConfigManager.updateClientBean(
            "frpc.ini",
            ClientBean("ssh", "tcp", "127.0.0.1", 3232, 400312)
        )
    }


    @Test
    fun removeClientBean() {
        frpConfigManager.removeClientBean(
            profileName = "frpc.ini",
            clientName = "ssh"
        )
    }

    @Test
    fun getCommonBean() {
        val commonBean = frpConfigManager.getCommonBean(
            profileName = "frpc.ini"
        )
        println(commonBean)
    }

    @Test
    fun updateCommonBean() {
        frpConfigManager.updateCommonBean(
            profileName = "frpc.ini",
            newCommonBean =
                CommonBean(
                    server_port = 7000,
                    server_addr = "49.232.155.160",
                    token = "cRvePrp8MLcQMg4Rd9BU"
                )
        )
    }
    // @Test
    // fun editServerBean() {
    //     var newCommonBean = CommonBean().apply {
    //         this.serverAddr = "4r2412341234"
    //         this.serverPort = 7000
    //         this.token = "12345678"
    //     }
    //
    //     frpConfigManager.updateCommonBean(newCommonBean)
    // }
    //
    // @Test
    // fun addClientBean() {
    //     frpConfigManager.addClientBean(ClientBean().apply {
    //         this.localPort = 22
    //         this.remotePort = 40000
    //         this.localIp = "127.0.0.1"
    //         this.type = "tcp"
    //         this.name = "ssh"
    //     })
    // }
    //
    // @Test
    // fun removeClientBean() {
    //     frpConfigManager.removeClientBean("ssh")
    // }
    //
    // @Test
    // fun updateClientBean() {
    //     var newClientBean = ClientBean().apply {
    //         this.name = "ssh"
    //         this.type = "fasdfasdf"
    //         this.localPort = 234234
    //         this.remotePort = 40000
    //         this.localIp = "127.0.0.1"
    //     }
    //
    //     frpConfigManager.updateClientBean(newClientBean)
    // }

}
