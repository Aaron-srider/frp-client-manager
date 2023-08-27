package com.example.app.rest

import com.example.app.FrpConfigManager
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.file.Paths
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.io.path.createDirectories


@Validated
@RequestMapping("/api")
interface FrpcApi {
    @Operation(summary = "manage frpc process", description = "", tags = [])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "execute process successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))
            ]
        )]
    )
    @RequestMapping(value = ["/test"], produces = ["application/json"], method = [RequestMethod.GET])
    fun frpcGet(
        @NotNull @Parameter(
            `in` = ParameterIn.QUERY,
            description = "",
            required = true,
            schema = Schema(allowableValues = ["start", "end", "restart"])
        ) @RequestParam(value = "command", required = true) command: @Valid String?,
    ): ResponseEntity<String>
}

@RestController
class FrpcApiController @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val request: HttpServletRequest,
) : FrpcApi {

    @Autowired
    lateinit var frpConfigManager: FrpConfigManager

    private val log = KotlinLogging.logger {}
    override fun frpcGet(
        @NotNull @Parameter(
            `in` = ParameterIn.QUERY,
            description = "",
            required = true,
            schema = Schema(allowableValues = ["start", "end", "restart"])
        ) @RequestParam(value = "command", required = true) command: @Valid String?,
    ): ResponseEntity<String> {
        val frpcExec = System.getProperty("frpc.exec")
        if (frpcExec != null && frpcExec != "") {

            // frpConfigManager.restartFrpc(property)
            println("0")
            return ResponseEntity.ok(JSONObject.valueToString("execute command successfully"))
        } else {
            println("1")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("frpc.exec is not set")
        }
    }


}


var frpcPid = -1L

val log = KotlinLogging.logger {}

fun startFrpc(frpcExec: String, configFile: String): Process {
    val profileName = File(configFile).name

    var profileLog = File(configFile).parentFile.resolve("${profileName}.log")

    var process = ProcessBuilder("sh", "-c", "${frpcExec} -c ${configFile} > ${profileLog.absolutePath}").start()
    frpcPid = process.pid()
    log.debug { "frpc process is started" }
    return process
}

@Component
class FrpcProfileManager {

    var lock: Any = Any()

    var configfilelistMap = mutableMapOf<String, Process?>(
        "frpc.ini" to null
    )

    var configFileDir: String = System.getProperty("config.dir") ?: throw RuntimeException("config.dir is not set")

    val frpcExec = System.getProperty("frpc.exec") ?: throw RuntimeException("frpc.exec is not set")

    init {
        Paths.get(configFileDir).createDirectories()
    }

    fun getLogContent(configFile: String): String? {
        synchronized(lock) {
            if (configfilelistMap.containsKey(configFile)) {
                var profileLog = File(configFileDir).resolve("${configFile}.log")
                if(profileLog.exists()){
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
                var configfile = File(configFileDir).resolve(it.key)
                var process: Process? = null
                try {
                    process = startFrpc(frpcExec, configfile.absolutePath)
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
                var configfile = File(configFileDir).resolve(entry.key)
                if (process == null || !process!!.isAlive) {
                    log.debug { "frpc process for ${configfile} is dead, restart it" }

                    var newProcess: Process? = null
                    try {
                        newProcess = startFrpc(frpcExec, configfile.absolutePath)
                    } catch (e: Exception) {
                        log.debug { "start frpc process for ${configfile} failed" }
                    }

                    configfilelistMap.put(entry.key, newProcess)
                }
            }
        }
    }
}


@Component
class FrpcRunner : CommandLineRunner {

    @Autowired
    lateinit var frpcProfileManager: FrpcProfileManager

    private val log = KotlinLogging.logger {}
    override fun run(vararg args: String?) {
        // start process for each config file
        frpcProfileManager.startProcessForEachProfile()

        // check processes status every 1 second
        while (true) {
            Thread.sleep(1000)
            frpcProfileManager.scanForRestartDeadProcess()
            val logContent = frpcProfileManager.getLogContent("frpc.ini")
            println(logContent)
        }



    }

}