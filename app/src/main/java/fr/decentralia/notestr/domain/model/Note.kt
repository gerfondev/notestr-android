package fr.decentralia.notestr.domain.model

import fr.decentralia.notestr.domain.noteTitle

data class Note(
    val identifier: String,
    val markdown: String,
    val eventId: String,
    val createdAt: Long,
    val eventJson: String,
    val pinned: Boolean = false
) {
    val title: String
        get() = noteTitle(markdown)
}

val noteOrder: Comparator<Note> = compareByDescending<Note> { it.pinned }
    .thenByDescending { it.createdAt }.thenBy { it.identifier }
