package isel.rl.core.http.pipeline.interceptors

import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.pipeline.AuthenticatedUserArgumentResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val authorizationCookieProcessor: RequestTokenProcessor,
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

            if (cookie == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_COOKIE, RequestTokenProcessor.SCHEME)
                return false
            }

            // enforce authentication
            val authToken = cookie.value
            val user = authorizationCookieProcessor.processAuthorizationCookieValue(authToken)
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
            response.addHeader(NAME_WWW_AUTHENTICATE_COOKIE, RequestTokenProcessor.SCHEME)
            false
        } else {
            AuthenticatedUserArgumentResolver.addUserTo(user, request)
            true
        }
    }

    companion object {
        private const val NAME_WWW_AUTHENTICATE_COOKIE = "WWW-Authenticate"
    }
}
