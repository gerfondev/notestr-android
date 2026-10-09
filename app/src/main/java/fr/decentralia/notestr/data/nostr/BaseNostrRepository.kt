package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.i18n.tr

import fr.decentralia.notestr.data.storage.EventCache
import fr.decentralia.notestr.domain.explicitLineBreaks
import fr.decentralia.notestr.domain.publicationTitle
import fr.decentralia.notestr.domain.model.Note
import fr.decentralia.notestr.domain.model.noteOrder
import java.security.SecureRandom
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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
import org.json.JSONObject

/** Shared protocol for local keys and Amber: identical backup and deletion semantics. */
abstract class BaseNostrRepository(
    protected val publicKey: PublicKey,
    private val relayStrings: List<String>,
    private val cache: EventCache
) : NostrRepository, AutoCloseable {
    private val client = Client()
    private val operationMutex = Mutex()
    private var connected = false
    private var events: List<Event> = emptyList()
    private var cacheLoaded = false
    // Session-only plaintext: cleared on lock, never written to disk.
    private val decrypted = mutableMapOf<String, String>()
    private suspend fun plaintext(event: Event): String {
        val id = event.id().toHex()
        return decrypted[id] ?: decrypt(event.content()).also { decrypted[id] = it }
    }

    protected open val operationContext: CoroutineContext = EmptyCoroutineContext

    protected abstract suspend fun sign(builder: EventBuilder): Event
    protected abstract suspend fun encrypt(markdown: String): String
    protected abstract suspend fun decrypt(content: String): String

    override fun publicKeyHex(): String = publicKey.toHex()

    private fun loadCache(): List<Event> = NoteEvents.verified(cache.load().mapNotNull {
        runCatching { Event.fromJson(it) }.getOrNull()
    }, publicKey)

    private fun merge(incoming: List<Event>) {
        if (!cacheLoaded) {
            events = NoteEvents.compact(events + loadCache())
            cacheLoaded = true
        }
        events = NoteEvents.compact(events + NoteEvents.verified(incoming, publicKey))
        val retained = events.map { it.id().toHex() }.toSet()
        decrypted.keys.retainAll(retained)
    }

    private fun persist() = cache.save(events.map(Event::asJson))

    override suspend fun cached(): Result<List<Note>> = operation {
        merge(emptyList())
        withPending(notes())
    }

    override suspend fun refresh(): Result<List<Note>> = operation {
        cacheLoaded = false
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
        withPending(notes())
    }

    private fun pendingEdits(): List<PendingEdit> = cache.pending(publicKeyHex()).map { PendingEdit.parse(it, publicKey) }.also {
        require(it.map { edit -> edit.identifier }.distinct().size == it.size) { tr("Fichier de modifications locales invalide.") }
    }
    private fun savePending(edits: List<PendingEdit>) = cache.savePending(publicKeyHex(), edits.map { it.json() })

    private fun conflict(edit: PendingEdit, current: Note?): Boolean {
        if (NoteEvents.permanentlyDeleted(events, publicKey, edit.identifier)) return true
        if (current?.trashed == true) return true
        if (current?.eventId == edit.update.id().toHex()) return false // acknowledgement lost before local commit
        return current?.eventId != edit.base?.id()?.toHex()
    }

    private suspend fun withPending(remote: List<Note>): List<Note> {
        val result = remote.toMutableList()
        val stored = pendingEdits()
        val retained = stored.filterNot { it.sent && NoteEvents.permanentlyDeleted(events, publicKey, it.identifier) }
        if (retained.size != stored.size) savePending(retained)
        for (edit in retained) {
            val current = remote.firstOrNull { it.identifier == edit.identifier }
            val conflicted = conflict(edit, current)
            // Keep the last acknowledged local edit for simultaneous-device races.
            // A remote child based on our event is intentional; a sibling is a conflict.
            if (edit.sent) {
                if (!conflicted || current == null || current.trashed) continue
                val remoteBase = NoteEvents.tags(Event.fromJson(current.eventJson), "notestr-base").firstOrNull()
                if (remoteBase != null && remoteBase != edit.base?.id()?.toHex().orEmpty()) continue
            }
            if (!conflicted) result.removeAll { it.identifier == edit.identifier }
            result += edit.update.toNote(plaintext(edit.update)).copy(
                pinned = current?.pinned ?: false, pending = true, conflicted = conflicted)
        }
        return result.sortedWith(noteOrder)
    }

    override suspend fun saveLocal(markdown: String, previous: Note?, asCopy: Boolean): Result<Publication> = operation {
        require(markdown.isNotBlank()) { tr("La note est vide.") }
        require(previous?.trashed != true) { tr("Restaurez la note depuis la corbeille avant de la modifier.") }
        merge(emptyList())
        val edits = pendingEdits().toMutableList()
        val existing = previous?.let { n -> edits.firstOrNull { it.identifier == n.identifier } }
        if (existing != null && !existing.sent) require(previous!!.pending && existing.update.id().toHex() == previous.eventId) {
            tr("La note a changé. Actualisez et ouvrez sa dernière version.")
        }
        // A locally created pending note has no remote base.
        val actualBase = if (asCopy) null else if (existing != null && previous?.pending == true) existing.base else previous?.let { Event.fromJson(it.eventJson) }
        actualBase?.let { require(NoteEvents.verified(listOf(it), publicKey).size == 1) }
        val identifier = if (asCopy) randomIdentifier() else previous?.identifier ?: randomIdentifier()
        val normalized = explicitLineBreaks(publicationTitle(markdown))
        require(normalized.toByteArray(Charsets.UTF_8).size in 1..65535) { tr("Le Markdown doit contenir entre 1 et 65 535 octets UTF-8 (NIP-44 v2).") }
        val created = maxOf(Timestamp.now().asSecs(), (actualBase?.createdAt()?.asSecs() ?: 0uL) + 1uL)
        val update = sign(EventBuilder(Kind(NoteEvents.NOTE), encrypt(normalized))
            .tags(listOf(Tag.identifier(identifier), Tag.parse(listOf("notestr-base", actualBase?.id()?.toHex().orEmpty()))))
            .customCreatedAt(Timestamp.fromSecs(created)))
        val backup = actualBase?.let { sign(NoteEvents.backupBuilder(it, update)) }
        val edit = PendingEdit(update, actualBase, backup)
        if (previous != null) edits.removeAll { it.identifier == previous.identifier }
        edits += edit
        savePending(edits) // durable save before any network request or success message
        val current = notes().firstOrNull { it.identifier == identifier }
        Publication(update.toNote(normalized).copy(pinned = current?.pinned ?: false, pending = true,
            conflicted = conflict(edit, current)), tr("Modifications enregistrées sur cet appareil, en attente de synchronisation."))
    }

    override suspend fun discardPending(note: Note): Result<List<Note>> = operation {
        val edits = pendingEdits()
        require(edits.any { it.identifier == note.identifier && it.update.id().toHex() == note.eventId }) {
            tr("La note a changé. Actualisez et ouvrez sa dernière version.")
        }
        savePending(edits.filterNot { it.identifier == note.identifier })
        withPending(notes())
    }

    override suspend fun syncPending(): Result<List<Note>> = operation {
        merge(emptyList())
        val edits = pendingEdits().toMutableList()
        if (edits.none { !it.sent }) return@operation withPending(notes())
        connect()
        // Every configured relay must finish its snapshot before automatic publication.
        merge(completeSnapshot(client, relayStrings, publicKey))
        persist()
        for (edit in edits.filterNot { it.sent }) {
            val current = notes().firstOrNull { it.identifier == edit.identifier }
            if (conflict(edit, current)) continue
            if (current?.eventId != edit.update.id().toHex()) {
                publishInOrder(edit.update, edit.backup, relayStrings.map(RelayUrl::parse),
                    send = { e, targets -> client.sendEvent(e, target = SendEventTarget.to(targets), ackPolicy = AckPolicy.all()).success },
                    saveBackup = { e -> merge(listOf(e)); persist() })
                merge(listOf(edit.update))
                persist() // retain pending entry if local commit fails after relay acceptance
            }
            edits[edits.indexOf(edit)] = edit.copy(sent = true)
            savePending(edits)
        }
        withPending(notes())
    }

    private suspend fun notes(): List<Note> {
        val pins = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .associateBy { NoteEvents.identifier(it) }
        return NoteEvents.latest(events, NoteEvents.NOTE, publicKey).mapNotNull { event ->
            try {
                val pin = pins[NoteEvents.pinAddress(NoteEvents.identifier(event)!!)]
                val pinned = pin?.let { pinValue(it) } ?: false
                val trash = pins[NoteEvents.trashAddress(NoteEvents.identifier(event)!!)]
                val value = trash?.let { plaintext(it) } ?: "false"
                require(value == "true" || value == "false") { tr("État de corbeille invalide.") }
                event.toNote(plaintext(event)).copy(pinned = pinned, trashed = value == "true")
            }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                if (requiresInteractiveDecryption) throw error
                null
            }
        }.sortedWith(noteOrder)
    }

    protected open val requiresInteractiveDecryption = false

    override suspend fun previous(note: Note): Result<String?> = operation {
        merge(emptyList())
        val backup = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.backupAddress(note.identifier) }
        backup?.let { decrypt(it.content()) }
    }

    override suspend fun publish(markdown: String, previous: Note?): Result<Publication> = operation {
        require(markdown.isNotBlank()) { tr("La note est vide.") }
        merge(emptyList())
        val old = previous?.let { note ->
            require(!note.trashed && !isTrashed(note) && !NoteEvents.permanentlyDeleted(events, publicKey, note.identifier)) { tr("Restaurez la note depuis la corbeille avant de la modifier.") }
            val event = Event.fromJson(note.eventJson)
            require(NoteEvents.verified(listOf(event), publicKey).size == 1 &&
                event.kind().asU16() == NoteEvents.NOTE && NoteEvents.identifier(event) == note.identifier &&
                event.id().toHex() == note.eventId) { tr("La version précédente est invalide.") }
            val latest = NoteEvents.latest(events, NoteEvents.NOTE, publicKey)
                .firstOrNull { NoteEvents.identifier(it) == note.identifier }
            require(latest == null || latest.id() == event.id()) {
                tr("La note a changé. Actualisez et ouvrez sa dernière version.")
            }
            event
        }
        val identifier = previous?.identifier ?: randomIdentifier()
        val existingBackup = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.backupAddress(identifier) }
        val timestamp = NoteEvents.updateTime(old, existingBackup)
        val normalized = explicitLineBreaks(publicationTitle(markdown))
        require(normalized.toByteArray(Charsets.UTF_8).size in 1..65535) {
            tr("Le Markdown doit contenir entre 1 et 65 535 octets UTF-8 (NIP-44 v2).")
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
            tr("Note publiée, mais écriture du cache local impossible. Actualisez avant de fermer l’application.")
        }
        Publication(update.toNote(normalized).copy(pinned = previous?.pinned ?: false), warning)
    }

    private suspend fun pinValue(event: Event): Boolean {
        val value = try { plaintext(event) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { if (requiresInteractiveDecryption) throw error else return false }
        return value == "true"
    }

    override suspend fun setPinned(note: Note, pinned: Boolean): Result<Publication> = operation {
        merge(emptyList())
        val address = NoteEvents.pinAddress(note.identifier)
        val previous = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == address }
        val timestamp = NoteEvents.updateTime(previous, null)
        val event = sign(EventBuilder(Kind(NoteEvents.BACKUP), encrypt(pinned.toString()))
            .tags(listOf(Tag.identifier(address))).customCreatedAt(timestamp))
        connect()
        check(client.sendEvent(event, ackPolicy = AckPolicy.all()).success.isNotEmpty()) {
            tr("Aucun relais n’a accepté l’épinglage. Réessayez après reconnexion.")
        }
        merge(listOf(event))
        val warning = try { persist(); null } catch (_: Exception) {
            tr("Épinglage synchronisé, mais écriture du cache local impossible. Actualisez avant de fermer.")
        }
        Publication(note.copy(pinned = pinned), warning)
    }

    private suspend fun isTrashed(note: Note): Boolean {
        val event = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.trashAddress(note.identifier) }
        return event?.let { plaintext(it) != "false" } ?: false
    }

    override suspend fun setTrashed(note: Note, trashed: Boolean): Result<Publication> = operation {
        merge(emptyList())
        require(!NoteEvents.permanentlyDeleted(events, publicKey, note.identifier)) { tr("Cette note a été supprimée définitivement.") }
        val current = NoteEvents.latest(events, NoteEvents.NOTE, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == note.identifier }
        require(current?.id()?.toHex() == note.eventId && isTrashed(note) == note.trashed) {
            tr("La note a changé. Actualisez et ouvrez sa dernière version.")
        }
        val address = NoteEvents.trashAddress(note.identifier)
        val previous = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == address }
        val event = sign(EventBuilder(Kind(NoteEvents.BACKUP), encrypt(trashed.toString()))
            .tags(listOf(Tag.identifier(address))).customCreatedAt(NoteEvents.updateTime(previous, null)))
        connect()
        check(client.sendEvent(event, ackPolicy = AckPolicy.all()).success.isNotEmpty()) {
            tr("Aucun relais n’a accepté la modification de la corbeille. Réessayez après reconnexion.")
        }
        merge(listOf(event))
        val warning = try { persist(); null } catch (_: Exception) {
            tr("Corbeille synchronisée, mais écriture du cache local impossible. Actualisez avant de fermer.")
        }
        Publication(note.copy(trashed = trashed), warning)
    }

    override suspend fun uploadImage(bytes: ByteArray): Result<String> = operation {
        val prepared = withContext(Dispatchers.IO) { ImagePreparation.toSanitizedJpeg(bytes) }
        val image = withContext(Dispatchers.IO) { EncryptedImage.fromJpeg(prepared) }
        connect()
        val profile = client.fetchEvents(
            ReqTarget.auto(listOf(Filter().author(publicKey).kind(Kind(10063u)).limit(20uL))),
            timeout = Duration.ofSeconds(10), maxEvents = 20u
        ).filter { it.author() == publicKey && it.kind().asU16() == 10063u.toUShort() && it.verify() }
            .maxWithOrNull(compareBy<Event> { it.createdAt().asSecs() }.thenByDescending { it.id().toHex() })
        val servers = BlossomServers.fromProfile(profile)
        var failure: Exception? = null
        for (server in servers.distinct()) {
            try {
                val uploadEvent = sign(EventBuilder(Kind(24242u), "Upload encrypted Notestr image")
                    .tags(listOf(
                        Tag.parse(listOf("t", "upload")),
                        Tag.parse(listOf("expiration", (System.currentTimeMillis() / 1000 + 300).toString())),
                        Tag.parse(listOf("server", URL(server).host.lowercase())),
                        Tag.parse(listOf("x", image.hash)),
                    )))
                val token = Base64.getUrlEncoder().withoutPadding().encodeToString(uploadEvent.asJson().toByteArray(Charsets.UTF_8))
                uploadEncryptedBlob(server, image, token)
                withContext(Dispatchers.IO) { cache.saveImage(image.hash, image.ciphertext) }
                return@operation image.markdownReference(server)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { failure = error }
        }
        throw failure ?: IllegalStateException(tr("Aucun serveur d’images n’est configuré."))
    }

    private suspend fun uploadEncryptedBlob(server: String, image: EncryptedImage, token: String) = withContext(Dispatchers.IO) {
        val connection = (URL("$server/upload").openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            instanceFollowRedirects = false
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setFixedLengthStreamingMode(image.ciphertext.size)
            setRequestProperty("Content-Type", "application/octet-stream")
            setRequestProperty("X-SHA-256", image.hash)
            setRequestProperty("Authorization", "Nostr $token")
        }
        try {
            connection.outputStream.use { it.write(image.ciphertext) }
            val status = connection.responseCode
            if (status !in 200..299) throw IllegalStateException("Le serveur Blossom a refusé l’image (HTTP $status).")
            val descriptor = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            check(descriptor.optString("sha256").equals(image.hash, ignoreCase = true)) { "Empreinte Blossom incorrecte." }
        } finally { connection.disconnect() }
    }

    override suspend fun loadImage(reference: String): Result<ByteArray> = operation {
        val uri = android.net.Uri.parse(reference)
        require(uri.scheme == "notestr-image" && uri.host?.matches(Regex("[0-9a-f]{64}")) == true) { tr("Référence d’image invalide.") }
        require(uri.getQueryParameter("mime") == "image/jpeg") { tr("Type d’image invalide.") }
        val server = BlossomServers.normalize(uri.getQueryParameter("server") ?: "") ?: error(tr("Serveur Blossom invalide."))
        val key = Base64.getUrlDecoder().decode(uri.getQueryParameter("key") ?: "")
        val nonce = Base64.getUrlDecoder().decode(uri.getQueryParameter("nonce") ?: "")
        require(key.size == 32 && nonce.size == 12) { tr("Clé d’image invalide.") }
        val digest = uri.host!!
        var encrypted = withContext(Dispatchers.IO) { cache.loadImage(digest) }
        val validCached = encrypted?.let { java.security.MessageDigest.getInstance("SHA-256").digest(it).joinToString("") { b -> "%02x".format(b) } == digest } == true
        if (!validCached) {
            if (encrypted != null) withContext(Dispatchers.IO) { cache.deleteImage(digest) }
            connect()
            val profile = client.fetchEvents(
                ReqTarget.auto(listOf(Filter().author(publicKey).kind(Kind(10063u)).limit(20uL))),
                timeout = Duration.ofSeconds(10), maxEvents = 20u
            ).filter { it.author() == publicKey && it.kind().asU16() == 10063u.toUShort() && it.verify() }
                .maxWithOrNull(compareBy<Event> { it.createdAt().asSecs() }.thenByDescending { it.id().toHex() })
            val allowedServers = BlossomServers.fromProfile(profile)
            val candidates = listOf(server).filter { it in allowedServers } + allowedServers.filter { it != server }
            var lastError: Exception? = null
            for (candidate in candidates) {
                try {
                    encrypted = withContext(Dispatchers.IO) {
                        val connection = (URL("$candidate/$digest.jpg").openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                            instanceFollowRedirects = false
                            connectTimeout = 12_000
                            readTimeout = 20_000
                        }
                        try {
                            check(connection.responseCode == 200) { tr("Image indisponible sur le serveur Blossom.") }
                            val bytes = connection.inputStream.use { stream ->
                                val output = java.io.ByteArrayOutputStream()
                                val buffer = ByteArray(8192)
                                while (true) {
                                    val count = stream.read(buffer)
                                    if (count < 0) break
                                    require(output.size() + count <= 20 * 1024 * 1024 + 16) { tr("Fichier d’image trop volumineux.") }
                                    output.write(buffer, 0, count)
                                }
                                output.toByteArray()
                            }
                            require(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
                                .joinToString("") { "%02x".format(it) } == digest) { tr("Empreinte Blossom incorrecte.") }
                            cache.saveImage(digest, bytes)
                            bytes
                        } finally { connection.disconnect() }
                    }
                    break
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { lastError = error }
            }
            check(encrypted != null) { lastError?.message ?: tr("Image indisponible sur les serveurs Blossom de ce compte.") }
        }
        val encryptedBytes = requireNotNull(encrypted)
        require(java.security.MessageDigest.getInstance("SHA-256").digest(encryptedBytes).joinToString("") { "%02x".format(it) } == digest) {
            tr("Empreinte Blossom incorrecte.")
        }
        EncryptedImage.decrypt(encryptedBytes, key, nonce)
    }

    override suspend fun delete(note: Note): Result<Unit> = operation {
        merge(emptyList())
        require(note.trashed && isTrashed(note)) { tr("Déplacez d’abord la note dans la corbeille.") }
        val backup = NoteEvents.latest(events, NoteEvents.BACKUP, publicKey)
            .firstOrNull { NoteEvents.identifier(it) == NoteEvents.backupAddress(note.identifier) }
        val timestamp = maxOf(Timestamp.now().asSecs(), note.createdAt.toULong(), backup?.createdAt()?.asSecs() ?: 0uL)
        val deletion = sign(EventBuilder(Kind(NoteEvents.DELETE), "Suppression depuis Notestr")
            .tags(listOf(
                Tag.parse(listOf("e", note.eventId)),
                Tag.parse(listOf("a", "${NoteEvents.NOTE}:${publicKey.toHex()}:${note.identifier}")),
                Tag.parse(listOf("k", NoteEvents.NOTE.toString())),
                Tag.parse(listOf("a", "${NoteEvents.BACKUP}:${publicKey.toHex()}:${NoteEvents.backupAddress(note.identifier)}")),
                Tag.parse(listOf("a", "${NoteEvents.BACKUP}:${publicKey.toHex()}:${NoteEvents.pinAddress(note.identifier)}")),
                Tag.parse(listOf("a", "${NoteEvents.BACKUP}:${publicKey.toHex()}:${NoteEvents.trashAddress(note.identifier)}")),
                Tag.parse(listOf("k", NoteEvents.BACKUP.toString()))
            )).customCreatedAt(Timestamp.fromSecs(timestamp)))
        connect()
        check(client.sendEvent(deletion, ackPolicy = AckPolicy.all()).success.isNotEmpty()) { tr("Aucun relais n’a accepté la suppression.") }
        merge(listOf(deletion))
        persist()
        savePending(pendingEdits().filterNot { it.sent && it.identifier == note.identifier })
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

    private suspend fun <T> operation(block: suspend () -> T): Result<T> = try { Result.success(withContext(operationContext) { operationMutex.withLock { block() } }) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { Result.failure(error) }

    override fun close() { decrypted.clear(); cacheLoaded = false; events = emptyList(); client.close(); publicKey.close() }
}

/** Only send the update to relays that acknowledged the backup, as on Linux. */
internal suspend fun <E, R> publishInOrder(
    update: E, backup: E?, relays: List<R>,
    send: suspend (E, List<R>) -> List<R>, saveBackup: (E) -> Unit
) {
    val targets = if (backup == null) relays else {
        val accepted = send(backup, relays)
        check(accepted.isNotEmpty()) { tr("Aucun relais n’a accepté la sauvegarde. La note n’a pas été modifiée.") }
        saveBackup(backup)
        accepted
    }
    check(send(update, targets).isNotEmpty()) { tr("Aucun relais n’a accepté la note. Votre texte reste dans l’éditeur.") }
}
