package fr.decentralia.notestr

import android.os.SystemClock
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import fr.decentralia.notestr.ui.NotestrViewModel
import fr.decentralia.notestr.ui.Screen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BackgroundLockTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun vm() = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]

    @Test fun briefActivityBackgroundPreservesEditor() {
        lateinit var model: NotestrViewModel
        compose.runOnIdle { model = vm(); model.edit() }
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertTrue(model.state.screen is Screen.Editor)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.runOnIdle { assertTrue(model.state.screen is Screen.Editor) }
    }

    @Test fun elapsedDeadlineIsCheckedOnReturnEvenIfTimerWasSuspended() {
        compose.runOnIdle {
            val model = vm(); model.edit()
            val now = SystemClock.elapsedRealtime()
            model.onAppStopped(now)
            model.onAppStarted(now + 179_999)
            assertTrue(model.state.screen is Screen.Editor)
            model.onAppStopped(now + 200_000)
            model.onAppStopped(now + 300_000) // Repeated stop must not extend the deadline.
            model.onAppStarted(now + 380_000)
            assertFalse(model.state.screen is Screen.Editor)
        }
    }

    @Test fun expiredBackgroundTimerLocksWithoutReturning() {
        lateinit var model: NotestrViewModel
        compose.runOnIdle {
            model = vm(); model.edit()
            model.onAppStopped(SystemClock.elapsedRealtime() - 180_000)
        }
        compose.waitUntil(5000) { model.state.screen !is Screen.Editor }
    }

    @Test fun manualLockRemainsImmediateAndReturnCannotUnlock() {
        compose.runOnIdle {
            val model = vm(); model.edit()
            model.onAppStopped()
            model.lock()
            assertFalse(model.state.screen is Screen.Editor)
            model.onAppStarted()
            assertFalse(model.state.screen is Screen.Editor)
        }
    }
}
