package fr.decentralia.notestr

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.keys.AmberAccount
import fr.decentralia.notestr.data.keys.AmberSigner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Real Android resolver/cursor/intent behavior with a synthetic signer, no personal account. */
class AmberSignerTest {
    private val account = AmberAccount("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", "test.amber")
    private fun context(answer: (Uri, Array<out String>?) -> Cursor?): Context {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val provider = object : ContentProvider() {
            override fun onCreate() = true
            override fun query(uri: Uri, projection: Array<out String>?, selection: String?, args: Array<out String>?, sort: String?) = answer(uri, projection)
            override fun getType(uri: Uri): String? = null
            override fun insert(uri: Uri, values: ContentValues?): Uri? = null
            override fun delete(uri: Uri, selection: String?, args: Array<out String>?) = 0
            override fun update(uri: Uri, values: ContentValues?, selection: String?, args: Array<out String>?) = 0
        }
        return object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getContentResolver(): ContentResolver = ContentResolver.wrap(provider)
        }
    }

    @Test fun missingPermissionOpensSignerForEncryptionAndSigning() = runBlocking {
        val requests = mutableListOf<Intent>()
        val signer = AmberSigner(context { _, _ -> null }, account) { intent, column ->
            requests += intent
            if (column == "event") "signed-event" else "encrypted"
        }
        val plain = "# Note\nTexte ? et # et é"
        assertEquals("encrypted", signer.nip44Encrypt(plain))
        assertEquals("signed-event", signer.signEvent("{\"kind\":33457}"))
        assertEquals(listOf("nip44_encrypt", "sign_event"), requests.map { it.getStringExtra("type") })
        assertEquals(plain, requests[0].data!!.schemeSpecificPart)
        requests.forEach {
            assertEquals(account.packageName, it.`package`)
            assertEquals(account.publicKey, it.getStringExtra("current_user"))
        }
        assertEquals(account.publicKey, requests[0].getStringExtra("pubkey"))
    }

    @Test fun rememberedPermissionDoesNotOpenSigner() = runBlocking {
        val signer = AmberSigner(context { uri, args ->
            assertEquals("content://test.amber.NIP44_DECRYPT", uri.toString())
            assertEquals(listOf("ciphertext", account.publicKey, account.publicKey), args!!.toList())
            MatrixCursor(arrayOf("result")).apply { addRow(arrayOf("plaintext")) }
        }, account) { _, _ -> error("Unexpected interactive request") }
        assertEquals("plaintext", signer.nip44Decrypt("ciphertext"))
    }

    @Test fun rememberedRejectionNeverOpensSigner() = runBlocking {
        var opened = false
        val signer = AmberSigner(context { _, _ -> MatrixCursor(arrayOf("rejected")) }, account) { _, _ ->
            opened = true
            "incorrect"
        }
        val error = runCatching { signer.nip44Encrypt("note") }.exceptionOrNull()
        assertTrue(error?.message.orEmpty().contains("refusée"))
        assertFalse(opened)
    }

    @Test fun interactiveRejectionPropagatesWithoutRetry() = runBlocking {
        var calls = 0
        val signer = AmberSigner(context { _, _ -> null }, account) { _, _ ->
            calls++
            error("Opération refusée dans Amber.")
        }
        assertTrue(runCatching { signer.signEvent("{}") }.isFailure)
        assertEquals(1, calls)
    }
}
