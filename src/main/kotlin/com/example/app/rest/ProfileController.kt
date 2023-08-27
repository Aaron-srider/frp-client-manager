package com.example.app.rest

import ProfileVO
import com.example.app.frpcProcessManager.FrpcProfileManager
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RequestMapping("/api")
@Validated
interface ProfilesApi {

    @Operation(summary = "", description = "", tags = [])
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "execute process successfully",
            content = [Content(
                mediaType = "application/json",
                array = ArraySchema(schema = Schema(implementation = ProfileVO::class))
            )]
        )]
    )
    @RequestMapping(value = ["/profiles"], produces = ["application/json"], method = [RequestMethod.GET])
    fun profilesGet(): ResponseEntity<kotlin.collections.List<ProfileVO>>

}

@RestController
class ProfilesApiImpl @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val request: HttpServletRequest,
) :
    ProfilesApi {

    @Autowired
    lateinit var frpcProfileManager: FrpcProfileManager

    private val log = KotlinLogging.logger {}

    override fun profilesGet(): ResponseEntity<List<ProfileVO>> {
        val profiles = frpcProfileManager.getProfiles()
        return ResponseEntity.ok(profiles)
    }

}
