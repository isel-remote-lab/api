package isel.rl.core.domain.group.domain

import isel.rl.core.domain.config.GroupsDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component

@Component
data class GroupsDomain(
    val domainConfig: GroupsDomainConfig,
) {
    fun validateCreateGroup(
        groupName: String,
        groupDescription: String,
        createdAt: Instant,
        ownerId: Int,
    ): ValidatedCreateGroup =
        ValidatedCreateGroup(
            validateGroupName(groupName),
            validateGroupDescription(groupDescription),
            createdAt,
            ownerId,
        )

    fun validateGroupName(groupName: String): GroupName =
        if (groupName.length in domainConfig.minLengthGroupName..domainConfig.maxLengthGroupName) {
            GroupName(groupName)
        } else {
            throw ServicesExceptions.Groups.InvalidGroupName(
                "Group name must be between ${domainConfig.minLengthGroupName} " +
                    "and ${domainConfig.maxLengthGroupName} characters.",
            )
        }

    fun validateGroupDescription(groupDescription: String): GroupDescription =
        if (groupDescription.length in domainConfig.minLengthGroupDescription..domainConfig.maxLengthGroupDescription) {
            GroupDescription(groupDescription)
        } else {
            throw ServicesExceptions.Groups.InvalidGroupDescription(
                "Group description must be between ${domainConfig.minLengthGroupDescription} " +
                    "and ${domainConfig.maxLengthGroupDescription} characters.",
            )
        }
}
