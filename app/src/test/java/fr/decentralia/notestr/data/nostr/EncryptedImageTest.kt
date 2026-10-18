package fr.decentralia.notestr.data.nostr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EncryptedImageTest {
    @Test fun encryptedBytesRoundTripAndTamperingIsRejected() {
        val source = "synthetic private image bytes".toByteArray()
        val image = EncryptedImage.fromJpeg(source)
        assertEquals(source.toList(), EncryptedImage.decrypt(image.ciphertext, image.key, image.nonce).toList())
        val changed = image.ciphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        try {
            EncryptedImage.decrypt(changed, image.key, image.nonce)
            throw AssertionError("A modified ciphertext must not decrypt")
        } catch (_: javax.crypto.AEADBadTagException) { }
    }

    @Test fun markdownReferenceCanCarryTheServerReturnedBlobUrl() {
        val image = EncryptedImage.fromJpeg("synthetic image".toByteArray())
        val blobUrl = "https://cdn.example/blobs/${image.hash}.bin"
        val markdown = image.markdownReference("https://upload.example", blobUrl)
        assertEquals(true, markdown.contains("&url=https%3A%2F%2Fcdn.example%2Fblobs%2F${image.hash}.bin"))
    }

    @Test fun blossomServerValidationRejectsInsecureOrUnexpectedEndpoints() {
        assertEquals("https://cdn.example", BlossomServers.normalize("https://cdn.example/"))
        assertNull(BlossomServers.normalize("http://cdn.example"))
        assertNull(BlossomServers.normalize("https://user:secret@cdn.example"))
        assertNull(BlossomServers.normalize("https://cdn.example/path"))
        assertNotEquals("https://cdn.example", BlossomServers.DEFAULT)
    }

    @Test fun profileServerOrderWinsAndEmptyOrInvalidProfileUsesNostrBuild() {
        assertEquals(listOf("https://first.example", "https://second.example"), BlossomServers.fromTags(listOf(
            listOf("server", "https://first.example"), listOf("server", "http://ignored.example"),
            listOf("server", "https://second.example"), listOf("server", "https://first.example")
        )))
        assertEquals(listOf("https://blossom.nostr.build"), BlossomServers.fromTags(emptyList()))
    }

    @Test fun imageFallbacksKeepTheReferenceServerWhenItWasRemovedFromTheProfile() {
        assertEquals(
            listOf("https://current.example", "https://old.example"),
            BlossomServers.imageFallbacks("https://old.example", listOf("https://current.example", "https://old.example/"))
        )
        assertEquals(
            listOf("https://blossom.nostr.build", "https://old.example"),
            BlossomServers.imageFallbacks("https://old.example", BlossomServers.fromTags(emptyList()))
        )
    }

    @Test fun blossomDescriptorUrlIsKeptAndTriedBeforeLegacyHashRoutes() {
        val digest = "a".repeat(64)
        val blobUrl = "https://cdn.example/blobs/$digest.bin"
        assertEquals(blobUrl, BlossomServers.validBlobUrl(blobUrl, digest))
        assertNull(BlossomServers.validBlobUrl("https://cdn.example/blobs/other.bin", digest))
        assertEquals(
            listOf(blobUrl, "https://upload.example/$digest", "https://upload.example/$digest.jpg",
                "https://upload.example/$digest.jpeg", "https://upload.example/$digest.png",
                "https://upload.example/$digest.webp", "https://upload.example/$digest.bin",
                "https://upload.example/blobs/$digest.bin"),
            BlossomServers.imageDownloadUrls(blobUrl, "https://upload.example", digest, emptyList())
        )
    }

    @Test fun legacyAuthorizationIsRetriedOnlyForAuthenticationFailures() {
        assertEquals(true, BlossomServers.mayRetryLegacyAuthorization(400, "Invalid auth string"))
        assertEquals(true, BlossomServers.mayRetryLegacyAuthorization(401, "signature mismatch"))
        assertEquals(false, BlossomServers.mayRetryLegacyAuthorization(415, "Unsupported Media Type"))
        assertEquals(false, BlossomServers.mayRetryLegacyAuthorization(400, "invalid content type"))
    }

    @Test fun blossomDownloadsUseAnIdentifiableNotestrUserAgent() {
        assertEquals("Notestr/3.9.3-beta", BlossomServers.userAgent("3.9.3-beta"))
    }
}
