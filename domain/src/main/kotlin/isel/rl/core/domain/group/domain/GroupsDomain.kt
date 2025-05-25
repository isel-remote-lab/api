package isel.rl.core.domain.group.domain

import isel.rl.core.domain.config.GroupsDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component

@Component
data class GroupsDomain(
    private val domainConfig: GroupsDomainConfig,
) {
    final val groupNameRequiredMsg = "Group name is required"
    final val groupDescriptionRequiredMsg = "Group description is required"
    final val invalidGroupNameLengthMsg =
        "Group name must be between ${domainConfig.minLengthGroupName} " +
            "and ${domainConfig.maxLengthGroupName} characters"
    final val invalidGroupDescriptionLengthMsg =
        "Group description must be between ${domainConfig.minLengthGroupDescription} " +
            "and ${domainConfig.maxLengthGroupDescription} characters"

    val invalidGroupId = ServicesExceptions.Groups.InvalidGroupId

    val requiredGroupName =
        ServicesExceptions.Groups.InvalidGroupName(
            groupNameRequiredMsg,
        )
    val invalidGroupNameLength =
        ServicesExceptions.Groups.InvalidGroupName(
            invalidGroupNameLengthMsg,
        )
    val requiredGroupDescription =
        ServicesExceptions.Groups.InvalidGroupDescription(
            groupDescriptionRequiredMsg,
        )
    val invalidGroupDescriptionLength =
        ServicesExceptions.Groups.InvalidGroupDescription(
            invalidGroupDescriptionLengthMsg,
        )
    val groupNotFound = ServicesExceptions.Groups.GroupNotFound

    val userAlreadyInGroup = ServicesExceptions.Groups.UserAlreadyInGroup

    val userNotInGroup = ServicesExceptions.Groups.UserNotInGroup

    fun validateCreateGroup(
        groupName: String?,
        groupDescription: String?,
        createdAt: Instant,
        ownerId: Int,
    ): Group {
        val validatedGroupName =
            when {
                domainConfig.isGroupNameOptional && groupName.isNullOrBlank() -> GroupName()
                groupName.isNullOrBlank() -> throw requiredGroupName
                else -> validateGroupName(groupName)
            }
        val validatedGroupDescription =
            when {
                domainConfig.isGroupDescriptionOptional && groupDescription.isNullOrBlank() -> GroupDescription()
                groupDescription.isNullOrBlank() -> throw requiredGroupDescription

                else -> validateGroupDescription(groupDescription)
            }

        return Group(
            groupName = validatedGroupName,
            groupDescription = validatedGroupDescription,
            createdAt = createdAt,
            ownerId = ownerId,
        )
    }

    fun validateGroupId(groupId: String): Int =
        try {
            groupId.toInt()
        } catch (e: Exception) {
            throw invalidGroupId
        }

    fun validateGroupName(groupName: String): GroupName =
        if (groupName.length in domainConfig.minLengthGroupName..domainConfig.maxLengthGroupName) {
            GroupName(groupName)
        } else {
            throw invalidGroupNameLength
        }

    fun validateGroupDescription(groupDescription: String): GroupDescription =
        if (groupDescription.length in domainConfig.minLengthGroupDescription..domainConfig.maxLengthGroupDescription) {
            GroupDescription(groupDescription)
        } else {
            throw invalidGroupDescriptionLength
        }
}
