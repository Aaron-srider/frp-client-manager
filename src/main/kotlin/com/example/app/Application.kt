package com.example.app

import com.example.app.exception.ApiException
import com.fasterxml.jackson.databind.RuntimeJsonMappingException
import io.swagger.annotations.Api
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.*
import java.lang.RuntimeException
import javax.swing.text.html.HTML.Tag.P


@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Component
class TestRunner: CommandLineRunner {

    private val log = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        log.info { "write some here" }
        Thread(Runnable {
            NettyServer("192.168.31.94", 8098).run()
        }).start()
    }

}

interface FrpConfigManager {

    fun setConfigFilePath(path: String)

    fun updateCommonBean(newCommonBean: CommonBean)

    fun addClientBean(clientBean: ClientBean)
    fun listClientBeans(): List<ClientBean>
    fun removeClientBean(clientName: String)

    fun updateClientBean(newClientBean: ClientBean)
    fun getCommonBean(): CommonBean
    fun restartFrpc(frpcExecutable: String)
}


class CommonBean {
    var serverAddr: String? = null
    var serverPort = 0
    var token: String? = null // Getters, Setters, Constructors, etc.

    // tostring
    override fun toString(): String {
        return "CommonBean(serverAddr=$serverAddr, serverPort=$serverPort, token=$token)"
    }
}

class ClientBean {
    var name // Unique name to identify the client block, like [ssh]
            : String? = null
    var type // http, tcp, etc.
            : String? = null
    var localIp: String? = null
    var localPort = 0
    var remotePort = 0 // Getters, Setters, Constructors, etc.

    // tostring
    override fun toString(): String {
        return "ClientBean(name=$name, type=$type, localIp=$localIp, localPort=$localPort, remotePort=$remotePort)"
    }
}

class FrpConfigManagerImpl : FrpConfigManager {
    val serverBeans: MutableList<CommonBean> = ArrayList<CommonBean>()
    val clientBeans: MutableList<ClientBean> = ArrayList()
    var CONFIG_FILE_PATH = "path_to_your_config_file.ini" // Define the actual path here.

    constructor(configFilePath: String) {
        this.setConfigFilePath(configFilePath)
    }

    val myLock = Any()

    override fun restartFrpc(frpcExecutable: String) {
        File("startfrp.log").exists().let {
            if (it) {
                File("startfrp.log").delete()
            }
        }
        startFrpWithConfig(frpcExecutable, this.CONFIG_FILE_PATH);
    }

    private fun startFrpWithConfig(frpcExecutable: String, configFile: String) {
        // Check and kill existing frpc process if running
        killExistingFrpProcess()


        val script = this.javaClass.classLoader.getResource("startFrpc.sh")
        println(script)

        // release the script to tmp


        val openStream = script.openStream()
        val scriptPath = "/tmp/startFrpc.sh"
        val fileOutputStream = FileOutputStream(scriptPath)
        openStream.copyTo(fileOutputStream)
        openStream.close()
        fileOutputStream.close()


        val processBuilder = ProcessBuilder("bash", "/tmp/startFrpc.sh", frpcExecutable, configFile)

        val process = processBuilder.start()
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach {
                    println(it)
                }
            }
            println("frpc started successfully.")
        } else {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach {
                    println(it)
                }
            }
            throw RuntimeException("frpc failed to start. Exit code: $exitCode")
        }
    }

    private fun killExistingFrpProcess() {
        val processName = "frpc" // Process name to look for

        val process = Runtime.getRuntime().exec("pgrep $processName")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val pid = line!!.trim().toInt()
            val killProcess = Runtime.getRuntime().exec("kill $pid")
            val waitFor = killProcess.waitFor()
            if (waitFor == 0) {
                println("Killed existing frpc process with PID: $pid")
            } else {
                throw RuntimeException("failed to frpc. Exit code: $waitFor")
            }
        }
    }

    override fun setConfigFilePath(path: String) {
        synchronized(myLock) {
            CONFIG_FILE_PATH = path
            loadFromFile()
        }
    }

    override fun updateCommonBean(newCommonBean: CommonBean) {
        synchronized(myLock) {
            val listServerBeans = this.listServerBeans()

            if (listServerBeans.isNotEmpty()) {
                val toList = listServerBeans.map { it.serverAddr!! }.toList()
                toList.forEach {
                    this.removeServerBean(it)
                }
            }
            this.addServerBean(newCommonBean)
        }
    }


    override fun addClientBean(clientBean: ClientBean) {
        synchronized(myLock) {
            clientBeans.add(clientBean)
            syncToFile()
        }
    }

    override fun listClientBeans(): List<ClientBean> {
        loadFromFile()
        return clientBeans
    }

    override fun removeClientBean(clientName: String) {
        synchronized(myLock) {
            clientBeans.removeIf { bean -> bean.name.equals(clientName) }
            syncToFile()
        }
    }

    override fun updateClientBean(newClientBean: ClientBean) {
        synchronized(myLock) {
            val listClientBeans = this.listClientBeans()

            if (listClientBeans.isNotEmpty()) {
                val toList = listClientBeans.map { it.name!! }.filter { it == newClientBean.name }.toList()
                toList.forEach {
                    this.removeClientBean(it)
                }
            }
            this.addClientBean(newClientBean)
        }
    }

    override fun getCommonBean(): CommonBean {
        synchronized(myLock) {
            val listServerBeans = this.listServerBeans()
            if (listServerBeans.isEmpty()) {
                throw ApiException(HttpStatus.NOT_FOUND, "No common bean found")
            }
            return listServerBeans[0]
        }
    }

    private fun addServerBean(commonBean: CommonBean) {
        serverBeans.add(commonBean)
        syncToFile()
    }

    private fun listServerBeans(): List<CommonBean> {
        loadFromFile() // Load the latest beans from the file before listing.
        return serverBeans
    }

    private fun removeServerBean(serverBeanAddr: String) {
        serverBeans.removeIf { bean: CommonBean ->
            bean.serverAddr.equals(serverBeanAddr)
        }
        syncToFile()
    }

    // Load beans from the file
    private fun loadFromFile() {
        synchronized(myLock) {
            serverBeans.clear()
            clientBeans.clear()
            try {
                BufferedReader(FileReader(CONFIG_FILE_PATH)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.trim { it <= ' ' } == "[common]") {
                            val bean = CommonBean()

                            // Assume that the file structure is predictable.
                            bean.serverAddr = (reader.readLine().split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1].trim { it <= ' ' })
                            bean.serverPort = (reader.readLine().split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1].trim { it <= ' ' }
                                .toInt())
                            bean.token = (reader.readLine().split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1].trim { it <= ' ' })
                            serverBeans.add(bean)
                        } else if (line!!.trim().startsWith("[")) {  // Assuming a client block starts with [name]
                            var bean: ClientBean = ClientBean();
                            bean.name = (line!!.trim().substring(1, line!!.length - 1)); // Extract name

                            bean.type = (reader.readLine().split("=")[1].trim());
                            bean.localIp = (reader.readLine().split("=")[1].trim());
                            bean.localPort = (Integer.parseInt(reader.readLine().split("=")[1].trim()));
                            bean.remotePort = (Integer.parseInt(reader.readLine().split("=")[1].trim()));

                            clientBeans.add(bean);
                        }
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("config file format error", e)
            }
        }
    }

    // Sync the beans to the file
    private fun syncToFile() {
        synchronized(myLock) {
            try {
                BufferedWriter(FileWriter(CONFIG_FILE_PATH)).use { writer ->
                    for (bean in serverBeans) {
                        writer.write("[common]")
                        writer.write("\n")
                        writer.write(
                            """
							${"server_addr = " + bean.serverAddr}
							""".trimIndent()
                        )
                        writer.write("\n")
                        writer.write(
                            """
							${"server_port = " + bean.serverPort}
							""".trimIndent()
                        )
                        writer.write("\n")
                        writer.write(
                            """
							${"token = " + bean.token}
							""".trimIndent()
                        )
                        writer.write("\n")
                    }

                    for (clientBean in clientBeans) {
                        writer.write(
                            """
                                [${clientBean.name}]
                            """.trimIndent()
                        )
                        writer.write("\n")
                        writer.write(
                            """
                                type = ${clientBean.type}
                            """.trimIndent()
                        )
                        writer.write("\n")
                        writer.write(
                            """
                                local_ip = ${clientBean.localIp}
                            """.trimIndent()
                        )
                        writer.write("\n")
                        writer.write(
                            """
                                local_port = ${clientBean.localPort}
                            """.trimIndent()
                        )
                        writer.write("\n")
                        writer.write(
                            """
                                remote_port = ${clientBean.remotePort}
                            """.trimIndent()
                        )
                        writer.write("\n")
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}