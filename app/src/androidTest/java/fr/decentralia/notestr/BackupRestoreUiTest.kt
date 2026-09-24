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
        override suspend fun delete(note: Note) = Result.success(Unit)
    }
    @Test fun restoreRequiresConfirmationAndExplicitPublishAndPreservesOriginalIdentity() {
        val repo = FakeRepository()
        compose.runOnIdle {
            val vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            NotestrViewModel::class.java.getDeclaredField("repository").apply { isAccessible = true }.set(vm, repo)
            vm.edit(note)
        }
        compose.onNodeWithText("Version précédente").performClick()
        compose.onNodeWithText("Annuler").performClick()
        compose.runOnIdle { assertNull(repo.published) }
        compose.onNodeWithText("Version précédente").performClick()
        compose.onNodeWithText("Charger").performClick()
        compose.onNodeWithText("Markdown", substring = false).performClick()
        compose.onNode(hasSetTextAction()).assertTextContains("Version sauvegardée")
        compose.runOnIdle { assertNull(repo.published) }
        compose.onNodeWithText("Publier").performClick()
        compose.runOnIdle {
            assertEquals("Version sauvegardée", repo.published)
            assertEquals(note, repo.previousAtPublish)
        }
    }
}
