package fr.decentralia.notestr

import android.content.Context
import android.os.Bundle
import android.security.keystore.KeyInfo
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.keys.BiometricVault
import fr.decentralia.notestr.data.keys.NostrKeyStore
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.security.KeyStore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

/** Run only in an isolated emulator with an enrolled test fingerprint, never a user's vault. */
class BiometricVaultTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun authenticate(cipher: Cipher, stage: String): Cipher {
        val complete = CountDownLatch(1)
        val result = AtomicReference<Cipher?>()
        val failure = AtomicReference<String?>()
        compose.activity.runOnUiThread {
            val prompt = BiometricPrompt(compose.activity, ContextCompat.getMainExecutor(compose.activity), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(value: BiometricPrompt.AuthenticationResult) {
                    result.set(value.cryptoObject?.cipher); complete.countDown()
                }
                override fun onAuthenticationError(code: Int, message: CharSequence) {
                    failure.set("$code: $message"); complete.countDown()
                }
            })
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Test Notestr $stage")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Annuler").build(), BiometricPrompt.CryptoObject(cipher))
        }
        instrumentation.sendStatus(2, Bundle().apply { putString("stream", "\nWAITING_FINGERPRINT_$stage\n") })
        assertTrue("Biometric prompt timed out", complete.await(90, TimeUnit.SECONDS))
        assertNull(failure.get())
        return requireNotNull(result.get())
    }

    private fun cancelPrompt(cipher: Cipher) {
        val done = CountDownLatch(1)
        val succeeded = java.util.concurrent.atomic.AtomicBoolean(false)
        compose.activity.runOnUiThread {
            val prompt = BiometricPrompt(compose.activity, ContextCompat.getMainExecutor(compose.activity), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(value: BiometricPrompt.AuthenticationResult) { succeeded.set(true); done.countDown() }
                override fun onAuthenticationError(code: Int, message: CharSequence) { done.countDown() }
            })
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Test annulation Notestr")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Mot de passe").build(), BiometricPrompt.CryptoObject(cipher))
        }
        instrumentation.waitForIdleSync()
        Thread.sleep(500)
        assertTrue(instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
        assertTrue("Prompt cancellation", done.await(10, TimeUnit.SECONDS))
        assertFalse(succeeded.get())
    }

    @Test fun cryptoRequiresBiometricAndPasswordRemainsAvailable() {
        org.junit.Assume.assumeTrue(android.os.Build.MODEL.contains("sdk"))
        val context = instrumentation.targetContext
        assertEquals(BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG))
        val vault = NostrKeyStore(context)
        val bio = BiometricVault(context)
        val password = "mot-de-passe-test".toCharArray()
        val credential = "0000000000000000000000000000000000000000000000000000000000000001".toCharArray()
        vault.clear()
        try {
            vault.create(password, credential)
            assertFalse(bio.isEnabled())
            val denied = bio.prepareEnrollment()
            assertThrows(Exception::class.java) { bio.enable(denied, credential) }
            assertFalse(bio.isEnabled())
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = keyStore.getKey(BiometricVault.ALIAS, null) as SecretKey
            val info = SecretKeyFactory.getInstance(key.algorithm, "AndroidKeyStore").getKeySpec(key, KeyInfo::class.java) as KeyInfo
            assertTrue(info.isUserAuthenticationRequired)
            assertEquals(if (android.os.Build.VERSION.SDK_INT >= 30) 0 else -1, info.userAuthenticationValidityDurationSeconds)
            assertTrue(info.isInvalidatedByBiometricEnrollment)
            bio.enable(authenticate(bio.prepareEnrollment(), "ENROLL"), credential)
            assertTrue(bio.isEnabled())
            val blocked = bio.prepareUnlock()
            assertThrows(Exception::class.java) { bio.unlock(blocked) }
            val opened = bio.unlock(authenticate(bio.prepareUnlock(), "UNLOCK"))
            assertArrayEquals(credential, opened); opened.fill('\u0000')
            cancelPrompt(bio.prepareUnlock())
            assertTrue(bio.isEnabled())
            val fallback = vault.unlock(password)
            assertArrayEquals(credential, fallback); fallback.fill('\u0000')
            val data = context.getSharedPreferences("notestr_biometric", Context.MODE_PRIVATE).all.values.joinToString()
            assertFalse(data.contains(credential.concatToString()))
            vault.changePassword(password, "nouveau-mot-de-passe".toCharArray())
            assertFalse(bio.isEnabled())
            assertFalse(keyStore.containsAlias(BiometricVault.ALIAS))
            assertArrayEquals(credential, vault.unlock("nouveau-mot-de-passe".toCharArray()))
        } finally { vault.clear(); credential.fill('\u0000'); password.fill('\u0000') }
    }
}
