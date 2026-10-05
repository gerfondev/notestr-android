package fr.decentralia.notestr.data.nostr

import org.json.JSONObject
import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.PublicKey

/** Only signed events with encrypted content are stored; never plaintext drafts. */
internal data class PendingEdit(val update: Event, val base: Event?, val backup: Event?, val sent: Boolean = false) {
    val identifier get() = requireNotNull(NoteEvents.identifier(update))
    fun json(): String = JSONObject().put("sent", sent).put("update", JSONObject(update.asJson()))
        .put("base", base?.let { JSONObject(it.asJson()) } ?: JSONObject.NULL)
        .put("backup", backup?.let { JSONObject(it.asJson()) } ?: JSONObject.NULL).toString()
    companion object {
        fun parse(raw: String, author: PublicKey): PendingEdit {
            val row = JSONObject(raw)
            val update = Event.fromJson(row.getJSONObject("update").toString())
            val base = row.optJSONObject("base")?.let { Event.fromJson(it.toString()) }
            val backup = row.optJSONObject("backup")?.let { Event.fromJson(it.toString()) }
            require(NoteEvents.verified(listOfNotNull(update, base, backup), author).size == listOfNotNull(update, base, backup).size)
            require(update.kind().asU16() == NoteEvents.NOTE && NoteEvents.identifier(update) != null)
            require(NoteEvents.tags(update, "notestr-base") == listOf(base?.id()?.toHex().orEmpty()))
            if (base != null) {
                require(base.kind().asU16() == NoteEvents.NOTE && NoteEvents.identifier(base) == NoteEvents.identifier(update))
                require(backup != null && backup.kind().asU16() == NoteEvents.BACKUP &&
                    NoteEvents.identifier(backup) == NoteEvents.backupAddress(requireNotNull(NoteEvents.identifier(base))) &&
                    backup.createdAt().asSecs() == update.createdAt().asSecs() &&
                    backup.content() == base.content() && NoteEvents.tags(backup, "e") == listOf(base.id().toHex()))
            } else require(backup == null)
            return PendingEdit(update, base, backup, row.optBoolean("sent", false))
        }
    }
}
