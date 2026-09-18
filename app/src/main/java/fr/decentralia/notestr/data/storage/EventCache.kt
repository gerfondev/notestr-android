package fr.decentralia.notestr.data.storage

import android.content.Context
import org.json.JSONArray

/** Cache uniquement les événements signés, dont le contenu des notes reste chiffré NIP-44. */
class EventCache(context: Context) {
    private val file = context.filesDir.resolve("events-cache.json")
    fun save(events: List<String>) {
        val array = JSONArray(); events.forEach(array::put)
        file.writeText(array.toString(), Charsets.UTF_8)
    }
    fun load(): List<String> = runCatching {
        val array = JSONArray(file.readText(Charsets.UTF_8)); List(array.length()) { array.getString(it) }
    }.getOrDefault(emptyList())
    fun clear() { file.delete() }
}
