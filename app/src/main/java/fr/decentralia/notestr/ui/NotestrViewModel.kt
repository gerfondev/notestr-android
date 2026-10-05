package fr.decentralia.notestr.ui

import fr.decentralia.notestr.i18n.tr

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
import fr.decentralia.notestr.domain.model.noteOrder
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import org.nostrdevkit.sdk.SecretKey

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
    val refreshing: Boolean = false,
    val syncing: Boolean = false,
    val showingTrash: Boolean = false,
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
    private var backgroundSince: Long? = null
    private var backgroundLockTask: Job? = null
    private var task: Job? = null
    private var syncTask: Job? = null
    private val sessionJobs = mutableListOf<Job>()
    private val network = application.getSystemService(ConnectivityManager::class.java)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(n: Network, caps: NetworkCapabilities) {
            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) viewModelScope.launch { synchronizePending() }
        }
    }
    private fun tracked(job: Job): Job {
        sessionJobs.removeAll { it.isCompleted }
        sessionJobs += job
        return job
    }
    private fun online(): Boolean = network.getNetworkCapabilities(network.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    private val prefs = AppPreferences(application)
    init { fr.decentralia.notestr.i18n.Strings.select(prefs.language) }
    private val cache = EventCache(application)
    private var credential: CharArray? = null
    private var repository: NostrRepository? = null
    private var amberAuthorizationActive = false

    data class AmberRequest(val intent: Intent, val column: String, val result: CompletableDeferred<String>, var launched: Boolean = false)
    var amberRequest by mutableStateOf<AmberRequest?>(null)
        private set

    internal suspend fun authorizeAmber(intent: Intent, column: String): String {
        check(amberRequest == null) { tr("Une demande Amber est déjà en cours.") }
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
            check(resultCode == Activity.RESULT_OK) { tr("Opération Amber annulée ou interrompue. Votre note reste dans l’éditeur.") }
            check(data?.getBooleanExtra("rejected", false) != true) { tr("Opération refusée dans Amber.") }
            data?.getStringExtra(request.column)?.takeIf { it.isNotEmpty() }
                ?: error(tr("Réponse Amber incomplète."))
        }
        result.fold(request.result::complete, request.result::completeExceptionally)
    }

    fun failAmberLaunch() {
        amberRequest?.result?.completeExceptionally(IllegalStateException(tr("Impossible d’ouvrir Amber. Vérifiez que l’application est installée.")))
    }

    var state by mutableStateOf(UiState(if (vault.isConfigured()) Screen.Locked else Screen.Setup, relays = prefs.relays, biometricEnabled = biometric.isEnabled()))
        private set

    init {
        network.registerDefaultNetworkCallback(networkCallback)
        viewModelScope.launch {
            while (isActive) { delay(30_000); synchronizePending() }
        }
    }

    private fun synchronizePending() {
        if (state.busy || state.syncing || !online() || state.notes.none { it.pending && !it.conflicted }) return
        val active = repository ?: return
        val expected = session
        state = state.copy(syncing = true)
        syncTask = tracked(viewModelScope.launch {
            try {
                val result = active.syncPending()
                if (expected != session) return@launch
                val notes = result.getOrNull() ?: active.cached().getOrThrow()
                if (expected == session) state = state.copy(notes = notes,
                    message = if (notes.any { it.conflicted }) tr("Conflit : les deux versions sont conservées. Ouvrez la modification locale pour choisir.")
                    else if (result.isSuccess && notes.none { it.pending }) tr("Modifications synchronisées.") else null)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Durable pending edits remain visible and will be retried. */ }
            finally { if (expected == session) state = state.copy(syncing = false) }
        })
    }

    fun setup(password: String, confirmation: String, privateKey: String, relayText: String) {
        if (password != confirmation) return fail(tr("Les mots de passe ne correspondent pas."))
        runCatching {
            val relays = parseRelays(relayText)
            val key = privateKey.trim().toCharArray()
            SecretKey.parse(key.concatToString()).close()
            vault.create(password.toCharArray(), key)
            prefs.relays = relays
            openSession(key.copyOf(), relays)
            key.fill('\u0000')
        }.onFailure { fail(it.message ?: tr("Configuration impossible.")) }
    }

    fun setupAmber(password: String, confirmation: String, publicKey: String, packageName: String, relayText: String) {
        if (password != confirmation) return fail(tr("Les mots de passe ne correspondent pas."))
        runCatching {
            val relays = parseRelays(relayText)
            val account = AmberAccount.create(publicKey, packageName)
            val stored = account.encode()
            vault.create(password.toCharArray(), stored)
            prefs.relays = relays
            openSession(stored.copyOf(), relays)
            stored.fill('\u0000')
        }.onFailure { fail(it.message ?: tr("Connexion à Amber impossible.")) }
    }

    fun unlock(password: String) {
        runCatching {
            val key = vault.unlock(password.toCharArray())
            openSession(key, prefs.relays)
        }.onFailure { fail(it.message ?: tr("Déverrouillage impossible.")) }
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
        state = state.copy(screen = Screen.Notes, notes = emptyList(), busy = false, refreshing = false, syncing = false, relays = relays, publicKey = repository!!.publicKeyHex(), message = null, biometricEnabled = biometric.isEnabled())
        refresh()
    }

    fun lock() {
        backgroundLockTask?.cancel(); backgroundLockTask = null
        backgroundSince = null
        amberAuthorizationActive = false
        closeSession()
        state = UiState(if (vault.isConfigured()) Screen.Locked else Screen.Setup, relays = prefs.relays, biometricEnabled = biometric.isEnabled())
    }

    fun beginAmberAuthorization() { amberAuthorizationActive = true }
    fun finishAmberAuthorization() { amberAuthorizationActive = false }
    fun onAppStopped(now: Long = android.os.SystemClock.elapsedRealtime()) {
        if (backgroundSince != null) return
        backgroundSince = now
        backgroundLockTask = viewModelScope.launch {
            val remaining = (180_000L - (android.os.SystemClock.elapsedRealtime() - now)).coerceAtLeast(0)
            delay(remaining)
            if (backgroundSince == now) lock()
        }
    }

    fun onAppStarted(now: Long = android.os.SystemClock.elapsedRealtime()) {
        val stopped = backgroundSince
        // Recheck before displaying notes: Android can suspend the process and its timer.
        if (stopped != null && now - stopped >= 180_000L) lock()
        backgroundLockTask?.cancel()
        backgroundLockTask = null
        backgroundSince = null
    }

    fun refresh() = runTask {
        val expected = session
        val active = repository ?: error(tr("Application verrouillée"))
        state = state.copy(refreshing = true)
        try {
            val cached = active.cached().getOrThrow()
            if (expected != session) return@runTask
            if (cached.isNotEmpty()) state = state.copy(notes = cached)
            val notes = active.refresh().getOrThrow()
            if (expected != session) return@runTask
            state = state.copy(notes = notes, message = if (notes.isEmpty()) tr("Aucune note trouvée.") else null)
        } finally {
            if (expected == session) state = state.copy(refreshing = false)
        }
    }

    fun edit(note: Note? = null) { state = state.copy(screen = Screen.Editor(note), message = null) }
    fun selectLanguage(language: String) {
        prefs.language = language
        fr.decentralia.notestr.i18n.Strings.select(prefs.language)
        state = state.copy(message = null)
    }

    fun settings() { state = state.copy(screen = Screen.Settings, message = null) }
    fun backToNotes() { state = state.copy(screen = Screen.Notes, message = null) }

    fun save(markdown: String, note: Note?, asCopy: Boolean = false) = runTask {
        val expected = session
        val publication = repository?.saveLocal(markdown, note, asCopy)?.getOrThrow() ?: error(tr("Application verrouillée"))
        val saved = publication.note
        if (expected != session) return@runTask
        state = state.copy(
            screen = Screen.Notes,
            notes = if (saved.pending) repository!!.cached().getOrThrow() else (state.notes.filterNot { it.identifier == saved.identifier } + saved).sortedWith(noteOrder),
            message = publication.warning ?: tr("Note publiée.")
        )
    }

    fun discardPending(note: Note) = runTask {
        val notes = repository?.discardPending(note)?.getOrThrow() ?: return@runTask
        state = state.copy(screen = Screen.Notes, notes = notes, message = tr("Modifications locales abandonnées."))
    }

    fun restorePrevious(note: Note, loaded: (String) -> Unit) = runTask {
        val expected = session
        val result = repository?.previous(note)?.getOrThrow() ?: run {
            if (expected == session) fail(tr("Aucune version précédente disponible pour cette note."))
            return@runTask
        }
        if (expected != session || state.screen != Screen.Editor(note)) return@runTask
        loaded(result)
        state = state.copy(message = tr("Version précédente chargée. Vérifiez puis appuyez sur Publier pour la restaurer."))
    }

    fun togglePinned(note: Note) = runTask {
        val expected = session
        val result = repository?.setPinned(note, !note.pinned)?.getOrThrow() ?: error(tr("Application verrouillée"))
        if (expected != session) return@runTask
        state = state.copy(
            notes = state.notes.map { if (it.identifier == note.identifier) result.note else it }.sortedWith(noteOrder),
            message = result.warning ?: if (result.note.pinned) tr("Note épinglée.") else tr("Note désépinglée.")
        )
    }

    fun toggleTrashView() { state = state.copy(screen = Screen.Notes, showingTrash = !state.showingTrash, message = null) }

    fun setTrashed(note: Note, trashed: Boolean) = runTask {
        val expected = session
        val result = repository?.setTrashed(note, trashed)?.getOrThrow() ?: error(tr("Application verrouillée"))
        if (expected != session) return@runTask
        state = state.copy(screen = Screen.Notes,
            notes = state.notes.map { if (it.identifier == note.identifier) result.note else it },
            message = result.warning ?: tr(if (trashed) "Note déplacée dans la corbeille." else "Note restaurée."))
    }

    fun delete(note: Note) = runTask {
        val expected = session
        repository?.delete(note)?.getOrThrow() ?: error(tr("Application verrouillée"))
        if (expected != session) return@runTask
        state = state.copy(screen = Screen.Notes, notes = state.notes.filterNot { it.identifier == note.identifier }, message = tr("Suppression publiée."))
    }

    fun saveSettings(relayText: String) {
        runCatching {
            val relays = parseRelays(relayText)
            prefs.relays = relays
            val key = credential?.copyOf() ?: error(tr("Application verrouillée"))
            openSession(key, relays)
        }.onFailure { fail(it.message ?: tr("Relais invalides.")) }
    }

    fun changePassword(old: String, new: String, confirmation: String) {
        if (new != confirmation) return fail(tr("Les nouveaux mots de passe ne correspondent pas."))
        runCatching { vault.changePassword(old.toCharArray(), new.toCharArray()) }
            .onSuccess { state = state.copy(message = tr("Mot de passe modifié. Vous pouvez réactiver la biométrie dans les réglages."), biometricEnabled = false) }
            .onFailure { fail(it.message ?: tr("Modification impossible.")) }
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
        require(values.isNotEmpty()) { tr("Ajoutez au moins un relais.") }
        require(values.all { it.startsWith("wss://") }) { tr("Chaque relais doit commencer par wss://") }
        return values
    }

    private fun runTask(block: suspend () -> Unit) {
        if (state.busy && !state.refreshing) return
        val interrupted = listOfNotNull(syncTask, task.takeIf { state.refreshing })
        interrupted.forEach { it.cancel() }
        val expectedSession = session
        state = state.copy(busy = true, refreshing = false, message = null)
        task = tracked(viewModelScope.launch {
            try { interrupted.joinAll(); block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (session == expectedSession) fail(error.message ?: tr("Une erreur est survenue.")) }
            finally {
                if (session == expectedSession) {
                    state = state.copy(busy = false)
                    synchronizePending()
                }
            }
        })
    }

    fun prepareBiometric(enroll: Boolean): Cipher? {
        if (biometricRequest != null) return null
        return runCatching {
        if (enroll) check(state.screen == Screen.Settings && credential != null) { tr("Déverrouillez d’abord Notestr avec votre mot de passe.") }
        else check(state.screen == Screen.Locked) { tr("Notestr est déjà déverrouillé.") }
        val cipher = if (enroll) biometric.prepareEnrollment() else biometric.prepareUnlock()
        biometricRequest = BiometricRequest(enroll, cipher, session)
        cipher
    }.getOrElse {
        // Invalidated keys do not affect the password-protected vault.
        if (!enroll && state.screen == Screen.Locked) {
            biometric.disable()
            state = state.copy(biometricEnabled = false)
        }
        fail((it.message ?: tr("Biométrie indisponible.")) + tr(" Utilisez le mot de passe ; vous pourrez réactiver la biométrie dans les réglages."))
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
                biometric.enable(request.cipher, credential ?: error(tr("Application verrouillée")))
                state = state.copy(biometricEnabled = true, message = tr("Déverrouillage biométrique activé."))
            } else {
                check(state.screen == Screen.Locked)
                openSession(biometric.unlock(request.cipher), prefs.relays)
            }
        }.onFailure { fail(tr("Authentification biométrique impossible. Utilisez votre mot de passe.")) }
    }

    fun cancelBiometric(message: String? = null) {
        biometricRequest = null
        if (message != null) fail(message)
    }

    fun disableBiometric() {
        biometricRequest = null
        biometric.disable()
        state = state.copy(biometricEnabled = false, message = tr("Déverrouillage biométrique désactivé."))
    }

    private fun fail(message: String) { state = state.copy(message = message, busy = false) }

    private fun closeSession() {
        session++
        val closingJobs = sessionJobs.toList()
        sessionJobs.clear()
        val closingRepository = repository as? AutoCloseable
        task = null; syncTask = null; repository = null
        closingJobs.forEach { it.cancel() }
        // Local saves can cancel a sync. Wait for every session job before freeing native keys.
        CoroutineScope(Dispatchers.IO).launch { closingJobs.joinAll(); closingRepository?.close() }
        amberRequest?.result?.cancel(); amberRequest = null
        amberAuthorizationActive = false
        biometricRequest = null
        credential?.fill('\u0000'); credential = null
    }

    override fun onCleared() { backgroundLockTask?.cancel(); network.unregisterNetworkCallback(networkCallback); closeSession(); super.onCleared() }
}
