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

class TrashCompatibilityTest {
    private val key = "0".repeat(63) + "1" // Public synthetic scalar, not a user key.
    @Test fun linuxTrashRestoreDeleteAndAndroidExport() = runBlocking {
        val test = InstrumentationRegistry.getInstrumentation()
        val context = test.targetContext
        val fixture = JSONObject(test.context.assets.open("linux-trash-synthetic.json").bufferedReader().use { it.readText() })
        val note = fixture.getJSONObject("note").toString()
        val trash = fixture.getJSONObject("trash").toString()
        val restore = fixture.getJSONObject("restore").toString()
        val deletion = fixture.getJSONObject("deletion").toString()
        val cache = EventCache(context)
        cache.save(listOf(note, trash))
        RustNostrRepository(key.toCharArray(), emptyList(), cache).use { repo ->
            val n = repo.cached().getOrThrow().single()
            assertTrue(n.trashed)
            assertEquals("# Note fictive\n\nTexte", n.markdown)
            assertTrue(repo.publish("Forbidden edit", n).isFailure)
            assertTrue(repo.setTrashed(n, false).isFailure) // No relay: no optimistic restore.
            assertTrue(repo.cached().getOrThrow().single().trashed)
            cache.save(listOf(note, trash, restore))
            assertFalse(repo.refresh().getOrThrow().single().trashed)
            cache.save(listOf(note, trash, restore, deletion))
            assertTrue(repo.refresh().getOrThrow().isEmpty())
        }
        SecretKey.parse(key).use { secret -> Keys(secret).use { keys ->
            fun metadata(value: Boolean, time: ULong) = EventBuilder(Kind(NoteEvents.BACKUP),
                nip44Encrypt(secret, keys.publicKey(), value.toString(), Nip44Version.V2))
                .tags(listOf(Tag.identifier(NoteEvents.trashAddress("trash-été"))))
                .customCreatedAt(Timestamp.fromSecs(time)).finalize(keys)
            val t = metadata(true, 110uL); val u = metadata(false, 111uL)
            val events = listOf(Event.fromJson(note), t, u)
            assertEquals(2, NoteEvents.compact(events).size)
            context.filesDir.resolve("android-trash-synthetic.json").writeText(JSONObject()
                .put("synthetic", true).put("note", JSONObject(note))
                .put("trash", JSONObject(t.asJson())).put("restore", JSONObject(u.asJson())).toString())
        } }
    }
}
