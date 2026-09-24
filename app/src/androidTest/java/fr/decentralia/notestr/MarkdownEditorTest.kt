package fr.decentralia.notestr

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import fr.decentralia.notestr.ui.MarkdownEditor
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MarkdownEditorTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

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

    @Test fun currentSanitizerProtectsCustomAndDefaultEditorPaths() {
        val source = mutableStateOf("Test sécurité")
        compose.setContent { MaterialTheme { MarkdownEditor(source.value, { source.value = it }) } }
        assertVisual("Test sécurité")
        org.junit.Assert.assertEquals(JSONObject.quote("3.4.16"), javascript("DOMPurify.version"))
        val result = javascript("""
            (function(){
                var sanitize=DOMPurify.sanitize, calls=0, extra=null, host=document.createElement('div');
                DOMPurify.sanitize=function(){calls++;return sanitize.apply(this,arguments);};
                var payload='<img src="https://example.invalid/image.png" onerror="alert(1)"><a href="javascript:alert(1)">lien</a><script>alert(1)</script>';
                function clean(el){return !el.querySelector('script,[onerror],[onclick],a[href^="javascript:"]');}
                try {
                    notesEditor.setDocument(payload,100);
                    var customOk=calls>0 && clean(document.querySelector('.toastui-editor-ww-container'));
                    calls=0;document.body.appendChild(host);
                    extra=new toastui.Editor({el:host,height:'200px',initialEditType:'wysiwyg',usageStatistics:false});
                    extra.setHTML(payload);
                    return customOk && calls>0 && clean(host);
                } finally {
                    if(extra)extra.destroy();host.remove();DOMPurify.sanitize=sanitize;
                }
            })()
        """.trimIndent())
        assertTrue("Both sanitizer paths must invoke current DOMPurify and remove active HTML", result == "true")
    }

    @Test fun firstLineSelectionStillAllowsBoldAndItalic() {
        val source = mutableStateOf("Première")
        compose.setContent { MaterialTheme { MarkdownEditor(source.value, { source.value = it }) } }
        assertVisual("Première")
        tapHtml(".toastui-editor-ww-container p", longPressText = true)
        compose.waitUntil(5000) { javascript("window.getSelection().toString()") == JSONObject.quote("Première") }
        // Use real touch events: a native menu covering the toolbar intercepts them.
        tapHtml("button.bold")
        compose.waitUntil(5000) { source.value == "**Première**" }
        tapHtml("button.italic")
        compose.waitUntil(5000) { javascript("!!document.querySelector('.toastui-editor-ww-container strong em, .toastui-editor-ww-container em strong')") == "true" }
    }

    @Test fun copyCodeBlockWritesOnlyCodeToAndroidClipboard() {
        val code = "val text = \"<tag> & é\"\n\n  println(text)"
        val markdown = "```kotlin\n$code\n```"
        val source = mutableStateOf(markdown)
        compose.setContent { MaterialTheme { MarkdownEditor(source.value, { source.value = it }) } }
        assertVisual("println(text)")
        compose.waitUntil(5000) { javascript("!!document.querySelector('.notes-copy-code:not([hidden])')") == "true" }
        tapHtml(".notes-copy-code")
        compose.waitUntil(5000) { javascript("document.querySelector('.notes-copy-code').textContent") == JSONObject.quote("Copié !") }
        compose.runOnIdle {
            val clipboard = compose.activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            org.junit.Assert.assertEquals(code, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
            org.junit.Assert.assertEquals(markdown, source.value)
        }
        org.junit.Assert.assertEquals(markdown, JSONObject("{\"value\":" + javascript("window.notesEditor.snapshot()") + "}").getString("value"))
    }

    @Test fun headingMenuIsVisibleAndTouchSelectsH2() {
        val source = mutableStateOf("Titre Android")
        compose.setContent { MaterialTheme { MarkdownEditor(source.value, { source.value = it }) } }
        assertVisual("Titre Android")
        compose.waitForIdle()
        tapHtml(".toastui-editor-ww-container p")
        tapHtml("button.heading")
        compose.waitUntil(5000) {
            javascript("(function(){var p=document.querySelector('.toastui-editor-popup');var r=p.getBoundingClientRect();return r.width>0 && r.left>=0 && r.right<=innerWidth && p.querySelectorAll('[data-level]').length===6;})()") == "true"
        }
        tapHtml(".toastui-editor-popup [data-level='2']")
        compose.waitUntil(5000) { source.value == "## Titre Android" }
        assertTrue(javascript("!!document.querySelector('.toastui-editor-ww-container h2')") == "true")
    }

    @Test fun rendersSourceAfterRepeatedModeSwitches() {
        val source = mutableStateOf("# Première note\n\n**Bonjour Android**")
        compose.setContent { MaterialTheme { MarkdownEditor(source.value, { source.value = it }) } }
        assertVisual("Bonjour Android")
        repeat(3) { index ->
            compose.onNode(hasText("Markdown") and hasClickAction()).performClick()
            val next = "# Note $index\n\n**Texte visuel $index**\n\n- Une liste"
            compose.onNode(hasSetTextAction()).performTextReplacement(next)
            compose.onNode(hasText("Visuel") and hasClickAction()).performClick()
            assertVisual("Texte visuel $index")
            assertTrue(javascript("window.notesEditor.snapshot()") == JSONObject.quote(next))
        }
        javascript("document.querySelector('.toastui-editor-ww-container [contenteditable=true]').focus(); document.execCommand('selectAll',false,null); document.execCommand('insertText',false,'Modification visuelle');")
        compose.waitUntil(5000) { source.value.contains("Modification visuelle") }
        assertVisual("Modification visuelle")
        compose.waitForIdle()
        Thread.sleep(350) // Allow the WebView compositor to present the verified DOM before capturing.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.filesDir, "visual-test.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
        compose.onNode(hasText("Markdown") and hasClickAction()).performClick()
        assertTrue(source.value.contains("Modification visuelle"))
    }
}
