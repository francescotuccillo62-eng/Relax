package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaapp.relax.ui.theme.RelaxTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// SCHERMATA REGISTRO BLOCCHI
// ─────────────────────────────────────────────────────────────

@Composable
fun SchermataRegistro(
    innerPadding: PaddingValues,
    onTornaIndietro: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val blocchi by PreferenzeFiltro
        .blocchiFlow(context)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var mostraConfermaSvuota by remember { mutableStateOf(false) }
    var numeroDaChiamare by remember { mutableStateOf<String?>(null) }
    var numeroInAttesaDiPermesso by remember { mutableStateOf<String?>(null) }

    val formatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN)
    }

    fun avviaChiamata(numero: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(numero)}")
            }
            context.startActivity(intent)
        } catch (e: SecurityException) {
            Log.e("SchermataRegistro", "Permesso CALL_PHONE mancante", e)
        } catch (e: Exception) {
            Log.e("SchermataRegistro", "Errore avvio chiamata", e)
        }
    }

    val launcherPermessoChiamata = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concesso ->
        if (concesso) {
            numeroInAttesaDiPermesso?.let { numero ->
                avviaChiamata(numero)
            }
        }
        numeroInAttesaDiPermesso = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.giallo_app))
            .padding(innerPadding)
    ) {
        // ═══════════════════════════════════════════════════════
        // BARRA IN ALTO
        // [← Freccia] [Box titolo centrato] [Cestino o Spacer]
        // ═══════════════════════════════════════════════════════
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp)
        ) {
            // 👈 Freccia indietro a SINISTRA
            IconButton(
                onClick = onTornaIndietro,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = Color(0xFF19A7A7),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = colorResource(R.color.giallo_app),
                    modifier = Modifier.size(30.dp)
                )
            }

            // Box titolo al centro
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .padding(horizontal = 12.dp)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        color = Color(0xFF19A7A7),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text(
                    text = "Registro blocchi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.giallo_app),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Cestino a destra (se ci sono blocchi), altrimenti spacer simmetrico
            if (blocchi.isNotEmpty()) {
                IconButton(
                    onClick = { mostraConfermaSvuota = true },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = Color(0xFF19A7A7),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Svuota registro",
                        tint = colorResource(R.color.giallo_app),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(42.dp))
            }
        }

        // ═══════════════════════════════════════════════════════
        // CONTENUTO
        // ═══════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

    // Dialog: conferma svuota
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

    // Dialog: conferma chiamata
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