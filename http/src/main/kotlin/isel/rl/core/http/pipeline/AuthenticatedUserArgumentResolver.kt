package isel.rl.core.http.pipeline

import isel.rl.core.http.model.user.AuthenticatedUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer


@Component
class AuthenticatedUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == AuthenticatedUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException()
        return getUserFrom(request) ?: throw IllegalStateException()
    }

    companion object {
        private const val KEY = "AuthenticatedUserArgumentResolver"

        fun addUserTo(
            user: AuthenticatedUser,
            request: HttpServletRequest,
        ) {
            return request.setAttribute(KEY, user)
        }

        fun getUserFrom(request: HttpServletRequest): AuthenticatedUser? {
            return request.getAttribute(KEY)?.let {
                it as? AuthenticatedUser
            }
        }
    }
}