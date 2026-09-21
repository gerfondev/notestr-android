package fr.decentralia.notestr

import android.os.SystemClock
import android.view.MotionEvent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import fr.decentralia.notestr.ui.NotestrViewModel
import fr.decentralia.notestr.ui.Screen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun backLabelHasSystemBarClearanceAndAcceptsTouchOnItsTopHalf() {
        lateinit var vm: NotestrViewModel
        compose.runOnIdle {
            // Recent Android versions enforce this mode for target SDK 36.
            WindowCompat.setDecorFitsSystemWindows(compose.activity.window, false)
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            vm.settings()
        }
        compose.waitForIdle()
        val label = compose.onNodeWithText("Retour", useUnmergedTree = true).fetchSemanticsNode().boundsInWindow
        var safeTop = 0
        var margin = 0f
        compose.runOnIdle {
            safeTop = ViewCompat.getRootWindowInsets(compose.activity.window.decorView)!!
                .getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()).top
            margin = 24 * compose.activity.resources.displayMetrics.density
        }
        assertTrue("The label must keep the form's margin below the system bar", label.top >= safeTop + margin - 1)
        // Send real screen touches, not performClick(), which bypasses hit testing.
        val x = label.center.x
        val y = label.top + label.height / 4
        val time = SystemClock.uptimeMillis()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val event = MotionEvent.obtain(time, SystemClock.uptimeMillis(), action, x, y, 0)
            instrumentation.sendPointerSync(event)
            event.recycle()
        }
        compose.waitUntil(5000) { vm.state.screen == Screen.Notes }
    }
}
