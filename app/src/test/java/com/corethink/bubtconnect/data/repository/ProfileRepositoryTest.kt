package com.corethink.bubtconnect.data.repository

import com.corethink.bubtconnect.data.model.StudentProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileRepositoryTest {

    @Test
    fun connectingRecordsASyncTime() {
        val repository = ProfileRepository()
        assertNull(repository.lastSyncedAt.value)

        repository.markAnnexConnected()

        assertTrue(repository.annexConnected.value)
        assertTrue(repository.lastSyncedAt.value != null)
    }

    @Test
    fun manualSyncUpdatesTimestamp() {
        val repository = ProfileRepository()
        repository.markSynced(at = 1_000L)
        assertEquals(1_000L, repository.lastSyncedAt.value)
    }

    @Test
    fun logoutClearsProfileAndSession() {
        val repository = ProfileRepository()
        repository.saveProfile(StudentProfile("22235103045", "CSE", "5th", "Rafi Ahmed"))
        repository.markAnnexConnected()

        repository.logout()

        assertNull(repository.profile.value)
        assertFalse(repository.annexConnected.value)
        assertNull(repository.lastSyncedAt.value)
    }
}
