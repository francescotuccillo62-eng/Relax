package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

// Android di base
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

// Activity Result (per launcher di permesso)
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// Layout di base
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Icone Material
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep

// Componenti Material3
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

// Runtime Compose
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

// Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Utility UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.miaapp.relax.ui.theme.RelaxTheme

// Utility di sistema
import androidx.core.content.ContextCompat

// Coroutine
import kotlinx.coroutines.launch

// Formattazione data/ora
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// SCHERMATA REGISTRO BLOCCHI
// ─────────────────────────────────────────────────────────────

/**
 * Schermata "Registro blocchi":
 *  - Mostra la lista delle chiamate bloccate (numero + data/ora)
 *  - Ogni voce: cestino per la cancellazione singola
 *  - Click sulla voce: dialog per chiamare il numero
 *  - Icona cestino in alto: svuota tutto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataRegistro(
    innerPadding: PaddingValues,
    onTornaIndietro: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Lista reattiva dei blocchi
    val blocchi by PreferenzeFiltro
        .blocchiFlow(context)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Stati dei dialog
    var mostraConfermaSvuota by remember { mutableStateOf(false) }
    var numeroDaChiamare by remember { mutableStateOf<String?>(null) }
    var numeroInAttesaDiPermesso by remember { mutableStateOf<String?>(null) }

    // Formattatore data/ora
    val formatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN)
    }

    // ───── Funzione per avviare la chiamata ─────
    fun avviaChiamata(numero: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(numero)}")
            }
            context.startActivity(intent)
        } catch (e: SecurityException) {
            // Permesso mancante: non dovrebbe succedere, ma gestiamo
            Log.e("SchermataRegistro", "Permesso CALL_PHONE mancante", e)
        } catch (e: Exception) {
            // Altri errori (app telefono mancante, ecc.)
            Log.e("SchermataRegistro", "Errore avvio chiamata", e)
        }
    }

    // ───── Launcher per il permesso CALL_PHONE ─────
    val launcherPermessoChiamata = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concesso ->
        // Se concesso, procediamo con la chiamata che era in attesa
        if (concesso) {
            numeroInAttesaDiPermesso?.let { numero ->
                avviaChiamata(numero)
            }
        }
        numeroInAttesaDiPermesso = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro blocchi") },
                navigationIcon = {
                    IconButton(onClick = onTornaIndietro) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    if (blocchi.isNotEmpty()) {
                        IconButton(onClick = { mostraConfermaSvuota = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Svuota registro",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        modifier = Modifier.padding(innerPadding)
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contatore in alto
            if (blocchi.isNotEmpty()) {
                Text(
                    text = if (blocchi.size == 1)
                        "1 chiamata bloccata"
                    else
                        "${blocchi.size} chiamate bloccate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Suggerimento
                Text(
                    text = "Tocca una voce per richiamare il numero",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Lista o messaggio "vuoto"
            if (blocchi.isEmpty()) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Nessuna chiamata bloccata",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Le chiamate bloccate appariranno qui.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                blocchi.forEach { blocco ->
                    Card(
                        // Click sulla Card → apre il dialog di conferma chiamata
                        onClick = {
                            if (blocco.numero.isNotBlank()) {
                                numeroDaChiamare = blocco.numero
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Colonna con numero + data/ora
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = blocco.numero.ifBlank { "Numero sconosciuto" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatter.format(Date(blocco.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            // Cestino per cancellare SOLO questa voce
                            // (l'IconButton intercetta il click, quindi NON apre il dialog)
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        PreferenzeFiltro.rimuoviBlocco(context, blocco)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Rimuovi",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ───── Dialog: conferma svuota registro ─────
    if (mostraConfermaSvuota) {
        AlertDialog(
            onDismissRequest = { mostraConfermaSvuota = false },
            title = { Text("Svuotare il registro?") },
            text = {
                Text("Tutte le chiamate bloccate verranno eliminate. L'operazione non può essere annullata.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            PreferenzeFiltro.svuotaBlocchi(context)
                        }
                        mostraConfermaSvuota = false
                    }
                ) {
                    Text("Svuota", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraConfermaSvuota = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // ───── Dialog: conferma chiamata ─────
    numeroDaChiamare?.let { numero ->
        AlertDialog(
            onDismissRequest = { numeroDaChiamare = null },
            title = { Text("Chiamare questo numero?") },
            text = { Text(numero) },
            confirmButton = {
                TextButton(
                    onClick = {
                        numeroDaChiamare = null

                        val permessoConcesso = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED

                        if (permessoConcesso) {
                            avviaChiamata(numero)
                        } else {
                            numeroInAttesaDiPermesso = numero
                            launcherPermessoChiamata.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                ) {
                    Text("Chiama")
                }
            },
            dismissButton = {
                TextButton(onClick = { numeroDaChiamare = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}
// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Registro")
@Composable
fun SchermataRegistroPreview() {
    RelaxTheme {
        SchermataRegistro(
            innerPadding = PaddingValues(0.dp),
            onTornaIndietro = {}
        )
    }
}