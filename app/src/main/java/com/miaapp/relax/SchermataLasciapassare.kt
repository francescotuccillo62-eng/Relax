package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miaapp.relax.ui.theme.RelaxTheme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// SCHERMATA LASCIAPASSARE
// ─────────────────────────────────────────────────────────────

@Composable
fun SchermataLasciapassare(
    innerPadding: PaddingValues,
    onTornaIndietro: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lasciapassare by PreferenzeFiltro
        .lasciapassareFlow(context)
        .collectAsState(initial = emptyList())

    var dialogoAperto by remember { mutableStateOf(false) }
    var indiceInModifica by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.giallo_app))
            .padding(innerPadding)
    ) {
        // ═══════════════════════════════════════════════════════
        // BARRA IN ALTO
        // [← Freccia] [Box titolo centrato] [Spacer simmetrico]
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
                    text = "Numeri Autorizzati",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.giallo_app),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Spacer simmetrico a destra (stessa larghezza della freccia)
            Spacer(modifier = Modifier.size(42.dp))
        }

        // ═══════════════════════════════════════════════════════
        // CONTENUTO
        // ═══════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Testo descrittivo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .padding(horizontal = 16.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = Color(0xFFA87D7D),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = "Numeri autorizzati che non sono in rubrica",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(R.color.giallo_app),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista
            if (lasciapassare.isEmpty()) {
                Text(
                    text = "Nessun numero in lista",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            } else {
                lasciapassare.forEachIndexed { indice, voce ->
                    Card(
                        onClick = {
                            indiceInModifica = indice
                            dialogoAperto = true
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
                                    text = voce.nome,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = voce.numero,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        PreferenzeFiltro.rimuoviLasciapassare(context, indice)
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

        // Pulsante "Aggiungi numero"
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                indiceInModifica = null
                dialogoAperto = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF304FFE)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Aggiungi numero",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    // Dialogo Aggiungi/Modifica
    if (dialogoAperto) {
        val voceInModifica = indiceInModifica?.let { lasciapassare.getOrNull(it) }

        DialogoAggiungiNumero(
            nomeIniziale = voceInModifica?.nome ?: "",
            numeroIniziale = voceInModifica?.numero ?: "",
            titolo = if (voceInModifica != null) "Modifica numero" else "Aggiungi numero",
            testoConferma = if (voceInModifica != null) "Salva" else "Aggiungi",
            onConferma = { nome, numero ->
                scope.launch {
                    if (indiceInModifica != null) {
                        PreferenzeFiltro.aggiornaLasciapassare(
                            context,
                            indiceInModifica!!,
                            VoceLasciapassare(nome = nome, numero = numero)
                        )
                    } else {
                        PreferenzeFiltro.aggiungiLasciapassare(
                            context,
                            VoceLasciapassare(nome = nome, numero = numero)
                        )
                    }
                    dialogoAperto = false
                    indiceInModifica = null
                }
            },
            onAnnulla = {
                dialogoAperto = false
                indiceInModifica = null
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// DIALOGO AGGIUNGI / MODIFICA NUMERO
// ─────────────────────────────────────────────────────────────

@Composable
fun DialogoAggiungiNumero(
    nomeIniziale: String = "",
    numeroIniziale: String = "",
    titolo: String = "Aggiungi numero",
    testoConferma: String = "Aggiungi",
    onConferma: (nome: String, numero: String) -> Unit,
    onAnnulla: () -> Unit
) {
    var nome by remember(nomeIniziale) { mutableStateOf(nomeIniziale) }
    var numero by remember(numeroIniziale) { mutableStateOf(numeroIniziale) }

    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text(titolo) },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it },
                    label = { Text("Numero") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nome.isNotBlank() && numero.isNotBlank()) {
                        onConferma(nome.trim(), numero.trim())
                    }
                },
                enabled = nome.isNotBlank() && numero.isNotBlank()
            ) {
                Text(testoConferma)
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) {
                Text("Annulla")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Lasciapassare")
@Composable
fun SchermataLasciapassarePreview() {
    RelaxTheme {
        SchermataLasciapassare(
            innerPadding = PaddingValues(0.dp),
            onTornaIndietro = {}
        )
    }
}

@Preview(showBackground = true, name = "Dialogo Aggiungi Numero")
@Composable
fun DialogoAggiungiNumeroPreview() {
    RelaxTheme {
        DialogoAggiungiNumero(
            nomeIniziale = "Mario Rossi",
            numeroIniziale = "+39 333 1234567",
            onConferma = { _, _ -> },
            onAnnulla = {}
        )
    }
}