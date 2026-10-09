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
}
