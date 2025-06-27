package isel.rl.core.http.pipeline.interceptors

import isel.rl.core.domain.Secrets
import isel.rl.core.http.annotations.RequireApiKey
import isel.rl.core.http.model.Problem
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ApiKeyInterceptor(
    private val secrets: Secrets,
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

            if (apikey == secrets.apiKey) {
                true
            } else {
                response.status = 403
                response.contentType = "application/problem+json"
                response.writer.println(
                    Problem.stringResponse(
                        "forbidden",
                        "Forbidden",
                        "You must provide a valid API key in the '$NAME_APIKEY_HEADER' header to access this resource.",
                    ),
                )
                false
            }
        } else {
            true
        }

    companion object {
        const val NAME_APIKEY_HEADER = "X-API-Key"
    }
}
