package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.utils.Either

/**
 * Result of creating a laboratory.
 * It can either be a success with the laboratory ID or a failure with an exception.
 */
typealias CreateLaboratoryResult = Either<ServicesExceptions, Laboratory>

/**
 * Result of getting a laboratory.
 * It can either be a success with the laboratory or a failure with an exception.
 */
typealias GetLaboratoryResult = Either<ServicesExceptions, Laboratory>

/**
 * Result of updating a laboratory.
 * It can either be a success with no value or a failure with an exception.
 */
typealias UpdateLaboratoryResult = Either<ServicesExceptions, Unit>

typealias GetAllLaboratoriesResult = Either<ServicesExceptions, List<Laboratory>>

/**
 * Interface for managing laboratories.
 */
interface ILaboratoriesService {
    /**
     * Creates a new laboratory.
     * The parameters are validated before creating the laboratory with the laboratory domain.
     * If the validation fails, a Service exception as failure.
     *
     * @param labName The name of the laboratory.
     * @param labDescription The description of the laboratory.
     * @param labDuration The duration of the laboratory in minutes.
     * @param labQueueLimit The maximum number of users allowed in the queue.
     * @param ownerId The ID of the owner of the laboratory.
     * @return A result indicating success or failure.
     */
    fun createLaboratory(
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        ownerId: Int,
    ): CreateLaboratoryResult

    /**
     * Retrieves a laboratory by its ID.
     * The ID is validated before retrieving the laboratory from the database.
     * If the validation fails, a Service exception as failure.
     * If the laboratory is not found, a failure result is returned.
     *
     * @param id The ID of the laboratory.
     * @return A result containing the laboratory or an exception.
     */
    fun getLaboratoryById(
        id: String,
        userId: Int,
    ): GetLaboratoryResult

    /**
     * Updates an existing laboratory.
     * The parameters are validated before updating the laboratory with the laboratory domain.
     * If the validation fails, a Service exception as failure.
     *
     * @param labId The ID of the laboratory to update.
     * @param labName The new name of the laboratory (optional).
     * @param labDescription The new description of the laboratory (optional).
     * @param labDuration The new duration of the laboratory in minutes (optional).
     * @param labQueueLimit The new maximum number of users allowed in the queue (optional).
     * @param ownerId The ID of the owner of the laboratory.
     * @return A result indicating success or failure.
     */
    fun updateLaboratory(
        labId: String,
        labName: String? = null,
        labDescription: String? = null,
        labDuration: Int? = null,
        labQueueLimit: Int? = null,
        ownerId: Int,
    ): UpdateLaboratoryResult

    fun getAllLaboratoriesByUser(
        userId: Int,
        limit: String? = null,
        skip: String? = null,
    ): GetAllLaboratoriesResult
}
