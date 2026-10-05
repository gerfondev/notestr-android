package fr.decentralia.notestr.ui

import fr.decentralia.notestr.i18n.tr

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.decentralia.notestr.domain.model.Note
import fr.decentralia.notestr.R
import java.text.DateFormat
import java.util.Date

@Composable
fun NotestrApp(vm: NotestrViewModel = viewModel(), requestBiometric: (Boolean) -> Unit) {
    val amberOperationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        vm.completeAmber(it.resultCode, it.data)
    }
    val amberRequest = vm.amberRequest
    LaunchedEffect(amberRequest) {
        amberRequest?.takeUnless { it.launched }?.let {
            it.launched = true
            try { amberOperationLauncher.launch(it.intent) }
            catch (_: ActivityNotFoundException) { vm.failAmberLaunch() }
            catch (_: SecurityException) { vm.failAmberLaunch() }
        }
    }
    val state = vm.state
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); vm.dismissMessage() } }
    Box(Modifier.fillMaxSize()) {
        when (val screen = state.screen) {
            Screen.Setup -> SetupScreen(
                state.relays, vm::setup, vm::setupAmber, vm::reportError,
                vm::beginAmberAuthorization, vm::finishAmberAuthorization
            )
            Screen.Locked -> LockedScreen(vm::unlock, state.biometricEnabled) { requestBiometric(false) }
            Screen.Notes -> NotesScreen(state, vm)
            is Screen.Editor -> key(screen.note?.listKey ?: "new") { EditorScreen(screen.note, vm) }
            Screen.Settings -> SettingsScreen(state, vm) { requestBiometric(true) }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        if (state.busy && (!state.refreshing || state.notes.isEmpty())) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}

@Composable
private fun SetupScreen(
    defaultRelays: List<String>,
    submitLocal: (String, String, String, String) -> Unit,
    submitAmber: (String, String, String, String, String) -> Unit,
    reportError: (String) -> Unit,
    beginAmberAuthorization: () -> Unit,
    finishAmberAuthorization: () -> Unit
) {
    var password by remember { mutableStateOf("") }; var confirmation by remember { mutableStateOf("") }
    var nsec by remember { mutableStateOf("") }; var relays by remember { mutableStateOf(defaultRelays.joinToString("\n")) }
    var amberMode by remember { mutableStateOf(false) }
    val amberLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { response ->
        finishAmberAuthorization()
        if (response.resultCode == Activity.RESULT_OK) {
            val publicKey = response.data?.getStringExtra("result").orEmpty()
            val packageName = response.data?.getStringExtra("package").orEmpty()
            if (publicKey.isBlank() || packageName.isBlank()) reportError(tr("Réponse Amber incomplète."))
            else submitAmber(password, confirmation, publicKey, packageName, relays)
        } else reportError(tr("Connexion à Amber annulée."))
    }
    FormPage(tr("Configurer Notestr")) {
        ConnectionLogo()
        Text(tr("Choisissez où votre clé privée est conservée."))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ amberMode = false }, enabled = amberMode, modifier = Modifier.weight(1f)) { Text(tr("Clé locale")) }
            OutlinedButton({ amberMode = true }, enabled = !amberMode, modifier = Modifier.weight(1f)) { Text("Amber") }
        }
        if (!amberMode) {
            Text(tr("La clé est chiffrée par votre mot de passe et par Android Keystore. Elle ne quitte pas l’appareil."))
            SecretField(tr("Clé privée nsec"), nsec) { nsec = it }
        } else {
            Text(tr("Amber conserve la clé privée et réalise le chiffrement et les signatures. Notestr ne reçoit jamais votre nsec."))
        }
        SecretField(tr("Mot de passe (8 caractères minimum)"), password) { password = it }
        SecretField(tr("Confirmer le mot de passe"), confirmation) { confirmation = it }
        OutlinedTextField(relays, { relays = it }, label = { Text(tr("Relais, un par ligne")) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        if (!amberMode) {
            Button({ submitLocal(password, confirmation, nsec, relays) }, Modifier.fillMaxWidth()) { Text(tr("Créer le coffre")) }
        } else {
            Button({
                if (password.length < 8) {
                    reportError(tr("Le mot de passe doit contenir au moins 8 caractères."))
                } else if (password != confirmation) {
                    reportError(tr("Les mots de passe ne correspondent pas."))
                } else {
                    val permissions = """[{"type":"sign_event","kind":33457},{"type":"sign_event","kind":5},{"type":"nip44_encrypt"},{"type":"nip44_decrypt"}]"""
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:")).apply {
                        putExtra("type", "get_public_key")
                        putExtra("permissions", permissions)
                        putExtra("appName", "Notestr")
                    }
                    beginAmberAuthorization()
                    try { amberLauncher.launch(intent) }
                    catch (_: ActivityNotFoundException) {
                        finishAmberAuthorization()
                        reportError(tr("Amber n’est pas installé sur cet appareil."))
                    }
                }
            }, Modifier.fillMaxWidth()) { Text(tr("Se connecter avec Amber")) }
        }
    }
}

@Composable
private fun LockedScreen(unlock: (String) -> Unit, biometricEnabled: Boolean, unlockBiometric: () -> Unit) {
    var password by remember { mutableStateOf("") }
    FormPage(tr("Notestr est verrouillé")) {
        ConnectionLogo()
        if (biometricEnabled) Button(unlockBiometric, Modifier.fillMaxWidth()) { Text(tr("Déverrouiller par biométrie")) }
        SecretField(tr("Mot de passe"), password) { password = it }
        Button({ unlock(password) }, Modifier.fillMaxWidth()) { Text(tr("Déverrouiller")) }
    }
}

@Composable
private fun ColumnScope.ConnectionLogo() {
    Image(
        painter = painterResource(R.drawable.notestr_logo),
        contentDescription = tr("Logo Notestr"),
        modifier = Modifier.size(128.dp).align(Alignment.CenterHorizontally)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen(state: UiState, vm: NotestrViewModel) {
    val visibleNotes = state.notes.filter { it.trashed == state.showingTrash }
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (state.showingTrash) tr("Corbeille") else "Notestr") }, actions = {
            TextButton(onClick = vm::toggleTrashView, enabled = !state.busy,
                modifier = Modifier.semantics { contentDescription = tr(if (state.showingTrash) "Notes" else "Corbeille") }) {
                Text(tr(if (state.showingTrash) "Notes" else "Corbeille"))
            }
            ActionIcon(tr("Actualiser"), R.drawable.ic_action_refresh, vm::refresh, enabled = !state.busy)
            ActionIcon(tr("Réglages"), R.drawable.ic_action_settings, vm::settings)
            ActionIcon(tr("Verrouiller"), R.drawable.ic_action_lock, vm::lock)
        }) },
        floatingActionButton = { if (!state.showingTrash) FloatingActionButton({ vm.edit() }) { Icon(painterResource(R.drawable.ic_action_add), contentDescription = tr("Nouvelle note")) } }
    ) { padding ->
        if (visibleNotes.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(tr(if (state.showingTrash) "La corbeille est vide." else "Vos notes privées apparaîtront ici.")) }
        else LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
            if (state.syncing) item { Text(tr("Synchronisation des modifications en attente…")) }
            if (state.refreshing) item { Text(tr("Synchronisation des relais en cours…")) }
            items(visibleNotes, key = Note::listKey) { note ->
                Card(Modifier.fillMaxWidth().clickable(enabled = !state.busy || state.refreshing) { vm.edit(note) }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            if (note.pending) Text(tr(if (note.conflicted) "Conflit — version locale" else "En attente de synchronisation"), style = MaterialTheme.typography.labelSmall)
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            if (note.pinned) Text(tr("Épinglée"), style = MaterialTheme.typography.labelSmall)
                            Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(note.createdAt * 1000)), style = MaterialTheme.typography.bodySmall)
                        }
                        if (!note.trashed && !note.pending) ActionIcon(if (note.pinned) tr("Désépingler") else tr("Épingler"),
                            if (note.pinned) R.drawable.ic_action_unpin else R.drawable.ic_action_pin,
                            { vm.togglePinned(note) }, enabled = !state.busy)
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(note: Note?, vm: NotestrViewModel) {
    var markdown by remember { mutableStateOf(note?.markdown.orEmpty()) }; var confirmDelete by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    var pendingLink by remember { mutableStateOf<String?>(null) }
    val browserContext = androidx.compose.ui.platform.LocalContext.current
    var editorRevision by remember { mutableStateOf(0) }
    val busy = vm.state.busy && !vm.state.refreshing
    Scaffold(topBar = { TopAppBar(
        title = { Text(if (note == null) tr("Nouvelle note") else note.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = { ActionIcon(tr("Retour"), R.drawable.ic_action_back, vm::backToNotes, enabled = !busy || vm.state.refreshing) },
        actions = {
            if (note != null && !note.pending) {
                if (note.trashed) ActionIcon(tr("Restaurer la note"), R.drawable.ic_action_history, { vm.setTrashed(note, false) }, enabled = !busy)
                else ActionIcon(tr("Version précédente"), R.drawable.ic_action_history, { confirmRestore = true }, enabled = !busy)
                ActionIcon(tr(if (note.trashed) "Supprimer définitivement" else "Supprimer"), R.drawable.ic_action_delete, { confirmDelete = true }, enabled = !busy)
            }
            if (note?.trashed != true) ActionIcon(tr("Publier"), R.drawable.ic_action_publish, { vm.save(markdown, note) }, enabled = !busy)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            if (note?.pending == true) {
                Text(tr(if (note.conflicted) "Conflit — la version distante est conservée dans la liste." else "Modifications enregistrées localement, en attente de synchronisation."), Modifier.padding(8.dp))
                Row {
                    if (note.conflicted) TextButton({ vm.save(markdown, note, asCopy = true) }, enabled = !busy) { Text(tr("Publier comme nouvelle note")) }
                    TextButton({ confirmDiscard = true }, enabled = !busy) { Text(tr("Abandonner les modifications locales")) }
                }
            }
            key(editorRevision) {
                MarkdownEditor(markdown, { markdown = it }, Modifier.fillMaxWidth().weight(1f), readOnly = note?.trashed == true, onOpenLink = { url ->
                    if (markdown != note?.markdown.orEmpty()) pendingLink = url
                    else openBrowser(browserContext, url)
                })
            }
        }
    }
    if (confirmDiscard && note != null) AlertDialog(onDismissRequest = { confirmDiscard = false },
        title = { Text(tr("Abandonner les modifications locales ?")) },
        text = { Text(tr("La version des relais sera conservée. Le texte local en attente sera supprimé de cet appareil.")) },
        confirmButton = { TextButton({ confirmDiscard = false; vm.discardPending(note) }) { Text(tr("Abandonner")) } },
        dismissButton = { TextButton({ confirmDiscard = false }) { Text(tr("Annuler")) } })
    pendingLink?.let { url -> AlertDialog(
        onDismissRequest = { pendingLink = null }, title = { Text(tr("Ouvrir le navigateur ?")) },
        text = { Text(tr("Notestr se verrouille après 3 minutes en arrière-plan. Les modifications non enregistrées seront alors perdues. Annulez pour les enregistrer d’abord.")) },
        confirmButton = { Button({ pendingLink = null; openBrowser(browserContext, url) }) { Text(tr("Ouvrir")) } },
        dismissButton = { TextButton({ pendingLink = null }) { Text(tr("Annuler")) } }
    ) }
    if (confirmRestore) AlertDialog(
        onDismissRequest = { confirmRestore = false }, title = { Text(tr("Charger la version précédente ?")) },
        text = { Text(tr("Le texte dans l’éditeur sera remplacé par la sauvegarde. Vérifiez-le puis appuyez sur Publier pour confirmer la restauration.")) },
        confirmButton = { Button({
            confirmRestore = false
            note?.let { vm.restorePrevious(it) { restored -> markdown = restored; editorRevision++ } }
        }, enabled = !busy) { Text(tr("Charger")) } },
        dismissButton = { OutlinedButton({ confirmRestore = false }) { Text(tr("Annuler")) } }
    )
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false }, title = { Text(tr(if (note?.trashed == true) "Supprimer définitivement cette note ?" else "Déplacer cette note dans la corbeille ?")) },
        text = { Text(tr(if (note?.trashed == true) "Une demande de suppression définitive sera envoyée aux relais. L’effacement de toutes les copies n’est pas garanti." else "La note publiée sera conservée dans la corbeille commune. Les modifications non publiées seront abandonnées.")) },
        confirmButton = { Button({ confirmDelete = false; note?.let { if (it.trashed) vm.delete(it) else vm.setTrashed(it, true) } }) { Text(tr(if (note?.trashed == true) "Supprimer définitivement" else "Déplacer")) } },
        dismissButton = { OutlinedButton({ confirmDelete = false }) { Text(tr("Annuler")) } }
    )
}

@Composable
private fun SettingsScreen(state: UiState, vm: NotestrViewModel, enableBiometric: () -> Unit) {
    var relays by remember { mutableStateOf(state.relays.joinToString("\n")) }
    var old by remember { mutableStateOf("") }; var new by remember { mutableStateOf("") }; var confirmation by remember { mutableStateOf("") }
    var confirmReset by remember { mutableStateOf(false) }
    FormPage(tr("Réglages"), back = vm::backToNotes) {
        Text(tr("Version de l’application") + " : " + fr.decentralia.notestr.BuildConfig.VERSION_NAME)
        Text(tr("Langue"), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton({ vm.selectLanguage("fr") }, enabled = fr.decentralia.notestr.i18n.Strings.language != "fr") { Text("Français") }
            OutlinedButton({ vm.selectLanguage("en") }, enabled = fr.decentralia.notestr.i18n.Strings.language != "en") { Text("English") }
        }
        HorizontalDivider()
        Text(tr("Clé publique : ") + state.publicKey.take(16) + "…", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(relays, { relays = it }, label = { Text(tr("Relais, un par ligne")) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Button({ vm.saveSettings(relays) }, Modifier.fillMaxWidth()) { Text(tr("Enregistrer les relais")) }
        HorizontalDivider(); Text(tr("Déverrouillage biométrique"), style = MaterialTheme.typography.titleMedium)
        Text(tr("Utilisez une empreinte ou un visage compatible pour ouvrir Notestr. Votre mot de passe reste disponible en secours."))
        if (state.biometricEnabled) {
            Text(tr("Biométrie activée"))
            OutlinedButton(vm::disableBiometric, Modifier.fillMaxWidth()) { Text(tr("Désactiver la biométrie")) }
        } else {
            Button(enableBiometric, Modifier.fillMaxWidth()) { Text(tr("Activer la biométrie")) }
        }
        HorizontalDivider(); Text(tr("Changer le mot de passe"), style = MaterialTheme.typography.titleMedium)
        SecretField(tr("Mot de passe actuel"), old) { old = it }; SecretField(tr("Nouveau mot de passe"), new) { new = it }; SecretField(tr("Confirmer"), confirmation) { confirmation = it }
        Button({ vm.changePassword(old, new, confirmation) }, Modifier.fillMaxWidth()) { Text(tr("Changer le mot de passe")) }
        HorizontalDivider(); Text(tr("Connexion Nostr"), style = MaterialTheme.typography.titleMedium)
        OutlinedButton({ confirmReset = true }, Modifier.fillMaxWidth()) { Text(tr("Changer de compte ou utiliser Amber")) }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text(tr("Reconfigurer la connexion ?")) },
        text = { Text(tr("Le coffre et le cache locaux seront effacés. Les notes publiées sur les relais ne seront pas supprimées.")) },
        confirmButton = { Button({ confirmReset = false; vm.resetConnection() }) { Text(tr("Reconfigurer")) } },
        dismissButton = { OutlinedButton({ confirmReset = false }) { Text(tr("Annuler")) } }
    )
}

@Composable
private fun SecretField(label: String, value: String, change: (String) -> Unit) = OutlinedTextField(
    value, change, label = { Text(label) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth()
)

@Composable
private fun FormPage(title: String, back: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    // Keep the whole scroll viewport below status bars/cutouts and above the IME.
    // Insets belong outside the scrollable content so scrolling cannot remove them.
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()
        .verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (back != null) ActionIcon(tr("Retour"), R.drawable.ic_action_back, back)
        Text(title, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}

/** Labels serve TalkBack and the long-press tooltip; touch targets remain 48 dp. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionIcon(label: String, @DrawableRes icon: Int, onClick: () -> Unit, enabled: Boolean = true) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp)) {
            Icon(painterResource(icon), contentDescription = label, modifier = Modifier.size(24.dp))
        }
    }
}
