package fr.decentralia.notestr

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.nostrdevkit.sdk.Keys
import org.nostrdevkit.sdk.SecretKey
import org.nostrdevkit.sdk.nip44Decrypt

@RunWith(AndroidJUnit4::class)
class Nip44CompatibilityTest {
    @Test fun decryptsOfficialNip44V2Vector() {
        val first = SecretKey.parse("0000000000000000000000000000000000000000000000000000000000000001")
        val second = SecretKey.parse("0000000000000000000000000000000000000000000000000000000000000002")
        val secondKeys = Keys(second)
        try {
            val payload = "AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABee0G5VSK0/9YypIObAtDKfYEAjD35uVkHyB0F4DwrcNaCXlCWZKaArsGrY6M9wnuTMxWfp1RTN9Xga8no+kF5Vsb"
            assertEquals("a", nip44Decrypt(first, secondKeys.publicKey(), payload))
        } finally {
            secondKeys.close(); second.close(); first.close()
        }
    }
}
