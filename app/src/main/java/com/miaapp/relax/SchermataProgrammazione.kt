package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miaapp.relax.ui.theme.RelaxTheme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// SCHERMATA PROGRAMMAZIONE ORARIA
// ─────────────────────────────────────────────────────────────

@Composable
fun SchermataProgrammazione(
    innerPadding: PaddingValues,
    onTornaIndietro: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val programmazioneAttiva by PreferenzeFiltro
        .programmazioneAttivaFlow(context)
        .collectAsState(initial = false)

    val tuttoIlGiorno by PreferenzeFiltro
        .tuttoIlGiornoFlow(context)
        .collectAsState(initial = false)

    val oraInizio by PreferenzeFiltro
        .oraInizioFlow(context)
        .collectAsState(initial = 14 * 60)

    val oraFine by PreferenzeFiltro
        .oraFineFlow(context)
        .collectAsState(initial = 16 * 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.giallo_app))
            .padding(innerPadding)
    ) {
        // ═══════════════════════════════════════════════════════
        // BARRA IN ALTO
        // ═══════════════════════════════════════════════════════
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp)
        ) {
            IconButton(
                onClick = onTornaIndietro,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = colorResource(R.color.turchese),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.white),
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

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .padding(horizontal = 12.dp)
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.white),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        color = colorResource(R.color.turchese),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text(
                    text = "Programmazione",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.giallo_app),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Limita l'azione del filtro a una fascia oraria specifica.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.testo_grigio),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Card: Programmazione attiva
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.card_verde)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Attiva programmazione",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.testo_scuro)
                        )
                        Text(
                            text = "Se attiva, il filtro agisce il giorno o la fascia oraria scelta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(R.color.testo_scuro)
                        )
                    }
                    Switch(
                        checked = programmazioneAttiva,
                        onCheckedChange = { nuovoValore ->
                            scope.launch {
                                PreferenzeFiltro.setProgrammazioneAttiva(context, nuovoValore)
                            }
                        }
                    )
                }
            }

            if (programmazioneAttiva) {
                Spacer(modifier = Modifier.height(16.dp))

                // Card: Tutto il giorno
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.card_lilla)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Giorno corrente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.testo_scuro)
                            )
                            val dataOggi = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ITALIAN).format(java.util.Date())
                            Text(
                                text = "Se attivo, il filtro agisce solo nel giorno corrente ($dataOggi).",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorResource(R.color.testo_scuro)
                            )
                        }
                        Switch(
                            checked = tuttoIlGiorno,
                            onCheckedChange = { nuovoValore ->
                                scope.launch {
                                    PreferenzeFiltro.setTuttoIlGiorno(context, nuovoValore)
                                }
                            }
                        )
                    }
                }

                if (!tuttoIlGiorno) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.card_giallo)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Scegli la fascia oraria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.testo_scuro),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OraPicker(
                                    etichetta = "Dalle",
                                    minutiCorrenti = oraInizio,
                                    onOraSelezionata = { nuoviMinuti ->
                                        scope.launch {
                                            PreferenzeFiltro.setOraInizio(context, nuoviMinuti)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                OraPicker(
                                    etichetta = "Alle",
                                    minutiCorrenti = oraFine,
                                    onOraSelezionata = { nuoviMinuti ->
                                        scope.launch {
                                            PreferenzeFiltro.setOraFine(context, nuoviMinuti)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Il filtro agirà solo tra le ${formatOra(oraInizio)} e le ${
                                    formatOra(
                                        oraFine
                                    )
                                }.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorResource(R.color.testo_scuro),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ORA PICKER
// ─────────────────────────────────────────────────────────────

@Composable
fun OraPicker(
    etichetta: String,
    minutiCorrenti: Int,
    onOraSelezionata: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val hh = minutiCorrenti / 60
            val mm = minutiCorrenti % 60

            TimePickerDialog(
                context,
                { _, oraScelta, minutoScelto ->
                    onOraSelezionata(oraScelta * 60 + minutoScelto)
                },
                hh,
                mm,
                true
            ).show()
        },
        modifier = modifier
    ) {
        Text(
            text = "$etichetta: ${formatOra(minutiCorrenti)}",
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Programmazione")
@Composable
fun SchermataProgrammazionePreview() {
    RelaxTheme {
        SchermataProgrammazione(
            innerPadding = PaddingValues(0.dp),
            onTornaIndietro = {}
        )
    }
}