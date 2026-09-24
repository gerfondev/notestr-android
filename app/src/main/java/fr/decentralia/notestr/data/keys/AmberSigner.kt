package fr.decentralia.notestr.data.keys

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import org.nostrdevkit.sdk.PublicKey

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

/** NIP-55 : autorisations mémorisées, puis demande interactive si nécessaire. */
class AmberSigner(
    context: Context,
    private val account: AmberAccount,
    private val authorize: suspend (Intent, String) -> String
) {
    private val resolver = context.applicationContext.contentResolver

    suspend fun signEvent(unsignedEventJson: String): String =
        query("SIGN_EVENT", arrayOf(unsignedEventJson, "", account.publicKey), "event")

    suspend fun nip44Encrypt(plainText: String): String =
        query("NIP44_ENCRYPT", arrayOf(plainText, account.publicKey, account.publicKey), "result")

    suspend fun nip44Decrypt(cipherText: String): String =
        query("NIP44_DECRYPT", arrayOf(cipherText, account.publicKey, account.publicKey), "result")

    private suspend fun query(operation: String, arguments: Array<String>, resultColumn: String): String {
        val cached = withContext(Dispatchers.IO) {
            val uri = Uri.parse("content://${account.packageName}.$operation")
            resolver.query(uri, arguments, null, null, null)?.use {
                // A remembered refusal must never be bypassed by opening the signer.
                check(it.getColumnIndex("rejected") < 0) { "Opération refusée dans Amber. Vous pouvez modifier cette autorisation dans Amber." }
                check(it.moveToFirst()) { "Réponse vide d’Amber." }
                val index = it.getColumnIndex(resultColumn)
                check(index >= 0) { "Réponse Amber incomplète." }
                it.getString(index) ?: error("Réponse Amber vide.")
            }
        }
        if (cached != null) return cached
        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("nostrsigner", arguments[0], null)).apply {
            setPackage(account.packageName)
            putExtra("type", operation.lowercase(java.util.Locale.ROOT))
            putExtra("current_user", account.publicKey)
            putExtra("appName", "Notestr")
            if (operation != "SIGN_EVENT") putExtra("pubkey", account.publicKey)
        }
        return authorize(intent, resultColumn)
    }
}
