package fr.decentralia.notestr.data.nostr

import java.util.UUID
import kotlinx.coroutines.*
import org.nostrdevkit.sdk.*

/** Unlike fetchEvents, absence of EOSE is never treated as an empty remote note. */
internal suspend fun completeSnapshot(client: Client, relays: List<String>, author: PublicKey): List<Event> = coroutineScope {
    require(relays.isNotEmpty()) { "Aucun relais configuré." }
    relays.map { address -> async {
        withTimeout(20_000) {
            val relay = RelayUrl.parse(address)
            val result = mutableListOf<Event>()
            for (kind in listOf(NoteEvents.NOTE, NoteEvents.DELETE, NoteEvents.BACKUP)) {
                var until: ULong? = null
                var finished = false
                for (pageNumber in 0 until 100) {
                    val id = "pending-" + UUID.randomUUID()
                    var filter = Filter().author(author).kind(Kind(kind)).limit(1000uL)
                    until?.let { filter = filter.until(Timestamp.fromSecs(it)) }
                    val page = mutableListOf<Event>()
                    client.notifications().use { stream ->
                        try {
                            val sent = client.subscribe(ReqTarget.single(relay, listOf(filter)), id)
                            check(sent.success.isNotEmpty()) { "Relais indisponible." }
                            while (true) {
                                val notification = stream.next() ?: error("Connexion interrompue.")
                                if (notification !is ClientNotification.Message || notification.relayUrl != relay) continue
                                when (val message = notification.message.asEnum()) {
                                    is RelayMessageEnum.EventMsg -> if (message.subscriptionId == id) {
                                        check(page.size < 10000) { "Réponse trop volumineuse." }
                                        if (filter.matchEvent(message.event) && message.event.verify()) page += message.event
                                    }
                                    is RelayMessageEnum.EndOfStoredEvents -> if (message.subscriptionId == id) break
                                    is RelayMessageEnum.Closed -> if (message.subscriptionId == id) error("Lecture refusée par le relais.")
                                    else -> Unit
                                }
                            }
                        } finally {
                            withContext(NonCancellable) { withTimeoutOrNull(1000) { client.unsubscribe(id) } }
                        }
                    }
                    result += page
                    if (page.isEmpty()) { finished = true; break }
                    val oldest = page.minOf { it.createdAt().asSecs() }
                    if (oldest == until) {
                        check(page.all { it.createdAt().asSecs() == oldest } && page.size < 1000) { "Lecture incomplète des relais." }
                        if (oldest == 0uL) { finished = true; break }
                        until = oldest - 1uL
                    } else until = oldest
                }
                check(finished) { "Lecture incomplète des relais." }
            }
            result
        }
    } }.awaitAll().flatten()
}
