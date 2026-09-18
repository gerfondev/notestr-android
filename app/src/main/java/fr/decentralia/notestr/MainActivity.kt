package fr.decentralia.notestr

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import fr.decentralia.notestr.ui.NotestrApp
import fr.decentralia.notestr.ui.theme.NotestrTheme

class MainActivity : FragmentActivity() {
    private val viewModel by viewModels<fr.decentralia.notestr.ui.NotestrViewModel>()

    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.completeBiometric(result.cryptoObject?.cipher)
            }
            override fun onAuthenticationError(code: Int, message: CharSequence) {
                val cancelled = code == BiometricPrompt.ERROR_NEGATIVE_BUTTON || code == BiometricPrompt.ERROR_USER_CANCELED || code == BiometricPrompt.ERROR_CANCELED
                viewModel.cancelBiometric(if (cancelled) null else "$message Vous pouvez utiliser votre mot de passe.")
            }
        })
        setContent {
            NotestrTheme {
                NotestrApp(viewModel, ::authenticateBiometric)
            }
        }
    }

    private fun authenticateBiometric(enroll: Boolean) {
        val available = BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (available != BiometricManager.BIOMETRIC_SUCCESS) {
            viewModel.reportError("Aucune biométrie forte disponible. Configurez une empreinte ou un visage compatible dans les réglages du téléphone, ou utilisez votre mot de passe.")
            return
        }
        val cipher = viewModel.prepareBiometric(enroll) ?: return
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(if (enroll) "Activer la biométrie" else "Déverrouiller Notestr")
            .setSubtitle(if (enroll) "Protéger l’accès à votre coffre" else "Accéder à vos notes privées")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(if (enroll) "Annuler" else "Utiliser le mot de passe")
            .build()
        runCatching { biometricPrompt.authenticate(info, BiometricPrompt.CryptoObject(cipher)) }
            .onFailure { viewModel.cancelBiometric("Impossible de démarrer la biométrie. Utilisez votre mot de passe.") }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            biometricPrompt.cancelAuthentication()
            viewModel.onAppStopped()
        }
    }
}
