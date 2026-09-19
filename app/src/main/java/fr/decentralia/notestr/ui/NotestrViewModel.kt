package fr.decentralia.notestr.ui

import android.app.Application
import android.app.Activity
import android.content.Intent
import kotlinx.coroutines.CompletableDeferred
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.decentralia.notestr.data.keys.AmberAccount
import fr.decentralia.notestr.data.keys.BiometricVault
import javax.crypto.Cipher
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import fr.decentralia.notestr.data.keys.NostrKeyStore
import fr.decentralia.notestr.data.nostr.AmberNostrRepository
import fr.decentralia.notestr.data.nostr.NostrRepository
import fr.decentralia.notestr.data.nostr.RustNostrRepository
import fr.decentralia.notestr.data.storage.AppPreferences
import fr.decentralia.notestr.data.storage.EventCache
import fr.decentralia.notestr.domain.model.Note
import kotlinx.coroutines.launch
import rust.nostr.sdk.SecretKey

sealed interface Screen {
    data object Setup : Screen
    data object Locked : Screen
    data object Notes : Screen
    data class Editor(val note: Note?) : Screen
    data object Settings : Screen
}

data class UiState(
    val screen: Screen,
    val notes: List<Note> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
    val relays: List<String> = listOf(AppPreferences.DEFAULT_RELAY),
    val publicKey: String = "",
    val biometricEnabled: Boolean = false
)

class NotestrViewModel(application: Application) : AndroidViewModel(application) {
    private val vault = NostrKeyStore(application)
    private val biometric = BiometricVault(application)
    private data class BiometricRequest(val enroll: Boolean, val cipher: Cipher, val session: Long)
    private var biometricRequest: BiometricRequest? = null
    private var session = 0L
    private var task: Job? = null
    private val prefs = AppPreferences(application)
    private val cache = EventCache(application)
    private var credential: CharArray? = null
    private var repository: NostrRepository? = null
    private var amberAuthorizationActive = false

    data class AmberRequest(val intent: Intent, val column: String, val result: CompletableDeferred<String>, var launched: Boolean = false)
    var amberRequest by mutableStateOf<AmberRequest?>(null)
        private set

    internal suspend fun authorizeAmber(intent: Intent, column: String): String {
        check(amberRequest == null) { "Une demande Amber est déjà en cours." }
        val request = AmberRequest(intent, column, CompletableDeferred())
        amberRequest = request
        beginAmberAuthorization()
        try { return request.result.await() }
        finally {
            if (amberRequest === request) {
                amberRequest = null
                finishAmberAuthorization()
            }
        }
    }

    fun completeAmber(resultCode: Int, data: Intent?) {
        val request = amberRequest ?: return
        val result = runCatching {
            check(resultCode == Activity.RESULT_OK) { "Opération Amber annulée ou interrompue. Votre note reste dans l’éditeur." }
            check(data?.getBooleanExtra("rejected", false) != true) { "Opération refusée dans Amber." }
            data?.getStringExtra(request.column)?.takeIf { it.isNotEmpty() }
                ?: error("Réponse Amber incomplète.")
        }
        result.fold(request.result::complete, request.result::completeExceptionally)
    }

    fun failAmberLaunch() {
        amberRequest?.result?.completeExceptionally(IllegalStateException("Impossible d’ouvrir Amber. Vérifiez que l’application est installée."))
    }

    var state by mutableStateOf(UiState(if (vault.isConfigured()) Screen.Locked else Screen.Setup, relays = prefs.relays, biometricEnabled = biometric.isEnabled()))
        private set

    fun setup(password: String, confirmation: String, privateKey: String, relayText: String) {
        if (password != confirmation) return fail("Les mots de passe ne correspondent pas.")
        runCatching {
            val relays = parseRelays(relayText)
            val key = privateKey.trim().toCharArray()
            SecretKey.parse(key.concatToString()).close()
            vault.create(password.toCharArray(), key)
            prefs.relays = relays
            openSession(key.copyOf(), relays)
            key.fill('\u0000')
        }.onFailure { fail(it.message ?: "Configuration impossible.") }
    }

    fun setupAmber(password: String, confirmation: String, publicKey: String, packageName: String, relayText: String) {
        if (password != confirmation) return fail("Les mots de passe ne correspondent pas.")
        runCatching {
            val relays = parseRelays(relayText)
            val account = AmberAccount.create(publicKey, packageName)
            val stored = account.encode()
            vault.create(password.toCharArray(), stored)
            prefs.relays = relays
            openSession(stored.copyOf(), relays)
            stored.fill('\u0000')
        }.onFailure { fail(it.message ?: "Connexion à Amber impossible.") }
    }

    fun unlock(password: String) {
        runCatching {
            val key = vault.unlock(password.toCharArray())
            openSession(key, prefs.relays)
        }.onFailure { fail(it.message ?: "Déverrouillage impossible.") }
    }

    private fun openSession(key: CharArray, relays: List<String>) {
        closeSession()
        credential = key
        val amber = AmberAccount.decode(key)
        repository = if (amber != null) {
            AmberNostrRepository(getApplication(), amber, relays, cache, ::authorizeAmber)
        } else {
            RustNostrRepository(key.copyOf(), relays, cache)
        }
        state = state.copy(screen = Screen.Notes, busy = false, relays = relays, publicKey = repository!!.publicKeyHex(), message = null, biometricEnabled = biometric.isEnabled())
        refresh()
    }

    fun lock() {
        amberAuthorizationActive = false
        closeSession()
        state = UiState(if (vault.isConfigured()) Screen.Locked else Screen.Setup, relays = prefs.relays, biometricEnabled = biometric.isEnabled())
    }

    fun beginAmberAuthorization() { amberAuthorizationActive = true }
    fun finishAmberAuthorization() { amberAuthorizationActive = false }
    fun onAppStopped() { if (!amberAuthorizationActive) lock() }

    fun refresh() = runTask {
        val expected = session
        val notes = repository?.refresh()?.getOrThrow() ?: error("Application verrouillée")
        if (expected != session) return@runTask
        state = state.copy(notes = notes, message = if (notes.isEmpty()) "Aucune note trouvée." else null)
    }

    fun edit(note: Note? = null) { state = state.copy(screen = Screen.Editor(note), message = null) }
    fun settings() { state = state.copy(screen = Screen.Settings, message = null) }
    fun backToNotes() { state = state.copy(screen = Screen.Notes, message = null) }

    fun save(markdown: String, note: Note?) = runTask {
        val expected = session
        val saved = repository?.publish(markdown, note?.identifier)?.getOrThrow() ?: error("Application verrouillée")
        if (expected != session) return@runTask
        state = state.copy(
            screen = Screen.Notes,
            notes = (state.notes.filterNot { it.identifier == saved.identifier } + saved).sortedByDescending(Note::createdAt),
            message = "Note publiée."
        )
    }

    fun delete(note: Note) = runTask {
        val expected = session
        repository?.delete(note)?.getOrThrow() ?: error("Application verrouillée")
        if (expected != session) return@runTask
        state = state.copy(screen = Screen.Notes, notes = state.notes.filterNot { it.identifier == note.identifier }, message = "Suppression publiée.")
    }

    fun saveSettings(relayText: String) {
        runCatching {
            val relays = parseRelays(relayText)
            prefs.relays = relays
            val key = credential?.copyOf() ?: error("Application verrouillée")
            openSession(key, relays)
        }.onFailure { fail(it.message ?: "Relais invalides.") }
    }

    fun changePassword(old: String, new: String, confirmation: String) {
        if (new != confirmation) return fail("Les nouveaux mots de passe ne correspondent pas.")
        runCatching { vault.changePassword(old.toCharArray(), new.toCharArray()) }
            .onSuccess { state = state.copy(message = "Mot de passe modifié. Vous pouvez réactiver la biométrie dans les réglages.", biometricEnabled = false) }
            .onFailure { fail(it.message ?: "Modification impossible.") }
    }

    fun resetConnection() {
        closeSession()
        vault.clear()
        cache.clear()
        state = UiState(Screen.Setup, relays = prefs.relays)
    }

    fun dismissMessage() { state = state.copy(message = null) }
    fun reportError(message: String) { fail(message) }

    private fun parseRelays(text: String): List<String> {
        val values = text.lineSequence().map(String::trim).filter(String::isNotEmpty).distinct().toList()
        require(values.isNotEmpty()) { "Ajoutez au moins un relais." }
        require(values.all { it.startsWith("wss://") }) { "Chaque relais doit commencer par wss://" }
        return values
    }

    private fun runTask(block: suspend () -> Unit) {
        if (state.busy) return
        val expectedSession = session
        state = state.copy(busy = true, message = null)
        task = viewModelScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (session == expectedSession) fail(error.message ?: "Une erreur est survenue.") }
            finally { if (session == expectedSession) state = state.copy(busy = false) }
        }
    }

    fun prepareBiometric(enroll: Boolean): Cipher? {
        if (biometricRequest != null) return null
        return runCatching {
        if (enroll) check(state.screen == Screen.Settings && credential != null) { "Déverrouillez d’abord Notestr avec votre mot de passe." }
        else check(state.screen == Screen.Locked) { "Notestr est déjà déverrouillé." }
        val cipher = if (enroll) biometric.prepareEnrollment() else biometric.prepareUnlock()
        biometricRequest = BiometricRequest(enroll, cipher, session)
        cipher
    }.getOrElse {
        // Invalidated keys do not affect the password-protected vault.
        if (!enroll && state.screen == Screen.Locked) {
            biometric.disable()
            state = state.copy(biometricEnabled = false)
        }
        fail("${it.message ?: "Biométrie indisponible."} Utilisez le mot de passe ; vous pourrez réactiver la biométrie dans les réglages.")
        null
        }
    }

    fun completeBiometric(cipher: Cipher?) {
        val request = biometricRequest ?: return
        biometricRequest = null
        if (request.session != session || cipher !== request.cipher) return
        runCatching {
            if (request.enroll) {
                check(state.screen == Screen.Settings)
                biometric.enable(request.cipher, credential ?: error("Application verrouillée"))
                state = state.copy(biometricEnabled = true, message = "Déverrouillage biométrique activé.")
            } else {
                check(state.screen == Screen.Locked)
                openSession(biometric.unlock(request.cipher), prefs.relays)
            }
        }.onFailure { fail("Authentification biométrique impossible. Utilisez votre mot de passe.") }
    }

    fun cancelBiometric(message: String? = null) {
        biometricRequest = null
        if (message != null) fail(message)
    }

    fun disableBiometric() {
        biometricRequest = null
        biometric.disable()
        state = state.copy(biometricEnabled = false, message = "Déverrouillage biométrique désactivé.")
    }

    private fun fail(message: String) { state = state.copy(message = message, busy = false) }

    private fun closeSession() {
        session++
        task?.cancel(); task = null
        amberRequest?.result?.cancel(); amberRequest = null
        amberAuthorizationActive = false
        biometricRequest = null
        (repository as? AutoCloseable)?.close(); repository = null
        credential?.fill('\u0000'); credential = null
    }

    override fun onCleared() { closeSession(); super.onCleared() }
}
