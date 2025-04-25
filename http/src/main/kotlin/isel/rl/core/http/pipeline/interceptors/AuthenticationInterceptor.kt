package isel.rl.core.http.pipeline.interceptors

import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.pipeline.AuthenticatedUserArgumentResolver
import isel.rl.core.http.pipeline.RequestJWTProcessor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val jwtProcessor: RequestJWTProcessor,
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
            val cookie = request.cookies?.find { it.name == "jwt-token" }

            if (cookie == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_COOKIE, SCHEME)
                return false
            }

            // enforce authentication
            val user = jwtProcessor.processAuthorizationCookieValue(cookie.value)
            return isUserAuthenticated(user, request, response)
        }

        return true
    }

    private fun isUserAuthenticated(
        user: AuthenticatedUser?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Boolean {
        return if (user == null) {
            response.status = 401
            response.addHeader(NAME_WWW_AUTHENTICATE_COOKIE, SCHEME)
            false
        } else {
            AuthenticatedUserArgumentResolver.addUserTo(user, request)
            true
        }
    }

    companion object {
        const val SCHEME = "Bearer"
        private const val NAME_WWW_AUTHENTICATE_COOKIE = "WWW-Authenticate"
    }
}