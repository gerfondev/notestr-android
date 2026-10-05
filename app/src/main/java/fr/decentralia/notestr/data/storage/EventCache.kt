package fr.decentralia.notestr.data.storage

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray

/** Cache uniquement les événements signés, dont le contenu des notes reste chiffré NIP-44. */
class EventCache(context: Context) {
    private val directory = context.filesDir
    private val file = AtomicFile(context.filesDir.resolve("events-cache.json"))
    fun save(events: List<String>) {
        val array = JSONArray(); events.forEach(array::put)
        val stream = file.startWrite()
        try {
            stream.write(array.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (error: Exception) {
            file.failWrite(stream)
            throw error
        }
    }
    fun load(): List<String> = runCatching {
        val array = JSONArray(file.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() })
        List(array.length()) { array.getString(it) }
    }.getOrDefault(emptyList())
    fun pending(author: String): List<String> {
        require(author.matches(Regex("[a-f0-9]{64}")))
        val pending = AtomicFile(directory.resolve("pending-$author.json"))
        if (!pending.baseFile.exists()) return emptyList()
        val array = JSONArray(pending.openRead().bufferedReader().use { it.readText() })
        return List(array.length()) { array.getString(it) }
    }
    fun savePending(author: String, events: List<String>) {
        require(author.matches(Regex("[a-f0-9]{64}")))
        val pending = AtomicFile(directory.resolve("pending-$author.json"))
        val array = JSONArray(); events.forEach(array::put)
        val stream = pending.startWrite()
        try { stream.write(array.toString().toByteArray(Charsets.UTF_8)); pending.finishWrite(stream) }
        catch (error: Exception) { pending.failWrite(stream); throw error }
    }
    // Pending edits survive account reconnection; they are scoped to the public key.
    fun clear() { file.delete() }
}
