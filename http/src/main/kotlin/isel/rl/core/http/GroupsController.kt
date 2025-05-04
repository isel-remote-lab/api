package isel.rl.core.http

import isel.rl.core.domain.Uris
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/*
@RestController
data class GroupsController(
    private val groupsService: IGroupsService,
) {
    @PostMapping(Uris.Groups.CREATE)
    fun createGroup(
        @RequestBody input: GroupCreateInputModel,
    ): ResponseEntity<*> =
        when (val result = groupsService.createGroup(input)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse(
                        message = "Group created successfully",
                        data = mapOf("groupId" to result.value),
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

}

 */