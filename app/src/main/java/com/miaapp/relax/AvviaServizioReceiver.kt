package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// ─────────────────────────────────────────────────────────────
// BROADCAST RECEIVER: AVVIO DOPO RIAVVIO DEL TELEFONO
// ─────────────────────────────────────────────────────────────

/**
 * Riceve l'evento BOOT_COMPLETED quando il telefono ha finito di avviarsi.
 *
 * Se il filtro era attivo prima del riavvio, riavvia il foreground service,
 * così la sentinella torna attiva automaticamente.
 *
 * ⚠️ Questo receiver NON parte se l'app non è mai stata aperta almeno una
 *    volta dopo l'installazione (Android non consegna BOOT_COMPLETED alle
 *    app mai avviate). È un limite del sistema, non un bug.
 */
class AvviaServizioReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AvviaServizioReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Verifica che sia effettivamente il boot completato
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Ricevuto BOOT_COMPLETED, verifico lo stato del filtro")

        try {
            // Legge lo stato del filtro da DataStore
            val stato = PreferenzeFiltro.leggiStatoPerServizio(context)

            if (stato.filtroAttivo) {
                Log.d(TAG, "Filtro era attivo → riavvio il servizio")
                // Avvia il foreground service (mostra la notifica)
                FiltroForegroundService.avvia(context)
            } else {
                Log.d(TAG, "Filtro spento → nessuna azione")
            }
            // Riprogramma l'allarme di scadenza dopo il riavvio del telefono
            ProgrammatoreAllarmi.aggiornaScadenzaFiltro(context)
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante il riavvio del servizio", e)
        }
    }
}