package fr.decentralia.notestr

import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.ui.NotestrViewModel
import fr.decentralia.notestr.ui.Screen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Tests only on disposable emulator; uses a public test key and localhost, no real relay. */
class BiometricUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun settingsEnrollmentBackgroundLockAndBiometricReopening() {
        org.junit.Assume.assumeTrue(android.os.Build.MODEL.contains("sdk"))
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var vm: NotestrViewModel
        compose.runOnIdle {
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            vm.resetConnection()
            vm.setup("mot-de-passe-test", "mot-de-passe-test", "0000000000000000000000000000000000000000000000000000000000000001", "wss://127.0.0.1:1")
        }
        try {
            compose.waitUntil(30000) { !vm.state.busy }
            assertEquals(Screen.Notes, vm.state.screen)
            compose.onNodeWithText("Réglages").performClick()
            compose.onNodeWithText("Activer la biométrie").performScrollTo().performClick()
            Thread.sleep(700)
            assertTrue(instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
            Thread.sleep(500)
            compose.waitForIdle()
            assertFalse(vm.state.biometricEnabled)
            assertEquals(Screen.Settings, vm.state.screen)
            compose.onNodeWithText("Activer la biométrie").performScrollTo().performClick()
            instrumentation.sendStatus(2, Bundle().apply { putString("stream", "\nWAITING_UI_ENROLL\n") })
            compose.waitUntil(90000) { vm.state.biometricEnabled }
            compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
            compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitUntil(10000) { vm.state.screen == Screen.Locked }
            assertTrue(vm.state.biometricEnabled)
            compose.onNodeWithText("Déverrouiller par biométrie").performClick()
            instrumentation.sendStatus(2, Bundle().apply { putString("stream", "\nWAITING_UI_UNLOCK\n") })
            compose.waitUntil(90000) { vm.state.screen == Screen.Notes }
            assertEquals("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", vm.state.publicKey)
            compose.waitUntil(30000) { !vm.state.busy }
            compose.onNodeWithText("Réglages").performClick()
            compose.onNodeWithText("Désactiver la biométrie").performScrollTo().performClick()
            assertFalse(vm.state.biometricEnabled)
        } finally { compose.runOnIdle { vm.resetConnection() } }
    }
}
