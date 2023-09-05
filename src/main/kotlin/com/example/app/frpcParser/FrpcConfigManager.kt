package com.example.app.frpcParser

import com.example.app.frpcProcessManager.AppProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

data class FrpcConfig(val common: CommonBean, val clients: List<ClientBean>)

data class CommonBean (val server_addr: String, val server_port: Int, val token: String)
{
    // default constructor
    constructor() : this("", 0, "")
}

data class ClientBean (val name: String, val type: String, val local_ip: String, val local_port: Int, val remote_port: Int)
{
    // default constructor
    constructor() : this("", "", "", 0, 0)
}

interface FrpConfigManager {

    fun updateCommonBean(profileName: String, newCommonBean: CommonBean)
    fun getCommonBean(profileName: String): CommonBean

    fun addClientBean(profileName: String, clientBean: ClientBean)
    fun removeClientBean(profileName: String, clientName: String)

    fun updateClientBean(profileName: String, newClientBean: ClientBean)
    fun listClientBeans(profileName: String): List<ClientBean>

}

@Component
class FrpConfigManagerImpl(private val parser: FrpcConfigParser) : FrpConfigManager {

    @Autowired
    lateinit var appProperties: AppProperties

    override fun updateCommonBean(profileName: String, newCommonBean: CommonBean) {
        var profile = appProperties.configFileDir + "/" + profileName
        val config = parser.parse(File(profile))
        val updatedConfig = config.copy(common = CommonBean(newCommonBean.server_addr, newCommonBean.server_port, newCommonBean.token))
        parser.writeToFile(profileName, updatedConfig, File(profile))
    }

    override fun getCommonBean(profileName: String): CommonBean {
        var profile = appProperties.configFileDir + "/" + profileName
        val config = parser.parse(File(profile))
        return CommonBean(config.common.server_addr, config.common.server_port, config.common.token)
    }

    override fun addClientBean(profileName: String, clientBean: ClientBean) {
        var profile = appProperties.configFileDir + "/" + profileName
        val config = parser.parse(File(profile))
        val updatedClients = config.clients + ClientBean(clientBean.name, clientBean.type, clientBean.local_ip, clientBean.local_port, clientBean.remote_port)
        parser.writeToFile(profileName, config.copy(clients = updatedClients), File(profile))
    }

    override fun removeClientBean(profileName: String, clientName: String) {
        var profile = appProperties.configFileDir + "/" + profileName
        val config = parser.parse(File(profile))
        val updatedClients = config.clients.filter { it.name != clientName }
        parser.writeToFile(profileName, config.copy(clients = updatedClients), File(profile))
    }

    override fun updateClientBean(profileName: String, newClientBean: ClientBean) {
        var profile = appProperties.configFileDir + "/" + profileName
        val config = parser.parse(File(profile))
        val updatedClients = config.clients.map {
            if (it.name == newClientBean.name) {
                ClientBean(newClientBean.name, newClientBean.type, newClientBean.local_ip, newClientBean.local_port, newClientBean.remote_port)
            } else {
                it
            }
        }
        parser.writeToFile(profileName, config.copy(clients = updatedClients), File(profile))
    }

    override fun listClientBeans(profileName: String): List<ClientBean> {
        var profile = appProperties.configFileDir + "/" + profileName
        val config = parser.parse(File(profile))
        return config.clients.map {
            ClientBean(it.name, it.type, it.local_ip, it.local_port, it.remote_port)
        }
    }
}