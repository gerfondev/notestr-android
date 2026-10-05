package fr.decentralia.notestr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import fr.decentralia.notestr.data.nostr.*
import fr.decentralia.notestr.domain.model.Note
import fr.decentralia.notestr.ui.NotestrViewModel
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class OfflineUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun saveInterruptsSlowRefreshAndSyncDoesNotReplaceEditorDraft() {
        val base = Note("synthetic", "# Original", "base", 1, "{}")
        val refreshGate = CompletableDeferred<Unit>()
        val syncGate = CompletableDeferred<Unit>()
        var notes = listOf(base)
        val repo = object : NostrRepository {
            override fun publicKeyHex() = "synthetic"
            override suspend fun cached() = Result.success(notes)
            override suspend fun refresh(): Result<List<Note>> { refreshGate.await(); return Result.success(notes) }
            override suspend fun saveLocal(markdown: String, previous: Note?, asCopy: Boolean): Result<Publication> {
                val local = base.copy(markdown = markdown, pending = true)
                notes = listOf(local)
                return Result.success(Publication(local))
            }
            override suspend fun syncPending(): Result<List<Note>> {
                syncGate.await(); notes = notes.map { it.copy(pending = false) }; return Result.success(notes)
            }
            override suspend fun publish(markdown: String, previous: Note?) = error("Save must not wait for relay publication")
            override suspend fun previous(note: Note) = Result.success<String?>(null)
            override suspend fun setPinned(note: Note, pinned: Boolean) = Result.success(Publication(note))
            override suspend fun setTrashed(note: Note, trashed: Boolean) = Result.success(Publication(note))
            override suspend fun delete(note: Note) = Result.success(Unit)
        }
        lateinit var vm: NotestrViewModel
        compose.runOnIdle {
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            NotestrViewModel::class.java.getDeclaredField("repository").apply { isAccessible = true }.set(vm, repo)
            vm.backToNotes(); vm.refresh()
        }
        compose.onNodeWithText("Original").performClick()
        compose.onNodeWithText("Markdown", substring = false).performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("# Saved offline")
        compose.onNodeWithContentDescription("Publier").performClick()
        compose.waitUntil(5000) { !vm.state.busy && vm.state.notes.single().pending }
        compose.onNodeWithText("En attente de synchronisation").assertExists()
        assertFalse(refreshGate.isCompleted) // local save did not wait for slow refresh
        compose.onNodeWithText("Saved offline").performClick()
        compose.onNodeWithText("Markdown", substring = false).performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("# Still typing")
        compose.runOnIdle { syncGate.complete(Unit) }
        compose.waitUntil(5000) { !vm.state.syncing }
        compose.onNode(hasSetTextAction()).assertTextContains("# Still typing")
        compose.runOnIdle { vm.lock() }
    }
}
