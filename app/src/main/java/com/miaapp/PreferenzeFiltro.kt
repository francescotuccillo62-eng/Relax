package com.miaapp.relax

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Nome del file DataStore (preferenze dell'app)
private val Context.dataStore by preferencesDataStore(name = "impostazioni_filtro")

// ─────────────────────────────────────────────────────────────
// MODELLI SERIALIZZABILI
// ─────────────────────────────────────────────────────────────

@Serializable
data class VoceLasciapassare(val nome: String, val numero: String)

@Serializable
data class Blocco(val numero: String, val timestamp: Long)

// ─────────────────────────────────────────────────────────────
// OGGETTO PREFERENZE
// ─────────────────────────────────────────────────────────────

object PreferenzeFiltro {

    // ── Chiavi DataStore ──
    private val CHIAVE_FILTRO_ATTIVO = booleanPreferencesKey("filtro_attivo")
    private val CHIAVE_PROGRAMMAZIONE_ATTIVA = booleanPreferencesKey("programmazione_attiva")
    private val CHIAVE_TUTTO_IL_GIORNO = booleanPreferencesKey("tutto_il_giorno")
    private val CHIAVE_ORA_INIZIO = intPreferencesKey("ora_inizio")
    private val CHIAVE_ORA_FINE = intPreferencesKey("ora_fine")
    private val CHIAVE_LASCIAPASSARE = stringPreferencesKey("lasciapassare_json")
    private val CHIAVE_REGISTRO_BLOCCHI = stringPreferencesKey("registro_blocchi_json")
    private val CHIAVE_EFFETTO_HOME = intPreferencesKey("effetto_home") // 👈 NUOVA

    // ── Valori di default ──
    private const val DEFAULT_ORA_INIZIO = 14 * 60
    private const val DEFAULT_ORA_FINE = 16 * 60
    private const val MAX_BLOCCHI_SALVATI = 50

    private val json = Json { ignoreUnknownKeys = true }

    // ═══════════════════════════════════════════════
    // FILTRO ON/OFF
    // ═══════════════════════════════════════════════
    fun filtroAttivoFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[CHIAVE_FILTRO_ATTIVO] ?: false }

    suspend fun setFiltroAttivo(context: Context, attivo: Boolean) {
        context.dataStore.edit { it[CHIAVE_FILTRO_ATTIVO] = attivo }
        ProgrammatoreAllarmi.aggiornaScadenzaFiltro(context)
    }

    // ═══════════════════════════════════════════════
    // PROGRAMMAZIONE ORARIA
    // ═══════════════════════════════════════════════
    fun programmazioneAttivaFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[CHIAVE_PROGRAMMAZIONE_ATTIVA] ?: false }

    suspend fun setProgrammazioneAttiva(context: Context, attiva: Boolean) {
        context.dataStore.edit { it[CHIAVE_PROGRAMMAZIONE_ATTIVA] = attiva }
        ProgrammatoreAllarmi.aggiornaScadenzaFiltro(context)
    }

    fun tuttoIlGiornoFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[CHIAVE_TUTTO_IL_GIORNO] ?: false }

    suspend fun setTuttoIlGiorno(context: Context, attivo: Boolean) {
        context.dataStore.edit { it[CHIAVE_TUTTO_IL_GIORNO] = attivo }
        ProgrammatoreAllarmi.aggiornaScadenzaFiltro(context)
    }

    fun oraInizioFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[CHIAVE_ORA_INIZIO] ?: DEFAULT_ORA_INIZIO }

    suspend fun setOraInizio(context: Context, minuti: Int) {
        context.dataStore.edit { it[CHIAVE_ORA_INIZIO] = minuti }
        ProgrammatoreAllarmi.aggiornaScadenzaFiltro(context)
    }

    fun oraFineFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[CHIAVE_ORA_FINE] ?: DEFAULT_ORA_FINE }

    suspend fun setOraFine(context: Context, minuti: Int) {
        context.dataStore.edit { it[CHIAVE_ORA_FINE] = minuti }
        ProgrammatoreAllarmi.aggiornaScadenzaFiltro(context)
    }

    // ═══════════════════════════════════════════════
    // EFFETTO HOME
    // ═══════════════════════════════════════════════
    fun effettoHomeFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[CHIAVE_EFFETTO_HOME] ?: 0 } // Default ROTAZIONE_COMPLETA

    suspend fun setEffettoHome(context: Context, indice: Int) {
        context.dataStore.edit { it[CHIAVE_EFFETTO_HOME] = indice }
    }

    // ═══════════════════════════════════════════════
    // LASCIAPASSARE
    // ═══════════════════════════════════════════════
    fun lasciapassareFlow(context: Context): Flow<List<VoceLasciapassare>> =
        context.dataStore.data.map { prefs -> leggiLasciapassareDaPrefs(prefs[CHIAVE_LASCIAPASSARE]) }

    suspend fun aggiungiLasciapassare(context: Context, voce: VoceLasciapassare) {
        context.dataStore.edit { prefs ->
            val attuali = leggiLasciapassareDaPrefs(prefs[CHIAVE_LASCIAPASSARE])
            val nuova = attuali + voce
            prefs[CHIAVE_LASCIAPASSARE] = json.encodeToString(nuova)
        }
    }

    suspend fun rimuoviLasciapassare(context: Context, indice: Int) {
        context.dataStore.edit { prefs ->
            val attuali = leggiLasciapassareDaPrefs(prefs[CHIAVE_LASCIAPASSARE]).toMutableList()
            if (indice in attuali.indices) attuali.removeAt(indice)
            prefs[CHIAVE_LASCIAPASSARE] = json.encodeToString(attuali)
        }
    }

    suspend fun aggiornaLasciapassare(context: Context, indice: Int, voce: VoceLasciapassare) {
        context.dataStore.edit { prefs ->
            val attuali = leggiLasciapassareDaPrefs(prefs[CHIAVE_LASCIAPASSARE]).toMutableList()
            if (indice in attuali.indices) attuali[indice] = voce
            prefs[CHIAVE_LASCIAPASSARE] = json.encodeToString(attuali)
        }
    }

    private fun leggiLasciapassareDaPrefs(jsonString: String?): List<VoceLasciapassare> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<VoceLasciapassare>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════
    // REGISTRO BLOCCHI
    // ═══════════════════════════════════════════════
    fun blocchiFlow(context: Context): Flow<List<Blocco>> =
        context.dataStore.data.map { prefs -> leggiBlocchiDaPrefs(prefs[CHIAVE_REGISTRO_BLOCCHI]).sortedByDescending { it.timestamp } }

    suspend fun aggiungiBlocco(context: Context, numero: String) {
        context.dataStore.edit { prefs ->
            val attuali = leggiBlocchiDaPrefs(prefs[CHIAVE_REGISTRO_BLOCCHI])
            val nuovo = Blocco(numero = numero, timestamp = System.currentTimeMillis())
            val aggiornata = (listOf(nuovo) + attuali).take(MAX_BLOCCHI_SALVATI)
            prefs[CHIAVE_REGISTRO_BLOCCHI] = json.encodeToString(aggiornata)
        }
    }

    suspend fun svuotaBlocchi(context: Context) {
        context.dataStore.edit { prefs -> prefs.remove(CHIAVE_REGISTRO_BLOCCHI) }
    }

    suspend fun rimuoviBlocco(context: Context, blocco: Blocco) {
        context.dataStore.edit { prefs ->
            val attuali = leggiBlocchiDaPrefs(prefs[CHIAVE_REGISTRO_BLOCCHI])
            val aggiornata =
                attuali.filterNot { it.numero == blocco.numero && it.timestamp == blocco.timestamp }
            prefs[CHIAVE_REGISTRO_BLOCCHI] = json.encodeToString(aggiornata)
        }
    }

    private fun leggiBlocchiDaPrefs(jsonString: String?): List<Blocco> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Blocco>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════
    // LETTURA SINCRONA
    // ═══════════════════════════════════════════════
    fun leggiStatoPerServizio(context: Context): StatoFiltro {
        return runBlocking {
            val prefs = context.dataStore.data.first()
            StatoFiltro(
                filtroAttivo = prefs[CHIAVE_FILTRO_ATTIVO] ?: false,
                programmazioneAttiva = prefs[CHIAVE_PROGRAMMAZIONE_ATTIVA] ?: false,
                tuttoIlGiorno = prefs[CHIAVE_TUTTO_IL_GIORNO] ?: false,
                oraInizio = prefs[CHIAVE_ORA_INIZIO] ?: DEFAULT_ORA_INIZIO,
                oraFine = prefs[CHIAVE_ORA_FINE] ?: DEFAULT_ORA_FINE,
                lasciapassare = leggiLasciapassareDaPrefs(prefs[CHIAVE_LASCIAPASSARE])
            )
        }
    }
}

data class StatoFiltro(
    val filtroAttivo: Boolean,
    val programmazioneAttiva: Boolean,
    val tuttoIlGiorno: Boolean,
    val oraInizio: Int,
    val oraFine: Int,
    val lasciapassare: List<VoceLasciapassare>
)