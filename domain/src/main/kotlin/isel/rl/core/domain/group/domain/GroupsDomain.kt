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
    fun validateCreateGroup(
        groupName: String?,
        groupDescription: String?,
        createdAt: Instant,
        ownerId: Int,
    ): Group {
        val validatedGroupName =
            when {
                domainConfig.isGroupNameOptional && groupName.isNullOrBlank() -> GroupName()
                groupName.isNullOrBlank() -> throw ServicesExceptions.Groups.InvalidGroupName("Group name is required")
                else -> validateGroupName(groupName)
            }
        val validatedGroupDescription =
            when {
                domainConfig.isGroupDescriptionOptional && groupDescription.isNullOrBlank() -> GroupDescription()
                groupDescription.isNullOrBlank() -> throw ServicesExceptions.Groups.InvalidGroupDescription(
                    "Group description is required",
                )
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
            throw ServicesExceptions.Groups.InvalidGroupId
        }

    fun validateGroupName(groupName: String): GroupName =
        if (groupName.length in domainConfig.minLengthGroupName..domainConfig.maxLengthGroupName) {
            GroupName(groupName)
        } else {
            throw ServicesExceptions.Groups.InvalidGroupName(
                "Group name must be between ${domainConfig.minLengthGroupName} " +
                    "and ${domainConfig.maxLengthGroupName} characters",
            )
        }

    fun validateGroupDescription(groupDescription: String): GroupDescription =
        if (groupDescription.length in domainConfig.minLengthGroupDescription..domainConfig.maxLengthGroupDescription) {
            GroupDescription(groupDescription)
        } else {
            throw ServicesExceptions.Groups.InvalidGroupDescription(
                "Group description must be between ${domainConfig.minLengthGroupDescription} " +
                    "and ${domainConfig.maxLengthGroupDescription} characters",
            )
        }
}
