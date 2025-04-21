package isel.rl.core.http.pipeline

import isel.rl.core.domain.ApiKey
import isel.rl.core.http.RequireApiKey
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ApiKeyInterceptor(
    private val apiKey: ApiKey,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean =
        if (handler is HandlerMethod &&
            handler.method.getAnnotation(RequireApiKey::class.java) != null
        ) {
            val apikey = request.getHeader(NAME_APIKEY_HEADER)

            if (apikey == apiKey.apiKeyInfo) {
                true
            } else {
                response.status = 403
                response.writer.write("Unauthorized")
                false
            }
        } else {
            true
        }

    companion object {
        const val NAME_APIKEY_HEADER = "X-API-Key"
    }
}