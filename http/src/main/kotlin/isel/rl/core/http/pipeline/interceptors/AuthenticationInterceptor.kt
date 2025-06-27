package isel.rl.core.http.pipeline.interceptors

import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.pipeline.AuthenticatedUserArgumentResolver
import isel.rl.core.http.pipeline.RequestTokenProcessor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Component
class AuthenticationInterceptor(
    private val authorizationTokenProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod &&
            handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            // check for the presence of the cookie in the request
            val cookie = request.cookies?.find { it.name == "token" }

            val decodedCookieValue =
                cookie?.value?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.name())
                }

            // check for the presence of the token in the authorization header
            val token = request.getHeader(NAME_AUTHORIZATION_HEADER)

            if (cookie == null && token == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                response.contentType = "application/problem+json"
                response.writer.println(
                    Problem.stringResponse(
                        "unauthorized",
                        "Unauthorized",
                        "You must provide a valid token in the 'Authorization' header or a 'token' cookie to access this resource.",
                    ),
                )
                return false
            }

            // enforce authentication
            val authToken = decodedCookieValue ?: token
            val user = authorizationTokenProcessor.processAuthorizationValue(authToken, cookie != null)
            return isUserAuthenticated(user, request, response)
        }

        return true
    }

    private fun isUserAuthenticated(
        user: AuthenticatedUser?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        return if (user == null) {
            response.status = 401
            response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
            response.contentType = "application/problem+json"
            response.writer.println(
                Problem.stringResponse(
                    "unauthorized",
                    "Unauthorized",
                    "You must provide a valid token in the 'Authorization' header or a 'token' cookie to access this resource.",
                ),
            )
            false
        } else {
            AuthenticatedUserArgumentResolver.addUserTo(user, request)
            true
        }
    }

    companion object {
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
