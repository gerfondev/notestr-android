package fr.decentralia.notestr.ui

import fr.decentralia.notestr.R
import fr.decentralia.notestr.i18n.tr

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.graphics.Rect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// A public bridge class, with callbacks bound to the WebView that owns it.
class MarkdownEditorBridge(private val receive: (String) -> Unit) {
    @JavascriptInterface fun postMessage(json: String) = receive(json)
}

/** Keep Android's floating selection menu above the editor's formatting toolbar. */
internal class EditorWebView(context: Context) : WebView(context) {
    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        if (type != ActionMode.TYPE_FLOATING) return super.startActionMode(callback, type)
        return super.startActionMode(object : ActionMode.Callback2() {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu) = callback.onCreateActionMode(mode, menu)
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = callback.onPrepareActionMode(mode, menu)
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = callback.onActionItemClicked(mode, item)
            override fun onDestroyActionMode(mode: ActionMode) = callback.onDestroyActionMode(mode)
            override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
                if (callback is ActionMode.Callback2) callback.onGetContentRect(mode, view, outRect)
                else super.onGetContentRect(mode, view, outRect)
                // Include the toolbar in the protected content, even when the first
                // line is selected. Android may also place the menu below selection.
                outRect.top = 0
            }
        }, type)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownEditor(markdown: String, onChange: (String) -> Unit, modifier: Modifier = Modifier,
                   onOpenLink: ((String) -> Unit)? = null, onLoadImage: ((String, (ByteArray?) -> Unit) -> Unit)? = null,
                   onPickImage: (((String) -> Unit) -> Unit)? = null,
                   readOnly: Boolean = false) {
    var visual by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var revision by remember { mutableStateOf(0) }
    var loadedRevision by remember { mutableStateOf(-1) }
    var error by remember { mutableStateOf<String?>(null) }
    var imageFullscreen by remember { mutableStateOf(false) }
    var pendingImageDownload by remember { mutableStateOf<ByteArray?>(null) }
    var pdfImagesPending by remember { mutableStateOf<Set<String>?>(null) }
    var pdfExportRequested by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageSaver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri ->
        val bytes = pendingImageDownload
        pendingImageDownload = null
        if (uri == null || bytes == null) return@rememberLauncherForActivityResult
        Thread {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri, "w")?.use { it.write(bytes) }
                    ?: error("Impossible d’ouvrir le fichier choisi.")
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, if (saved.isSuccess) tr("Image enregistrée.") else tr("Enregistrement de l’image impossible."), Toast.LENGTH_LONG).show()
            }
        }.start()
    }
    var sourceValue by remember { mutableStateOf(TextFieldValue(markdown)) }
    val currentOnOpenLink by rememberUpdatedState(onOpenLink)
    val currentReadOnly by rememberUpdatedState(readOnly)
    val currentMarkdown by rememberUpdatedState(markdown)
    val currentOnChange by rememberUpdatedState(onChange)
    val currentOnLoadImage by rememberUpdatedState(onLoadImage)
    val currentOnPickImage by rememberUpdatedState(onPickImage)
    LaunchedEffect(markdown) {
        if (sourceValue.text != markdown) sourceValue = TextFieldValue(markdown, TextRange(markdown.length))
    }
    LaunchedEffect(visual, loadedRevision, pdfExportRequested) {
        if (visual && loadedRevision >= 0 && pdfExportRequested && error == null) {
            pdfExportRequested = false
            val owner = webView
            if (owner == null) {
                Toast.makeText(context, tr("Impossible de récupérer les images pour le PDF."), Toast.LENGTH_LONG).show()
            } else owner.evaluateJavascript("window.notesEditor.imagesPendingForPrint()") { result ->
                if (webView !== owner || !visual || loadedRevision < 0) return@evaluateJavascript
                val references = runCatching { org.json.JSONArray(result).let { array ->
                    (0 until array.length()).map { array.getString(it) }.toSet()
                } }.getOrNull()
                if (references == null) {
                    Toast.makeText(context, tr("Impossible de préparer les images du PDF."), Toast.LENGTH_LONG).show()
                } else if (references.isNotEmpty() && currentOnLoadImage == null) {
                    Toast.makeText(context, tr("Impossible de récupérer les images pour le PDF."), Toast.LENGTH_LONG).show()
                } else {
                    // Only unresolved images are fetched. Images already visible in
                    // the editor stay in the WebView and are reused by PrintManager.
                    pdfImagesPending = references
                    if (references.isEmpty()) owner.evaluateJavascript("window.notesEditor.prepareForPrint()", null)
                    else owner.evaluateJavascript("window.notesEditor.resolvePrintImages()", null)
                }
            }
        }
    }
    BackHandler(enabled = imageFullscreen) {
        webView?.evaluateJavascript("window.notesEditor.closeImageLightbox()", null)
        imageFullscreen = false
    }

    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            if (!readOnly) {
                TextButton(onClick = {
                loadedRevision = -1
                error = null
                visual = true
                }, enabled = !visual) { Text(tr("Visuel")) }
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
            TextButton(onClick = {
                if (visual && webView != null && loadedRevision >= 0 && error == null) {
                    pdfExportRequested = true
                    // Start immediately; LaunchedEffect runs on the next frame.
                } else {
                    pdfExportRequested = true
                    visual = true
                    loadedRevision = -1
                    error = null
                }
            }) { Text(tr("Exporter en PDF")) }
            if (!visual && onPickImage != null) IconButton(onClick = {
                val captured = sourceValue
                currentOnPickImage?.invoke { reference ->
                    val start = minOf(captured.selection.start, captured.selection.end).coerceIn(0, captured.text.length)
                    val end = maxOf(captured.selection.start, captured.selection.end).coerceIn(start, captured.text.length)
                    val prefix = if (start > 0 && captured.text[start - 1] != '\n') "\n\n" else ""
                    val suffix = if (end < captured.text.length && captured.text[end] != '\n') "\n\n" else ""
                    val insertion = prefix + reference + suffix
                    val updated = captured.text.replaceRange(start, end, insertion)
                    sourceValue = TextFieldValue(updated, TextRange(start + insertion.length))
                    currentOnChange(updated)
                }
            }, enabled = !readOnly) {
                Icon(painterResource(R.drawable.ic_upload_image), contentDescription = tr("Insérer une image"))
            }
        }
        if (visual) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(
                    factory = { context ->
                        EditorWebView(context).apply {
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
                                        message.optString("type") == "printImagesReady" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        pdfImagesPending = null
                                        if (message.optBoolean("ready")) {
                                            startWebViewPrint(context, owner, currentMarkdown)
                                        } else {
                                            val loaded = message.optInt("loadedCount", 0)
                                            val total = message.optInt("imageCount", 0)
                                            val pending = message.optInt("pendingCount", 0)
                                            Toast.makeText(context, "Images du PDF : $loaded/$total chargées, $pending à récupérer.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        message.optString("type") == "imageViewer" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        imageFullscreen = message.optBoolean("open")
                                    }
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        !currentReadOnly && message.optString("type") == "change" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        currentOnChange(message.getString("markdown"))
                                    }
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        message.optString("type") == "openLink" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        val url = message.optString("url")
                                        if (browserIntent(url) != null) {
                                            if (!currentReadOnly) currentOnChange(message.getString("markdown"))
                                            currentOnOpenLink?.invoke(url) ?: openBrowser(context, url)
                                        }
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
                                    if (webView === owner && visual && loadedRevision >= 0 && !currentReadOnly &&
                                        message.optString("type") == "pickImage" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        val expectedRevision = loadedRevision
                                        currentOnPickImage?.invoke { reference ->
                                            owner.post {
                                                if (webView === owner && visual && loadedRevision == expectedRevision && !currentReadOnly) {
                                                    owner.evaluateJavascript("window.notesEditor.insertImageReference(${JSONObject.quote(reference)})") { inserted ->
                                                        if (inserted == "true" && webView === owner && loadedRevision == expectedRevision) {
                                                            owner.evaluateJavascript("window.notesEditor.snapshot()") { json ->
                                                                if (webView === owner && loadedRevision == expectedRevision) {
                                                                    runCatching { JSONObject("{\"v\":$json}") }
                                                                        .getOrNull()?.opt("v")?.let { if (it is String) currentOnChange(it) }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        message.optString("type") == "loadImage" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        val reference = message.optString("reference")
                                        currentOnLoadImage?.invoke(reference) { bytes -> owner.post {
                                            if (webView !== owner || !visual || loadedRevision < 0) return@post
                                            if (bytes == null) {
                                                owner.evaluateJavascript("window.notesEditor.imageLoadFailed(${JSONObject.quote(reference)})", null)
                                                if (pdfImagesPending?.contains(reference) == true) {
                                                    pdfImagesPending = null
                                                    Toast.makeText(context, tr("Impossible de récupérer les images pour le PDF."), Toast.LENGTH_LONG).show()
                                                }
                                            } else {
                                                val data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                                owner.evaluateJavascript("window.notesEditor.imageResult(${JSONObject.quote(reference)},${JSONObject.quote(data)})") { result ->
                                                    owner.post {
                                                        val pending = pdfImagesPending
                                                        if (pending != null && reference in pending) {
                                                            if (result == "true") {
                                                                val remaining = pending - reference
                                                                pdfImagesPending = remaining
                                                                if (remaining.isEmpty()) {
                                                                    owner.evaluateJavascript("window.notesEditor.prepareForPrint()", null)
                                                                }
                                                            } else {
                                                                pdfImagesPending = null
                                                                Toast.makeText(context, tr("Impossible de préparer les images du PDF."), Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } }
                                    }
                                    if (webView === owner && visual && loadedRevision >= 0 &&
                                        message.optString("type") == "downloadImage" &&
                                        message.optInt("epoch", -1) == loadedRevision) {
                                        val reference = message.optString("reference")
                                        val expectedRevision = loadedRevision
                                        android.app.AlertDialog.Builder(context)
                                            .setMessage(tr("Enregistrer cette image dans un emplacement de votre choix ?"))
                                            .setNegativeButton(tr("Annuler"), null)
                                            .setPositiveButton(tr("Télécharger")) { _, _ ->
                                                currentOnLoadImage?.invoke(reference) { bytes -> owner.post {
                                                    if (webView !== owner || !visual || loadedRevision != expectedRevision) return@post
                                                    if (bytes == null) {
                                                        Toast.makeText(context, tr("Image indisponible."), Toast.LENGTH_LONG).show()
                                                    } else {
                                                        pendingImageDownload = bytes
                                                        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date())
                                                        imageSaver.launch("Notestr-image-$stamp.jpg")
                                                    }
                                                } }
                                            }
                                            .show()
                                    }
                                }
                            }, "AndroidNotes")
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                    val url = request.url.toString()
                                    if (url == EDITOR_URL) return false
                                    if (browserIntent(url) != null) currentOnOpenLink?.invoke(url) ?: openBrowser(context, url)
                                    return true
                                }

                                override fun onReceivedError(view: WebView, request: WebResourceRequest, failure: WebResourceError) {
                                    if (request.isForMainFrame && webView === owner) error = tr("La page de l’éditeur n’a pas pu être chargée.")
                                }

                                override fun onPageFinished(view: WebView, url: String) {
                                    if (webView !== owner || !visual || url != EDITOR_URL) return
                                    revision++
                                    val expected = revision
                                    // setDocument can synchronously emit loadImage from the JS bridge.
                                    // Mark this revision active before evaluating it so those messages
                                    // are not discarded while evaluateJavascript's result is pending.
                                    loadedRevision = expected
                                    // Page completion drives initialization; no one-shot JS 'ready' message is required.
                                    val source = JSONObject.quote(currentMarkdown)
                                    val editorLanguage = JSONObject.quote(fr.decentralia.notestr.i18n.Strings.language)
                                    view.evaluateJavascript("""
                                        (function() {
                                            try {
                                                if (!window.notesEditor) throw new Error(window.notesEditorFailure || 'Le moteur visuel ne s’est pas initialisé');
                                                window.notesEditor.setDocument($source, $expected, $editorLanguage, $currentReadOnly);
                                                return {ok:true};
                                            } catch (e) { return {ok:false,error:String(e.message || e)}; }
                                        })()
                                    """.trimIndent()) { result ->
                                        if (webView === owner && visual && revision == expected) {
                                            val status = runCatching { JSONObject(result) }.getOrNull()
                                            if (status?.optBoolean("ok") == true) {
                                                loadedRevision = expected
                                                error = null
                                            } else {
                                                loadedRevision = -1
                                                error = status?.optString("error") ?: tr("Le moteur visuel n’a pas répondu.")
                                            }
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
                        Text(tr("Impossible d’afficher le mode Visuel. Votre Markdown est conservé."))
                        Text(detail)
                        Text("WebView : ${WebView.getCurrentWebViewPackage()?.versionName ?: "inconnue"}")
                        TextButton(onClick = {
                            error = null
                            loadedRevision = -1
                            webView?.reload()
                        }) { Text(tr("Réessayer")) }
                    }
                }
            }
            LaunchedEffect(webView, visual, error, loadedRevision) {
                if (webView != null && visual && error == null && loadedRevision >= 0) {
                    webView?.evaluateJavascript("window.notesEditor.resolveImages(true)", null)
                    delay(15000)
                    if (loadedRevision < 0) error = tr("Le chargement de l’éditeur a dépassé 15 secondes.")
                }
            }
        } else {
            OutlinedTextField(value = sourceValue, onValueChange = { sourceValue = it; onChange(it.text) }, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("Markdown") })
        }
    }
}

private const val EDITOR_URL = "file:///android_asset/editor/index.html"

private fun startWebViewPrint(context: Context, webView: WebView?, markdown: String) {
    val view = webView ?: return
    val title = markdown.lineSequence().firstOrNull { it.isNotBlank() }
        ?.trim()?.trimStart('#', ' ')
        ?.take(80)?.ifBlank { null } ?: tr("Note Nostr")
    val css = """
        (function(){
          var id='notestr-print-styles';
          var style=document.getElementById(id);
          if(!style){style=document.createElement('style');style.id=id;document.head.appendChild(style);}
          style.textContent='@page{size:A4;margin:17mm 18mm 18mm;}'+
            '@media print{html,body{height:auto!important;overflow:visible!important;color:#202124!important;font:11pt/1.5 sans-serif!important;}'+
            '.toastui-editor-defaultUI-toolbar,.toastui-editor-mode-switch,.toastui-editor-md-tab-container,.toastui-editor-popup,.notes-code-actions,.notes-image-lightbox{display:none!important;}'+
            '#editor,.toastui-editor-defaultUI,.toastui-editor-main,.toastui-editor-main-container,.toastui-editor-ww-container,.toastui-editor-ww-container .ProseMirror{height:auto!important;min-height:0!important;max-height:none!important;overflow:visible!important;width:auto!important;}'+
            '.toastui-editor-ww-container{border:0!important;} .toastui-editor-contents{padding:0!important;}'+
            '.toastui-editor-contents img{display:block;max-width:100%!important;max-height:165mm!important;width:auto!important;height:auto!important;object-fit:contain!important;margin:6pt 0 8pt!important;}'+
            '.toastui-editor-contents h1,.toastui-editor-contents h2,.toastui-editor-contents h3{break-after:avoid-page;}'+
            '.toastui-editor-contents table,.toastui-editor-contents pre{break-inside:avoid-page;} }';
          return true;
        })()
    """.trimIndent()
    view.evaluateJavascript(css) {
        try {
            val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            manager.print(title, view.createPrintDocumentAdapter(title), PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build())
        } catch (_: Exception) {
            Toast.makeText(context, tr("Export PDF impossible."), Toast.LENGTH_LONG).show()
        }
    }
}

internal fun browserIntent(url: String): Intent? {
    if (url.any { it.isWhitespace() || it.code < 32 } || '\\' in url) return null
    val uri = Uri.parse(url)
    if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) return null
    return Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
}

internal fun openBrowser(context: Context, url: String) {
    val intent = browserIntent(url) ?: return
    try { context.startActivity(intent) }
    catch (_: android.content.ActivityNotFoundException) {
        Toast.makeText(context, tr("Aucun navigateur disponible pour ouvrir ce lien."), Toast.LENGTH_LONG).show()
    }
}
