package com.corethink.bubtconnect.data.repository

import kotlinx.coroutines.delay

sealed interface AnnexAuthResult {
    data object Success : AnnexAuthResult
    data object InvalidCredentials : AnnexAuthResult
}

interface AnnexAuthRepository {
    suspend fun login(studentId: String, password: String, keepSignedIn: Boolean): AnnexAuthResult

    /** Re-pulls attendance, results and routine; returns false on failure. */
    suspend fun sync(): Boolean
}

/**
 * Placeholder until the real Annex client lands in data/source/remote.
 * Accepts any 11-digit ID with a password of 6+ characters.
 */
object FakeAnnexAuthRepository : AnnexAuthRepository {
    override suspend fun login(studentId: String, password: String, keepSignedIn: Boolean): AnnexAuthResult {
        delay(1200)
        val valid = studentId.length == 11 && studentId.all(Char::isDigit) && password.length >= 6
        if (!valid) return AnnexAuthResult.InvalidCredentials
        ProfileRepository.Default.markAnnexConnected()
        return AnnexAuthResult.Success
    }

    override suspend fun sync(): Boolean {
        delay(1500)
        ProfileRepository.Default.markSynced()
        return true
    }
}
