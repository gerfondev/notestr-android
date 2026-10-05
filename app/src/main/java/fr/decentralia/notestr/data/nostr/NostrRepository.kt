package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.domain.model.Note

interface NostrRepository {
    fun publicKeyHex(): String
    suspend fun cached(): Result<List<Note>> = Result.success(emptyList())
    suspend fun refresh(): Result<List<Note>>
    suspend fun publish(markdown: String, previous: Note? = null): Result<Publication>
    suspend fun saveLocal(markdown: String, previous: Note? = null, asCopy: Boolean = false): Result<Publication> = publish(markdown, previous)
    suspend fun syncPending(): Result<List<Note>> = cached()
    suspend fun discardPending(note: Note): Result<List<Note>> = cached()
    suspend fun previous(note: Note): Result<String?>
    suspend fun delete(note: Note): Result<Unit>
    suspend fun setTrashed(note: Note, trashed: Boolean): Result<Publication>
    suspend fun setPinned(note: Note, pinned: Boolean): Result<Publication>
}

data class Publication(val note: Note, val warning: String? = null)
