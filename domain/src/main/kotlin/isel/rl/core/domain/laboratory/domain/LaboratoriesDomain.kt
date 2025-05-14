package isel.rl.core.domain.laboratory.domain

import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
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
    fun validateCreateLaboratory(
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        createdAt: Instant,
        ownerId: Int,
    ): ValidatedCreateLaboratory =
        ValidatedCreateLaboratory(
            checkLaboratoryName(labName),
            checkLabDescription(labDescription),
            checkLabDuration(labDuration),
            checkLabQueueLimit(labQueueLimit),
            createdAt,
            ownerId,
        )

    fun validateUpdateLaboratory(
        labId: Int,
        labName: String? = null,
        labDescription: String? = null,
        labDuration: Int? = null,
        labQueueLimit: Int? = null,
    ): ValidatedUpdateLaboratory =
        ValidatedUpdateLaboratory(
            labId,
            labName?.let { checkLaboratoryName(labName) },
            labDescription?.let { checkLabDescription(labDescription) },
            labDuration?.let { checkLabDuration(labDuration) },
            labQueueLimit?.let { checkLabQueueLimit(labQueueLimit) },
        )

    fun validateLaboratoryId(labId: String): Int =
        try {
            labId.toInt()
        } catch (e: Exception) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryId
        }

    fun checkLaboratoryName(labName: String?): LabName {
        // Check if labName is optional and if it is null. If both are true,
        // return an LabName with null meaning that the labName is not required.
        if (labName.isNullOrBlank()) {
            if (domainConfig.isLabNameOptional) return LabName()
            throw ServicesExceptions.Laboratories.InvalidLaboratoryName(
                "Laboratory name must be between ${domainConfig.minLabNameLength} and " +
                    "${domainConfig.maxLabNameLength} characters",
            )
        }
        if (labName.length !in domainConfig.minLabNameLength..domainConfig.maxLabNameLength) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryName(
                "Laboratory name must be between ${domainConfig.minLabNameLength} and " +
                    "${domainConfig.maxLabNameLength} characters",
            )
        }
        return LabName(labName)
    }

    fun checkLabDescription(labDescription: String?): LabDescription {
        // Check if labDescription is optional and if it is blank. If both are true,
        // return an empty LabDescription meaning that the labDescription is not required.
        if (labDescription.isNullOrBlank()) {
            if (domainConfig.isLabDescriptionOptional) return LabDescription()
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDescription(
                "Laboratory description must be between ${domainConfig.minLabDescriptionLength} and " +
                    "${domainConfig.maxLabDescriptionLength} characters",
            )
        }
        if (labDescription.length !in domainConfig.minLabDescriptionLength..domainConfig.maxLabDescriptionLength) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDescription(
                "Laboratory description must be between ${domainConfig.minLabDescriptionLength} and " +
                    "${domainConfig.maxLabDescriptionLength} characters",
            )
        }
        return LabDescription(labDescription)
    }

    fun checkLabDuration(labDuration: Int?): LabDuration {
        if (labDuration == null) {
            if (domainConfig.isLabDurationOptional) return LabDuration()
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDuration(
                "Laboratory duration must be between ${domainConfig.minLabDuration} and " +
                    "${domainConfig.maxLabDuration} minutes",
            )
        }
        val duration = labDuration.toDuration(domainConfig.labDurationUnit)
        if (duration !in domainConfig.minLabDuration..domainConfig.maxLabDuration) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryDuration(
                "Laboratory duration must be between ${domainConfig.minLabDuration.toInt(domainConfig.labDurationUnit)} and " +
                    "${domainConfig.maxLabDuration.toInt(domainConfig.labDurationUnit)} minutes",
            )
        }
        return LabDuration(duration)
    }

    fun checkLabQueueLimit(labQueueLimit: Int?): LabQueueLimit {
        if (labQueueLimit == null) {
            if (domainConfig.isLabQueueLimitOptional) return LabQueueLimit()
            throw ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit(
                "Laboratory queue limit must be between ${domainConfig.minLabQueueLimit} and " +
                    "${domainConfig.maxLabQueueLimit}",
            )
        }
        if (labQueueLimit !in domainConfig.minLabQueueLimit..domainConfig.maxLabQueueLimit) {
            throw ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit(
                "Laboratory queue limit must be between ${domainConfig.minLabQueueLimit} and " +
                    "${domainConfig.maxLabQueueLimit}",
            )
        }
        return LabQueueLimit(labQueueLimit)
    }
}
