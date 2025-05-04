package isel.rl.core.domain.user.token

import java.security.MessageDigest
import java.util.Base64

/**
 * Encodes tokens using SHA-256.
 */
class Sha256TokenEncoder : TokenEncoder {
    override fun createValidationInformation(token: String): TokenValidationInfo = TokenValidationInfo(hash(token))

    /**
     * Hashes the specified input using SHA-256.
     *
     * @param input the input to be hashed
     * @return the hashed input
     */
    private fun hash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA256")
        return Base64.getUrlEncoder().encodeToString(
            messageDigest.digest(
                Charsets.UTF_8.encode(input).array(),
            ),
        )
    }
}