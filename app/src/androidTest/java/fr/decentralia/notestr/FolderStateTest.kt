package fr.decentralia.notestr

import fr.decentralia.notestr.domain.model.FolderState
import fr.decentralia.notestr.domain.model.NoteFolder
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderStateTest {
    @Test fun serializedFolderAssignmentsRoundTrip() {
        val folder = NoteFolder("0123456789abcdef0123456789abcdef", "Voyages")
        val state = FolderState(listOf(folder), mapOf("note-1" to folder.id))
        assertEquals(state, FolderState.fromJson(state.toJson()))
    }
}
