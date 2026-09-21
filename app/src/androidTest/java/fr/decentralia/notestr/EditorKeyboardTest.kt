package fr.decentralia.notestr

import androidx.test.platform.app.InstrumentationRegistry
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EditorKeyboardTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun findWebView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) {
            findWebView(view.getChildAt(i))?.let { return it }
        }
        return null
    }

    private fun javascript(script: String): String {
        val done = CountDownLatch(1)
        var result = "null"
        compose.activity.runOnUiThread {
            val web = findWebView(compose.activity.window.decorView)
            if (web == null) done.countDown() else web.evaluateJavascript(script) { result = it; done.countDown() }
        }
        assertTrue("WebView callback", done.await(5, TimeUnit.SECONDS))
        return result
    }

    private fun assertVisual(text: String) {
        val quoted = JSONObject.quote(text)
        try { compose.waitUntil(timeoutMillis = 20000) {
            javascript("""
                (function(){
                    var el=document.querySelector('.toastui-editor-ww-container');
                    return !!el && el.getBoundingClientRect().height > 0 && el.innerText.indexOf($quoted) >= 0;
                })()
            """.trimIndent()) == "true"
        } } catch (e: Throwable) {
            throw AssertionError("WebView diagnostic: " + javascript("JSON.stringify(Array.from(document.querySelectorAll('html,body,#editor,.toastui-editor-defaultUI,.toastui-editor-main,.toastui-editor-main-container,.toastui-editor-ww-container')).map(function(e){return {name:e.className||e.tagName,rect:e.getBoundingClientRect().toJSON(),display:getComputedStyle(e).display,height:getComputedStyle(e).height,text:e.innerText.slice(0,160)}}))"), e)
        }
    }

    private fun tapHtml(selector: String, longPressText: Boolean = false) {
        val raw = javascript("""
            (function(){var el=document.querySelector(${JSONObject.quote(selector)});
            var range=document.createRange();range.selectNodeContents(el);
            var r=$longPressText ? range.getBoundingClientRect() : el.getBoundingClientRect();
            return {x:r.left+r.width/2,y:r.top+r.height/2,width:innerWidth};})()
        """.trimIndent())
        val point = JSONObject(raw)
        var x = 0f; var y = 0f
        compose.runOnIdle {
            val web = findWebView(compose.activity.window.decorView)!!
            val location = IntArray(2); web.getLocationOnScreen(location)
            val scale = web.width / point.getDouble("width")
            x = (location[0] + point.getDouble("x") * scale).toFloat()
            y = (location[1] + point.getDouble("y") * scale).toFloat()
        }
        val time = android.os.SystemClock.uptimeMillis()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        for (action in listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP)) {
            val event = android.view.MotionEvent.obtain(time, android.os.SystemClock.uptimeMillis(), action, x, y, 0)
            instrumentation.sendPointerSync(event); event.recycle()
            if (longPressText && action == android.view.MotionEvent.ACTION_DOWN) Thread.sleep(800)
        }
    }

    @Test fun typingAtBottomKeepsToolbarVisibleAboveKeyboard() {
        compose.runOnIdle {
            // Reproduce the edge-to-edge window enforced on recent Android versions.
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(compose.activity.window, false)
            androidx.lifecycle.ViewModelProvider(compose.activity)[fr.decentralia.notestr.ui.NotestrViewModel::class.java].edit()
        }
        assertVisual("")
        compose.waitUntil(10000) { javascript("!!window.notesEditor") == "true" }
        var initialTop = 0
        compose.runOnIdle {
            val point = IntArray(2)
            findWebView(compose.activity.window.decorView)!!.getLocationOnScreen(point)
            initialTop = point[1]
        }
        tapHtml(".toastui-editor-ww-container p")
        compose.waitUntil(10000) {
            androidx.core.view.ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == true
        }
        val text = (1..45).joinToString("\n") { "Ligne $it de cette longue note" }
        javascript("document.execCommand('insertText', false, ${JSONObject.quote(text)})")
        InstrumentationRegistry.getInstrumentation().sendStringSync(" fin")
        compose.waitUntil(5000) { javascript("window.notesEditor.snapshot().includes('fin')") == "true" }
        compose.waitForIdle()
        compose.runOnIdle {
            val web = findWebView(compose.activity.window.decorView)!!
            val point = IntArray(2); web.getLocationOnScreen(point)
            assertTrue("Window must not pan above the toolbar: initial=$initialTop, current=${point[1]}", point[1] >= initialTop - 2)
            assertTrue("WebView itself must not scroll its toolbar away", web.scrollY == 0)
            val root = compose.activity.window.decorView
            val ime = androidx.core.view.ViewCompat.getRootWindowInsets(root)!!
                .getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            assertTrue("Editor must fit above the keyboard", point[1] + web.height <= root.height - ime + 2)
        }
        assertTrue("HTML toolbar must remain in the viewport", javascript("(function(){var r=document.querySelector('.toastui-editor-toolbar').getBoundingClientRect();return r.top>=0 && r.bottom<=innerHeight;})()") == "true")
        tapHtml("button.heading")
        compose.waitUntil(5000) { javascript("document.querySelector('.toastui-editor-popup').getBoundingClientRect().height>0") == "true" }
        tapHtml(".toastui-editor-popup [data-level='2']")
        compose.waitUntil(5000) { javascript("window.notesEditor.snapshot().includes('## Ligne 45')") == "true" }
    }
}
