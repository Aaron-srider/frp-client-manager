package com.example.app.exception

import cn.hutool.core.util.ReflectUtil
import mu.KotlinLogging
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import java.lang.reflect.Field
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolationException
import javax.validation.Path

enum class ErrorCode {
    UNKNOWN,
    SUCCESS,
    INVALID_PARAMETER,
    UPLOAD_FILE_SIZE_EXCEED_UPPER_LIMIT,
    SERVER_ERROR,
    KEY_NOT_EXISTS
}


class BackendException : RuntimeException {
    var data: Any?
    var code: String
    var msg: String

    constructor(data: Any?, msg: String, code: String) {
        this.data = data
        this.code = code
        this.msg = msg
    }

    constructor(data: Any?, respCode: RespCode) {
        this.data = data
        this.code = respCode.getCode()
        msg = respCode.msg
    }

    constructor(cause: Throwable?, data: Any?, respCode: RespCode) : super(cause) {
        this.data = data
        this.code = respCode.getCode()
        msg = respCode.msg
    }
}

class ApiException : RuntimeException {
    var httpStatus: HttpStatus
    var msg: Any?

    constructor(httpStatus: HttpStatus, msg: Any?) {
        this.httpStatus = httpStatus
        this.msg = msg
    }

    constructor(message: String?, httpStatus: HttpStatus, msg: Any?) : super(message) {
        this.httpStatus = httpStatus
        this.msg = msg
    }

    constructor(message: String?, cause: Throwable?, httpStatus: HttpStatus, msg: Any?) : super(message, cause) {
        this.httpStatus = httpStatus
        this.msg = msg
    }

    override fun toString(): String {
        return "ApiException{" +
                "httpStatus=" + httpStatus +
                ", msg=" + msg +
                '}'
    }
}

/**
 * 全局异常处理类
 */
@ControllerAdvice
class GlobalExceptionHandler {

    private val log = KotlinLogging.logger {}

    class ParameterCheckResult {
        var paramCheckMap: JSONObject = JSONObject()
        fun putResult(field: String, message: String?) {
            paramCheckMap.put(field, message)
        }

        // tostring
        override fun toString(): String {
            return paramCheckMap.toString()
        }
    }

    // @Value("\${spring.servlet.multipart.max-file-size}")
    var uploadLimit: String? = null

    // region: error code handler
    @ExceptionHandler(ApiException::class)
    @ResponseBody
    fun errorCodeException(req: HttpServletResponse, ex: ApiException): ResponseEntity<*> {
        log.error("[{}] {}", ex.httpStatus, ex.msg)
        return turnBackendExceptionIntoJsonResult(req, ex)
    }

    /**
     * @param ex the exception
     * @return the json result
     */
    private fun turnBackendExceptionIntoJsonResult(req: HttpServletResponse, ex: ApiException): ResponseEntity<*> {
        return ResponseEntity(JSONObject.valueToString(ex.msg), ex.httpStatus)
    }

    // endregion
    // region: validation error
    @ExceptionHandler(
        BindException::class,
        MethodArgumentNotValidException::class
    )
    fun paramValidateException(ex: Exception?): ResponseEntity<*> {
        return try {
            val fieldValue:BindingResult = ReflectUtil.getFieldValue(ex!!, "bindingResult") as BindingResult
            val parameterCheckResult = extractValidationErrorEntries(fieldValue)
            log.error("Request parameter error:{}", parameterCheckResult)
            ResponseEntity(parameterCheckResult, HttpStatus.BAD_REQUEST)
        } catch (e: NoSuchFieldException) {
            ResponseEntity(Any(), HttpStatus.BAD_REQUEST)
        }
    }

    @Throws(NoSuchFieldException::class)
    fun <T> getFieldValue(obj: Any, fieldName: String?, fieldType: Class<T>): T? {
        var declaredField: Field? = null
        val targetClass: Class<*> = obj.javaClass
        declaredField = try {
            targetClass.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            throw e
        }
        val fieldActualClass = declaredField!!.type
        if (!fieldType.isAssignableFrom(fieldActualClass)) {
            log.error("指定的字段类型:{}与实际属性的类型：{}不匹配", fieldType, fieldActualClass)
            throw RuntimeException("指定的字段类型与实际属性的类型不匹配")
        }
        declaredField.isAccessible = true
        var targetFieldValue: T? = null
        targetFieldValue = try {
            declaredField[obj] as T
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
        return targetFieldValue
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationException(ex: ConstraintViolationException): ResponseEntity<*> {
        val constraintViolations = ex.constraintViolations
        val parameterCheckResult = ParameterCheckResult()
        for (constraintViolation in constraintViolations) {
            parameterCheckResult.putResult(
                getLastPathNode(constraintViolation.propertyPath),
                constraintViolation.message
            )
        }
        return ResponseEntity<Any?>(parameterCheckResult, HttpStatus.BAD_REQUEST)
    }

    private fun extractValidationErrorEntries(bindingResult: BindingResult): ParameterCheckResult {
        val parameterCheckResult = ParameterCheckResult()
        for (objectError in bindingResult.allErrors) {
            val fieldError = objectError as FieldError
            parameterCheckResult.putResult(fieldError.field, fieldError.defaultMessage)
        }
        return parameterCheckResult
    }

    // endregion
    // region: other exception
    @ExceptionHandler(Exception::class)
    fun otherException(req: HttpServletResponse, ex: Exception): ResponseEntity<*> {
        log.error(
            "Server Exception-Name:{}，Server Exception-Msg:{}",
            ex.javaClass.typeName,
            ex.message
        )
        if (ex is HttpMessageNotReadableException) {
            return turnBackendExceptionIntoJsonResult(
                req,
                ApiException(HttpStatus.BAD_REQUEST, "Request paramater is invalid")
            )
        }
        if (ex is MissingServletRequestParameterException) {
            return turnBackendExceptionIntoJsonResult(
                req,
                ApiException(HttpStatus.BAD_REQUEST, "Required request body is missing")
            )
        }
        if (ex is MaxUploadSizeExceededException) {
            return turnBackendExceptionIntoJsonResult(
                req, ApiException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Limitation: $uploadLimit"
                )
            )
        }
        if (ex is MissingServletRequestPartException) {
            return turnBackendExceptionIntoJsonResult(req, ApiException(HttpStatus.BAD_REQUEST, ex.message))
        }
        ex.printStackTrace()
        return turnBackendExceptionIntoJsonResult(req, ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.message))
    } // endregion

    companion object {
        private fun getLastPathNode(path: Path): String {
            val wholePath = path.toString()
            val i = wholePath.lastIndexOf(".")
            return if (i != -1) {
                wholePath.substring(i + 1)
            } else wholePath
        }
    }
}


