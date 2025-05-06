package isel.rl.core.http

import org.springframework.web.bind.annotation.RestController

@RestController
class GroupsController {
/*
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
*/
}
