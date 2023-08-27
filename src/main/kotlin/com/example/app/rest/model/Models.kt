import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


/**
 * Convert the given object to string with each line indented by 4 spaces
 * (except the first line).
 */
fun toIndentedString(o: Any?): String {
    return o?.toString()?.replace("\n", "\n    ") ?: "null"
}


data class ProfileVO(var name: String) {
    constructor() : this("")
}


@Validated
class DeleteClientBeanDTO {

    @JsonProperty("profileName")
    @NotEmpty
    var profileName: String? = null

    @JsonProperty("name")
    @NotEmpty
    var name: String? = null

    override fun toString(): String {
        val sb = java.lang.StringBuilder()
        sb.append("class DeleteClientBeanDTO {\n")
        sb.append("    name: ").append(toIndentedString(name)).append("\n")
        sb.append("}")
        return sb.toString()
    }
}

/**
 * ClientBean
 */
@Validated
class ClientBeanVO {
    @JsonProperty("name")
    var name: String? = null

    @JsonProperty("local_port")
    var localPort: BigDecimal? = null

    @JsonProperty("remote_port")
    var remotePort: BigDecimal? = null

    @JsonProperty("local_ip")
    var localIp: String? = null

    @JsonProperty("type")
    var type: TypeEnum? = null
}


/**
 * CommonBean
 */
@Validated
class CommonBeanVO {
    @JsonProperty("server_addr")
    var serverAddr: String? = null

    @JsonProperty("server_port")
    var serverPort: BigDecimal? = null

    @JsonProperty("token")
    var token: String? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class CommonBean {\n")
        sb.append("    serverAddr: ").append(toIndentedString(serverAddr)).append("\n")
        sb.append("    serverPort: ").append(toIndentedString(serverPort)).append("\n")
        sb.append("    token: ").append(toIndentedString(token)).append("\n")
        sb.append("}")
        return sb.toString()
    }
}


/**
 * CommonBean
 */
@Validated
class UpdateCommonBeanDTO {

    @JsonProperty("profileName")
    @NotEmpty
    var profileName: String? = null

    @JsonProperty("server_addr")
    @NotEmpty
    var serverAddr: String? = null

    @JsonProperty("server_port")
    @NotNull
    @Valid
    var serverPort: BigDecimal? = null

    @JsonProperty("token")
    @NotEmpty
    var token: String? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class CommonBean {\n")
        sb.append("    serverAddr: ").append(toIndentedString(serverAddr)).append("\n")
        sb.append("    serverPort: ").append(toIndentedString(serverPort)).append("\n")
        sb.append("    token: ").append(toIndentedString(token)).append("\n")
        sb.append("}")
        return sb.toString()
    }


}

/**
 * Gets or Sets type
 */
enum class TypeEnum(private val value: String) {
    UDP("udp"),
    TCP("tcp");

    @JsonValue
    override fun toString(): String {
        return value
    }

    companion object {
        @JsonCreator
        fun fromValue(text: String): TypeEnum? {
            for (b in values()) {
                if (b.value == text) {
                    return b
                }
            }
            return null
        }
    }
}

@Validated
class CreateClientBeanDTO {

    @JsonProperty("profileName")
    @NotEmpty
    var profileName: String? = null

    @JsonProperty("name")
    @NotEmpty
    var name: String? = null


    @JsonProperty("local_port")
    @NotNull
    @Valid
    var localPort: BigDecimal? = null

    @JsonProperty("remote_port")
    @Valid
    @NotNull
    var remotePort: BigDecimal? = null

    @NotEmpty
    @JsonProperty("local_ip")
    var localIp: String? = null


    @JsonProperty("type")
    var type: TypeEnum? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class CreateClientBeanDTO {\n")
        sb.append("    name: ").append(toIndentedString(name)).append("\n")
        sb.append("    localPort: ").append(toIndentedString(localPort)).append("\n")
        sb.append("    remotePort: ").append(toIndentedString(remotePort)).append("\n")
        sb.append("    localIp: ").append(toIndentedString(localIp)).append("\n")
        sb.append("    type: ").append(toIndentedString(type)).append("\n")
        sb.append("}")
        return sb.toString()
    }


}


@Validated
class UpdateClientBeanDTO {

    @JsonProperty("profileName")
    @NotEmpty
    var profileName: String? = null

    @JsonProperty("name")
    @NotEmpty
    var name: String? = null

    @JsonProperty("local_port")
    @NotNull
    @Valid
    var localPort: BigDecimal? = null

    @JsonProperty("remote_port")
    @Valid
    @NotNull
    var remotePort: BigDecimal? = null

    @NotEmpty
    @JsonProperty("local_ip")
    var localIp: String? = null

    @JsonProperty("type")
    var type: TypeEnum? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class CreateClientBeanDTO {\n")
        sb.append("    name: ").append(toIndentedString(name)).append("\n")
        sb.append("    localPort: ").append(toIndentedString(localPort)).append("\n")
        sb.append("    remotePort: ").append(toIndentedString(remotePort)).append("\n")
        sb.append("    localIp: ").append(toIndentedString(localIp)).append("\n")
        sb.append("    type: ").append(toIndentedString(type)).append("\n")
        sb.append("}")
        return sb.toString()
    }


}