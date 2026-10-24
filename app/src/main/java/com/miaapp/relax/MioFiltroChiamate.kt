package com.miaapp.relax

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class MioFiltroChiamate : CallScreeningService() {

    companion object {
        private const val TAG = "MioFiltroChiamate"
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val numeroUri: Uri? = callDetails.handle
        val numeroRaw = numeroUri?.schemeSpecificPart ?: ""
        Log.d(TAG, "Chiamata in arrivo da: $numeroRaw")

        // 1. Legge tutto lo stato in una volta sola
        val stato = PreferenzeFiltro.leggiStatoPerServizio(this)
        Log.d(
            TAG, "Stato: filtro=${stato.filtroAttivo}, " +
                    "programmazione=${stato.programmazioneAttiva}, " +
                    "tuttoIlGiorno=${stato.tuttoIlGiorno}, " +
                    "fascia=${stato.oraInizio}→${stato.oraFine}, " +
                    "lasciapassare=${stato.lasciapassare.size} voci"
        )

        // 2. Il filtro è spento → lascia passare
        if (!stato.filtroAttivo) {
            Log.d(TAG, "Filtro spento → lascio passare")
            rispondiLasciaPassare(callDetails)
            return
        }

        // 3. Programmazione attiva → controlla fascia o "tutto il giorno"
        if (stato.programmazioneAttiva) {
            if (stato.tuttoIlGiorno) {
                Log.d(TAG, "Tutto il giorno attivo → filtro sempre attivo")
                // Nessun controllo fascia, procedi
            } else {
                val dentroFascia = isDentroFascia(stato.oraInizio, stato.oraFine)
                Log.d(TAG, "Dentro la fascia oraria? $dentroFascia")

                if (!dentroFascia) {
                    Log.d(TAG, "Fuori fascia → lascio passare")
                    rispondiLasciaPassare(callDetails)
                    return
                }
            }
        }
        // 4. Controlla la lista LASCIAPASSARE (prima della rubrica, è più veloce)
        val inLasciapassare = isInLasciapassare(stato.lasciapassare, numeroRaw)
        Log.d(TAG, "È in lasciapassare? $inLasciapassare")

        if (inLasciapassare) {
            Log.d(TAG, "Numero in lasciapassare → lascio passare")
            rispondiLasciaPassare(callDetails)
            return
        }

        // 5. Controlla la rubrica
        val eInRubrica = isNumeroInRubrica(this, numeroRaw)
        Log.d(TAG, "È in rubrica? $eInRubrica")

        if (eInRubrica) {
            Log.d(TAG, "Contatto riconosciuto → lascio passare")
            rispondiLasciaPassare(callDetails)
        } else {
            Log.d(TAG, "Numero sconosciuto → BLOKKO")
            // Salviamo nel registro blocchi e rispondiamo al sistema
            rispondiBlocca(callDetails, numeroRaw)
        }
    }

    /** Lascia squillare normalmente */
    private fun rispondiLasciaPassare(callDetails: Call.Details) {
        val risposta = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .build()
        respondToCall(callDetails, risposta)
    }

    /**
     * Silenzia e rifiuta la chiamata, e la salva nel registro blocchi.
     */
    private fun rispondiBlocca(callDetails: Call.Details, numero: String) {
        // 1) Salva nel registro blocchi (prima di rispondere, con try/catch
        //    per non rischiare di far crashare il servizio).
        salvaBlocco(numero)

        // 2) Risponde al sistema: silenzia e rifiuta
        val risposta = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSilenceCall(true)
            .build()
        respondToCall(callDetails, risposta)
    }

    /**
     * Salva il numero bloccato nel registro persistente (DataStore).
     * Usa `runBlocking` perché onScreenCall deve dare una risposta sincrona
     * al sistema. Il salvataggio è rapidissimo (piccolo JSON).
     */
    private fun salvaBlocco(numero: String) {
        if (numero.isBlank()) return
        try {
            runBlocking {
                PreferenzeFiltro.aggiungiBlocco(this@MioFiltroChiamate, numero)
            }
            Log.d(TAG, "Blocco salvato nel registro: $numero")
        } catch (e: Exception) {
            Log.e(TAG, "Errore salvataggio blocco nel registro", e)
        }
    }

    /**
     * Verifica se l'ora corrente è dentro la fascia [oraInizio, oraFine].
     * I parametri sono minuti dalla mezzanotte (es. 14:00 = 840).
     * Gestisce il caso "attraversa la mezzanotte".
     */
    private fun isDentroFascia(oraInizio: Int, oraFine: Int): Boolean {
        val cal = Calendar.getInstance()
        val oraCorrente = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        return if (oraInizio <= oraFine) {
            oraCorrente in oraInizio..oraFine
        } else {
            oraCorrente >= oraInizio || oraCorrente <= oraFine
        }
    }

    /**
     * Verifica se il numero del chiamante corrisponde a una voce della lista
     * lasciapassare. Confronta le ultime 9 cifre, come per la rubrica.
     */
    private fun isInLasciapassare(
        lista: List<VoceLasciapassare>,
        numeroChiamante: String
    ): Boolean {
        if (numeroChiamante.isBlank()) return false

        val cifreChiamante = numeroChiamante.replace(Regex("[^0-9]"), "")
        if (cifreChiamante.length < 6) return false

        val suffissoChiamante = cifreChiamante.takeLast(9)

        for (voce in lista) {
            val cifreVoce = voce.numero.replace(Regex("[^0-9]"), "")
            if (cifreVoce.length < 6) continue

            val suffissoVoce = cifreVoce.takeLast(9)
            if (suffissoVoce == suffissoChiamante) {
                return true
            }
        }

        return false
    }

    /**
     * Verifica se un numero è presente nella rubrica.
     */
    private fun isNumeroInRubrica(context: Context, numeroChiamante: String): Boolean {
        if (numeroChiamante.isBlank()) return false

        val cifreChiamante = numeroChiamante.replace(Regex("[^0-9]"), "")
        if (cifreChiamante.length < 6) return false

        val suffissoChiamante = cifreChiamante.takeLast(9)

        val resolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val proiezione = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        try {
            resolver.query(uri, proiezione, null, null, null)?.use { cursor ->
                val idxNumero = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                while (cursor.moveToNext()) {
                    val numeroRubrica = cursor.getString(idxNumero) ?: continue
                    val cifreRubrica = numeroRubrica.replace(Regex("[^0-9]"), "")
                    if (cifreRubrica.length < 6) continue

                    val suffissoRubrica = cifreRubrica.takeLast(9)
                    if (suffissoRubrica == suffissoChiamante) {
                        return true
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permesso READ_CONTACTS mancante!", e)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Errore lettura rubrica", e)
            return true
        }

        return false
    }
}