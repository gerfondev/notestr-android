package fr.decentralia.notestr.domain.model

import org.junit.Assert.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderStateTest {
    private val folder = NoteFolder("0123456789abcdef0123456789abcdef", "Voyages")

    @Test fun folderNamesAreNormalizedBeforeSaving() {
        val state = FolderState(listOf(folder.copy(name = "  Cafe\u0301  "))).validated()
        assertEquals("Café", state.folders.single().name)
    }

    @Test fun duplicateNamesAndUnknownFolderAssignmentsAreRejected() {
        val other = NoteFolder("abcdef0123456789abcdef0123456789", "Voyages")
        assertThrows(IllegalArgumentException::class.java) {
            FolderState(listOf(folder, other)).validated()
        }
        assertThrows(IllegalArgumentException::class.java) {
            FolderState(listOf(folder), mapOf("note-1" to "f".repeat(32))).validated()
        }
    }

    @Test fun folderNamesAreUniqueRegardlessOfCase() {
        val other = NoteFolder("abcdef0123456789abcdef0123456789", " voyages ")
        assertThrows(IllegalArgumentException::class.java) {
            FolderState(listOf(folder, other)).validated()
        }
    }
}
