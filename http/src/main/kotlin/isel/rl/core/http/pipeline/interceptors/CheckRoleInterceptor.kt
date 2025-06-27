package isel.rl.core.http.pipeline.interceptors

import isel.rl.core.domain.user.props.Role
import isel.rl.core.http.annotations.RequireRole
import isel.rl.core.http.model.Problem
import isel.rl.core.http.pipeline.AuthenticatedUserArgumentResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class CheckRoleInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean =
        if (handler is HandlerMethod &&
            handler.method.getAnnotation(RequireRole::class.java) != null
        ) {
            // Get the required role from the annotation
            val requiredRole = handler.method.getAnnotation(RequireRole::class.java)!!.role

            // Get the authenticated user from the request
            val user = AuthenticatedUserArgumentResolver.getUserFrom(request)!!

            // Check if the user has the required role
            if (Role.checkHierarchyPermission(user.user.role, requiredRole)) {
                true
            } else {
                response.status = 403
                response.contentType = "application/problem+json"
                response.writer.println(
                    Problem.stringResponse(
                        "forbidden",
                        "Forbidden",
                        "You do not have the required role to access this resource.",
                    ),
                )
                false
            }
        } else {
            true
        }
}
