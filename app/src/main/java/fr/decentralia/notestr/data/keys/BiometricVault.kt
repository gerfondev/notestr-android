package fr.decentralia.notestr.data.keys

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Optional credential envelope. Every encryption/decryption requires a strong biometric. */
class BiometricVault(context: Context) {
    private val prefs = context.getSharedPreferences("notestr_biometric", Context.MODE_PRIVATE)
    private val vault = context.getSharedPreferences("notestr_vault", Context.MODE_PRIVATE)

    private fun binding(): ByteArray = MessageDigest.getInstance("SHA-256").digest(
        (vault.getString("payload", null) ?: error("Coffre non configuré.")).toByteArray(Charsets.UTF_8)
    )

    fun isEnabled(): Boolean = prefs.contains("payload") && prefs.contains("iv") &&
        runCatching { prefs.getString("binding", null) == encode(binding()) }.getOrDefault(false)

    fun prepareEnrollment(): Cipher {
        check(vault.contains("payload")) { "Déverrouillez d’abord le coffre avec votre mot de passe." }
        disable()
        val builder = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true).setInvalidatedByBiometricEnrollment(true)
        if (Build.VERSION.SDK_INT >= 30) builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        val key = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(builder.build()); generateKey()
        }
        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    fun enable(authenticatedCipher: Cipher, credential: CharArray) {
        val clear = credential.concatToString().toByteArray(Charsets.UTF_8)
        try {
            authenticatedCipher.updateAAD(binding())
            val encrypted = authenticatedCipher.doFinal(clear)
            check(prefs.edit().putString("iv", encode(authenticatedCipher.iv))
                .putString("payload", encode(encrypted)).putString("binding", encode(binding())).commit()) {
                "Impossible d’enregistrer le déverrouillage biométrique."
            }
        } finally { clear.fill(0) }
    }

    fun prepareUnlock(): Cipher {
        check(isEnabled()) { "Activez la biométrie dans les réglages après déverrouillage par mot de passe." }
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = store.getKey(ALIAS, null) as? SecretKey ?: error("Clé biométrique indisponible.")
        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, decode(prefs.getString("iv", null)!!)))
        }
    }

    fun unlock(authenticatedCipher: Cipher): CharArray {
        check(isEnabled()) { "Configuration biométrique modifiée." }
        authenticatedCipher.updateAAD(binding())
        val clear = authenticatedCipher.doFinal(decode(prefs.getString("payload", null)!!))
        return try { clear.toString(Charsets.UTF_8).toCharArray() } finally { clear.fill(0) }
    }

    fun disable() {
        prefs.edit().clear().commit()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(ALIAS) }
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(text: String) = Base64.decode(text, Base64.NO_WRAP)
    companion object { const val ALIAS = "fr.decentralia.notestr.biometric.v1" }
}
