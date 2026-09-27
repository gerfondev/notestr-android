package fr.decentralia.notestr
import fr.decentralia.notestr.domain.model.Note
import fr.decentralia.notestr.domain.model.noteOrder
import org.junit.Assert.*
import org.junit.Test
class PinOrderTest {
    @Test fun pinnedFirstThenDateThenIdentifier() {
        val a = Note("a", "a", "test", 1, "{}", true)
        val b = a.copy(identifier = "b", createdAt = 2)
        val c = a.copy(identifier = "c", createdAt = 999, pinned = false)
        val d = b.copy(identifier = "d")
        assertEquals(listOf(b, d, a, c), listOf(c, a, d, b).sortedWith(noteOrder))
    }
}
