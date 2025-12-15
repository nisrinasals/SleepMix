package com.example.sleepmix.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility untuk hash password dengan SHA-256 + Salt
 */
object PasswordHasher {

    /**
     * Hash password dengan salt
     * @param password Password plain text
     * @return Hashed password dalam format "salt:hash"
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashWithSalt(password, salt)
        return "$salt:$hash"
    }

    /**
     * Verifikasi password
     * @param password Password input dari user
     * @param storedHash Hash yang tersimpan di database (format "salt:hash")
     * @return true jika password cocok
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = parts[0]
        val originalHash = parts[1]
        val hashOfInput = hashWithSalt(password, salt)

        return hashOfInput == originalHash
    }

    /**
     * Generate random salt
     */
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return bytesToHex(salt)
    }

    /**
     * Hash password dengan salt menggunakan SHA-256
     */
    private fun hashWithSalt(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val saltedPassword = "$salt$password"
        val bytes = saltedPassword.toByteArray()
        val digest = md.digest(bytes)
        return bytesToHex(digest)
    }

    /**
     * Convert byte array ke hex string
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}