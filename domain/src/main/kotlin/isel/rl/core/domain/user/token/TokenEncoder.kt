package isel.rl.core.domain.user.token

/**
 * Token encoder.
 */
interface TokenEncoder {
    /**
     * Encodes the token.
     *
     * @param token the token to encode
     * @return the encoded token
     */
    fun createValidationInformation(token: String): TokenValidationInfo
}
