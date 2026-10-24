package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat

// ─────────────────────────────────────────────────────────────
// FOREGROUND SERVICE: FILTRO ATTIVO
// ─────────────────────────────────────────────────────────────

/**
 * Servizio in foreground che gira finché il filtro è attivo.
 *
 * ⚠️ Idempotente: chiamate ripetute di `avvia()` mentre è già attivo
 *    non fanno nulla. Chiamate ripetute di `ferma()` mentre è già fermo
 *    non fanno nulla.
 */
class FiltroForegroundService : Service() {

    companion object {
        private const val TAG = "FiltroForegroundService"

        // ID della notifica (univoco per l'app)
        private const val ID_NOTIFICA = 1001

        // ID del canale di notifica (Android 8+)
        private const val ID_CANALE = "canale_filtro_attivo_v5"

        // Azioni che il servizio sa gestire
        const val ACTION_AVVIA = "com.miaapp.relax.AVVIA_FILTRO_SERVICE"
        const val ACTION_FERMA = "com.miaapp.relax.FERMA_FILTRO_SERVICE"

        // Flag che indica se il servizio è già in esecuzione.
        // Usato per rendere avvia/ferma idempotenti.
        @Volatile
        private var inEsecuzione: Boolean = false

        /** Restituisce true se il servizio è attualmente in esecuzione. */
        fun isInEsecuzione(): Boolean = inEsecuzione

        /**
         * Avvia il servizio in foreground. Se è già avviato, non fa nulla.
         */
        fun avvia(context: Context) {
            if (inEsecuzione) {
                Log.d(TAG, "avvia() ignorata: servizio già in esecuzione")
                return
            }
            val intent = Intent(context, FiltroForegroundService::class.java).apply {
                action = ACTION_AVVIA
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Errore durante avvia()", e)
            }
        }

        /**
         * Ferma il servizio. Se è già fermo, non fa nulla.
         */
        fun ferma(context: Context) {
            if (!inEsecuzione) {
                Log.d(TAG, "ferma() ignorata: servizio già fermo")
                return
            }
            val intent = Intent(context, FiltroForegroundService::class.java).apply {
                action = ACTION_FERMA
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Errore durante ferma()", e)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // CICLO DI VITA DEL SERVIZIO
    // ═══════════════════════════════════════════════

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_AVVIA -> {
                if (inEsecuzione) {
                    Log.d(TAG, "onStartCommand AVVIA: già attivo, ignoro")
                    return START_STICKY
                }
                creaCanaleSeNecessario()
                avviaForeground()
                inEsecuzione = true
                Log.d(TAG, "Servizio avviato in foreground")
            }

            ACTION_FERMA -> {
                if (!inEsecuzione) {
                    Log.d(TAG, "onStartCommand FERMA: già fermo, ignoro")
                    return START_NOT_STICKY
                }
                inEsecuzione = false
                fermaServizio()
                Log.d(TAG, "Servizio fermato")
            }

            else -> {
                // Chiamata senza action: se non è in esecuzione, avvia.
                if (!inEsecuzione) {
                    creaCanaleSeNecessario()
                    avviaForeground()
                    inEsecuzione = true
                    Log.d(TAG, "Servizio avviato in foreground (no action)")
                }
            }
        }

        return START_STICKY
    }

    // ═══════════════════════════════════════════════
    // GESTIONE FOREGROUND
    // ═══════════════════════════════════════════════

    private fun avviaForeground() {
        val notifica = costruisciNotifica()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ID_NOTIFICA,
                notifica,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(ID_NOTIFICA, notifica)
        }
    }

    private fun fermaServizio() {
        NotificationManagerCompat.from(this).cancel(ID_NOTIFICA)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ═══════════════════════════════════════════════
    // COSTRUZIONE DELLA NOTIFICA
    // ═══════════════════════════════════════════════

    private fun costruisciNotifica(): android.app.Notification {
        val intentSpegni = Intent(this, SpegniFiltroReceiver::class.java).apply {
            action = SpegniFiltroReceiver.ACTION_SPEGNI_FILTRO
        }
        val pendingSpegni = PendingIntent.getBroadcast(
            this,
            0,
            intentSpegni,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intentApri = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingApri = PendingIntent.getActivity(
            this,
            1,
            intentApri,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // ── PendingIntent per lo SWIPE sulla notifica ──
        // Quando l'utente fa swipe per rimuovere la notifica, Android
        // invia questo intent. Noi ne approfittiamo per:
        //   1) spegnere il filtro (gesto = "non voglio più la sentinella")
        //   2) aprire l'app per mostrare che il filtro è spento
        val intentSwipe = Intent(this, SpegniFiltroReceiver::class.java).apply {
            action = SpegniFiltroReceiver.ACTION_SPEGNI_E_APRI
        }
        val pendingSwipe = PendingIntent.getBroadcast(
            this,
            3,   // requestCode diverso dagli altri (2 è già usato)
            intentSwipe,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ID_CANALE)
            .setSmallIcon(R.drawable.ic_notifica)
            .setLargeIcon(
                android.graphics.BitmapFactory.decodeResource(
                    resources,
                    R.drawable.logo_app
                )
            )
            .setContentTitle("Relax — Filtro attivo")
            .setContentText("Le chiamate indesiderate sono bloccate")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setDeleteIntent(pendingSwipe)   // 👈 gestisce lo swipe
            .setContentIntent(pendingApri)
            .setSound(null)      // 👈 silenzio
            .setVibrate(null)    // 👈 niente vibrazione
            .addAction(
                R.drawable.logo_app,
                "Spegni filtro",
                pendingSpegni
            )
            .build()
    }

    // ═══════════════════════════════════════════════
    // CANALE DI NOTIFICA
    // ═══════════════════════════════════════════════

    private fun creaCanaleSeNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nomeCanale = "Stato filtro chiamate"
            val descrizione = "Notifica del servizio che tiene attivo il filtro"
            val importanza = NotificationManager.IMPORTANCE_DEFAULT

            val canale = NotificationChannel(ID_CANALE, nomeCanale, importanza).apply {
                description = descrizione
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canale)
        }
    }

    // ═══════════════════════════════════════════════
    // PULIZIA QUANDO IL SERVIZIO VIENE DISTRUTTO
    // ═══════════════════════════════════════════════

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Servizio distrutto")
        inEsecuzione = false
        NotificationManagerCompat.from(this).cancel(ID_NOTIFICA)
    }
}