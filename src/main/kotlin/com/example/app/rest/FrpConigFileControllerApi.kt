package com.example.app.rest

import ClientBeanVO
import CommonBeanVO
import CreateClientBeanDTO
import DeleteClientBeanDTO
import UpdateClientBeanDTO
import UpdateCommonBeanDTO
import com.example.app.CommonBean
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.math.BigDecimal
import javax.validation.Valid

@Validated
@RequestMapping("/api")
interface ClientConfigApi {
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
    fun clientConfigsGet(): ResponseEntity<List<ClientBeanVO>>

}


@RequestMapping("/api")
@Validated
interface CommonConfigApi {
    @Operation(summary = "Returns the common block", description = "", tags = ["common-config"])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "A JSON of common block",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonBean::class))]
        )]
    )
    @RequestMapping(value = ["/common-config"], produces = ["application/json"], method = [RequestMethod.GET])
    fun commonConfigGet(): ResponseEntity<CommonBeanVO>

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
        ) @RequestBody body: @Valid UpdateCommonBeanDTO?,
    ): ResponseEntity<CommonBeanVO>
}


