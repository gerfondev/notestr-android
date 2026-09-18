package fr.decentralia.notestr

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.data.keys.NostrKeyStore
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NostrKeyStoreTest {
    @Test fun createsAndUnlocksVaultWithAndroidGeneratedIv() {
        val vault = NostrKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)
        val password = "mot-de-passe-test".toCharArray()
        val privateKey = "0000000000000000000000000000000000000000000000000000000000000001".toCharArray()
        vault.clear()
        try {
            vault.create(password, privateKey)
            assertArrayEquals(privateKey, vault.unlock(password))
        } finally {
            vault.clear(); password.fill('\u0000'); privateKey.fill('\u0000')
        }
    }
}
