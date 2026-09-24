package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.data.storage.EventCache
import fr.decentralia.notestr.domain.explicitLineBreaks
import fr.decentralia.notestr.domain.publicationTitle
import fr.decentralia.notestr.domain.model.Note
import java.security.SecureRandom
import java.time.Duration
import kotlinx.coroutines.CancellationException
import org.nostrdevkit.sdk.AckPolicy
import org.nostrdevkit.sdk.ReqTarget
import org.nostrdevkit.sdk.SendEventTarget
import org.nostrdevkit.sdk.Client
import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.EventBuilder
import org.nostrdevkit.sdk.Filter
import org.nostrdevkit.sdk.Kind
import org.nostrdevkit.sdk.PublicKey
import org.nostrdevkit.sdk.RelayUrl
import org.nostrdevkit.sdk.Tag
import org.nostrdevkit.sdk.Timestamp

/** Shared protocol for local keys and Amber: identical backup and deletion semantics. */
abstract class BaseNostrRepository(
    protected val publicKey: PublicKey,
    private val relayStrings: List<String>,
    private val cache: EventCache
) : NostrRepository, AutoCloseable {
    private val client = Client()
    private var connected = false
    private var events: List<Event> = emptyList()

    protected abstract suspend fun sign(builder: EventBuilder): Event
    protected abstract suspend fun encrypt(markdown: String): String
    protected abstract suspend fun decrypt(content: String): String

    override fun publicKeyHex(): String = publicKey.toHex()

    private fun loadCache(): List<Event> = NoteEvents.verified(cache.load().mapNotNull {
        runCatching { Event.fromJson(it) }.getOrNull()
    }, publicKey)

    private fun merge(incoming: List<Event>) {
        events = NoteEvents.compact(NoteEvents.verified(events + loadCache() + incoming, publicKey))
    }

    private fun persist() = cache.save(events.map(Event::asJson))

    override suspend fun refresh(): Result<List<Note>> = operation {
        merge(emptyList())
        // Fetch separately so backups cannot displace notes/deletions in a shared limit.
        for (kind in listOf(NoteEvents.NOTE, NoteEvents.DELETE, NoteEvents.BACKUP)) {
            try {
                connect()
                val fetched = client.fetchEvents(ReqTarget.auto(listOf(Filter().author(publicKey).kind(Kind(kind)).limit(2_000uL))),
                    timeout = Duration.ofSeconds(10), maxEvents = 2_000u)
                merge(fetched)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Keep encrypted local data on a relay failure. */ }
        }
        persist()
        NoteEvents.latest(events, NoteEvents.NOTE, publicKey).mapNotNull { event ->
            try { event.toNote(decrypt(event.content())) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                if (requiresInteractiveDecryption) throw error
                null
            }
        }.sortedByDescending(Note::createdAt)
    }

    protected open val requiresInteractiveDecryption = false

    override suspend fun previous(note: Note): Result<String?> = operation {
        merge(emptyList())
        val backup = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.backupAddress(note.identifier) }
        backup?.let { decrypt(it.content()) }
    }

    override suspend fun publish(markdown: String, previous: Note?): Result<Publication> = operation {
        require(markdown.isNotBlank()) { "La note est vide." }
        merge(emptyList())
        val old = previous?.let { note ->
            val event = Event.fromJson(note.eventJson)
            require(NoteEvents.verified(listOf(event), publicKey).size == 1 &&
                event.kind().asU16() == NoteEvents.NOTE && NoteEvents.identifier(event) == note.identifier &&
                event.id().toHex() == note.eventId) { "La version précédente est invalide." }
            val latest = NoteEvents.latest(events, NoteEvents.NOTE, publicKey)
                .firstOrNull { NoteEvents.identifier(it) == note.identifier }
            require(latest == null || latest.id() == event.id()) {
                "La note a changé. Actualisez et ouvrez sa dernière version."
            }
            event
        }
        val identifier = previous?.identifier ?: randomIdentifier()
        val existingBackup = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.backupAddress(identifier) }
        val timestamp = NoteEvents.updateTime(old, existingBackup)
        val normalized = explicitLineBreaks(publicationTitle(markdown))
        require(normalized.toByteArray(Charsets.UTF_8).size in 1..65535) {
            "Le Markdown doit contenir entre 1 et 65 535 octets UTF-8 (NIP-44 v2)."
        }
        val update = sign(EventBuilder(Kind(NoteEvents.NOTE), encrypt(normalized))
            .tags(listOf(Tag.identifier(identifier))).customCreatedAt(timestamp))
        val backup = old?.let { sign(NoteEvents.backupBuilder(it, update)) }
        connect()
        publishInOrder(update, backup, relayStrings.map(RelayUrl::parse),
            send = { event, targets -> client.sendEvent(event, target = SendEventTarget.to(targets), ackPolicy = AckPolicy.all()).success },
            saveBackup = { event -> merge(listOf(event)); persist() })
        merge(listOf(update))
        val warning = try { persist(); null } catch (_: Exception) {
            "Note publiée, mais écriture du cache local impossible. Actualisez avant de fermer l’application."
        }
        Publication(update.toNote(normalized), warning)
    }

    override suspend fun delete(note: Note): Result<Unit> = operation {
        merge(emptyList())
        val backup = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.backupAddress(note.identifier) }
        val timestamp = maxOf(Timestamp.now().asSecs(), note.createdAt.toULong(), backup?.createdAt()?.asSecs() ?: 0uL)
        val deletion = sign(EventBuilder(Kind(NoteEvents.DELETE), "Suppression depuis Notestr")
            .tags(listOf(
                Tag.parse(listOf("e", note.eventId)),
                Tag.parse(listOf("a", "${NoteEvents.NOTE}:${publicKey.toHex()}:${note.identifier}")),
                Tag.parse(listOf("k", NoteEvents.NOTE.toString())),
                Tag.parse(listOf("a", "${NoteEvents.BACKUP}:${publicKey.toHex()}:${NoteEvents.backupAddress(note.identifier)}")),
                Tag.parse(listOf("k", NoteEvents.BACKUP.toString()))
            )).customCreatedAt(Timestamp.fromSecs(timestamp)))
        connect()
        check(client.sendEvent(deletion, ackPolicy = AckPolicy.all()).success.isNotEmpty()) { "Aucun relais n’a accepté la suppression." }
        merge(listOf(deletion))
        persist()
    }

    private suspend fun connect() {
        if (connected) return
        relayStrings.forEach { client.addRelay(RelayUrl.parse(it)) }
        client.connect()
        connected = true
    }

    private fun Event.toNote(markdown: String) = Note(
        requireNotNull(NoteEvents.identifier(this)), markdown, id().toHex(), createdAt().asSecs().toLong(), asJson()
    )

    private fun randomIdentifier(): String {
        val random = SecureRandom()
        val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(6) { repeat(6) { append(alphabet[random.nextInt(alphabet.length)]) } }
    }

    private suspend fun <T> operation(block: suspend () -> T): Result<T> = try { Result.success(block()) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { Result.failure(error) }

    override fun close() { events = emptyList(); client.close(); publicKey.close() }
}

/** Only send the update to relays that acknowledged the backup, as on Linux. */
internal suspend fun <E, R> publishInOrder(
    update: E, backup: E?, relays: List<R>,
    send: suspend (E, List<R>) -> List<R>, saveBackup: (E) -> Unit
) {
    val targets = if (backup == null) relays else {
        val accepted = send(backup, relays)
        check(accepted.isNotEmpty()) { "Aucun relais n’a accepté la sauvegarde. La note n’a pas été modifiée." }
        saveBackup(backup)
        accepted
    }
    check(send(update, targets).isNotEmpty()) { "Aucun relais n’a accepté la note. Votre texte reste dans l’éditeur." }
}
