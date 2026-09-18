package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.data.storage.EventCache
import fr.decentralia.notestr.domain.model.Note
import fr.decentralia.notestr.domain.explicitLineBreaks
import fr.decentralia.notestr.domain.pagesCompatibleTitle
import java.security.SecureRandom
import java.time.Duration
import rust.nostr.sdk.Client
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.EventDeletionRequest
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Keys
import rust.nostr.sdk.Kind
import rust.nostr.sdk.Nip44Version
import rust.nostr.sdk.NostrSigner
import rust.nostr.sdk.RelayUrl
import rust.nostr.sdk.SecretKey
import rust.nostr.sdk.Tag
import rust.nostr.sdk.nip44Decrypt
import rust.nostr.sdk.nip44Encrypt

class RustNostrRepository(
    privateKey: CharArray,
    private val relayStrings: List<String>,
    private val cache: EventCache
) : NostrRepository, AutoCloseable {
    private val secret = SecretKey.parse(privateKey.concatToString())
    private val keys = Keys(secret)
    private val publicKey = keys.publicKey()
    private val client = Client(NostrSigner.keys(keys))
    private var connected = false

    init { privateKey.fill('\u0000') }

    override suspend fun refresh(): Result<List<Note>> = runCatching {
        val networkEvents = runCatching {
            connect()
            client.fetchEvents(
                Filter().author(publicKey).kinds(listOf(Kind(KIND_FILE), Kind(KIND_DELETE))).limit(2_000uL),
                Duration.ofSeconds(10)
            ).toVec().filter { it.verify() && it.author() == publicKey }
        }
        val events = networkEvents.getOrElse {
            cache.load().mapNotNull { json -> runCatching { Event.fromJson(json) }.getOrNull() }
        }
        if (networkEvents.isSuccess) cache.save(events.map(Event::asJson))
        decode(events)
    }

    override suspend fun publish(markdown: String, identifier: String?): Result<Note> = runCatching {
        require(markdown.isNotBlank()) { "La note est vide." }
        val d = identifier ?: randomIdentifier()
        require(D_PATTERN.matches(d)) { "Identifiant de note invalide." }
        connect()
        val normalized = explicitLineBreaks(pagesCompatibleTitle(markdown))
        val encrypted = nip44Encrypt(secret, publicKey, normalized, Nip44Version.V2)
        val event = EventBuilder(Kind(KIND_FILE), encrypted)
            .tags(listOf(Tag.identifier(d))).signWithKeys(keys)
        val output = client.sendEvent(event)
        check(output.success.isNotEmpty()) { "Aucun relais n'a accepté la note." }
        mergeCache(event)
        event.toNote(d, normalized)
    }

    override suspend fun delete(note: Note): Result<Unit> = runCatching {
        connect()
        val request = EventDeletionRequest(
            ids = listOf(EventId.parse(note.eventId)),
            coordinates = listOf(Coordinate(Kind(KIND_FILE), publicKey, note.identifier)),
            reason = "Suppression depuis Notestr"
        )
        val deletion = EventBuilder.delete(request).signWithKeys(keys)
        val output = client.sendEvent(deletion)
        check(output.success.isNotEmpty()) { "Aucun relais n'a accepté la suppression." }
        mergeCache(deletion)
    }

    override fun publicKeyHex(): String = publicKey.toHex()

    private suspend fun connect() {
        if (connected) return
        relayStrings.forEach { client.addRelay(RelayUrl.parse(it)) }
        client.connect()
        connected = true
    }

    private fun decode(events: List<Event>): List<Note> {
        val deletions = events.filter { it.kind().asU16() == KIND_DELETE }
        val deletedIds = deletions.flatMap { event ->
            event.tags().toVec().map(Tag::asVec).filter { it.firstOrNull() == "e" }.mapNotNull { it.getOrNull(1) }
        }.toSet()
        val deletedCoordinates = deletions.flatMap { event ->
            event.tags().toVec().map(Tag::asVec).filter { it.firstOrNull() == "a" }.mapNotNull { it.getOrNull(1) }
        }.toSet()
        return events.asSequence()
            .filter { it.kind().asU16() == KIND_FILE && it.id().toHex() !in deletedIds }
            .mapNotNull { event -> event.identifier()?.let { it to event } }
            .filter { (d, _) -> "$KIND_FILE:${publicKey.toHex()}:$d" !in deletedCoordinates }
            .groupBy({ it.first }, { it.second })
            .mapNotNull { (d, versions) ->
                val event = versions.maxByOrNull { it.createdAt().asSecs() } ?: return@mapNotNull null
                runCatching {
                    val clear = nip44Decrypt(secret, publicKey, event.content())
                    event.toNote(d, clear)
                }.getOrNull()
            }.sortedByDescending(Note::createdAt)
    }

    private fun Event.identifier(): String? = tags().toVec().asSequence().map(Tag::asVec)
        .firstOrNull { it.firstOrNull() == "d" }?.getOrNull(1)?.takeIf(D_PATTERN::matches)

    private fun Event.toNote(identifier: String, markdown: String) = Note(
        identifier, markdown, id().toHex(), createdAt().asSecs().toLong(), asJson()
    )

    private fun mergeCache(event: Event) {
        val cached = cache.load().toMutableList(); cached += event.asJson(); cache.save(cached.takeLast(2_500))
    }

    private fun randomIdentifier(): String = buildString(6) {
        repeat(6) { append(ALPHABET[SecureRandom().nextInt(ALPHABET.length)]) }
    }

    override fun close() { client.close(); keys.close(); secret.close() }

    private companion object {
        val KIND_FILE: UShort = 33457u
        val KIND_DELETE: UShort = 5u
        val D_PATTERN = Regex("^[a-z0-9]{6}$")
        const val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
    }
}
