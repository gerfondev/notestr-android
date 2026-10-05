package fr.decentralia.notestr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import fr.decentralia.notestr.data.nostr.NostrRepository
import fr.decentralia.notestr.data.nostr.Publication
import fr.decentralia.notestr.domain.model.Note
import fr.decentralia.notestr.ui.NotestrViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BackupRestoreUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val note = Note("abc123", "Version actuelle", "synthetic", 1, "{}")
    private class FakeRepository : NostrRepository {
        var published: String? = null
        var previousAtPublish: Note? = null
        override fun publicKeyHex() = "synthetic"
        override suspend fun refresh() = Result.success(emptyList<Note>())
        override suspend fun previous(note: Note) = Result.success("Version sauvegardée")
        override suspend fun publish(markdown: String, previous: Note?): Result<Publication> {
            published = markdown; previousAtPublish = previous
            return Result.success(Publication(requireNotNull(previous).copy(markdown = markdown)))
        }
        override suspend fun setTrashed(note: Note, trashed: Boolean) = Result.success(Publication(note.copy(trashed = trashed)))
        override suspend fun setPinned(note: Note, pinned: Boolean) = Result.success(Publication(note.copy(pinned = pinned)))
        override suspend fun delete(note: Note) = Result.success(Unit)
    }
    @Test fun trashMoveRestoreAndPermanentDeleteAreManual() {
        var permanent = 0
        val repo = object : NostrRepository by FakeRepository() {
            override suspend fun refresh() = Result.success(listOf(note))
            override suspend fun delete(note: Note): Result<Unit> { permanent++; return Result.success(Unit) }
        }
        lateinit var vm: NotestrViewModel
        compose.runOnIdle {
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            NotestrViewModel::class.java.getDeclaredField("repository").apply { isAccessible = true }.set(vm, repo)
            vm.backToNotes(); vm.refresh()
        }
        compose.waitUntil(5000) { !vm.state.busy }
        compose.onNodeWithText("Version actuelle").performClick()
        compose.onNodeWithContentDescription("Supprimer").performClick()
        compose.onNodeWithText("Annuler").performClick()
        compose.runOnIdle { assertFalse(vm.state.notes.single().trashed); assertEquals(0, permanent) }
        compose.onNodeWithContentDescription("Supprimer").performClick()
        compose.onNodeWithText("Déplacer", substring = false).performClick()
        compose.waitUntil(5000) { !vm.state.busy }
        compose.onNodeWithText("Version actuelle").assertDoesNotExist()
        compose.onNodeWithContentDescription("Corbeille").performClick()
        compose.onNodeWithText("Version actuelle").performClick()
        compose.onNodeWithContentDescription("Publier").assertDoesNotExist()
        compose.onNodeWithContentDescription("Restaurer la note").performClick()
        compose.waitUntil(5000) { !vm.state.busy }
        compose.runOnIdle { assertFalse(vm.state.notes.single().trashed); assertEquals(0, permanent); vm.setTrashed(vm.state.notes.single(), true) }
        compose.waitUntil(5000) { !vm.state.busy }
        compose.onNodeWithText("Version actuelle").performClick()
        compose.onNodeWithContentDescription("Supprimer définitivement").performClick()
        compose.onNodeWithText("Annuler").performClick()
        compose.runOnIdle { assertEquals(0, permanent) }
        compose.onNodeWithContentDescription("Supprimer définitivement").performClick()
        compose.onNodeWithText("Supprimer définitivement", substring = false).performClick()
        compose.waitUntil(5000) { !vm.state.busy }
        compose.runOnIdle { assertEquals(1, permanent); assertTrue(vm.state.notes.isEmpty()) }
    }

    @Test fun cachedNoteCanBeOpenedBeforeNetworkFinishesAndDraftSurvivesRefresh() {
        val network = kotlinx.coroutines.CompletableDeferred<Unit>()
        val repo = object : NostrRepository by FakeRepository() {
            override suspend fun cached() = Result.success(listOf(note))
            override suspend fun refresh(): Result<List<Note>> {
                network.await()
                return Result.success(listOf(note.copy(markdown = "Version distante")))
            }
        }
        lateinit var vm: NotestrViewModel
        compose.runOnIdle {
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            NotestrViewModel::class.java.getDeclaredField("repository").apply { isAccessible = true }.set(vm, repo)
            vm.backToNotes()
            vm.refresh()
        }
        compose.onNodeWithText("Version actuelle").performClick()
        compose.onNodeWithText("Markdown", substring = false).performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("Brouillon conservé")
        compose.runOnIdle {
            assertTrue(vm.state.refreshing)
            network.complete(Unit)
        }
        compose.waitUntil(5000) { !vm.state.busy }
        compose.onNode(hasSetTextAction()).assertTextContains("Brouillon conservé")
        compose.runOnIdle { assertEquals("Version distante", vm.state.notes.single().markdown) }
    }

    @Test fun restoreRequiresConfirmationAndExplicitPublishAndPreservesOriginalIdentity() {
        val repo = FakeRepository()
        compose.runOnIdle {
            val vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            NotestrViewModel::class.java.getDeclaredField("repository").apply { isAccessible = true }.set(vm, repo)
            vm.edit(note)
        }
        compose.onNodeWithContentDescription("Version précédente").performClick()
        compose.onNodeWithText("Annuler").performClick()
        compose.runOnIdle { assertNull(repo.published) }
        compose.onNodeWithContentDescription("Version précédente").performClick()
        compose.onNodeWithText("Charger").performClick()
        compose.onNodeWithText("Markdown", substring = false).performClick()
        compose.onNode(hasSetTextAction()).assertTextContains("Version sauvegardée")
        compose.runOnIdle { assertNull(repo.published) }
        compose.onNodeWithContentDescription("Publier").performClick()
        compose.runOnIdle {
            assertEquals("Version sauvegardée", repo.published)
            assertEquals(note, repo.previousAtPublish)
        }
    }
    @Test fun pinAndUnpinFromListKeepContentAndDate() {
        val repo = FakeRepository()
        lateinit var vm: NotestrViewModel
        compose.runOnIdle {
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            NotestrViewModel::class.java.getDeclaredField("repository").apply { isAccessible = true }.set(vm, repo)
            vm.save(note.markdown, note)
        }
        compose.onNodeWithContentDescription("Épingler").performClick()
        compose.onNodeWithText("Épinglée").assertExists()
        compose.runOnIdle {
            assertTrue(vm.state.notes.single().pinned)
            assertEquals(note.markdown, vm.state.notes.single().markdown)
            assertEquals(note.createdAt, vm.state.notes.single().createdAt)
        }
        compose.onNodeWithContentDescription("Désépingler").performClick()
        compose.onNodeWithText("Épinglée").assertDoesNotExist()
        compose.runOnIdle { assertFalse(vm.state.notes.single().pinned) }
    }
}
