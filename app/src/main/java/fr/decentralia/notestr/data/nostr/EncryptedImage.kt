package fr.decentralia.notestr.data.nostr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Binary attachments are encrypted before any request leaves the device. */
internal data class EncryptedImage(
    val ciphertext: ByteArray,
    val hash: String,
    val key: ByteArray,
    val nonce: ByteArray,
) {
    companion object {
        fun fromJpeg(plaintext: ByteArray): EncryptedImage {
            require(plaintext.isNotEmpty() && plaintext.size <= 30 * 1024 * 1024) { "Image trop volumineuse." }
            val key = ByteArray(32).also(SecureRandom()::nextBytes)
            val nonce = ByteArray(12).also(SecureRandom()::nextBytes)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
            val encrypted = cipher.doFinal(plaintext)
            return EncryptedImage(encrypted, sha256(encrypted), key, nonce)
        }

        fun decrypt(ciphertext: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
            require(key.size == 32 && nonce.size == 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
            return cipher.doFinal(ciphertext)
        }

        private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

    fun markdownReference(server: String): String {
        val b64 = Base64.getUrlEncoder().withoutPadding()
        val encodedKey = b64.encodeToString(key)
        val encodedNonce = b64.encodeToString(nonce)
        return "![Image](notestr-image://$hash?server=${server.encodeURL()}&key=$encodedKey&nonce=$encodedNonce&mime=image%2Fjpeg)"
    }
}

internal object ImagePreparation {
    /** Decode and re-encode to JPEG so EXIF, GPS and other source metadata are discarded. */
    fun toSanitizedJpeg(bytes: ByteArray, maxDimension: Int = 2560): ByteArray {
        require(bytes.isNotEmpty() && bytes.size <= 30 * 1024 * 1024) { "Image vide ou supérieure à 30 Mio." }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth in 1..12000 && bounds.outHeight in 1..12000) { "Format d’image non pris en charge." }
        var sample = 1
        while (bounds.outWidth / sample > maxDimension * 2 || bounds.outHeight / sample > maxDimension * 2) sample *= 2
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("Impossible de lire l’image.")
        val scaled = if (maxOf(bitmap.width, bitmap.height) > maxDimension) {
            val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true).also { if (it !== bitmap) bitmap.recycle() }
        } else bitmap
        return try {
            ByteArrayOutputStream().use { stream ->
                check(scaled.compress(Bitmap.CompressFormat.JPEG, 86, stream)) { "Compression JPEG impossible." }
                stream.toByteArray().also { require(it.size <= 20 * 1024 * 1024 - 16) { "Image supérieure à 20 Mio après préparation." } }
            }
        } finally { scaled.recycle() }
    }
}

private fun String.encodeURL() = java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
