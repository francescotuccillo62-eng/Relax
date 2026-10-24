package com.miaapp.relax

// ─────────────────────────────────────────────────────────────
// IMPORT
// ─────────────────────────────────────────────────────────────

// Layout di base
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

// Icone Material
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// Componenti Material3
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

// Runtime Compose
import androidx.compose.runtime.Composable

// Utility UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// ─────────────────────────────────────────────────────────────
// SCHERMATA INFO
// ─────────────────────────────────────────────────────────────

/**
 * Schermata "Info":
 *  - Card 1: Istruzioni per l'uso
 *  - Card 2: Ringraziamenti
 *  - Card 3: Info sull'app (nome, versione, autore, privacy)
 *
 * Tutti i testi sono in strings.xml.
 * I testi che citano il nome dell'app (info_nome, istr_2) usano
 * buildAnnotatedString per applicare il grassetto al nome app.
 */
@Composable
fun SchermataInfo(
    innerPadding: PaddingValues,
    onTornaIndietro: () -> Unit
) {
    val nomeApp = stringResource(R.string.app_name)
    val titoloInfo = stringResource(R.string.info_titolo)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo in alto
                Image(
                    painter = painterResource(R.drawable.logo_app),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sotto: nome_app e pulsante indietro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.size(48.dp))
                    Text(
                        text = nomeApp,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF00BFA5),
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = Color(0xFF19A7A7),
                                shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ){
                    IconButton(onClick = onTornaIndietro) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = titoloInfo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    }
                }
            }
        },
        modifier = Modifier.padding(innerPadding)
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ───── CARD 1: ISTRUZIONI ─────
            CardInfo(
                titolo = stringResource(R.string.info_card_istruzioni_titolo),
                coloreSfondo = Color(0xFFB8E0D8)
            ) {
                TestoIstruzioni()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── CARD 2: RINGRAZIAMENTI ─────
            CardInfo(
                titolo = stringResource(R.string.info_card_ringraziamenti_titolo),
                coloreSfondo = Color(0xFFE6D8F0)
            ) {
                Text(
                    text = stringResource(R.string.info_ringraziamenti),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1F1F1F)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── CARD 3: INFO SULL'APP ─────
            CardInfo(
                titolo = stringResource(R.string.info_card_app_titolo),
                coloreSfondo = Color(0xFFFFF3C4)
            ) {
                // Nome app: composto in 3 parti con nomeApp in grassetto
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.info_nome_p1))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(nomeApp)
                        }
                        append(stringResource(R.string.info_nome_p3))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.info_versione),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.info_autore),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.info_anno),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.info_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF555555)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.powered_by),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF33988A),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * Card riutilizzabile con titolo + contenuto.
 */
@Composable
private fun CardInfo(
    titolo: String,
    coloreSfondo: Color,
    contenuto: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = coloreSfondo
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = titolo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1F4E45),
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            contenuto()
        }
    }
}

/**
 * Contenuto della Card "Istruzioni".
 * L'istruzione 2 usa buildAnnotatedString per il nome app in grassetto.
 */
@Composable
private fun TestoIstruzioni() {
    val stile = MaterialTheme.typography.bodyMedium
    val colore = Color(0xFF1F1F1F)
    val nomeApp = stringResource(R.string.app_name)

    Column {
        // Istruzione 1
        Text(
            text = stringResource(R.string.istr_1_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = buildAnnotatedString {
                //append(stringResource(R.string.istr_1_testo_1))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(nomeApp)
                }
                append(stringResource(R.string.istr_1_testo))
            },
            style = stile, color = colore
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Permessi (nome app in grassetto)
        Text(
            text = stringResource(R.string.istr_2_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.istr_2_p1))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(nomeApp)
                }
                append(stringResource(R.string.istr_2_p3))
            },
            style = stile, color = colore
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Uso 3
        Text(
            text = stringResource(R.string.istr_3_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.istr_3_testo))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(nomeApp)
                }
                append(stringResource(R.string.istr_3_testo_1))
            },
            style = stile, color = colore
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Istruzione 4
        Text(
            text = stringResource(R.string.istr_4_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = stringResource(R.string.istr_4_testo),
            style = stile, color = colore
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Istruzione 5
        Text(
            text = stringResource(R.string.istr_5_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = stringResource(R.string.istr_5_testo),
            style = stile, color = colore
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Istruzione 6
        Text(
            text = stringResource(R.string.istr_6_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = stringResource(R.string.istr_6_testo),
            style = stile, color = colore
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Istruzione 7 (Privacy)
        Text(
            text = stringResource(R.string.istr_7_titolo),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E45)
        )
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.istr_7_testo))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(nomeApp)
                }
                append(stringResource(R.string.istr_7_testo_1))
            },
            style = stile, color = colore
        )
    }
}


// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Info")
@Composable
fun SchermataInfoPreview() {
    RelaxTheme {
        SchermataInfo(
            innerPadding = PaddingValues(0.dp),
            onTornaIndietro = {}
        )
    }
}

@Preview(showBackground = true, name = "Card Info")
@Composable
fun CardInfoPreview() {
    RelaxTheme {
        CardInfo(
            titolo = "📖  Esempio di Card",
            coloreSfondo = Color(0xFFB8E0D8)
        ) {
            Text(
                text = "Questo è il contenuto di esempio della Card.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1F1F1F)
            )
        }
    }
}