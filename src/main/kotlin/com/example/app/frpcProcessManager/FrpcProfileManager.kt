package com.example.app.frpcProcessManager

import ProfileVO
import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.io.File


val log = KotlinLogging.logger {}

fun startFrpc(frpcExec: String, configFile: String): Process {

    val profileName = File(configFile).name

    // Create a ProcessBuilder for the command
    val processBuilder = ProcessBuilder(frpcExec, "-c", configFile)

    var profileLog = File(configFile).parentFile.resolve("${profileName}.log")

    // Redirect the output to a log file
    val logFile = File(profileLog.absolutePath)
    processBuilder.redirectOutput(logFile)

    var process =  processBuilder.start()

    log.debug { "frpc profile ${configFile} is started" }
    return process
}

@Component
class AppProperties : InitializingBean {
    var configFileDir: String = ""
    var frpcExec: String = ""
    override fun afterPropertiesSet() {
        var configFileDir: String = System.getProperty("config.dir") ?: throw RuntimeException("config.dir is not set")
        (File(configFileDir).exists() == true && File(configFileDir).isDirectory) || throw RuntimeException("config dir ${configFileDir} is not found")

        var frpcExec = System.getProperty("frpc.exec") ?: throw RuntimeException("frpc.exec is not set")
        (File(frpcExec).exists() == true && File(frpcExec).isFile) || throw RuntimeException("frpc.exec ${frpcExec} is not found")

        this.configFileDir = configFileDir
        this.frpcExec = frpcExec
    }
}

@Component
class FrpcProfileManager {

    var lock: Any = Any()

    var configfilelistMap = mutableMapOf<String, Process?>(
        "frpc.ini" to null
    )

    @Autowired
    lateinit var appProperties: AppProperties

    fun killProcess(configFile: String) {
        synchronized(lock) {
            if (configfilelistMap.containsKey(configFile)) {
                var process = configfilelistMap.get(configFile)
                if (process != null && process!!.isAlive) {
                    process!!.destroy()
                    configfilelistMap.put(configFile, null)
                }
            }
        }
    }

    fun getProfiles(): MutableList<ProfileVO> {
        configfilelistMap.map {
            ProfileVO().apply { this.name = it.key }
        }.toMutableList().apply { return this }
    }

    fun getLogContent(configFile: String): String? {
        synchronized(lock) {
            if (configfilelistMap.containsKey(configFile)) {
                var profileLog = File(appProperties.configFileDir).resolve("${configFile}.log")
                if (profileLog.exists()) {
                    var logContent = profileLog.readText()
                    return logContent
                }
                return null
            }
            return null;
        }
    }

    fun startProcessForEachProfile() {
        synchronized(lock) {
            // start process for each config file
            configfilelistMap.forEach {
                var configfile = File(appProperties.configFileDir).resolve(it.key)
                var process: Process? = null
                try {
                    process = startFrpc(appProperties.frpcExec, configfile.absolutePath)
                } catch (e: Exception) {
                    log.debug { "start frpc process for ${configfile} failed" }
                }
                configfilelistMap.put(it.key, process)
            }
        }
    }

    fun scanForRestartDeadProcess() {
        synchronized(lock) {
            configfilelistMap.forEach { entry ->
                var process = entry.value
                var configfile = File(appProperties.configFileDir).resolve(entry.key)
                if (process == null || !process!!.isAlive) {
                    log.debug { "frpc process for ${configfile} is dead, restart it" }

                    var newProcess: Process? = null
                    try {
                        newProcess = startFrpc(appProperties.frpcExec, configfile.absolutePath)
                    } catch (e: Exception) {
                        log.debug { "start frpc process for ${configfile} failed" }
                    }

                    configfilelistMap.put(entry.key, newProcess)
                }
            }
        }
    }

    fun profileStatus(profileName:String): Boolean {
        synchronized(lock) {
            configfilelistMap.get(profileName)?.let {
                return it.isAlive
            } ?: return false
        }
    }

    fun reloadProfile(profileName: String) {
        synchronized(lock) {
            configfilelistMap.get(profileName)?.let {
                deadlyKillAProcess(it)

                var configfile = File(appProperties.configFileDir).resolve(profileName)
                var newProcess: Process? = null
                try {
                    newProcess = startFrpc(appProperties.frpcExec, configfile.absolutePath)
                } catch (e: Exception) {
                    log.debug { "start frpc process for ${configfile} failed" }
                    configfilelistMap.remove(profileName)
                    throw RuntimeException("start frpc process for ${configfile} failed", e)
                }

                configfilelistMap.put(profileName, newProcess)
            }
        }
    }
}


fun deadlyKillAProcess(process: Process) {
    dokill(process.toHandle())
}

fun dokill(processHandler: ProcessHandle) {
    val children = processHandler.children()
    children.forEach {
        dokill(it)
    }

    processHandler.destroy()
}

@Component
class FrpcRunner : CommandLineRunner {
    @Autowired
    lateinit var frpcProfileManager: FrpcProfileManager
    override fun run(vararg args: String?) {

        if (true) {
            Thread {
                // start process for each config file
                frpcProfileManager.startProcessForEachProfile()

                // check processes status every 1 second
                while (true) {
                    Thread.sleep(1000)
                    frpcProfileManager.scanForRestartDeadProcess()
                    // val logContent = frpcProfileManager.getLogContent("frpc.ini")
                    // println(logContent)
                }
            }.start()
        }


    }

}