package isel.rl.core.domain.group.domain

import isel.rl.core.domain.config.GroupsDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component

/**
 * GroupsDomain is a [Component] that encapsulates the domain logic for managing groups.
 * It provides methods to validate group creation, group IDs, group names, and group descriptions.
 * It also defines various exceptions related to group operations.
 *
 * @param domainConfig The configuration for group domain, which includes validation rules.
 */
@Component
data class GroupsDomain(
    private val domainConfig: GroupsDomainConfig,
) {
    /*
     * Constants and exceptions related to group operations.
     */
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

    /**
     * Validates the creation of a group with the provided parameters.
     *
     * @param groupName The name of the group, which can be optional based on configuration.
     * @param groupDescription The description of the group, which can also be optional.
     * @param createdAt The timestamp when the group is created.
     * @param ownerId The ID of the user who owns the group.
     * @return A validated [Group] object.
     * @throws ServicesExceptions.Groups.InvalidGroupName if the group name is invalid.
     * @throws ServicesExceptions.Groups.InvalidGroupDescription if the group description is invalid.
     */
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
            name = validatedGroupName,
            description = validatedGroupDescription,
            createdAt = createdAt,
            ownerId = ownerId,
        )
    }

    /**
     * Validates the group ID by attempting to convert it to an integer.
     *
     * @param groupId The group ID as a string.
     * @return The validated group ID as an integer.
     * @throws ServicesExceptions.Groups.InvalidGroupId if the group ID is not a valid integer.
     */
    fun validateGroupId(groupId: String): Int =
        try {
            groupId.toInt()
        } catch (e: Exception) {
            throw invalidGroupId
        }

    /**
     * Validates the group name based on the configured length constraints.
     *
     * @param groupName The name of the group to validate.
     * @return A [GroupName] object if the validation passes.
     * @throws ServicesExceptions.Groups.InvalidGroupName if the group name does not meet the length requirements.
     */
    fun validateGroupName(groupName: String): GroupName =
        if (groupName.length in domainConfig.minLengthGroupName..domainConfig.maxLengthGroupName) {
            GroupName(groupName)
        } else {
            throw invalidGroupNameLength
        }

    /**
     * Validates the group description based on the configured length constraints.
     *
     * @param groupDescription The description of the group to validate.
     * @return A [GroupDescription] object if the validation passes.
     * @throws ServicesExceptions.Groups.InvalidGroupDescription if the group description does not meet the length requirements.
     */
    fun validateGroupDescription(groupDescription: String): GroupDescription =
        if (groupDescription.length in domainConfig.minLengthGroupDescription..domainConfig.maxLengthGroupDescription) {
            GroupDescription(groupDescription)
        } else {
            throw invalidGroupDescriptionLength
        }
}
