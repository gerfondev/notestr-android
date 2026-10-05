package fr.decentralia.notestr.domain.model

import fr.decentralia.notestr.domain.noteTitle

data class Note(
    val identifier: String,
    val markdown: String,
    val eventId: String,
    val createdAt: Long,
    val eventJson: String,
    val pinned: Boolean = false,
    val trashed: Boolean = false,
    val pending: Boolean = false,
    val conflicted: Boolean = false
) {
    val listKey: String get() = (if (pending) "pending:" else "note:") + identifier
    val title: String
        get() = noteTitle(markdown)
}

val noteOrder: Comparator<Note> = compareByDescending<Note> { it.pinned }
    .thenByDescending { it.createdAt }.thenBy { it.identifier }
