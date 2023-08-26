package com.example.app.rest

import ClientBeanVO
import CommonBeanVO
import CreateClientBeanDTO
import DeleteClientBeanDTO
import UpdateClientBeanDTO
import UpdateCommonBeanDTO
import com.example.app.ClientBean
import com.example.app.CommonBean
import com.example.app.FrpConfigManager
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.annotation.Generated
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

import io.swagger.v3.oas.annotations.media.Schema;
import mu.KotlinLogging
import org.json.JSONObject
import org.springframework.web.bind.annotation.RequestBody;


@Generated(
    value = ["io.swagger.codegen.v3.generators.java.SpringCodegen"],
    date = "2023-08-26T05:45:28.537187824Z[GMT]"
)
@RestController
class ClientConfigApiController @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val request: HttpServletRequest,
) :
    ClientConfigApi {

    @Autowired
    lateinit var frpConfigManager: FrpConfigManager

    private val log = KotlinLogging.logger {}

    override fun clientConfigDelete(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid DeleteClientBeanDTO?,
    ): ResponseEntity<String> {
        frpConfigManager.removeClientBean(body!!.name!!)
        return ResponseEntity.ok(JSONObject.valueToString(body!!.name!!))
    }

    override fun clientConfigPost(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid CreateClientBeanDTO?,
    ): ResponseEntity<ClientBeanVO> {
        frpConfigManager.addClientBean(
            ClientBean().apply {
                this.localIp = body!!.localIp
                this.name = body.name
                this.localPort = body.localPort!!.toInt()
                this.type = body.type.toString()
                this.remotePort = body.remotePort!!.toInt()
            }
        )
        return ResponseEntity.ok(ClientBeanVO().apply {
            this.localIp = body!!.localIp
            this.name = body.name
            this.localPort = body.localPort
            this.type = body.type
            this.remotePort = body.remotePort
        })
    }

    override fun clientConfigPut(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid UpdateClientBeanDTO?,
    ): ResponseEntity<ClientBeanVO> {
        frpConfigManager.updateClientBean(
            ClientBean().apply {
                this.localIp = body!!.localIp
                this.name = body.name
                this.localPort = body.localPort!!.toInt()
                this.type = body.type.toString()
                this.remotePort = body.remotePort!!.toInt()
            }
        )
        return ResponseEntity.ok(ClientBeanVO().apply {
            this.localIp = body!!.localIp
            this.name = body.name
            this.localPort = body.localPort
            this.type = body.type
            this.remotePort = body.remotePort
        })
    }


    override fun clientConfigsGet(): ResponseEntity<List<ClientBeanVO>> {
        val listClientBeans = frpConfigManager.listClientBeans()
        listClientBeans.map {
            ClientBeanVO().apply {
                this.localIp = it.localIp
                this.name = it.name
                this.localPort = BigDecimal(it.localPort)
                this.type = TypeEnum.fromValue(it.type!!)
                this.remotePort = BigDecimal(it.remotePort)
            }
        }.toList().apply {
            return ResponseEntity.ok(this)
        }
    }
}


@Generated(
    value = ["io.swagger.codegen.v3.generators.java.SpringCodegen"],
    date = "2023-08-26T05:45:28.537187824Z[GMT]"
)
@RestController
class CommonConfigApiController @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val request: HttpServletRequest,
) :
    CommonConfigApi {

    private val log = KotlinLogging.logger {}

    @Autowired
    lateinit var frpConfigManager: FrpConfigManager

    override fun commonConfigGet(): ResponseEntity<CommonBeanVO> {
        var commonBean:CommonBean = frpConfigManager.getCommonBean()
        return ResponseEntity.ok(CommonBeanVO().apply {
            this.serverAddr = commonBean.serverAddr
            this.serverPort = BigDecimal(commonBean.serverPort)
            this.token = commonBean.token
        })
    }

    override fun commonConfigPut(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid UpdateCommonBeanDTO?,
    ): ResponseEntity<CommonBeanVO> {
        frpConfigManager.updateCommonBean(
            CommonBean().apply {
                this.serverAddr = body!!.serverAddr
                this.serverPort = body.serverPort!!.toInt()
                this.token = body.token
            }
        )
        return ResponseEntity.ok(CommonBeanVO().apply {
            this.serverAddr = body!!.serverAddr
            this.serverPort = body.serverPort
            this.token = body.token
        })
    }

}
