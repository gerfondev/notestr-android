package fr.decentralia.notestr.domain

/** A display label only: do not rewrite the note's Markdown to clean up a list title. */
fun noteTitle(markdown: String): String {
    var text = markdown.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: return "Sans titre"
    text = text.replace(Regex("^#{1,6}\\s+"), "").replace(Regex("\\s+#+\\s*$"), "")
    val literals = mutableListOf<String>()
    fun protect(value: String): String { literals.add(value); return "\uE000${literals.lastIndex}\uE001" }
    // Escaped punctuation and code spans are literal, not emphasis delimiters.
    text = Regex("\\\\([\\p{Punct}])").replace(text) { protect(it.groupValues[1]) }
    text = Regex("(`+)(.+?)\\1").replace(text) { protect(it.groupValues[2]) }
    text = Regex("!?\\[([^]\\n]+)]\\([^\\n]*?\\)").replace(text) { it.groupValues[1] }
    repeat(3) {
        text = Regex("(\\*{1,3}|~~)(?=\\S)(.+?)(?<=\\S)\\1").replace(text) { it.groupValues[2] }
        text = Regex("(?<![\\p{L}\\p{N}])(_{1,3})(?=\\S)(.+?)(?<=\\S)\\1(?![\\p{L}\\p{N}])")
            .replace(text) { it.groupValues[2] }
    }
    text = Regex("\uE000(\\d+)\uE001").replace(text) { literals.getOrNull(it.groupValues[1].toInt()) ?: it.value }
    return text.trim().ifBlank { "Sans titre" }
}

/** Normalize a simple emphasized first line to an explicit Markdown heading at publication.
 * Convert only a simple, wholly emphasized first line; preserve all other content.
 */
fun publicationTitle(markdown: String): String {
    val first = Regex("[^\\r\\n]+").findAll(markdown).firstOrNull { it.value.isNotBlank() } ?: return markdown
    val emphasized = Regex("^(\\*{1,3}|_{1,3})([^*_`<>\\[\\]\\\\]+)\\1$").matchEntire(first.value.trim()) ?: return markdown
    val title = emphasized.groupValues[2].trim()
    if (title.isBlank()) return markdown
    return markdown.replaceRange(first.range, "# $title")
}
