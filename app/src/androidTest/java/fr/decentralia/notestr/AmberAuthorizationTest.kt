package fr.decentralia.notestr

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.ui.NotestrViewModel
import fr.decentralia.notestr.ui.Screen
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class AmberAuthorizationTest {
    private fun viewModel() = NotestrViewModel(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)

    @Test fun amberRoundTripPreservesEditorAndNormalBackgroundStillLocks() = runBlocking {
        withContext(Dispatchers.Main) {
            val vm = viewModel()
            vm.edit()
            val pending = async { vm.authorizeAmber(Intent(), "result") }
            yield()
            assertNotNull(vm.amberRequest)
            vm.onAppStopped()
            assertTrue(vm.state.screen is Screen.Editor)
            vm.completeAmber(Activity.RESULT_OK, Intent().putExtra("result", "ciphertext"))
            assertEquals("ciphertext", pending.await())
            assertNull(vm.amberRequest)
            vm.onAppStopped()
            assertFalse(vm.state.screen is Screen.Editor)
        }
    }

    @Test fun cancellationAndRejectionKeepEditorAndClearPendingRequest() = runBlocking {
        withContext(Dispatchers.Main) {
            for (code in listOf(Activity.RESULT_CANCELED, Activity.RESULT_OK)) {
                val vm = viewModel()
                vm.edit()
                val pending = async { runCatching { vm.authorizeAmber(Intent(), "event") } }
                yield()
                vm.completeAmber(code, Intent().putExtra("rejected", true))
                assertTrue(pending.await().isFailure)
                assertTrue(vm.state.screen is Screen.Editor)
                assertNull(vm.amberRequest)
                vm.lock()
            }
        }
    }

    @Test fun lockCancelsPendingAuthorizationAndIgnoresLateResponse() = runBlocking {
        withContext(Dispatchers.Main) {
            val vm = viewModel()
            vm.edit()
            val pending = async { runCatching { vm.authorizeAmber(Intent(), "result") } }
            yield()
            vm.lock()
            vm.completeAmber(Activity.RESULT_OK, Intent().putExtra("result", "late"))
            assertTrue(pending.await().isFailure)
            assertNull(vm.amberRequest)
            assertFalse(vm.state.screen is Screen.Editor)
        }
    }
}
