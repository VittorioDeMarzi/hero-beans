package techcourse.herobeans.configurations

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey
import kotlin.text.toByteArray

@Component
class JwtTokenProvider {
    @Value("\${security.jwt.token.secret-key}")
    private lateinit var secretKeyString: String

    @Value("\${security.jwt.token.expire-length}")
    private var validityInMilliseconds: Long = 0

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKeyString.toByteArray(StandardCharsets.UTF_8))
    }

    fun createToken(email: String): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun getEmailFromToken(token: String): String? {
        try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token).payload

            return claims.subject
        } catch (e: JwtException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }
    }
}
