package isel.rl.core.http.pipeline.filters

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LoggingFilter(
    private val clock: Clock,
) : Filter {
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain,
    ) {
        val req = request as HttpServletRequest
        val resp = response as HttpServletResponse
        val startTime = clock.now()
        val requestId = generateRequestId()

        logRequest(req, requestId)

        try {
            chain.doFilter(request, response)
        } finally {
            val duration = clock.now() - startTime
            logResponse(resp, requestId, duration.inWholeMilliseconds)
        }
    }

    private fun logRequest(
        request: HttpServletRequest,
        requestId: String,
    ) {
        val headers = getImportantHeaders(request)
        val userAgent = request.getHeader("User-Agent") ?: "Unknown"
        val contentLength = request.contentLengthLong.takeIf { it >= 0 } ?: "Unknown"

        LOG.info(
            "REQUEST [{}] | {} {} | Remote: {}:{} | Content-Type: {} | Content-Length: {} | User-Agent: {} | Headers: {}",
            requestId,
            request.method,
            request.requestURI + if (request.queryString != null) "?${request.queryString}" else "",
            request.remoteAddr,
            request.remotePort,
            request.contentType ?: "None",
            contentLength,
            userAgent,
            headers,
        )
    }

    private fun logResponse(
        response: HttpServletResponse,
        requestId: String,
        durationMs: Long,
    ) {
        LOG.info(
            "RESPONSE [{}] | Status: {} | Duration: {}ms | Content-Type: {}",
            requestId,
            response.status,
            durationMs,
            response.contentType ?: "None",
        )
    }

    private fun getImportantHeaders(request: HttpServletRequest): Map<String, String> {
        val importantHeaders =
            setOf(
                "Authorization",
                "X-Forwarded-For",
                "X-Real-IP",
                "Accept",
                "Accept-Language",
                "Accept-Encoding",
            )

        return importantHeaders.mapNotNull { headerName ->
            request.getHeader(headerName)?.let { headerName to it }
        }.toMap()
    }

    private fun generateRequestId(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LoggingFilter::class.java)
    }
}
