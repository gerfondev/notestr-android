package fr.decentralia.notestr.data.nostr

import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.Tag

internal object BlossomServers {
    const val DEFAULT = "https://blossom.nostr.build"

    fun fromProfile(profile: Event?): List<String> {
        return fromTags(profile?.tags()?.map(Tag::toVec).orEmpty())
    }

    fun fromTags(tags: List<List<String>>): List<String> {
        val servers = tags
            .filter { it.firstOrNull() == "server" }
            .mapNotNull { it.getOrNull(1)?.let(::normalize) }.distinct()
        return servers.ifEmpty { listOf(DEFAULT) }
    }

    fun normalize(value: String): String? = try {
        val uri = java.net.URI(value.trim())
        if (uri.scheme != "https" || uri.host.isNullOrBlank() || uri.userInfo != null ||
            (uri.rawPath.isNotEmpty() && uri.rawPath != "/") || uri.rawQuery != null || uri.rawFragment != null) null
        else "https://${uri.rawAuthority}"
    } catch (_: Exception) { null }
}
