package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking

// ─────────────────────────────────────────────────────────────
// BROADCAST RECEIVER: SPEGNI FILTRO
// ─────────────────────────────────────────────────────────────

/**
 * Riceve i broadcast relativi allo spegnimento del filtro.
 *
 * Azioni gestite:
 *  1. ACTION_SPEGNI_FILTRO → spegne il filtro (dal pulsante nella notifica).
 *     L'app resta in background.
 *  2. ACTION_SPEGNI_E_APRI → spegne il filtro E apre l'app.
 *     Usato quando l'utente fa swipe sulla notifica (gesto = "non voglio più la sentinella").
 */
class SpegniFiltroReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SpegniFiltroReceiver"

        // Nome dell'action per il pulsante "Spegni filtro".
        const val ACTION_SPEGNI_FILTRO = "com.miaapp.relax.SPEGNI_FILTRO"

        // Nome dell'action per lo swipe sulla notifica (spegni + apri app).
        const val ACTION_SPEGNI_E_APRI = "com.miaapp.relax.SPEGNI_E_APRI"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SPEGNI_FILTRO -> {
                Log.d(TAG, "Ricevuto comando: spegni filtro")
                spegniFiltro(context)
            }

            ACTION_SPEGNI_E_APRI -> {
                Log.d(TAG, "Ricevuto comando: spegni filtro + apri app")
                spegniFiltro(context)
                apriApp(context)
            }

            else -> {
                Log.d(TAG, "Azione non gestita: ${intent.action}")
            }
        }
    }

    /**
     * Spegne il filtro in DataStore e ferma il foreground service.
     * (Bloccante perché il receiver ha vita breve.)
     */
    private fun spegniFiltro(context: Context) {
        try {
            // 1) Ferma il foreground service (rimuove la notifica)
            FiltroForegroundService.ferma(context)

            // 2) Spegne il filtro in DataStore
            runBlocking {
                PreferenzeFiltro.setFiltroAttivo(context, false)
                PreferenzeFiltro.setProgrammazioneAttiva(context, false)
            }

            Log.d(TAG, "Filtro spento")
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante lo spegnimento del filtro", e)
        }
    }

    /**
     * Apre l'app in primo piano (Activity principale).
     * Usato quando l'utente fa swipe sulla notifica: gli mostriamo che
     * il filtro è stato spento.
     */
    private fun apriApp(context: Context) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante l'apertura dell'app", e)
        }
    }
}