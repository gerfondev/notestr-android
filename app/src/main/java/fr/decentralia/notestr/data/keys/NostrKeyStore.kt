package fr.decentralia.notestr.data.keys

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Coffre local à deux couches : mot de passe puis clé matérielle Android Keystore. */
class NostrKeyStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("notestr_vault", Context.MODE_PRIVATE)
    private val random = SecureRandom()

    fun isConfigured(): Boolean = prefs.contains(KEY_PAYLOAD)

    fun create(password: CharArray, credential: CharArray) {
        require(password.size >= 8) { "Le mot de passe doit contenir au moins 8 caractères." }
        require(credential.isNotEmpty()) { "L’identifiant de connexion est obligatoire." }
        val salt = ByteArray(16).also(random::nextBytes)
        val passwordKey = derive(password, salt)
        try {
            val inner = encrypt(passwordKey, credential.concatToString().toByteArray(Charsets.UTF_8))
            val outer = encrypt(androidKey(), inner)
            check(prefs.edit().putString(KEY_SALT, salt.b64()).putString(KEY_PAYLOAD, outer.b64()).commit()) { "Enregistrement du coffre impossible." }
            BiometricVault(context).disable()
        } finally { passwordKey.fill(0) }
    }

    fun unlock(password: CharArray): CharArray {
        val salt = prefs.getString(KEY_SALT, null)?.b64Bytes() ?: error("Coffre non configuré")
        val payload = prefs.getString(KEY_PAYLOAD, null)?.b64Bytes() ?: error("Coffre non configuré")
        val passwordKey = derive(password, salt)
        return try {
            val inner = decrypt(androidKey(), payload)
            val clear = decrypt(passwordKey, inner)
            clear.toString(Charsets.UTF_8).toCharArray().also { clear.fill(0) }
        } catch (_: Exception) {
            throw IllegalArgumentException("Mot de passe incorrect")
        } finally { passwordKey.fill(0) }
    }

    fun changePassword(oldPassword: CharArray, newPassword: CharArray) {
        val credential = unlock(oldPassword)
        try { create(newPassword, credential) } finally { credential.fill('\u0000') }
    }

    fun clear() { BiometricVault(context).disable(); prefs.edit().clear().commit() }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, 310_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    private fun androidKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
            generateKey()
        }
    }

    private fun encrypt(keyBytes: ByteArray, clear: ByteArray) = encrypt(SecretKeySpec(keyBytes, "AES"), clear)
    private fun encrypt(key: SecretKey, clear: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // Android Keystore interdit un IV fourni par l'appelant lors du chiffrement.
        // Le fournisseur génère ici un IV aléatoire, que nous préfixons au ciphertext.
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(clear)
        val iv = cipher.iv ?: error("Android Keystore n'a pas généré d'IV")
        return ByteBuffer.allocate(1 + iv.size + encrypted.size)
            .put(iv.size.toByte()).put(iv).put(encrypted).array()
    }

    private fun decrypt(keyBytes: ByteArray, encoded: ByteArray) = decrypt(SecretKeySpec(keyBytes, "AES"), encoded)
    private fun decrypt(key: SecretKey, encoded: ByteArray): ByteArray {
        val buffer = ByteBuffer.wrap(encoded)
        val iv = ByteArray(buffer.get().toInt()).also(buffer::get)
        val ciphertext = ByteArray(buffer.remaining()).also(buffer::get)
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv)); doFinal(ciphertext)
        }
    }

    private fun ByteArray.b64() = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.b64Bytes() = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val ALIAS = "fr.decentralia.notestr.vault.v1"
        const val KEY_SALT = "salt"
        const val KEY_PAYLOAD = "payload"
    }
}
