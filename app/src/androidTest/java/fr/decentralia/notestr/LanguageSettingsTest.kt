package fr.decentralia.notestr

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.lifecycle.ViewModelProvider
import fr.decentralia.notestr.ui.NotestrViewModel
import fr.decentralia.notestr.data.storage.AppPreferences
import fr.decentralia.notestr.i18n.Strings
import fr.decentralia.notestr.i18n.tr
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LanguageSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun settingsShowInstalledVersionAndRememberLanguageWithoutChangingRelays() {
        lateinit var vm: NotestrViewModel
        lateinit var relays: List<String>
        compose.runOnIdle {
            vm = ViewModelProvider(compose.activity)[NotestrViewModel::class.java]
            vm.selectLanguage("fr")
            relays = AppPreferences(compose.activity).relays
            vm.settings()
        }
        try {
            compose.onNodeWithText("Version de l’application : " + BuildConfig.VERSION_NAME).assertIsDisplayed()
            compose.onNodeWithText("English").performClick()
            compose.onNodeWithText("Settings").assertIsDisplayed()
            compose.onNodeWithText("App version : " + BuildConfig.VERSION_NAME).assertIsDisplayed()
            compose.runOnIdle {
                assertEquals("en", AppPreferences(compose.activity).language)
                assertEquals(relays, AppPreferences(compose.activity).relays)
                assertEquals("Settings", tr("Réglages"))
                // An arbitrary note string is not interpreted by the message catalogue.
                assertEquals("My note: Réglages", tr("My note: Réglages"))
            }
            compose.activityRule.scenario.recreate()
            compose.onNodeWithText("Settings").assertIsDisplayed()
            compose.onNodeWithText("Français").performClick()
            compose.onNodeWithText("Réglages").assertIsDisplayed()
            compose.runOnIdle { assertEquals("fr", AppPreferences(compose.activity).language) }
        } finally {
            compose.runOnIdle {
                AppPreferences(compose.activity).language = "fr"
                Strings.select("fr")
            }
        }
    }
}
