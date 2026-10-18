package fr.decentralia.notestr.data.nostr

import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.Tag

internal object BlossomServers {
    const val DEFAULT = "https://blossom.nostr.build"

    fun userAgent(versionName: String): String = "Notestr/$versionName"

    fun fromProfile(profile: Event?): List<String> {
        return fromTags(profile?.tags()?.map(Tag::toVec).orEmpty())
    }

    fun fromTags(tags: List<List<String>>): List<String> {
        val servers = tags
            .filter { it.firstOrNull() == "server" }
            .mapNotNull { it.getOrNull(1)?.let(::normalize) }.distinct()
        return servers.ifEmpty { listOf(DEFAULT) }
    }

    /** Preserve the note's original blob location after trying current profile mirrors. */
    fun imageFallbacks(referenceServer: String, profileServers: List<String>): List<String> {
        val origin = normalize(referenceServer) ?: return profileServers.mapNotNull(::normalize).distinct()
        return (profileServers + origin).mapNotNull(::normalize).distinct()
    }

    /** Prefer the exact URL returned by BUD-02, then try portable hash routes on mirrors. */
    fun imageDownloadUrls(blobUrl: String?, referenceServer: String, digest: String, profileServers: List<String>): List<String> {
        val urls = linkedSetOf<String>()
        val descriptor = blobUrl?.let { validBlobUrl(it, digest) }
        if (descriptor != null) urls += descriptor
        val parsedDescriptor = descriptor?.let { runCatching { java.net.URI(it) }.getOrNull() }
        val servers = imageFallbacks(referenceServer, profileServers)
        for (server in servers) {
            val base = normalize(server) ?: continue
            listOf("", ".jpg", ".jpeg", ".png", ".webp", ".bin").forEach { suffix -> urls += "$base/$digest$suffix" }
            if (parsedDescriptor != null) {
                val authority = java.net.URI(base).rawAuthority
                urls += java.net.URI("https", authority, parsedDescriptor.rawPath, parsedDescriptor.rawQuery, null).toASCIIString()
            }
        }
        return urls.toList()
    }

    fun validBlobUrl(value: String, digest: String): String? = try {
        val uri = java.net.URI(value.trim())
        if (uri.scheme != "https" || uri.host.isNullOrBlank() || uri.userInfo != null ||
            !uri.rawPath.contains(digest) || uri.rawFragment != null) null else uri.toASCIIString()
    } catch (_: Exception) { null }

    fun normalize(value: String): String? = try {
        val uri = java.net.URI(value.trim())
        if (uri.scheme != "https" || uri.host.isNullOrBlank() || uri.userInfo != null ||
            (uri.rawPath.isNotEmpty() && uri.rawPath != "/") || uri.rawQuery != null || uri.rawFragment != null) null
        else "https://${uri.rawAuthority}"
    } catch (_: Exception) { null }

    fun mayRetryLegacyAuthorization(status: Int, detail: String): Boolean = status in setOf(400, 401) &&
        Regex("invalid auth|signature mismatch|invalid nostr event", RegexOption.IGNORE_CASE).containsMatchIn(detail)
}
