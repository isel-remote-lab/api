package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.laboratory.LaboratoryCreateInputModel
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
data class LaboratoriesController(
    private val laboratoriesService: ILaboratoriesService,
) {
    @PostMapping(Uris.Laboratories.CREATE)
    fun createLaboratory(
        @RequestBody input: LaboratoryCreateInputModel,
    ): ResponseEntity<*> =
        when (val result = laboratoriesService.createLaboratory(
            input.labName,
            input.labDescription,
            input.labDuration,
            input.labQueue,
            input.ownerId,
        )) {
            is Success -> {
                ResponseEntity.status(201).body(result.value)
            }

            is Failure -> {
                when (result.value) {
                    else -> Problem.response(500, Problem.unexpectedBehaviour)
                }
            }

        }
}