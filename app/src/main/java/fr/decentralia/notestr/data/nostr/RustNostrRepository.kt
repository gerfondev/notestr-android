package fr.decentralia.notestr.data.nostr

import fr.decentralia.notestr.data.storage.EventCache
import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.EventBuilder
import org.nostrdevkit.sdk.Keys
import org.nostrdevkit.sdk.Nip44Version
import org.nostrdevkit.sdk.SecretKey
import org.nostrdevkit.sdk.nip44Decrypt
import org.nostrdevkit.sdk.nip44Encrypt

class RustNostrRepository private constructor(
    private val keys: Keys,
    relayStrings: List<String>,
    cache: EventCache
) : BaseNostrRepository(keys.publicKey(), relayStrings, cache) {
    constructor(privateKey: CharArray, relayStrings: List<String>, cache: EventCache) :
        this(parseKeys(privateKey), relayStrings, cache)

    override suspend fun sign(builder: EventBuilder): Event = builder.finalize(keys)
    override suspend fun encrypt(markdown: String): String = keys.secretKey().use {
        nip44Encrypt(it, publicKey, markdown, Nip44Version.V2)
    }
    override suspend fun decrypt(content: String): String = keys.secretKey().use {
        nip44Decrypt(it, publicKey, content)
    }
    override fun close() { super.close(); keys.close() }

    private companion object {
        fun parseKeys(privateKey: CharArray): Keys = try {
            SecretKey.parse(privateKey.concatToString()).use { Keys(it) }
        } finally { privateKey.fill('\u0000') }
    }
}
