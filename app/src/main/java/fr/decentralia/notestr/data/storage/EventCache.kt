package fr.decentralia.notestr.data.storage

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray

/** Cache uniquement les événements signés, dont le contenu des notes reste chiffré NIP-44. */
class EventCache(context: Context) {
    private val directory = context.filesDir
    private val imageDirectory = context.filesDir.resolve("image-cache").apply { mkdirs() }
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
    /** Persist only the encrypted media blob; the decryption key remains inside the NIP-44 note. */
    fun loadImage(hash: String): ByteArray? {
        require(hash.matches(Regex("[a-f0-9]{64}")))
        val file = AtomicFile(imageDirectory.resolve("$hash.bin"))
        return runCatching { file.openRead().use { input -> input.readBytes().takeIf { it.size in 17..(20 * 1024 * 1024 + 16) } } }.getOrNull()
    }
    fun saveImage(hash: String, bytes: ByteArray) {
        require(hash.matches(Regex("[a-f0-9]{64}")) && bytes.size in 17..(20 * 1024 * 1024 + 16))
        val file = AtomicFile(imageDirectory.resolve("$hash.bin"))
        val stream = file.startWrite()
        try { stream.write(bytes); file.finishWrite(stream) }
        catch (error: Exception) { file.failWrite(stream); throw error }
    }
    fun deleteImage(hash: String) {
        if (hash.matches(Regex("[a-f0-9]{64}"))) AtomicFile(imageDirectory.resolve("$hash.bin")).delete()
    }
    // Pending edits survive account reconnection; they are scoped to the public key.
    fun clear() {
        file.delete()
        imageDirectory.listFiles()?.forEach { it.delete() }
        imageDirectory.delete()
    }
}
