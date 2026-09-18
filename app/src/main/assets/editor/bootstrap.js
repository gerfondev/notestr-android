// Loaded before the editor bundle so initialization failures can reach Android.
window.notesEditorFailure = '';
window.addEventListener('error', function (event) {
  window.notesEditorFailure = event.message || 'Une ressource de l’éditeur n’a pas pu être chargée.';
});
window.addEventListener('unhandledrejection', function (event) {
  window.notesEditorFailure = String(event.reason && event.reason.message || event.reason || 'Erreur du moteur visuel');
});
