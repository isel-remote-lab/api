package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.config.DomainConfig
import isel.rl.core.http.annotations.RequireApiKey
import isel.rl.core.http.model.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DomainController(
    private val domainConfig: DomainConfig,
) {
    @RequireApiKey
    @GetMapping(Uris.Private.GET_DOMAIN)
    fun getDomainConf(): ResponseEntity<*> =
        ResponseEntity.ok(
            SuccessResponse(
                message = "Domain configuration retrieved successfully",
                data = domainConfig,
            ),
        )
}
