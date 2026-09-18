package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.domain.model.Note

interface NostrRepository {
    fun publicKeyHex(): String
    suspend fun refresh(): Result<List<Note>>
    suspend fun publish(markdown: String, identifier: String? = null): Result<Note>
    suspend fun delete(note: Note): Result<Unit>
}
