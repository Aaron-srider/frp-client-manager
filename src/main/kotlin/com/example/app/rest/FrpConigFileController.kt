package com.example.app.rest

import ClientBeanVO
import CommonBeanVO
import CreateClientBeanDTO
import DeleteClientBeanDTO
import TypeEnum
import UpdateClientBeanDTO
import UpdateCommonBeanDTO
import com.example.app.frpcParser.ClientBean
import com.example.app.frpcParser.CommonBean
import com.example.app.frpcParser.FrpConfigManager
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Validated
@RequestMapping("/api")
interface FrpcConfigApi {
    @Operation(summary = "delete a client config block", description = "", tags = ["client-config"])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "The added client block",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = BigDecimal::class))]
        )]
    )
    @RequestMapping(
        value = ["/client-config"],
        produces = ["application/json"],
        consumes = ["application/json"],
        method = [RequestMethod.DELETE]
    )
    fun clientConfigDelete(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid DeleteClientBeanDTO?,
    ): ResponseEntity<String>

    @Operation(summary = "add a new client config block", description = "", tags = ["client-config"])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "The added client block",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ClientBeanVO::class))]
        ), ApiResponse(
            responseCode = "409",
            description = "Client block exists",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
        )]
    )
    @RequestMapping(
        value = ["/client-config"],
        produces = ["application/json"],
        consumes = ["application/json"],
        method = [RequestMethod.POST]
    )
    fun clientConfigPost(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid CreateClientBeanDTO?,
    ): ResponseEntity<ClientBeanVO>

    @Operation(summary = "update a client config block", description = "", tags = ["client-config"])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "The added client block",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ClientBeanVO::class))]
        )]
    )
    @RequestMapping(
        value = ["/client-config"],
        produces = ["application/json"],
        consumes = ["application/json"],
        method = [RequestMethod.PUT]
    )
    fun clientConfigPut(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody body: @Valid UpdateClientBeanDTO?,
    ): ResponseEntity<ClientBeanVO>


    @Operation(summary = "Returns a full list of config blocks", description = "", tags = ["client-config"])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "A JSON array of client blocks",
            content = [Content(
                mediaType = "application/json",
                array = ArraySchema(schema = Schema(implementation = ClientBeanVO::class))
            )]
        )]
    )
    @RequestMapping(value = ["/client-configs"], produces = ["application/json"], method = [RequestMethod.GET])
    fun clientConfigsGet(
        @NotNull
        @Valid
        @RequestParam(value = "profileName", required = true)
        @Parameter(`in` = ParameterIn.QUERY, description = "", required = true, schema = Schema())
        profileName: String
    ): ResponseEntity<List<ClientBeanVO>>




    @Operation(summary = "Returns the common block", description = "", tags = ["common-config"])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "A JSON of common block",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonBean::class))]
        )]
    )
    @RequestMapping(value = ["/common-config"], produces = ["application/json"], method = [RequestMethod.GET])
    fun commonConfigGet(@NotEmpty @Valid profileName: String): ResponseEntity<CommonBeanVO>

    @Operation(
        summary = "Update the common block, if the common block does not exist, create it",
        description = "",
        tags = ["common-config"]
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "A JSON of common block",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonBean::class))]
        )]
    )
    @RequestMapping(
        value = ["/common-config"],
        produces = ["application/json"],
        consumes = ["application/json"],
        method = [RequestMethod.PUT]
    )
    fun commonConfigPut(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody @Valid body: UpdateCommonBeanDTO?,
    ): ResponseEntity<CommonBeanVO>
}





@RestController
class FrpcConfigApiImpl @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val request: HttpServletRequest,
) :
    FrpcConfigApi {

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
        frpConfigManager.removeClientBean(
            profileName = body!!.profileName!!,
            clientName = body!!.name!!
        )
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
            body!!.profileName!!,
            ClientBean(
                body!!.name!!,
                body.type.toString(),
                body!!.localIp!!,
                body!!.localPort!!.toInt(),
                body.remotePort!!.toInt()
            )
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
            body!!.profileName!!,
            ClientBean(
                body!!.name!!,
                body.type.toString(),
                body!!.localIp!!,
                body!!.localPort!!.toInt(),
                body.remotePort!!.toInt()
            )
        )
        return ResponseEntity.ok(ClientBeanVO().apply {
            this.localIp = body!!.localIp
            this.name = body.name
            this.localPort = body.localPort
            this.type = body.type
            this.remotePort = body.remotePort
        })
    }


    override fun clientConfigsGet(profileName: String): ResponseEntity<List<ClientBeanVO>> {
        val listClientBeans = frpConfigManager.listClientBeans(profileName)
        listClientBeans.map {
            ClientBeanVO().apply {
                this.localIp = it.local_ip
                this.name = it.name
                this.localPort = BigDecimal(it.local_port)
                this.type = TypeEnum.fromValue(it.type!!)
                this.remotePort = BigDecimal(it.remote_port)
            }
        }.toList().apply {
            return ResponseEntity.ok(this)
        }
    }



    override fun commonConfigGet(profileName: String): ResponseEntity<CommonBeanVO> {
        var commonBean: CommonBean = frpConfigManager.getCommonBean(profileName = profileName)
        return ResponseEntity.ok(CommonBeanVO().apply {
            this.serverAddr = commonBean.server_addr
            this.serverPort = BigDecimal(commonBean.server_port)
            this.token = commonBean.token
        })
    }

    override fun commonConfigPut(
        @Parameter(
            `in` = ParameterIn.DEFAULT,
            description = "",
            schema = Schema()
        ) @RequestBody @Valid body: UpdateCommonBeanDTO?,
    ): ResponseEntity<CommonBeanVO> {
        frpConfigManager.updateCommonBean(
            body!!.profileName!!,
            CommonBean(body!!.serverAddr!!, body.serverPort!!.toInt(), body!!.token!!)
        )
        return ResponseEntity.ok(CommonBeanVO().apply {
            this.serverAddr = body!!.serverAddr
            this.serverPort = body.serverPort
            this.token = body.token
        })
    }


}



