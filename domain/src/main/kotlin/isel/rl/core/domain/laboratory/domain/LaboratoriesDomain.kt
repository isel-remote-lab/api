package isel.rl.core.domain.laboratory.domain

import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.*
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Component
data class LaboratoriesDomain(
    private val domainConfig: LaboratoriesDomainConfig,
) {
    fun validateCreateLaboratory(
        labName: String,
        labDescription: String,
        labDuration: Int,
        labQueueLimit: Int,
        createdAt: Instant,
        ownerId: Int
    ): ValidatedLaboratory = ValidatedLaboratory(
        checkLaboratoryName(labName),
        checkLabDescription(labDescription),
        checkLabDuration(labDuration),
        checkLabQueueLimit(labQueueLimit),
        createdAt,
        ownerId
    )


    fun checkLaboratoryName(
        labName: String,
    ): LabName =
        if (labName.length in domainConfig.minLengthLabName..domainConfig.maxLengthLabName) {
            LabName(labName)
        } else {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryName(
                "Laboratory name must be between ${domainConfig.minLengthLabName} and ${domainConfig.maxLengthLabName} characters"
            )
        }

    fun checkLabDescription(
        labDescription: String,
    ): LabDescription =
        if (labDescription.length in domainConfig.minLengthLabDescription..domainConfig.maxLengthLabDescription) {
            LabDescription(labDescription)
        } else {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDescription(
                "Laboratory description must be between ${domainConfig.minLengthLabDescription} and ${domainConfig.maxLengthLabDescription} characters"
            )
        }

    fun checkLabDuration(
        labDuration: Int,
    ): LabDuration =
        if (labDuration in domainConfig.minLabDuration.inWholeMinutes..domainConfig.maxLabDuration.inWholeMinutes) {
            LabDuration(
                labDuration.toDuration(DurationUnit.MINUTES)
            )
        } else {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDuration(
                "Laboratory duration must be between ${domainConfig.minLabDuration.inWholeMinutes} and ${domainConfig.maxLabDuration.inWholeMinutes} minutes"
            )
        }

    fun checkLabQueueLimit(
        labQueueLimit: Int,
    ): LabQueueLimit =
        if (labQueueLimit in domainConfig.minLabQueueLimit..domainConfig.maxLabQueueLimit) {
            LabQueueLimit(labQueueLimit)
        } else {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit(
                "Laboratory queue limit must be between ${domainConfig.minLabQueueLimit} and ${domainConfig.maxLabQueueLimit}"
            )
        }
}
