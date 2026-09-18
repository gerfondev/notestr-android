package fr.decentralia.notestr

import fr.decentralia.notestr.domain.noteTitle
import fr.decentralia.notestr.domain.pagesCompatibleTitle
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTitleTest {
    @Test fun emphasisIsNotPartOfTheTitle() {
        assertEquals("Test version Android", noteTitle("***Test version Android***\nSuite"))
        assertEquals("Titre important", noteTitle("## **Titre** *important*"))
        assertEquals("Titre", noteTitle("###### ___Titre___ ###"))
        assertEquals("Titre barré", noteTitle("~~Titre barré~~"))
    }
    @Test fun keepsLiteralPunctuationAndCode() {
        assertEquals("snake_case et 2 * 3", noteTitle("snake_case et 2 * 3"))
        assertEquals("*littéral*", noteTitle("\\*littéral\\*"))
        assertEquals("**code**", noteTitle("`**code**`"))
        assertEquals("C#", noteTitle("C#"))
    }
    @Test fun blankAndLinkLabels() {
        assertEquals("Sans titre", noteTitle(" \n\n"))
        assertEquals("Lien", noteTitle("\n[**Lien**](https://example.test)\nSuite"))
    }
    @Test fun simpleEmphasizedTitleBecomesPagesHeadingOnlyAtPublication() {
        assertEquals("# Test version Android\n\n~~Texte barré~~", pagesCompatibleTitle("***Test version Android***\n\n~~Texte barré~~"))
        assertEquals("\r\n# Titre\r\nCorps", pagesCompatibleTitle("\r\n**Titre**\r\nCorps"))
        assertEquals("# Titre", pagesCompatibleTitle(pagesCompatibleTitle("___Titre___")))
    }
    @Test fun publicationPreservesComplexAndExistingTitles() {
        for (text in listOf("# Titre", "## Titre", "**Un** et **deux**", "* Liste", "***", "Texte\n**Suite**", "`code`")) {
            assertEquals(text, pagesCompatibleTitle(text))
        }
    }
}
