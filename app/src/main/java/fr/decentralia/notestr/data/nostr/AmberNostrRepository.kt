package fr.decentralia.notestr.data.nostr

import android.content.Context
import android.content.Intent
import fr.decentralia.notestr.data.keys.AmberAccount
import fr.decentralia.notestr.data.keys.AmberSigner
import fr.decentralia.notestr.data.storage.EventCache
import org.nostrdevkit.sdk.Event
import org.nostrdevkit.sdk.EventBuilder
import org.nostrdevkit.sdk.PublicKey

class AmberNostrRepository(
    context: Context,
    account: AmberAccount,
    relayStrings: List<String>,
    cache: EventCache,
    authorize: suspend (Intent, String) -> String
) : BaseNostrRepository(PublicKey.parse(account.publicKey), relayStrings, cache) {
    private val signer = AmberSigner(context, account, authorize)
    override val requiresInteractiveDecryption = true

    override suspend fun sign(builder: EventBuilder): Event {
        val unsigned = builder.finalizeUnsigned(publicKey).ensureId()
        val event = Event.fromJson(signer.signEvent(unsigned.asJson()))
        check(event.verify() && event.author() == publicKey && event.id() == unsigned.id()) { "Signature Amber invalide." }
        return event
    }
    override suspend fun encrypt(markdown: String): String = signer.nip44Encrypt(markdown)
    override suspend fun decrypt(content: String): String = signer.nip44Decrypt(content)
}
