package fr.decentralia.notestr

import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.nostr.NoteEvents
import org.junit.Assert.*
import org.junit.Test
import org.nostrdevkit.sdk.*

/** Public synthetic key and historical deletions; no network or user cache. */
class CacheStartupTest {
    @Test fun largeDeletionHistory() {
        SecretKey.parse("0".repeat(63) + "1").use { secret -> Keys(secret).use { keys ->
            val author = keys.publicKey().toHex()
            val notes = (0 until 300).map { n -> EventBuilder(Kind(NoteEvents.NOTE), "synthetic")
                .tags(listOf(Tag.identifier("fixture-$n"))).finalize(keys) }
            val deletions = (0 until 300).map { n -> EventBuilder(Kind(NoteEvents.DELETE), "synthetic")
                .tags(listOf(Tag.parse(listOf("a", "${NoteEvents.NOTE}:$author:old-$n")))).finalize(keys) }
            val start = android.os.SystemClock.elapsedRealtime()
            val compacted = NoteEvents.compact(notes + deletions)
            val elapsed = android.os.SystemClock.elapsedRealtime() - start
            InstrumentationRegistry.getInstrumentation().sendStatus(2, Bundle().apply {
                putString("stream", "\nCOMPACT_600_EVENTS_MS=$elapsed\n")
            })
            assertEquals(600, compacted.size)
            assertTrue("Cache compaction blocked for $elapsed ms", elapsed < 2000)
        } }
    }
    @Test fun foreignDeletionCannotRemoveAnotherAuthorsNote() {
        SecretKey.parse("0".repeat(63) + "1").use { secret -> Keys(secret).use { keys ->
            val author = keys.publicKey().toHex()
            val note = EventBuilder(Kind(NoteEvents.NOTE), "synthetic")
                .tags(listOf(Tag.identifier("test"))).finalize(keys)
            SecretKey.parse("0".repeat(63) + "2").use { other -> Keys(other).use { otherKeys ->
                val foreign = EventBuilder(Kind(NoteEvents.DELETE), "synthetic")
                    .tags(listOf(Tag.parse(listOf("a", "${NoteEvents.NOTE}:$author:test")))).finalize(otherKeys)
                assertTrue(NoteEvents.compact(listOf(note, foreign)).contains(note))
            } }
            val own = EventBuilder(Kind(NoteEvents.DELETE), "synthetic")
                .tags(listOf(Tag.parse(listOf("a", "${NoteEvents.NOTE}:$author:test")))).finalize(keys)
            assertEquals(listOf(own), NoteEvents.compact(listOf(note, own)))
        } }
    }

}
