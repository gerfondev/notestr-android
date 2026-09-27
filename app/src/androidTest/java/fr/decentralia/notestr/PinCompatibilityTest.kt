package fr.decentralia.notestr

import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.nostr.NoteEvents
import fr.decentralia.notestr.data.nostr.RustNostrRepository
import fr.decentralia.notestr.data.storage.EventCache
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.nostrdevkit.sdk.*

class PinCompatibilityTest {
    private val key = "0".repeat(63) + "1" // Public synthetic scalar, never a user key.
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun linuxPinUnpinAndAndroidExport() = runBlocking {
        val fixture = JSONObject(InstrumentationRegistry.getInstrumentation().context.assets
            .open("linux-pins-synthetic.json").bufferedReader().use { it.readText() })
        val input = fixture.getJSONArray("events")
        val events = List(input.length()) { input.getJSONObject(it).toString() }
        val cache = EventCache(context)
        cache.save(events)
        RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
            val notes = repo.refresh().getOrThrow()
            assertEquals(listOf("legacy-été", "recent"), notes.map { it.identifier })
            assertTrue(notes.first().pinned)
            assertEquals(100L, notes.first().createdAt)
            assertEquals("Ancienne été 漢字", notes.first().markdown)
            assertNull(repo.previous(notes.first()).getOrThrow())
            cache.save(events + fixture.getJSONObject("unpin").toString())
            val unpinned = repo.refresh().getOrThrow()
            assertEquals(listOf("recent", "legacy-été"), unpinned.map { it.identifier })
            assertFalse(unpinned.any { it.pinned })
            // Rejected publication leaves the cached pin state unchanged.
            assertTrue(repo.setPinned(unpinned.last(), true).isFailure)
            assertFalse(repo.refresh().getOrThrow().any { it.pinned })
        }
        SecretKey.parse(key).use { secret -> Keys(secret).use { keys ->
            val pin = EventBuilder(Kind(NoteEvents.BACKUP), nip44Encrypt(secret, keys.publicKey(), "true", Nip44Version.V2))
                .tags(listOf(Tag.identifier(NoteEvents.pinAddress("legacy-été"))))
                .customCreatedAt(Timestamp.fromSecs(104uL)).finalize(keys)
            val compact = NoteEvents.compact((events + fixture.getJSONObject("unpin").toString() + pin.asJson()).map(Event::fromJson))
            assertEquals(1, compact.count { it.kind().asU16() == NoteEvents.BACKUP })
            context.filesDir.resolve("android-pins-synthetic.json").writeText(JSONObject()
                .put("synthetic", true).put("events", JSONArray(compact.map { JSONObject(it.asJson()) })).toString())
        } }
    }
}
