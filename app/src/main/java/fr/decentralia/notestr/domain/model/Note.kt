package fr.decentralia.notestr.domain.model

import fr.decentralia.notestr.domain.noteTitle

data class Note(
    val identifier: String,
    val markdown: String,
    val eventId: String,
    val createdAt: Long,
    val eventJson: String
) {
    val title: String
        get() = noteTitle(markdown)
}
