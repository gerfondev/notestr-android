package fr.decentralia.notestr.data.keys

import android.content.Context
import android.net.Uri
import rust.nostr.sdk.PublicKey

data class AmberAccount(val publicKey: String, val packageName: String) {
    fun encode(): CharArray = "$PREFIX$publicKey|$packageName".toCharArray()

    companion object {
        private const val PREFIX = "amber|"
        private val PACKAGE_PATTERN = Regex("^[A-Za-z0-9_.]+$")

        fun create(publicKey: String, packageName: String): AmberAccount {
            require(PACKAGE_PATTERN.matches(packageName)) { "Réponse Amber invalide." }
            val normalized = PublicKey.parse(publicKey).use { it.toHex() }
            return AmberAccount(normalized, packageName)
        }

        fun decode(value: CharArray): AmberAccount? {
            val text = value.concatToString()
            if (!text.startsWith(PREFIX)) return null
            val parts = text.removePrefix(PREFIX).split('|', limit = 2)
            require(parts.size == 2) { "Configuration Amber invalide." }
            return create(parts[0], parts[1])
        }
    }
}

/** Accès NIP-55 au ContentProvider du signer sélectionné. */
class AmberSigner(context: Context, private val account: AmberAccount) {
    private val resolver = context.applicationContext.contentResolver

    fun signEvent(unsignedEventJson: String): String =
        query("SIGN_EVENT", arrayOf(unsignedEventJson, "", account.publicKey), "event")

    fun nip44Encrypt(plainText: String): String =
        query("NIP44_ENCRYPT", arrayOf(plainText, account.publicKey, account.publicKey), "result")

    fun nip44Decrypt(cipherText: String): String =
        query("NIP44_DECRYPT", arrayOf(cipherText, account.publicKey, account.publicKey), "result")

    private fun query(operation: String, arguments: Array<String>, resultColumn: String): String {
        val uri = Uri.parse("content://${account.packageName}.$operation")
        val cursor = resolver.query(uri, arguments, null, null, null)
            ?: error("Amber n’a pas autorisé cette opération. Reconnectez Amber et mémorisez les autorisations.")
        cursor.use {
            check(it.moveToFirst()) { "Réponse vide d’Amber." }
            check(it.getColumnIndex("rejected") < 0) { "Opération refusée dans Amber." }
            val index = it.getColumnIndex(resultColumn)
            check(index >= 0) { "Réponse Amber incomplète." }
            return it.getString(index) ?: error("Réponse Amber vide.")
        }
    }
}
