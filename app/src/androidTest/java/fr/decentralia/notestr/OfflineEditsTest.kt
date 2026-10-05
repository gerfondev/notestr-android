package fr.decentralia.notestr

import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.nostr.*
import fr.decentralia.notestr.data.storage.EventCache
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.nostrdevkit.sdk.*

/** Only public synthetic key 1 and a loopback relay. Never a user's account. */
class OfflineEditsTest {
    private val key = "0".repeat(63) + "1"
    private val author = "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798"
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun original(): Event = SecretKey.parse(key).use { secret -> Keys(secret).use { keys ->
        EventBuilder(Kind(NoteEvents.NOTE), nip44Encrypt(secret, keys.publicKey(), "# Original", Nip44Version.V2))
            .tags(listOf(Tag.identifier("offline-" + java.util.UUID.randomUUID())))
            .customCreatedAt(Timestamp.fromSecs(100uL)).finalize(keys)
    } }

    @Test fun encryptedSaveRestartRepeatedEditsAndDiscard() = runBlocking {
        val cache = EventCache(context); cache.savePending(author, emptyList())
        val event = original(); cache.save(listOf(event.asJson()))
        try {
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                val base = repo.cached().getOrThrow().single()
                val first = repo.saveLocal("# Secret offline", base).getOrThrow().note
                val second = repo.saveLocal("# Changed twice", first).getOrThrow().note
                assertTrue(second.pending)
                assertEquals(1, cache.pending(author).size)
                assertFalse(cache.pending(author).single().contains("Changed twice"))
                assertTrue(repo.syncPending().isFailure)
            }
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                val pending = repo.cached().getOrThrow().single()
                assertEquals("# Changed twice", pending.markdown)
                assertTrue(pending.pending)
                assertEquals("# Original", repo.discardPending(pending).getOrThrow().single().markdown)
            }
        } finally { cache.savePending(author, emptyList()); cache.clear() }
    }

    @Test fun remoteChangePreservesBothVersionsAndCopy() = runBlocking {
        val cache = EventCache(context); cache.savePending(author, emptyList())
        val event = original(); cache.save(listOf(event.asJson()))
        try {
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                repo.saveLocal("# Local edit", repo.cached().getOrThrow().single()).getOrThrow()
            }
            val remote = SecretKey.parse(key).use { secret -> Keys(secret).use { keys ->
                EventBuilder(Kind(NoteEvents.NOTE), nip44Encrypt(secret, keys.publicKey(), "# Other device", Nip44Version.V2))
                    .tags(listOf(Tag.identifier(NoteEvents.identifier(event)!!)))
                    .customCreatedAt(Timestamp.fromSecs(200uL)).finalize(keys)
            } }
            cache.save(listOf(event.asJson(), remote.asJson()))
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                val notes = repo.cached().getOrThrow()
                assertEquals(2, notes.size)
                val local = notes.single { it.pending }
                assertTrue(local.conflicted)
                assertEquals("# Local edit", local.markdown)
                assertEquals("# Other device", notes.single { !it.pending }.markdown)
                val copy = repo.saveLocal(local.markdown, local, asCopy = true).getOrThrow().note
                assertNotEquals(local.identifier, copy.identifier)
                assertFalse(copy.conflicted)
            }
        } finally { cache.savePending(author, emptyList()); cache.clear() }
    }

    @Test fun linuxOfflineFormatAndAndroidExport() = runBlocking {
        val test = InstrumentationRegistry.getInstrumentation()
        val fixture = test.context.assets.open("linux-offline-synthetic.json").bufferedReader().use { it.readText() }
        val row = org.json.JSONObject(fixture)
        val cache = EventCache(context)
        cache.save(listOf(row.getJSONObject("base").toString()))
        cache.savePending(author, listOf(fixture))
        try {
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                val note = repo.cached().getOrThrow().single()
                assertTrue(note.pending)
                assertEquals("# Linux offline edit", note.markdown)
                repo.saveLocal("# Android offline edit", note).getOrThrow()
                context.filesDir.resolve("android-offline-synthetic.json").writeText(cache.pending(author).single())
            }
        } finally { cache.savePending(author, emptyList()); cache.clear() }
    }

    @Test fun acknowledgedConcurrentEditIsRecoverableAndDeletionPurgesIt() = runBlocking {
        val cache = EventCache(context); cache.savePending(author, emptyList())
        val event = original(); cache.save(listOf(event.asJson()))
        try {
            lateinit var local: fr.decentralia.notestr.domain.model.Note
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                local = repo.saveLocal("# Device A", repo.cached().getOrThrow().single()).getOrThrow().note
            }
            val receipt = org.json.JSONObject(cache.pending(author).single()).put("sent", true).toString()
            cache.savePending(author, listOf(receipt))
            SecretKey.parse(key).use { secret -> Keys(secret).use { keys ->
                val remote = EventBuilder(Kind(NoteEvents.NOTE), nip44Encrypt(secret, keys.publicKey(), "# Device B", Nip44Version.V2))
                    .tags(listOf(Tag.identifier(local.identifier), Tag.parse(listOf("notestr-base", event.id().toHex()))))
                    .customCreatedAt(Timestamp.fromSecs(local.createdAt.toULong() + 1uL)).finalize(keys)
                cache.save(listOf(event.asJson(), local.eventJson, remote.asJson()))
                RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                    val notes = repo.cached().getOrThrow()
                    assertEquals(2, notes.size)
                    assertEquals("# Device A", notes.single { it.pending && it.conflicted }.markdown)
                }
                val deletion = EventBuilder(Kind(NoteEvents.DELETE), "synthetic deletion")
                    .tags(listOf(Tag.parse(listOf("a", "${NoteEvents.NOTE}:$author:${local.identifier}"))))
                    .finalize(keys)
                cache.save(listOf(event.asJson(), local.eventJson, remote.asJson(), deletion.asJson()))
                RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                    assertTrue(repo.cached().getOrThrow().isEmpty())
                    assertTrue(cache.pending(author).isEmpty())
                }
            } }
        } finally { cache.savePending(author, emptyList()); cache.clear() }
    }

    @Test fun corruptQueueCannotBeOverwritten() = runBlocking {
        val cache = EventCache(context); cache.save(listOf(original().asJson()))
        cache.savePending(author, listOf("corrupt"))
        try {
            RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
                assertTrue(repo.saveLocal("# Keep broken draft for recovery", null).isFailure)
                assertEquals(listOf("corrupt"), cache.pending(author))
            }
        } finally { cache.savePending(author, emptyList()); cache.clear() }
    }

    @Test fun completeRelayReadAckAndIdempotency() = runBlocking {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("localBackupRelay") == "true")
        val cache = EventCache(context); cache.savePending(author, emptyList()); cache.clear()
        val relays = listOf("ws://10.0.2.2:18765/accept")
        try {
            RustNostrRepository(key.toCharArray(), relays, cache).use { repo ->
                val saved = repo.saveLocal("# Offline relay synthetic", null).getOrThrow().note
                val snapshot = cache.pending(author)
                assertTrue(repo.syncPending().getOrThrow().any { it.identifier == saved.identifier && !it.pending })
                assertTrue(cache.pending(author).all { org.json.JSONObject(it).getBoolean("sent") })
                // Simulate process death after ACK and cache commit but before outbox removal.
                cache.savePending(author, snapshot)
                assertTrue(repo.syncPending().getOrThrow().any { it.eventId == saved.eventId && !it.pending })
                assertTrue(cache.pending(author).all { org.json.JSONObject(it).getBoolean("sent") })
                val published = repo.cached().getOrThrow().single { it.identifier == saved.identifier }
                kotlinx.coroutines.delay(1100)
                repo.saveLocal("# Offline second version", published).getOrThrow()
                repo.syncPending().getOrThrow()
                assertEquals("# Offline relay synthetic", repo.previous(published).getOrThrow())
            }
        } finally { cache.savePending(author, emptyList()); cache.clear() }
    }
}
