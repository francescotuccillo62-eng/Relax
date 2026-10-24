package com.miaapp.relax

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SchermataTestDue() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ─── STATI LOGO ───
    val rotazioneAnim = remember { Animatable(0f) }
    val scalaXAnim = remember { Animatable(1f) }
    val scalaYAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    var effettoSelezionato by remember { mutableStateOf(Effetto.ROTAZIONE_COMPLETA) }
    var menuLogoAperto by remember { mutableStateOf(false) }

    // ─── STATI SCUDO + SOMBRERO ───
    val scudoRotazioneAnim = remember { Animatable(0f) }
    val scudoOffsetXAnim = remember { Animatable(0f) }
    val scudoOffsetYAnim = remember { Animatable(Y_RIPOSO_SOMBRERO) }

    var effettoScudoSelezionato by remember { mutableStateOf(EffettoScudo.VOLO_CLASSICO) }
    var menuScudoAperto by remember { mutableStateOf(false) }

    // ─── FUNZIONI ───
    fun lanciaEffettoLogo() {
        eseguiEffetto(
            effetto = effettoSelezionato,
            scope = scope,
            rotazioneAnim = rotazioneAnim,
            scalaXAnim = scalaXAnim,
            scalaYAnim = scalaYAnim,
            offsetXAnim = offsetXAnim,
            offsetYAnim = offsetYAnim
        )
    }

    fun lanciaEffettoScudo() {
        eseguiEffettoScudo(
            effetto = effettoScudoSelezionato,
            scope = scope,
            rotazioneAnim = scudoRotazioneAnim,
            offsetXAnim = scudoOffsetXAnim,
            offsetYAnim = scudoOffsetYAnim
        )
    }

    LaunchedEffect(Unit) {
        lanciaEffettoLogo()
        lanciaEffettoScudo()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.turchese))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ═══════════════════════════════════════════════
        // SEZIONE 1: LOGO
        // ═══════════════════════════════════════════════
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(42.dp))
            Text(
                "Logo Animato",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box {
                IconButton(
                    onClick = { menuLogoAperto = true },
                    modifier = Modifier
                        .size(42.dp)
                        .background(colorResource(R.color.giallo_app), shape = CircleShape)
                ) {
                    Icon(Icons.Filled.MoreVert, "Menu logo", tint = colorResource(R.color.turchese))
                }
                DropdownMenu(
                    expanded = menuLogoAperto,
                    onDismissRequest = { menuLogoAperto = false }) {
                    Effetto.entries.forEach { effetto ->
                        DropdownMenuItem(
                            text = { Text(if (effetto == effettoSelezionato) "✓  ${effetto.etichetta}" else "     ${effetto.etichetta}") },
                            onClick = {
                                effettoSelezionato = effetto
                                menuLogoAperto = false
                                scope.launch {
                                    PreferenzeFiltro.setEffettoHome(
                                        context,
                                        effetto.ordinal
                                    )
                                }
                                lanciaEffettoLogo()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.giallo_app).copy(
                    alpha = 0.2f
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LogoAnimato(
                    effetto = effettoSelezionato,
                    rotazioneAnim = rotazioneAnim,
                    scalaXAnim = scalaXAnim,
                    scalaYAnim = scalaYAnim,
                    offsetXAnim = offsetXAnim,
                    offsetYAnim = offsetYAnim,
                    modifier = Modifier.size(110.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Effetto: ${effettoSelezionato.etichetta}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.giallo_app)
        )
        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = { lanciaEffettoLogo() },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.giallo_app)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(2.dp, colorResource(R.color.white), RoundedCornerShape(16.dp))
        ) {
            Text("🔁 Riavvia", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(32.dp))

        // ═══════════════════════════════════════════════
        // SEZIONE 2: SCUDO + SOMBRERO
        // ═══════════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(42.dp))
            Text(
                "Scudo + Sombrero",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box {
                IconButton(
                    onClick = { menuScudoAperto = true },
                    modifier = Modifier
                        .size(42.dp)
                        .background(colorResource(R.color.giallo_app), shape = CircleShape)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        "Menu scudo",
                        tint = colorResource(R.color.turchese)
                    )
                }
                DropdownMenu(
                    expanded = menuScudoAperto,
                    onDismissRequest = { menuScudoAperto = false }) {
                    EffettoScudo.entries.forEach { effetto ->
                        DropdownMenuItem(
                            text = { Text(if (effetto == effettoScudoSelezionato) "✓  ${effetto.etichetta}" else "     ${effetto.etichetta}") },
                            onClick = {
                                effettoScudoSelezionato = effetto
                                menuScudoAperto = false
                                lanciaEffettoScudo()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.giallo_app).copy(
                    alpha = 0.2f
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ScudoSombreroAnimato(
                    effetto = effettoScudoSelezionato,
                    rotazioneAnim = scudoRotazioneAnim,
                    offsetXAnim = scudoOffsetXAnim,
                    offsetYAnim = scudoOffsetYAnim
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Effetto: ${effettoScudoSelezionato.etichetta}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.giallo_app)
        )
        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = { lanciaEffettoScudo() },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.giallo_app)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(2.dp, colorResource(R.color.white), RoundedCornerShape(16.dp))
        ) {
            Text("🔁 Riavvia", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true, name = "Test Due — Vetrina Effetti")
@Composable
fun TestDuePreview() {
    SchermataTestDue()
}