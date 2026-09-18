package fr.decentralia.notestr.data.storage

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("notestr_settings", Context.MODE_PRIVATE)
    var relays: List<String>
        get() = prefs.getString(KEY_RELAYS, DEFAULT_RELAY).orEmpty().lineSequence()
            .map(String::trim).filter(String::isNotEmpty).distinct().toList().ifEmpty { listOf(DEFAULT_RELAY) }
        set(value) { prefs.edit().putString(KEY_RELAYS, value.joinToString("\n")).apply() }
    companion object {
        const val DEFAULT_RELAY = "wss://relay.decentralia.fr"
        private const val KEY_RELAYS = "relays"
    }
}
