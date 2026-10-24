package com.miaapp.relax

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object ProgrammatoreAllarmi {

    private const val TAG = "ProgrammatoreAllarmi"
    private const val REQUEST_CODE_SCADENZA = 2001

    /**
     * Calcola l'orario di scadenza del filtro in base alla programmazione
     * ed imposta un allarme esatto di sistema via AlarmManager.
     */
    fun aggiornaScadenzaFiltro(context: Context) {
        val stato = PreferenzeFiltro.leggiStatoPerServizio(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ScadenzaFiltroReceiver::class.java).apply {
            action = ScadenzaFiltroReceiver.ACTION_SCADENZA_FILTRO
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCADENZA,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Se il filtro o la programmazione NON sono attivi, cancelliamo eventuale allarme esistente
        if (!stato.filtroAttivo || !stato.programmazioneAttiva) {
            Log.d(TAG, "Filtro o programmazione non attivi -> Cancello allarme di scadenza")
            alarmManager.cancel(pendingIntent)
            return
        }

        val oraCorrenteMillis = System.currentTimeMillis()
        val calTarget = Calendar.getInstance()

        if (stato.tuttoIlGiorno) {
            // "Tutto il giorno": scade a mezzanotte di domani (00:00:00)
            calTarget.set(Calendar.HOUR_OF_DAY, 0)
            calTarget.set(Calendar.MINUTE, 0)
            calTarget.set(Calendar.SECOND, 0)
            calTarget.set(Calendar.MILLISECOND, 0)
            calTarget.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            // "Fascia oraria": scade a oraFine
            val oraFineH = stato.oraFine / 60
            val oraFineM = stato.oraFine % 60

            calTarget.set(Calendar.HOUR_OF_DAY, oraFineH)
            calTarget.set(Calendar.MINUTE, oraFineM)
            calTarget.set(Calendar.SECOND, 0)
            calTarget.set(Calendar.MILLISECOND, 0)

            // Se l'ora di fine impostata è già passata per oggi, la scadenza è domani alla stessa ora
            if (calTarget.timeInMillis <= oraCorrenteMillis) {
                calTarget.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val targetMillis = calTarget.timeInMillis
        Log.d(TAG, "Allarme scadenza programmato per: ${calTarget.time} ($targetMillis ms)")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Impossibile impostare allarme esatto -> Uso allarme standard", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Errore programmazione allarme scadenza filtro", e)
        }
    }
}