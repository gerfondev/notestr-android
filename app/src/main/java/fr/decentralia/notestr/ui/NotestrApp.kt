package fr.decentralia.notestr.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.decentralia.notestr.domain.model.Note
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
            is Screen.Editor -> key(screen.note?.identifier ?: "new") { EditorScreen(screen.note, vm) }
            Screen.Settings -> SettingsScreen(state, vm) { requestBiometric(true) }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        if (state.busy) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
            if (publicKey.isBlank() || packageName.isBlank()) reportError("Réponse Amber incomplète.")
            else submitAmber(password, confirmation, publicKey, packageName, relays)
        } else reportError("Connexion à Amber annulée.")
    }
    FormPage("Configurer Notestr") {
        Text("Choisissez où votre clé privée est conservée.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ amberMode = false }, enabled = amberMode, modifier = Modifier.weight(1f)) { Text("Clé locale") }
            OutlinedButton({ amberMode = true }, enabled = !amberMode, modifier = Modifier.weight(1f)) { Text("Amber") }
        }
        if (!amberMode) {
            Text("La clé est chiffrée par votre mot de passe et par Android Keystore. Elle ne quitte pas l’appareil.")
            SecretField("Clé privée nsec", nsec) { nsec = it }
        } else {
            Text("Amber conserve la clé privée et réalise le chiffrement et les signatures. Notestr ne reçoit jamais votre nsec.")
        }
        SecretField("Mot de passe (8 caractères minimum)", password) { password = it }
        SecretField("Confirmer le mot de passe", confirmation) { confirmation = it }
        OutlinedTextField(relays, { relays = it }, label = { Text("Relais, un par ligne") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        if (!amberMode) {
            Button({ submitLocal(password, confirmation, nsec, relays) }, Modifier.fillMaxWidth()) { Text("Créer le coffre") }
        } else {
            Button({
                if (password.length < 8) {
                    reportError("Le mot de passe doit contenir au moins 8 caractères.")
                } else if (password != confirmation) {
                    reportError("Les mots de passe ne correspondent pas.")
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
                        reportError("Amber n’est pas installé sur cet appareil.")
                    }
                }
            }, Modifier.fillMaxWidth()) { Text("Se connecter avec Amber") }
        }
    }
}

@Composable
private fun LockedScreen(unlock: (String) -> Unit, biometricEnabled: Boolean, unlockBiometric: () -> Unit) {
    var password by remember { mutableStateOf("") }
    FormPage("Notestr est verrouillé") {
        if (biometricEnabled) Button(unlockBiometric, Modifier.fillMaxWidth()) { Text("Déverrouiller par biométrie") }
        SecretField("Mot de passe", password) { password = it }
        Button({ unlock(password) }, Modifier.fillMaxWidth()) { Text("Déverrouiller") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen(state: UiState, vm: NotestrViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Notestr") }, actions = {
            TextButton(vm::refresh) { Text("Actualiser") }; TextButton(vm::settings) { Text("Réglages") }; TextButton(vm::lock) { Text("Verrouiller") }
        }) },
        floatingActionButton = { FloatingActionButton({ vm.edit() }) { Text("+") } }
    ) { padding ->
        if (state.notes.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Vos notes privées apparaîtront ici.") }
        else LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
            items(state.notes, key = Note::identifier) { note ->
                Card(Modifier.fillMaxWidth().clickable { vm.edit(note) }) { Column(Modifier.padding(16.dp)) {
                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                    Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(note.createdAt * 1000)), style = MaterialTheme.typography.bodySmall)
                } }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(note: Note?, vm: NotestrViewModel) {
    var markdown by remember { mutableStateOf(note?.markdown.orEmpty()) }; var confirmDelete by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(
        title = { Text(if (note == null) "Nouvelle note" else note.title, maxLines = 1) },
        navigationIcon = { TextButton(vm::backToNotes) { Text("Retour") } },
        actions = { if (note != null) TextButton({ confirmDelete = true }) { Text("Supprimer") }; TextButton({ vm.save(markdown, note) }) { Text("Publier") } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) }) { padding -> MarkdownEditor(markdown, { markdown = it }, Modifier.fillMaxSize().padding(padding)) }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false }, title = { Text("Supprimer cette note ?") },
        text = { Text("Une demande de suppression NIP-09 sera publiée sur les relais.") },
        confirmButton = { Button({ confirmDelete = false; note?.let(vm::delete) }) { Text("Supprimer") } },
        dismissButton = { OutlinedButton({ confirmDelete = false }) { Text("Annuler") } }
    )
}

@Composable
private fun SettingsScreen(state: UiState, vm: NotestrViewModel, enableBiometric: () -> Unit) {
    var relays by remember { mutableStateOf(state.relays.joinToString("\n")) }
    var old by remember { mutableStateOf("") }; var new by remember { mutableStateOf("") }; var confirmation by remember { mutableStateOf("") }
    var confirmReset by remember { mutableStateOf(false) }
    FormPage("Réglages", back = vm::backToNotes) {
        Text("Clé publique : ${state.publicKey.take(16)}…", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(relays, { relays = it }, label = { Text("Relais, un par ligne") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Button({ vm.saveSettings(relays) }, Modifier.fillMaxWidth()) { Text("Enregistrer les relais") }
        HorizontalDivider(); Text("Déverrouillage biométrique", style = MaterialTheme.typography.titleMedium)
        Text("Utilisez une empreinte ou un visage compatible pour ouvrir Notestr. Votre mot de passe reste disponible en secours.")
        if (state.biometricEnabled) {
            Text("Biométrie activée")
            OutlinedButton(vm::disableBiometric, Modifier.fillMaxWidth()) { Text("Désactiver la biométrie") }
        } else {
            Button(enableBiometric, Modifier.fillMaxWidth()) { Text("Activer la biométrie") }
        }
        HorizontalDivider(); Text("Changer le mot de passe", style = MaterialTheme.typography.titleMedium)
        SecretField("Mot de passe actuel", old) { old = it }; SecretField("Nouveau mot de passe", new) { new = it }; SecretField("Confirmer", confirmation) { confirmation = it }
        Button({ vm.changePassword(old, new, confirmation) }, Modifier.fillMaxWidth()) { Text("Changer le mot de passe") }
        HorizontalDivider(); Text("Connexion Nostr", style = MaterialTheme.typography.titleMedium)
        OutlinedButton({ confirmReset = true }, Modifier.fillMaxWidth()) { Text("Changer de compte ou utiliser Amber") }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("Reconfigurer la connexion ?") },
        text = { Text("Le coffre et le cache locaux seront effacés. Les notes publiées sur les relais ne seront pas supprimées.") },
        confirmButton = { Button({ confirmReset = false; vm.resetConnection() }) { Text("Reconfigurer") } },
        dismissButton = { OutlinedButton({ confirmReset = false }) { Text("Annuler") } }
    )
}

@Composable
private fun SecretField(label: String, value: String, change: (String) -> Unit) = OutlinedTextField(
    value, change, label = { Text(label) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth()
)

@Composable
private fun FormPage(title: String, back: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (back != null) TextButton(back) { Text("Retour") }; Text(title, style = MaterialTheme.typography.headlineSmall); content()
    }
}
