package fr.decentralia.notestr

import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.nostr.RustNostrRepository
import fr.decentralia.notestr.data.nostr.NoteEvents
import fr.decentralia.notestr.data.storage.EventCache
import fr.decentralia.notestr.domain.model.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.nostrdevkit.sdk.Event

/** Requires the synthetic loopback relay documented in VALIDATION.md; never a public relay. */
class BackupRelayTest {
    @Test fun publishRefreshRestoreDeleteAndBackupRefusal() = runBlocking {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("localBackupRelay") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val raw = InstrumentationRegistry.getInstrumentation().context.assets
            .open("linux-backup-synthetic.json").bufferedReader().use { it.readText() }
        val initial = JSONObject(raw).getJSONArray("events")
        val cache = EventCache(context)
        cache.save(List(initial.length()) { initial.getJSONObject(it).toString() })
        val old = Event.fromJson(initial.getJSONObject(2).toString())
        val note = Note(NoteEvents.identifier(old)!!, "# Essai partagé\n\nVersion Linux modifiée.",
            old.id().toHex(), old.createdAt().asSecs().toLong(), old.asJson())
        val key = "0".repeat(63) + "1" // Public synthetic test key.
        val relays = listOf("ws://10.0.2.2:18765/accept", "ws://10.0.2.2:18765/reject")
        RustNostrRepository(key.toCharArray(), relays, cache).use { repo ->
            val published = repo.publish("# Essai partagé\n\nVersion réseau Android.", note).getOrThrow()
            assertNull(published.warning)
            assertEquals(note.markdown, repo.previous(published.note).getOrThrow())
            assertEquals(1, cache.load().map(Event::fromJson).count { it.kind().asU16() == NoteEvents.BACKUP })
            // A new app instance retrieves the encrypted backup from the relay, with an empty cache.
            cache.clear()
            RustNostrRepository(key.toCharArray(), relays, cache).use { fresh ->
                val current = fresh.refresh().getOrThrow().single()
                assertEquals(published.note.eventId, current.eventId)
                val restoredText = fresh.previous(current).getOrThrow()!!
                assertEquals(note.markdown, restoredText)
                delay(1100)
                val restored = fresh.publish(restoredText, current).getOrThrow().note
                assertEquals(note.identifier, restored.identifier)
                assertEquals(current.markdown, fresh.previous(restored).getOrThrow())
                assertEquals(1, cache.load().map(Event::fromJson).count { it.kind().asU16() == NoteEvents.BACKUP })
                assertEquals(current.markdown, fresh.previous(fresh.refresh().getOrThrow().single()).getOrThrow())
                RustNostrRepository(key.toCharArray(), listOf("ws://10.0.2.2:18765/empty"), cache).use { emptyRelay ->
                    val fromCache = emptyRelay.refresh().getOrThrow().single()
                    assertEquals(restored.eventId, fromCache.eventId)
                    assertEquals(current.markdown, emptyRelay.previous(fromCache).getOrThrow())
                }
                delay(1100)
                RustNostrRepository(key.toCharArray(), listOf(relays.last()), cache).use { refusing ->
                    assertTrue(refusing.publish("refused update", restored).isFailure)
                }
                fresh.delete(restored).getOrThrow()
                assertTrue(fresh.refresh().getOrThrow().isEmpty())
                assertNull(fresh.previous(restored).getOrThrow())
            }
        }
    }
}
