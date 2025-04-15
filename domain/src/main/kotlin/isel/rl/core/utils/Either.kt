package isel.rl.core.utils

/**
* A sealed class representing a value of one of two possible types (a disjoint union).
* Instances of `Either` are either an instance of `Left` or `Right`.
*
* @param L the type of the `Left` value
* @param R the type of the `Right` value
*/
sealed class Either<out L, out R> {
    /**
     * Represents the left side of `Either` which by convention is a failure.
     *
     * @param L the type of the value
     * @property value the value of the `Left`
     */
    data class Left<out L>(val value: L) : Either<L, Nothing>()

    /**
     * Represents the right side of `Either` which by convention is a success.
     *
     * @param R the type of the value
     * @property value the value of the `Right`
     */
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/**
 * Creates an `Either.Right` instance representing a success.
 *
 * @param R the type of the value
 * @param value the value of the success
 * @return an `Either.Right` instance containing the value
 */
fun <R> success(value: R) = Either.Right(value)

/**
 * Creates an `Either.Left` instance representing a failure.
 *
 * @param L the type of the error
 * @param error the error value
 * @return an `Either.Left` instance containing the error
 */
fun <L> failure(error: L) = Either.Left(error)

typealias Success<S> = Either.Right<S>
typealias Failure<F> = Either.Left<F>
