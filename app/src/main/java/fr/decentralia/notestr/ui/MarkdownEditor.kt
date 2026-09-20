package fr.decentralia.notestr.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.json.JSONObject

// A public bridge class, with callbacks bound to the WebView that owns it.
class MarkdownEditorBridge(private val receive: (String) -> Unit) {
    @JavascriptInterface fun postMessage(json: String) = receive(json)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownEditor(markdown: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var visual by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var revision by remember { mutableStateOf(0) }
    var loadedRevision by remember { mutableStateOf(-1) }
    var error by remember { mutableStateOf<String?>(null) }
    val currentMarkdown by rememberUpdatedState(markdown)
    val currentOnChange by rememberUpdatedState(onChange)

    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = {
                loadedRevision = -1
                error = null
                visual = true
            }, enabled = !visual) { Text("Visuel") }
            TextButton(onClick = {
                val active = webView
                // Never replace the source with an empty/uninitialized editor snapshot.
                if (active != null && loadedRevision >= 0 && error == null) {
                    active.evaluateJavascript("window.notesEditor.snapshot()") { json ->
                        if (webView === active && visual) {
                            runCatching { JSONObject("{\"v\":$json}") }
                                .getOrNull()?.opt("v")?.let { if (it is String) currentOnChange(it) }
                            visual = false
                            loadedRevision = -1
                        }
                    }
                } else {
                    visual = false
                    loadedRevision = -1
                }
            }, enabled = visual) { Text("Markdown") }
        }
        if (visual) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            val owner = this
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            settings.javaScriptEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = false
                            settings.domStorageEnabled = false
                            settings.setSupportMultipleWindows(false)
                            webChromeClient = WebChromeClient()
                            addJavascriptInterface(MarkdownEditorBridge { json ->
                                val message = runCatching { JSONObject(json) }.getOrNull()
                                if (message != null) owner.post {
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        message.optString("type") == "change" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        currentOnChange(message.getString("markdown"))
                                    }
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        message.optString("type") == "copyCode" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        val id = message.optInt("id", -1)
                                        val copied = runCatching {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", message.getString("text")))
                                        }.isSuccess
                                        owner.evaluateJavascript("window.notesEditor.copyResult($id, $copied)", null)
                                    }
                                }
                            }, "AndroidNotes")
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                                    request.url.toString() != EDITOR_URL

                                override fun onReceivedError(view: WebView, request: WebResourceRequest, failure: WebResourceError) {
                                    if (request.isForMainFrame && webView === owner) error = "La page de l’éditeur n’a pas pu être chargée."
                                }

                                override fun onPageFinished(view: WebView, url: String) {
                                    if (webView !== owner || !visual || url != EDITOR_URL) return
                                    revision++
                                    val expected = revision
                                    loadedRevision = -1
                                    // Page completion drives initialization; no one-shot JS 'ready' message is required.
                                    val source = JSONObject.quote(currentMarkdown)
                                    view.evaluateJavascript("""
                                        (function() {
                                            try {
                                                if (!window.notesEditor) throw new Error(window.notesEditorFailure || 'Le moteur visuel ne s’est pas initialisé');
                                                window.notesEditor.setDocument($source, $expected);
                                                return {ok:true};
                                            } catch (e) { return {ok:false,error:String(e.message || e)}; }
                                        })()
                                    """.trimIndent()) { result ->
                                        if (webView === owner && visual && revision == expected) {
                                            val status = runCatching { JSONObject(result) }.getOrNull()
                                            if (status?.optBoolean("ok") == true) {
                                                loadedRevision = expected
                                                error = null
                                            } else error = status?.optString("error") ?: "Le moteur visuel n’a pas répondu."
                                        }
                                    }
                                }
                            }
                            webView = this
                            loadUrl(EDITOR_URL)
                        }
                    },
                    onRelease = { released ->
                        if (webView === released) webView = null
                        released.stopLoading()
                        released.removeJavascriptInterface("AndroidNotes")
                        released.destroy()
                    },
                    modifier = Modifier.fillMaxSize()
                )
                if (loadedRevision < 0 && error == null) CircularProgressIndicator(Modifier.align(Alignment.Center))
                error?.let { detail ->
                    Column(Modifier.align(Alignment.Center)) {
                        Text("Impossible d’afficher le mode Visuel. Votre Markdown est conservé.")
                        Text(detail)
                        Text("WebView : ${WebView.getCurrentWebViewPackage()?.versionName ?: "inconnue"}")
                        TextButton(onClick = {
                            error = null
                            loadedRevision = -1
                            webView?.reload()
                        }) { Text("Réessayer") }
                    }
                }
            }
            LaunchedEffect(webView, visual, error) {
                if (webView != null && visual && error == null) {
                    delay(15000)
                    if (loadedRevision < 0) error = "Le chargement de l’éditeur a dépassé 15 secondes."
                }
            }
        } else {
            OutlinedTextField(value = markdown, onValueChange = onChange, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("Markdown") })
        }
    }
}

private const val EDITOR_URL = "file:///android_asset/editor/index.html"
