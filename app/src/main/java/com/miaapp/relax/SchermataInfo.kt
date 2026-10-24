package com.miaapp.relax

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miaapp.relax.ui.theme.RelaxTheme

@Composable
fun SchermataInfo(
    innerPadding: PaddingValues,
    onTornaIndietro: () -> Unit
) {
    val context = LocalContext.current
    val nomeApp = stringResource(R.string.app_name)
    val titoloInfo = stringResource(R.string.info_titolo)

    // Legge lo stato del filtro (per cambiare il logo)
    val filtroAttivo by PreferenzeFiltro
        .filtroAttivoFlow(context)
        .collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.giallo_app))
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ───── LOGO: Cambia in base al filtro (senza animazione) ─────
            Image(
                painter = painterResource(R.drawable.logo_app),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onTornaIndietro,
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorResource(R.color.turchese), shape = CircleShape)
                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        titoloInfo,
                        tint = colorResource(R.color.giallo_app),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = nomeApp,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorResource(R.color.turchese),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardInfo(
                titolo = stringResource(R.string.info_card_istruzioni_titolo),
                coloreSfondo = colorResource(R.color.card_verde)
            ) { TestoIstruzioni() }
            Spacer(modifier = Modifier.height(16.dp))
            CardInfo(
                titolo = stringResource(R.string.info_card_ringraziamenti_titolo),
                coloreSfondo = colorResource(R.color.card_lilla)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.info_ringraziamenti))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(nomeApp) }
                        append(stringResource(R.string.info_ringraziamenti_1))
                        append(stringResource(R.string.info_ringraziamenti_2))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(nomeApp) }
                        append(stringResource(R.string.info_ringraziamenti_3))
                    },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            CardInfo(
                titolo = stringResource(R.string.info_card_app_titolo),
                coloreSfondo = colorResource(R.color.card_giallo)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.info_nome_p1))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(nomeApp) }
                        append(stringResource(R.string.info_nome_p3))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.testo_scuro)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.info_versione),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.testo_scuro)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.info_autore),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.testo_scuro)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.info_anno),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.testo_scuro)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.info_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.testo_grigio)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.powered_by),
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.turchese),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CardInfo(titolo: String, coloreSfondo: Color, contenuto: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = coloreSfondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                titolo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.testo_titolo_card),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            contenuto()
        }
    }
}

@Composable
private fun TestoIstruzioni() {
    val stile = MaterialTheme.typography.bodyMedium
    val colore = colorResource(R.color.testo_scuro)
    val nomeApp = stringResource(R.string.app_name)
    Column {
        Text(
            stringResource(R.string.istr_7_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(
                    nomeApp
                )
            }; append(stringResource(R.string.istr_7_testo_1))
        }, style = stile, color = colore)
        Text(
            stringResource(R.string.istr_1_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(
                    nomeApp
                )
            }; append(stringResource(R.string.istr_1_testo))
        }, style = stile, color = colore)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.istr_2_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(text = buildAnnotatedString {
            append(stringResource(R.string.istr_2_p1)); withStyle(
            SpanStyle(fontWeight = FontWeight.Bold)
        ) { append(nomeApp) }; append(stringResource(R.string.istr_2_p3))
        }, style = stile, color = colore)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.istr_3_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(text = buildAnnotatedString {
            append(stringResource(R.string.istr_3_testo)); withStyle(
            SpanStyle(fontWeight = FontWeight.Bold)
        ) { append(nomeApp) }; append(stringResource(R.string.istr_3_testo_1))
        }, style = stile, color = colore)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.istr_4_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(stringResource(R.string.istr_4_testo), style = stile, color = colore)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.istr_5_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(stringResource(R.string.istr_5_testo), style = stile, color = colore)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.istr_6_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.testo_titolo_card)
        )
        Text(stringResource(R.string.istr_6_testo), style = stile, color = colore)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview(showBackground = true, name = "Info")
@Composable
fun SchermataInfoPreview() {
    RelaxTheme { SchermataInfo(innerPadding = PaddingValues(0.dp), onTornaIndietro = {}) }
}