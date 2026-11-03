package fr.decentralia.notestr.domain.model

import fr.decentralia.notestr.i18n.tr
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale

data class NoteFolder(val id: String, val name: String)

data class FolderState(
    val folders: List<NoteFolder> = emptyList(),
    val assignments: Map<String, String> = emptyMap()
) {
    fun validated(): FolderState {
        require(folders.size <= 500) { tr("Données de dossiers invalides.") }
        val ids = mutableSetOf<String>()
        val names = mutableSetOf<String>()
        val normalized = folders.map { folder ->
            val name = Normalizer.normalize(folder.name.trim(), Normalizer.Form.NFC)
            require(folder.id.matches(Regex("[a-f0-9]{32}")) && ids.add(folder.id) &&
                name.isNotEmpty() && name.length <= 80 && name.none { it.code < 32 } &&
                names.add(name.lowercase(Locale.ROOT))) { tr("Nom ou identifiant de dossier invalide ou déjà utilisé.") }
            folder.copy(name = name)
        }
        require(assignments.size <= 10_000) { tr("Trop d’affectations de dossiers.") }
        val validIds = normalized.map { it.id }.toSet()
        val clean = assignments.filter { (noteId, folderId) ->
            require(noteId.isNotEmpty() && noteId.length <= 4096 && noteId.none { it.code < 32 }) { tr("Affectation de note invalide.") }
            require(folderId in validIds) { tr("Le dossier choisi n’existe pas.") }
            true
        }
        return FolderState(normalized, clean)
    }

    fun toJson(): String {
        val valid = validated()
        val foldersJson = JSONArray().apply {
            valid.folders.forEach { put(JSONObject().put("id", it.id).put("name", it.name)) }
        }
        val assignmentsJson = JSONObject().apply {
            valid.assignments.toSortedMap().forEach { (noteId, folderId) -> put(noteId, folderId) }
        }
        return JSONObject().put("version", 1).put("folders", foldersJson)
            .put("assignments", assignmentsJson).toString()
    }

    companion object {
        fun fromJson(value: String): FolderState {
            val root = JSONObject(value)
            require(root.length() == 3 && root.getInt("version") == 1) { tr("Métadonnées de dossiers invalides.") }
            val folders = buildList {
                val items = root.getJSONArray("folders")
                for (index in 0 until items.length()) {
                    val item = items.getJSONObject(index)
                    require(item.length() == 2) { tr("Métadonnées de dossiers invalides.") }
                    add(NoteFolder(item.getString("id"), item.getString("name")))
                }
            }
            val assignmentsJson = root.getJSONObject("assignments")
            val assignments = buildMap {
                val keys = assignmentsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, assignmentsJson.getString(key))
                }
            }
            return FolderState(folders, assignments).validated()
        }
    }
}
