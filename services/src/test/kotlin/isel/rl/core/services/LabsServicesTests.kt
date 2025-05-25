package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.services.utils.GroupsServicesUtils
import isel.rl.core.services.utils.LabsServicesUtils
import isel.rl.core.services.utils.UsersServicesUtils
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class LabsServicesTests {
    @Nested
    inner class CreateLaboratory {
        @Test
        fun `create laboratory`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            LabsServicesUtils.createLab(
                labsServices,
            )
        }

        @Test
        fun `create laboratory with invalid lab name (null)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab name
            if (LabsServicesUtils.isLabNameOptional) {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            name = null,
                        ),
                )
            } else {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            name = null,
                        ),
                    expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryName::class,
                )
            }
        }

        @Test
        fun `create laboratory with invalid lab name (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab name
            if (LabsServicesUtils.isLabNameOptional) {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            name = LabsServicesUtils.newTestInvalidLabNameMin(),
                        ),
                )
            } else {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            name = LabsServicesUtils.newTestInvalidLabNameMin(),
                        ),
                    expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryName::class,
                )
            }
        }

        @Test
        fun `create laboratory with invalid lab name (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab name
            LabsServicesUtils.createLab(
                labsServices,
                initialLab =
                    LabsServicesUtils.InitialLab(
                        name = LabsServicesUtils.newTestInvalidLabNameMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryName::class,
            )
        }

        @Test
        fun `create laboratory with invalid lab description (null)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab description
            if (LabsServicesUtils.isLabDescriptionOptional) {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            description = null,
                        ),
                )
            } else {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            description = null,
                        ),
                    expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDescription::class,
                )
            }
        }

        @Test
        fun `create laboratory with invalid lab description (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab description
            if (LabsServicesUtils.isLabDescriptionOptional) {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            description = LabsServicesUtils.newTestInvalidLabDescriptionMin(),
                        ),
                )
            } else {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            description = LabsServicesUtils.newTestInvalidLabDescriptionMin(),
                        ),
                    expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDescription::class,
                )
            }
        }

        @Test
        fun `create laboratory with invalid lab description (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab description
            LabsServicesUtils.createLab(
                labsServices,
                initialLab =
                    LabsServicesUtils.InitialLab(
                        description = LabsServicesUtils.newTestInvalidLabDescriptionMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDescription::class,
            )
        }

        @Test
        fun `create laboratory with invalid lab duration (null)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab duration
            if (LabsServicesUtils.isLabDurationOptional) {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            duration = null,
                        ),
                )
            } else {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            duration = null,
                        ),
                    expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDuration::class,
                )
            }
        }

        @Test
        fun `create laboratory with invalid lab duration (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab duration
            LabsServicesUtils.createLab(
                labsServices,
                initialLab =
                    LabsServicesUtils.InitialLab(
                        duration = LabsServicesUtils.newTestInvalidLabDurationMin(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDuration::class,
            )
        }

        @Test
        fun `create laboratory with invalid lab duration (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab duration
            LabsServicesUtils.createLab(
                labsServices,
                initialLab =
                    LabsServicesUtils.InitialLab(
                        duration = LabsServicesUtils.newTestInvalidLabDurationMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDuration::class,
            )
        }

        @Test
        fun `create laboratory with invalid lab queue limit (null)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab queue limit
            if (LabsServicesUtils.isLabQueueLimitOptional) {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            queueLimit = null,
                        ),
                )
            } else {
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab =
                        LabsServicesUtils.InitialLab(
                            queueLimit = null,
                        ),
                    expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit::class,
                )
            }
        }

        @Test
        fun `create laboratory with invalid lab queue limit (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab queue limit
            LabsServicesUtils.createLab(
                labsServices,
                initialLab =
                    LabsServicesUtils.InitialLab(
                        queueLimit = LabsServicesUtils.newTestInvalidLabQueueLimitMin(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit::class,
            )
        }

        @Test
        fun `create laboratory with invalid lab queue limit (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory with invalid lab queue limit
            LabsServicesUtils.createLab(
                labsServices,
                initialLab =
                    LabsServicesUtils.InitialLab(
                        queueLimit = LabsServicesUtils.newTestInvalidLabQueueLimitMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit::class,
            )
        }
    }

    @Nested
    inner class LaboratoryRetrieval {
        @Test
        fun `get laboratory by id`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: creating a lab
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: getting the lab by ID
            LabsServicesUtils.getLabById(labsServices, lab.ownerId, lab)
        }

        @Test
        fun `get laboratory by id with invalid lab id (not Int)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting a laboratory by ID with invalid ID
            LabsServicesUtils.getLabById(
                labsServices,
                "invalid_id",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryId::class,
            )
        }

        @Test
        fun `get laboratory by id with invalid lab id (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting a laboratory by ID with invalid ID
            LabsServicesUtils.getLabById(
                labsServices,
                "-1",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `get non existent lab by id`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting a laboratory by ID with invalid ID
            LabsServicesUtils.getLabById(
                labsServices,
                "99999",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `get laboratory by id with a user that do not belong to the lab`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: creating a user that does not belong to the lab
            val user = UsersServicesUtils.loginUser(usersServices)

            // When: Getting a laboratory by ID with a user that does not belong to the lab
            LabsServicesUtils.getLabById(
                labsServices,
                lab.id.toString(),
                user.id,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `get user laboratories`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(usersServices)

            // When: Creating two laboratories
            val lab1 =
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab = LabsServicesUtils.InitialLab(ownerId = user.id),
                )

            val lab2 =
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab = LabsServicesUtils.InitialLab(ownerId = user.id),
                )

            // When: Getting all laboratories by user ID
            LabsServicesUtils.getUserLabs(
                labsServices,
                user.id,
                expectedLabs = listOf(lab1, lab2),
            )
        }

        @Test
        fun `get user laboratories with limit and skip`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(usersServices)

            // When: Creating three laboratories
            val lab1 =
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab = LabsServicesUtils.InitialLab(ownerId = user.id),
                )

            val lab2 =
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab = LabsServicesUtils.InitialLab(ownerId = user.id),
                )

            val lab3 =
                LabsServicesUtils.createLab(
                    labsServices,
                    initialLab = LabsServicesUtils.InitialLab(ownerId = user.id),
                )

            // When: Getting all laboratories by user ID with limit and skip
            LabsServicesUtils.getUserLabs(
                labsServices,
                user.id,
                limit = "2",
                skip = "1",
                expectedLabs = listOf(lab2, lab3),
            )
        }

        @Test
        fun `get user laboratories with invalid limit (not a number)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting all laboratories by user ID with invalid limit and skip
            LabsServicesUtils.getUserLabs(
                labsServices,
                1,
                limit = "invalid",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get user laboratories with invalid limit (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting all laboratories by user ID with invalid limit and skip
            LabsServicesUtils.getUserLabs(
                labsServices,
                1,
                limit = "-1",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get user laboratories with invalid skip (not a number)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting all laboratories by user ID with invalid limit and skip
            LabsServicesUtils.getUserLabs(
                labsServices,
                1,
                skip = "invalid",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get user laboratories with invalid skip (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Getting all laboratories by user ID with invalid limit and skip
            LabsServicesUtils.getUserLabs(
                labsServices,
                1,
                skip = "-1",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }
    }

    @Nested
    inner class UpdateLaboratory {
        @Test
        fun `update laboratory`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory
            LabsServicesUtils.updateLab(
                labsServices,
                lab,
            )
        }

        @Test
        fun `update laboratory with invalid lab id (not Int)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Updating a laboratory with invalid ID
            LabsServicesUtils.updateLab(
                labsServices,
                labId = "invalid_id",
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryId::class,
            )
        }

        @Test
        fun `update laboratory with invalid lab id (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Updating a laboratory with invalid ID
            LabsServicesUtils.updateLab(
                labsServices,
                labId = "-1",
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `update non existent lab by id`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Updating the laboratory with invalid ID
            LabsServicesUtils.updateLab(
                labsServices,
                labId = "99999",
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `update laboratory with a user that do not own the lab`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: creating a user that does not own the lab
            val user = UsersServicesUtils.loginUser(usersServices)

            // When: Updating the laboratory with a user that does not own the lab
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab = LabsServicesUtils.InitialLab(ownerId = user.id),
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `update laboratory (null lab name)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab name
            // It is expected to succeed. Null name means no change.
            LabsServicesUtils.updateLab(
                labsServices,
                lab,
                expectedLab =
                    LabsServicesUtils.InitialLab(
                        id = lab.id,
                        name = null,
                        createdAt = lab.createdAt,
                        ownerId = lab.ownerId,
                    ),
            )
        }

        @Test
        fun `update laboratory with invalid lab name (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab name
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        name = LabsServicesUtils.newTestInvalidLabNameMin(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryName::class,
            )
        }

        @Test
        fun `update laboratory with invalid lab name (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab name
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        name = LabsServicesUtils.newTestInvalidLabNameMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryName::class,
            )
        }

        @Test
        fun `update laboratory (null lab description)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab description
            // It is expected to succeed. Null description means no change.
            LabsServicesUtils.updateLab(
                labsServices,
                lab,
                expectedLab =
                    LabsServicesUtils.InitialLab(
                        id = lab.id,
                        description = null,
                        createdAt = lab.createdAt,
                        ownerId = lab.ownerId,
                    ),
            )
        }

        @Test
        fun `update laboratory with invalid lab description (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab description
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        description = LabsServicesUtils.newTestInvalidLabDescriptionMin(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDescription::class,
            )
        }

        @Test
        fun `update laboratory with invalid lab description (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab description
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        description = LabsServicesUtils.newTestInvalidLabDescriptionMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDescription::class,
            )
        }

        @Test
        fun `update laboratory (null lab duration)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab duration
            // It is expected to succeed. Null duration means no change.
            LabsServicesUtils.updateLab(
                labsServices,
                lab,
                expectedLab =
                    LabsServicesUtils.InitialLab(
                        id = lab.id,
                        duration = null,
                        createdAt = lab.createdAt,
                        ownerId = lab.ownerId,
                    ),
            )
        }

        @Test
        fun `update laboratory with invalid lab duration (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab duration
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        duration = LabsServicesUtils.newTestInvalidLabDurationMin(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDuration::class,
            )
        }

        @Test
        fun `update laboratory with invalid lab duration (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab duration
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        duration = LabsServicesUtils.newTestInvalidLabDurationMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryDuration::class,
            )
        }

        @Test
        fun `update laboratory with invalid lab queue limit (null lab queue limit)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab queue limit
            // It is expected to succeed. Null queue limit means no change.
            LabsServicesUtils.updateLab(
                labsServices,
                lab,
                expectedLab =
                    LabsServicesUtils.InitialLab(
                        id = lab.id,
                        queueLimit = null,
                        createdAt = lab.createdAt,
                        ownerId = lab.ownerId,
                    ),
            )
        }

        @Test
        fun `update laboratory with invalid lab queue limit (min)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab queue limit
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        queueLimit = LabsServicesUtils.newTestInvalidLabQueueLimitMin(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit::class,
            )
        }

        @Test
        fun `update laboratory with invalid lab queue limit (max)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Updating a laboratory with invalid lab queue limit
            LabsServicesUtils.updateLab(
                labsServices,
                lab.id.toString(),
                updateLab =
                    LabsServicesUtils.InitialLab(
                        queueLimit = LabsServicesUtils.newTestInvalidLabQueueLimitMax(),
                    ),
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit::class,
            )
        }
    }

    @Nested
    inner class GroupAndLaboratoryAssociation {
        @Test
        fun `add group to laboratory`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val groupsServices = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupsServices,
                )

            // When: Adding the group to the laboratory
            LabsServicesUtils.addGroupToLab(
                labsServices,
                lab.id.toString(),
                group.id.toString(),
                lab.ownerId,
            )
        }

        @Test
        fun `add group to laboratory with invalid lab id (not Int)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Adding a group to a laboratory with invalid ID
            LabsServicesUtils.addGroupToLab(
                labsServices,
                "invalid_id",
                "1",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryId::class,
            )
        }

        @Test
        fun `add group to laboratory with invalid lab id (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Adding a group to a laboratory with invalid ID
            LabsServicesUtils.addGroupToLab(
                labsServices,
                "-1",
                "1",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `add group to non existent lab by id`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Adding a group to the laboratory with invalid ID
            LabsServicesUtils.addGroupToLab(
                labsServices,
                "99999",
                "1",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `add group to laboratory with a user that do not own the lab`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: creating a user that does not own the lab
            val user = UsersServicesUtils.loginUser(usersServices)

            // When: Adding a group to the laboratory with a user that does not own the lab
            LabsServicesUtils.addGroupToLab(
                labsServices,
                lab.id.toString(),
                "1",
                user.id,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `add group to laboratory with invalid group id (not Int)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Adding a group to a laboratory with invalid group ID
            LabsServicesUtils.addGroupToLab(
                labsServices,
                "1",
                "invalid_group_id",
                1,
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupId::class,
            )
        }

        @Test
        fun `add group to laboratory with invalid group id (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Adding a group to a laboratory with invalid group ID
            LabsServicesUtils.addGroupToLab(
                labsServices,
                "1",
                "-1",
                1,
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `add non existent group to laboratory`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Adding a group to the laboratory with invalid group ID
            LabsServicesUtils.addGroupToLab(
                labsServices,
                "1",
                "99999",
                1,
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }
    }

    @Nested
    inner class DeleteLaboratory {
        @Test
        fun `delete laboratory`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: Deleting a laboratory
            LabsServicesUtils.deleteLab(
                labsServices,
                lab.id.toString(),
                lab.ownerId,
            )
        }

        @Test
        fun `delete laboratory with invalid lab id (not Int)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Deleting a laboratory with invalid ID
            LabsServicesUtils.deleteLab(
                labsServices,
                "invalid_id",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.InvalidLaboratoryId::class,
            )
        }

        @Test
        fun `delete laboratory with invalid lab id (negative)`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Deleting a laboratory with invalid ID
            LabsServicesUtils.deleteLab(
                labsServices,
                "-1",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `delete non existent lab by id`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)

            // When: Deleting the laboratory with invalid ID
            LabsServicesUtils.deleteLab(
                labsServices,
                "99999",
                1,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }

        @Test
        fun `delete laboratory with a user that do not own the lab`() {
            // When: given a lab service
            val clock = TestClock()
            val labsServices = LabsServicesUtils.createLabsServices(clock)
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a laboratory
            val lab =
                LabsServicesUtils.createLab(
                    labsServices,
                )

            // When: creating a user that does not own the lab
            val user = UsersServicesUtils.loginUser(usersServices)

            // When: Deleting the laboratory with a user that does not own the lab
            LabsServicesUtils.deleteLab(
                labsServices,
                lab.id.toString(),
                user.id,
                expectedServiceException = ServicesExceptions.Laboratories.LaboratoryNotFound::class,
            )
        }
    }
}
