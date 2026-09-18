package fr.decentralia.notestr

import fr.decentralia.notestr.domain.explicitLineBreaks
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownRulesTest {
    @Test fun simpleBreaksBecomeExplicitAndIdempotent() {
        val source = "**Gras**\n~~Barré~~\n*Italique*\n\nJe suis content\nje suis pas content"
        val expected = "**Gras**  \n~~Barré~~  \n*Italique*\n\nJe suis content  \nje suis pas content"
        assertEquals(expected, explicitLineBreaks(source))
        assertEquals(expected, explicitLineBreaks(expected))
    }

    @Test fun codeHeadingsTablesAndListItemsArePreserved() {
        listOf("```python\na=1\nb=2\n```", "# Titre\n\nTexte", "| A | B |\n| - | - |", "- a\n- b", "un  \ndeux")
            .forEach { assertEquals(it, explicitLineBreaks(it)) }
    }

    @Test fun listContinuationAndCrLf() {
        assertEquals("- un  \n  suite\n- autre", explicitLineBreaks("- un\n  suite\n- autre"))
        assertEquals("un  \r\ndeux", explicitLineBreaks("un\r\ndeux"))
    }
}
