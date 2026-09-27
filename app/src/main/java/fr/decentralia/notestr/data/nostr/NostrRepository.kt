package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.domain.model.Note

interface NostrRepository {
    fun publicKeyHex(): String
    suspend fun refresh(): Result<List<Note>>
    suspend fun publish(markdown: String, previous: Note? = null): Result<Publication>
    suspend fun previous(note: Note): Result<String?>
    suspend fun delete(note: Note): Result<Unit>
    suspend fun setPinned(note: Note, pinned: Boolean): Result<Publication>
}

data class Publication(val note: Note, val warning: String? = null)
