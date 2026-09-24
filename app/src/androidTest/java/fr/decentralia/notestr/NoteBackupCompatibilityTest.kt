package fr.decentralia.notestr

import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.nostr.NoteEvents
import fr.decentralia.notestr.data.nostr.RustNostrRepository
import fr.decentralia.notestr.data.storage.EventCache
import fr.decentralia.notestr.domain.model.Note
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.nostrdevkit.sdk.*

class NoteBackupCompatibilityTest {
    // Synthetic, publicly known scalar. Never a user credential.
    private val syntheticSecret = "0".repeat(63) + "1"
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun fixture(): JSONObject = JSONObject(InstrumentationRegistry.getInstrumentation().context.assets
        .open("linux-backup-synthetic.json").bufferedReader().use { it.readText() })
    private fun events(): List<Event> = fixture().getJSONArray("events").let { a ->
        List(a.length()) { Event.fromJson(a.getJSONObject(it).toString()) }
    }
    private fun event(keys: Keys, kind: UShort, d: String, timestamp: ULong, content: String = "synthetic") =
        EventBuilder(Kind(kind), content).tags(listOf(Tag.identifier(d)))
            .customCreatedAt(Timestamp.fromSecs(timestamp)).finalize(keys)

    @Test fun readsLinuxBackupWithLegacyIdentifierAndUnicode() = runBlocking {
        val source = events()
        SecretKey.parse(syntheticSecret).use { secret -> Keys(secret).use { keys ->
            val author = keys.publicKey()
            assertEquals(3, NoteEvents.verified(source, author).size)
            val note = NoteEvents.latest(source, NoteEvents.NOTE, author).single()
            val backup = NoteEvents.latest(source, NoteEvents.BACKUP, author).single()
            assertEquals(NoteEvents.backupAddress("ancien-identifiant-linux"), NoteEvents.identifier(backup))
            assertEquals(source.first().content(), backup.content())
            assertEquals(source.first().id().toHex(), NoteEvents.tags(backup, "e").single())
            assertEquals(fixture().getString("previousMarkdown"), nip44Decrypt(secret, author, backup.content()))
            val cache = EventCache(context)
            cache.save(source.map(Event::asJson))
            RustNostrRepository(syntheticSecret.toCharArray(), emptyList(), cache).use { repo ->
                assertEquals(fixture().getString("previousMarkdown"), repo.previous(Note(
                    NoteEvents.identifier(note)!!, "unused", note.id().toHex(), note.createdAt().asSecs().toLong(), note.asJson()
                )).getOrThrow())
            }
        } }
    }

    @Test fun emitsAndroidBackupForLinuxAndRetainsOnlyOneVersion() {
        SecretKey.parse(syntheticSecret).use { secret -> Keys(secret).use { keys ->
            val source = events()
            val previous = source.last()
            val clear = "# Essai partagé\n\nVersion Android : été 漢字."
            val update = event(keys, NoteEvents.NOTE, "ancien-identifiant-linux", 1700000002uL,
                nip44Encrypt(secret, keys.publicKey(), clear, Nip44Version.V2))
            val backup = NoteEvents.backupBuilder(previous, update).finalize(keys)
            val compact = NoteEvents.compact(source + backup + update)
            assertEquals(1, compact.count { it.kind().asU16() == NoteEvents.BACKUP })
            assertEquals(previous.content(), backup.content())
            assertEquals(update.createdAt().asSecs(), backup.createdAt().asSecs())
            val payload = JSONObject().put("synthetic", true).put("events", JSONArray(compact.map { JSONObject(it.asJson()) }))
            context.filesDir.resolve("android-backup-synthetic.json").writeText(payload.toString())
        } }
    }

    @Test fun tieBreakingDeletionCutoffAndAccountIsolationMatchLinux() {
        SecretKey.parse(syntheticSecret).use { secret -> Keys(secret).use { keys ->
            val author = keys.publicKey()
            val address = NoteEvents.backupAddress("abc123")
            val a = event(keys, NoteEvents.BACKUP, address, 100uL, "a")
            val b = event(keys, NoteEvents.BACKUP, address, 100uL, "b")
            val winner = listOf(a, b).minBy { it.id().toHex() }
            assertEquals(winner.id(), NoteEvents.latest(listOf(b, a), NoteEvents.BACKUP, author).single().id())
            assertEquals(winner.id(), NoteEvents.compact(listOf(a, b)).single().id())
            val deletion = EventBuilder(Kind(NoteEvents.DELETE), "")
                .tags(listOf(Tag.parse(listOf("a", "30078:${author.toHex()}:$address"))))
                .customCreatedAt(Timestamp.fromSecs(100uL)).finalize(keys)
            assertTrue(NoteEvents.latest(listOf(a, b, deletion), NoteEvents.BACKUP, author).isEmpty())
            val later = event(keys, NoteEvents.BACKUP, address, 101uL)
            assertEquals(later.id(), NoteEvents.latest(listOf(a, b, deletion, later), NoteEvents.BACKUP, author).single().id())
            SecretKey.parse("0".repeat(63) + "2").use { other -> Keys(other).use { otherKeys ->
                assertTrue(NoteEvents.verified(listOf(a), otherKeys.publicKey()).isEmpty())
            } }
            val forged = JSONObject(a.asJson()).put("content", "tampered").toString()
            val invalid = runCatching { Event.fromJson(forged) }.getOrNull()
            assertTrue(invalid == null || NoteEvents.verified(listOf(invalid), author).isEmpty())
        } }
    }

    @Test fun rejectsSameSecondAndFutureDatedUpdate() {
        SecretKey.parse(syntheticSecret).use { secret -> Keys(secret).use { keys ->
            val previous = event(keys, NoteEvents.NOTE, "abc123", 100uL)
            assertTrue(runCatching { NoteEvents.updateTime(previous, null, Timestamp.fromSecs(100uL)) }.isFailure)
            assertTrue(runCatching { NoteEvents.updateTime(null, previous, Timestamp.fromSecs(99uL)) }.isFailure)
            assertEquals(101uL, NoteEvents.updateTime(previous, previous, Timestamp.fromSecs(101uL)).asSecs())
        } }
    }
}
