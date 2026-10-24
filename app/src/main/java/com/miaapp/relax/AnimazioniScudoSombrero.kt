package com.miaapp.relax

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// ENUM EFFETTI SCUDO + SOMBRERO
// ─────────────────────────────────────────────────────────────
enum class EffettoScudo(val etichetta: String) {
    VOLO_CLASSICO("Volo Classico"),
    CADUTA_GRAVITA("Caduta Gravità"),
    SCATTO_ELASTICO("Scatto Elastico"),
    RIMBALZO_VERTICALE("Rimbalzo Verticale"),
    ZIG_ZAG("Zig Zag")// 👈 nuovo
}

// ─────────────────────────────────────────────────────────────
// COSTANTE PER LA POSIZIONE DI RIPOSO
// ─────────────────────────────────────────────────────────────
/**
 * Valore Y di "riposo" del sombrero: dove sta quando è appoggiato sopra lo scudo.
 *
 * Formula: -(altezza_scudo / 2) - (altezza_sombrero / 2) + sovrapposizione
 * Con scudo 90dp e sombrero 90dp, una buona sovrapposizione è ~25dp:
 *   -45 - 45 + 25 = -65
 *
 * Se vuoi cambiarlo, modifica SOLO questa costante e il valore
 * iniziale in TestDue.kt deve essere identico.
 */
const val Y_RIPOSO_SOMBRERO = -45f

// ─────────────────────────────────────────────────────────────
// COMPOSABLE SCUDO + SOMBRERO
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")
@Composable
fun ScudoSombreroAnimato(
    effetto: EffettoScudo,
    rotazioneAnim: Animatable<Float, AnimationVector1D>,
    offsetXAnim: Animatable<Float, AnimationVector1D>,
    offsetYAnim: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(160.dp, 160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Scudo (fisso al centro)
        Image(
            painter = painterResource(R.drawable.logo_scudo),
            contentDescription = "Scudo",
            modifier = Modifier.size(80.dp, 90.dp)
        )

        // Sombrero (animato)
        Image(
            painter = painterResource(R.drawable.logo_sombrero),
            contentDescription = "Sombrero",
            modifier = Modifier
                .size(70.dp, 90.dp)
                .offset {
                    IntOffset(
                        x = offsetXAnim.value.dp.roundToPx(),
                        y = offsetYAnim.value.dp.roundToPx()
                    )
                }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// MOTORE ANIMAZIONI SCUDO + SOMBRERO
// ─────────────────────────────────────────────────────────────
fun eseguiEffettoScudo(
    effetto: EffettoScudo,
    scope: CoroutineScope,
    rotazioneAnim: Animatable<Float, AnimationVector1D>,
    offsetXAnim: Animatable<Float, AnimationVector1D>,
    offsetYAnim: Animatable<Float, AnimationVector1D>
) {
    scope.launch {
        // Reset di sicurezza: TUTTI gli effetti ripartono da qui
        rotazioneAnim.snapTo(0f)
        offsetXAnim.snapTo(0f)
        offsetYAnim.snapTo(Y_RIPOSO_SOMBRERO) // 👈 Usa la costante

        when (effetto) {
            EffettoScudo.VOLO_CLASSICO -> {
                // Spostamento X, Y e rotazione TUTTI INSIEME in 600ms
                launch { offsetXAnim.animateTo(20f, tween(600)) }
                launch { rotazioneAnim.animateTo(25f, tween(600)) }
                offsetYAnim.animateTo(Y_RIPOSO_SOMBRERO, tween(600)) // 👈 Torna a riposo
            }

            EffettoScudo.CADUTA_GRAVITA -> {
                offsetYAnim.animateTo(Y_RIPOSO_SOMBRERO, keyframes {
                    durationMillis = 1300
                    Y_RIPOSO_SOMBRERO at 0
                    (-60f) at 300      // Sale
                    20f at 600          // Tocca terra
                    (-60f) at 900       // Rimbalza
                    40f at 1200         // Ricade
                    Y_RIPOSO_SOMBRERO at 1300 // 👈 Torna a riposo
                })
                rotazioneAnim.animateTo(0f, keyframes {
                    durationMillis = 1200
                    0f at 0
                    15f at 600
                    (-15f) at 800
                    0f at 1200
                })
            }

            EffettoScudo.SCATTO_ELASTICO -> {
                launch {
                    offsetXAnim.animateTo(20f, keyframes {
                        durationMillis = 600
                        0f at 0
                        20f at 150
                        30f at 300
                        (-40f) at 450
                        0f at 600
                    })
                }
                offsetYAnim.animateTo(Y_RIPOSO_SOMBRERO, tween(600)) // 👈 Torna a riposo
                rotazioneAnim.animateTo(30f, keyframes {
                    durationMillis = 300
                    25f at 150
                    15f at 300
                })
            }

            EffettoScudo.RIMBALZO_VERTICALE -> {
                offsetYAnim.animateTo(Y_RIPOSO_SOMBRERO, keyframes {
                    durationMillis = 1200
                    Y_RIPOSO_SOMBRERO at 0
                    (-80f) at 200      // Salta in alto
                    80f at 400         //scende giù
                    (-60f) at 800      // Salta di nuovo (più basso)
                    40f at 1000        //scende giù di nuovo
                    Y_RIPOSO_SOMBRERO at 1200 // 👈 Torna a riposo
                })
                rotazioneAnim.animateTo(0f, keyframes {
                    durationMillis = 1000
                    0f at 0
                    360f at 500
                    (-180f) at 1000
                    //360f at 1500
                })
            }

            EffettoScudo.ZIG_ZAG -> {
                // Le tre animazioni vanno in parallelo: stessa durata, stessi tempi
                launch {
                    offsetXAnim.animateTo(0f, keyframes {
                        durationMillis = 1200
                        0f at 0
                        20f at 300
                        (-20f) at 600
                        20f at 900
                        0f at 1200
                    })
                }
                launch {
                    rotazioneAnim.animateTo(0f, keyframes {
                        durationMillis = 1200
                        0f at 0
                        25f at 300
                        (-25f) at 600
                        25f at 900
                        0f at 1200
                    })
                }
                offsetYAnim.animateTo(30f, keyframes {
                    durationMillis = 1600
                    Y_RIPOSO_SOMBRERO at 0
                    (-45f) at 400
                    (-25f) at 800
                    (-5f) at 1200
                    30f at 1600
                    //Y_RIPOSO_SOMBRERO at 1700
                })
            }
        }

    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Scudo + Sombrero Animato")
@Composable
fun ScudoSombreroAnimatoPreview() {
    val rotazioneAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(Y_RIPOSO_SOMBRERO) }

    ScudoSombreroAnimato(
        effetto = EffettoScudo.VOLO_CLASSICO,
        rotazioneAnim = rotazioneAnim,
        offsetXAnim = offsetXAnim,
        offsetYAnim = offsetYAnim
    )
}