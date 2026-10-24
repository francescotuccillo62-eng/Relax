package com.miaapp.relax

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking

/**
 * Riceve il broadcast dallo scadere della programmazione del filtro (AlarmManager).
 * Disattiva automaticamente il filtro e ferma la sentinella (foreground service).
 */
class ScadenzaFiltroReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScadenzaFiltroReceiver"
        const val ACTION_SCADENZA_FILTRO = "com.miaapp.relax.ACTION_SCADENZA_FILTRO"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SCADENZA_FILTRO) {
            Log.d(TAG, "Ricevuto broadcast scadenza filtro! Disattivo il filtro e la sentinella.")

            // 1) Ferma il foreground service (rimuove la notifica sentinella)
            FiltroForegroundService.ferma(context)

            // 2) Disattiva il filtro e la programmazione in DataStore
            runBlocking {
                PreferenzeFiltro.setFiltroAttivo(context, false)
            }

            Log.d(TAG, "Filtro spento automaticamente per scadenza oraria.")
        }
    }
}