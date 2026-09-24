package fr.decentralia.notestr.data.storage

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray

/** Cache uniquement les événements signés, dont le contenu des notes reste chiffré NIP-44. */
class EventCache(context: Context) {
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
    fun clear() { file.delete() }
}
