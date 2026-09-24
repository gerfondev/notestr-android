package fr.decentralia.notestr.data.nostr

import java.security.MessageDigest
import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.EventBuilder
import org.nostrdevkit.sdk.Kind
import org.nostrdevkit.sdk.PublicKey
import org.nostrdevkit.sdk.Tag
import org.nostrdevkit.sdk.Timestamp

/** Wire format shared with Notestr Linux (core.py). Only ciphertext is backed up. */
internal object NoteEvents {
    val NOTE: UShort = 33457u
    val BACKUP: UShort = 30078u
    val DELETE: UShort = 5u
    const val PREFIX = "notestr/previous/"

    fun backupAddress(identifier: String): String = PREFIX + MessageDigest.getInstance("SHA-256")
        .digest(identifier.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    fun tags(event: Event, name: String): List<String> = event.tags().map(Tag::toVec)
        .filter { it.firstOrNull() == name }.mapNotNull { it.getOrNull(1) }

    fun identifier(event: Event): String? = tags(event, "d").firstOrNull()

    // NIP-01: highest timestamp, then lowest event ID at equal timestamps.
    private val newest = compareByDescending<Event> { it.createdAt().asSecs() }.thenBy { it.id().toHex() }

    fun verified(events: List<Event>, author: PublicKey): List<Event> = events.filter {
        it.author() == author && it.kind().asU16() in listOf(NOTE, BACKUP, DELETE) && it.verify()
    }

    fun compact(events: List<Event>): List<Event> {
        val unique = events.distinctBy { it.id().toHex() }
        return unique.filter { it.kind().asU16() != BACKUP } + unique
            .filter { it.kind().asU16() == BACKUP && identifier(it)?.startsWith(PREFIX) == true }
            .groupBy { identifier(it) }.values.map { it.minWith(newest) }
    }

    fun latest(events: List<Event>, kind: UShort, author: PublicKey): List<Event> {
        val deletions = events.filter { it.kind().asU16() == DELETE }
        val deletedIds = deletions.flatMap { tags(it, "e") }.toSet()
        val deletedAddresses = mutableMapOf<String, ULong>()
        deletions.forEach { deletion -> tags(deletion, "a").forEach { address ->
            deletedAddresses[address] = maxOf(deletedAddresses[address] ?: 0uL, deletion.createdAt().asSecs())
        } }
        return events.filter { it.kind().asU16() == kind && identifier(it) != null }
            .groupBy { identifier(it)!! }.map { (_, versions) -> versions.minWith(newest) }
            .filter { event ->
                val cutoff = deletedAddresses["$kind:${author.toHex()}:${identifier(event)}"]
                event.id().toHex() !in deletedIds && (cutoff == null || event.createdAt().asSecs() > cutoff)
            }
    }

    fun backupBuilder(previous: Event, update: Event): EventBuilder =
        EventBuilder(Kind(BACKUP), previous.content())
            .tags(listOf(Tag.identifier(backupAddress(requireNotNull(identifier(previous)))), Tag.event(previous.id())))
            .customCreatedAt(update.createdAt())

    fun updateTime(previous: Event?, backup: Event?, now: Timestamp = Timestamp.now()): Timestamp {
        require(listOfNotNull(previous, backup).all { it.createdAt().asSecs() < now.asSecs() }) {
            "Attendre la seconde suivante avant de republier cette note ; vérifier l’horloge si nécessaire."
        }
        return now
    }
}
