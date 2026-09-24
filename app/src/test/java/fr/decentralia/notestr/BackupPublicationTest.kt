package fr.decentralia.notestr

import fr.decentralia.notestr.data.nostr.publishInOrder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BackupPublicationTest {
    @Test fun updateGoesOnlyToRelaysThatAcceptedBackup() = runBlocking {
        val calls = mutableListOf<Pair<String, List<String>>>()
        var cached = false
        publishInOrder("update", "backup", listOf("a", "b"), { event, relays ->
            calls += event to relays
            if (event == "backup") listOf("a") else { assertTrue(cached); relays }
        }, { cached = true })
        assertEquals(listOf("backup" to listOf("a", "b"), "update" to listOf("a")), calls)
    }
    @Test fun refusalOfBackupPreventsUpdate() = runBlocking {
        val calls = mutableListOf<String>()
        val result = runCatching { publishInOrder("update", "backup", listOf("a"), { event, _ ->
            calls += event; emptyList<String>()
        }, { fail("Refused backup must not be committed locally") }) }
        assertTrue(result.isFailure)
        assertEquals(listOf("backup"), calls)
    }
    @Test fun localBackupFailurePreventsUpdate() = runBlocking {
        val calls = mutableListOf<String>()
        val result = runCatching { publishInOrder("update", "backup", listOf("a"), { event, relays ->
            calls += event; relays
        }, { error("Synthetic disk failure") }) }
        assertTrue(result.isFailure)
        assertEquals(listOf("backup"), calls)
    }
    @Test fun newNoteDoesNotCreateBackup() = runBlocking {
        val calls = mutableListOf<String>()
        publishInOrder("update", null, listOf("a"), { event, relays -> calls += event; relays }, { fail() })
        assertEquals(listOf("update"), calls)
    }
    @Test fun refusalOfUpdateIsReportedAndBackupIsKept() = runBlocking {
        var cached = false
        val result = runCatching { publishInOrder("update", "backup", listOf("a"), { event, relays ->
            if (event == "backup") relays else emptyList()
        }, { cached = true }) }
        assertTrue(result.isFailure)
        assertTrue(cached)
    }
}
