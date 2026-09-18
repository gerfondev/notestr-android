package fr.decentralia.notestr.domain

/** Rend visibles dans CommonMark les retours simples saisis par l’utilisateur. */
fun explicitLineBreaks(markdown: String): String {
    val separator = if (markdown.contains("\r\n")) "\r\n" else "\n"
    val lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split('\n').toMutableList()
    var fenced = false
    var html = false
    var inlineTicks = false
    for (i in 0 until lines.lastIndex) {
        val current = lines[i]; val next = lines[i + 1]
        val trimmed = current.trimStart(); val nextTrimmed = next.trimStart()
        if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) { fenced = !fenced; continue }
        if (trimmed.startsWith("<") && !trimmed.startsWith("<!--")) html = !trimmed.contains(Regex("</[^>]+>"))
        if (fenced || html || current.startsWith("    ") || current.startsWith("\t")) continue
        if (current.count { it == '`' } % 2 == 1) inlineTicks = !inlineTicks
        if (inlineTicks || current.isBlank() || next.isBlank()) continue
        if (current.endsWith("  ") || current.endsWith("\\")) continue
        if (trimmed.startsWith("#") || trimmed.startsWith("|") || nextTrimmed.startsWith("|") || trimmed.startsWith("<")) continue
        val list = Regex("^(?:[-+*]|\\d+[.)])\\s+")
        if (list.containsMatchIn(trimmed) && list.containsMatchIn(nextTrimmed)) continue
        if ((current.startsWith("  ") || current.startsWith("\t")) && list.containsMatchIn(nextTrimmed)) continue
        lines[i] = current.trimEnd(' ', '\t') + "  "
    }
    return lines.joinToString(separator)
}
