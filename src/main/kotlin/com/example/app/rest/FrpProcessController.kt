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
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import mu.KotlinLogging
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import javax.annotation.Generated
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.NotNull


@Validated
@RequestMapping("/api")
interface FrpcApi {
    @Operation(summary = "manage frpc process", description = "", tags = [])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "execute process successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))
        ])]
    )
    @RequestMapping(value = ["/frpc"], produces = ["application/json"], method = [RequestMethod.GET])
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
        val property = System.getProperty("frpc.exec")
        if(property != null && property != "") {
            frpConfigManager.restartFrpc(property)
            return ResponseEntity.ok(JSONObject.valueToString("execute command successfully"))
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("frpc.exec is not set")
        }
    }


}
