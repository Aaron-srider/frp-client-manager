package com.example.app

import com.example.app.frpcProcessManager.FrpcProfileManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FrpcProcessManagerTests {

    init {
        System.setProperty("config.dir", "./fordebug")
        System.setProperty("frpc.exec", "/home/wc/apps/frp_0.29.0_linux_amd64/frpc")
    }

    @Autowired
    lateinit var frpcProfileManager: FrpcProfileManager

    @Test
    fun getProfiles() {
        val profiles = frpcProfileManager.getProfiles()
        println(profiles)
    }

    @Test
    fun test() {
        frpcProfileManager.startProcessForEachProfile()

        while(true) {
            Thread.sleep(1000)
            frpcProfileManager.getLogContent("frpc.ini").let { println(it) }
        }
        Thread.sleep(100000)
    }
}