package isel.rl.core.domain.laboratory.domain

import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import kotlin.time.toDuration

@Component
data class LaboratoriesDomain(
    private val domainConfig: LaboratoriesDomainConfig,
) {
    val isLabNameOptional = domainConfig.isLabNameOptional

    val isLabDescriptionOptional = domainConfig.isLabDescriptionOptional

    val isLabDurationOptional = domainConfig.isLabDurationOptional

    val isLabQueueLimitOptional = domainConfig.isLabQueueLimitOptional

    fun validateCreateLaboratory(
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        createdAt: Instant,
        ownerId: Int,
    ): Laboratory {
        val validatedLabName =
            when {
                isLabNameOptional && labName.isNullOrBlank() -> LabName()
                labName.isNullOrBlank() -> throw ServicesExceptions.Laboratories.InvalidLaboratoryName(
                    "Laboratory name is required",
                )

                else -> validateLaboratoryName(labName)
            }

        val validatedLabDescription =
            when {
                isLabDescriptionOptional && labDescription.isNullOrBlank() -> LabDescription()
                labDescription.isNullOrBlank() -> throw ServicesExceptions.Laboratories.InvalidLaboratoryDescription(
                    "Laboratory description is required",
                )

                else -> validateLabDescription(labDescription)
            }

        val validatedLabDuration =
            when {
                isLabDurationOptional && labDuration == null -> LabDuration()
                labDuration == null -> throw ServicesExceptions.Laboratories.InvalidLaboratoryDuration(
                    "Laboratory duration is required",
                )

                else -> validateLabDuration(labDuration)
            }

        val validatedLabQueueLimit =
            when {
                isLabQueueLimitOptional && labQueueLimit == null -> LabQueueLimit()
                labQueueLimit == null -> throw ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit(
                    "Laboratory queue limit is required",
                )

                else -> validateLabQueueLimit(labQueueLimit)
            }

        return Laboratory(
            name = validatedLabName,
            description = validatedLabDescription,
            duration = validatedLabDuration,
            queueLimit = validatedLabQueueLimit,
            createdAt = createdAt,
            ownerId = ownerId,
        )
    }

    fun validateLaboratoryId(labId: String): Int =
        try {
            labId.toInt()
        } catch (e: Exception) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryId
        }

    fun validateLaboratoryName(labName: String): LabName {
        if (labName.length !in domainConfig.minLabNameLength..domainConfig.maxLabNameLength) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryName(
                "Laboratory name must be between ${domainConfig.minLabNameLength} and " +
                    "${domainConfig.maxLabNameLength} characters",
            )
        }
        return LabName(labName)
    }

    fun validateLabDescription(labDescription: String): LabDescription {
        if (labDescription.length !in domainConfig.minLabDescriptionLength..domainConfig.maxLabDescriptionLength) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDescription(
                "Laboratory description must be between ${domainConfig.minLabDescriptionLength} and " +
                    "${domainConfig.maxLabDescriptionLength} characters",
            )
        }
        return LabDescription(labDescription)
    }

    fun validateLabDuration(labDuration: Int): LabDuration {
        val duration = labDuration.toDuration(domainConfig.labDurationUnit)
        if (duration !in domainConfig.minLabDuration..domainConfig.maxLabDuration) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDuration(
                "Laboratory duration must be between ${domainConfig.minLabDuration.toInt(domainConfig.labDurationUnit)} and " +
                    "${domainConfig.maxLabDuration.toInt(domainConfig.labDurationUnit)} minutes",
            )
        }
        return LabDuration(duration)
    }

    fun validateLabQueueLimit(labQueueLimit: Int): LabQueueLimit {
        if (labQueueLimit !in domainConfig.minLabQueueLimit..domainConfig.maxLabQueueLimit) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit(
                "Laboratory queue limit must be between ${domainConfig.minLabQueueLimit} and " +
                    "${domainConfig.maxLabQueueLimit}",
            )
        }
        return LabQueueLimit(labQueueLimit)
    }
}
