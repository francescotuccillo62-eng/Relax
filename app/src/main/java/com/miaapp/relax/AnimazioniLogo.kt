package com.miaapp.relax

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// ENUM EFFETTI
// ─────────────────────────────────────────────────────────────
enum class Effetto(val etichetta: String) {
    ROTAZIONE_COMPLETA("Rotazione completa"),
    OSCILLAZIONE("Oscillazione"),
    GIRO_CON_RIMBALZO("Giro + rimbalzo"),
    DOPPIO_GIRO("Doppio giro"),
    HEARTBEAT("Heartbeat"),
    POP("Pop"),
    SHAKE_E_ROTAZIONE("Shake + rotazione"),
    PENDOLO("Pendolo"),
    FLIP_3D("Flip 3D"),
    BOUNCE_VERTICALE("Bounce verticale"),
    ROTAZIONE_E_SCALA("Rotazione + scala"),
    ELLISSE("Ellisse"),
    SQUASH_STRETCH("Squash & stretch"),
    JELLY("Jelly")
}

// ─────────────────────────────────────────────────────────────
// COMPOSABLE LOGO ANIMATO
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")
@Composable
fun LogoAnimato(
    effetto: Effetto,
    rotazioneAnim: Animatable<Float, *>,
    scalaXAnim: Animatable<Float, *>,
    scalaYAnim: Animatable<Float, *>,
    offsetXAnim: Animatable<Float, *>,
    offsetYAnim: Animatable<Float, *>,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.logo_app),
        contentDescription = "Logo App",
        modifier = modifier
            .size(80.dp) // Dimensione di default della Home
            .graphicsLayer {
                rotationY = if (effetto == Effetto.FLIP_3D) rotazioneAnim.value else 0f
                rotationZ = if (effetto != Effetto.FLIP_3D) rotazioneAnim.value else 0f
                scaleX = scalaXAnim.value
                scaleY = scalaYAnim.value
            }
            .offset {
                IntOffset(
                    x = offsetXAnim.value.dp.roundToPx(),
                    y = offsetYAnim.value.dp.roundToPx()
                )
            }
    )
}

// ─────────────────────────────────────────────────────────────
// MOTORE ANIMAZIONI (DRY)
// ─────────────────────────────────────────────────────────────
fun eseguiEffetto(
    effetto: Effetto,
    scope: CoroutineScope,
    rotazioneAnim: Animatable<Float, *>,
    scalaXAnim: Animatable<Float, *>,
    scalaYAnim: Animatable<Float, *>,
    offsetXAnim: Animatable<Float, *>,
    offsetYAnim: Animatable<Float, *>,
    filtroAttivo: Boolean = true // Usato solo per la direzione della rotazione
) {
    scope.launch {
        // Reset di sicurezza
        rotazioneAnim.snapTo(0f)
        scalaXAnim.snapTo(1f)
        scalaYAnim.snapTo(1f)
        offsetXAnim.snapTo(0f)
        offsetYAnim.snapTo(0f)

        when (effetto) {
            Effetto.ROTAZIONE_COMPLETA -> {
                val dir = if (filtroAttivo) 360f else -360f
                rotazioneAnim.animateTo(dir, tween(900, easing = FastOutSlowInEasing))
                rotazioneAnim.snapTo(0f)
            }

            Effetto.OSCILLAZIONE -> {
                rotazioneAnim.animateTo(0f, keyframes {
                    durationMillis =
                        800; 0f at 0; 30f at 150; (-30f) at 300; 20f at 450; (-20f) at 600; 10f at 700; 0f at 800
                })
            }

            Effetto.GIRO_CON_RIMBALZO -> {
                rotazioneAnim.animateTo(360f, keyframes {
                    durationMillis = 900; 0f at 0; (-15f) at 80; 360f at 900
                }); rotazioneAnim.snapTo(0f)
            }

            Effetto.DOPPIO_GIRO -> {
                rotazioneAnim.animateTo(
                    720f,
                    tween(1200, easing = FastOutSlowInEasing)
                ); rotazioneAnim.snapTo(0f)
            }

            Effetto.HEARTBEAT -> {
                launch {
                    scalaXAnim.animateTo(1f, keyframes {
                        durationMillis =
                            1000; 1f at 0; 1.2f at 120; 1f at 240; 1.15f at 360; 1f at 500; 1f at 1000
                    })
                }
                scalaYAnim.animateTo(1f, keyframes {
                    durationMillis =
                        1000; 1f at 0; 1.2f at 120; 1f at 240; 1.15f at 360; 1f at 500; 1f at 1000
                })
            }

            Effetto.POP -> {
                launch {
                    scalaXAnim.animateTo(1f, keyframes {
                        durationMillis = 500; 1f at 0; 1.25f at 150; 0.95f at 300; 1f at 500
                    })
                }
                scalaYAnim.animateTo(1f, keyframes {
                    durationMillis = 500; 1f at 0; 1.25f at 150; 0.95f at 300; 1f at 500
                })
            }

            Effetto.SHAKE_E_ROTAZIONE -> {
                launch {
                    offsetXAnim.animateTo(0f, keyframes {
                        durationMillis =
                            600; 0f at 0; 10f at 60; (-10f) at 120; 8f at 200; (-8f) at 280; 4f at 400; (-4f) at 500; 0f at 600
                    })
                }
                launch {
                    rotazioneAnim.animateTo(0f, keyframes {
                        durationMillis =
                            600; 0f at 0; 15f at 100; (-15f) at 250; 8f at 400; (-8f) at 500; 0f at 600
                    })
                }
            }

            Effetto.PENDOLO -> {
                rotazioneAnim.animateTo(0f, keyframes {
                    durationMillis =
                        1200; 0f at 0; 25f at 200; (-25f) at 500; 15f at 800; (-10f) at 1000; 0f at 1200
                })
            }

            Effetto.FLIP_3D -> {
                rotazioneAnim.animateTo(
                    360f,
                    tween(800, easing = FastOutSlowInEasing)
                ); rotazioneAnim.snapTo(0f)
            }

            Effetto.BOUNCE_VERTICALE -> {
                offsetYAnim.animateTo(0f, keyframes {
                    durationMillis =
                        900; 0f at 0; (-30f) at 200; 0f at 400; (-15f) at 550; 0f at 700; (-5f) at 800; 0f at 900
                })
            }

            Effetto.ROTAZIONE_E_SCALA -> {
                launch {
                    rotazioneAnim.animateTo(
                        360f,
                        tween(900, easing = FastOutSlowInEasing)
                    ); rotazioneAnim.snapTo(0f)
                }
                launch {
                    scalaXAnim.animateTo(1f, keyframes {
                        durationMillis = 900; 1f at 0; 1.4f at 300; 1.2f at 600; 1f at 900
                    })
                }
                scalaYAnim.animateTo(1f, keyframes {
                    durationMillis = 900; 1f at 0; 1.4f at 300; 1.2f at 600; 1f at 900
                })
            }

            Effetto.ELLISSE -> {
                launch {
                    offsetXAnim.animateTo(0f, keyframes {
                        durationMillis =
                            1200; 0f at 0; 25f at 300; 0f at 600; (-25f) at 900; 0f at 1200
                    })
                }
                offsetYAnim.animateTo(0f, keyframes {
                    durationMillis =
                        1200; (-25f) at 0; 0f at 300; 25f at 600; 0f at 900; (-25f) at 1200
                })
            }

            Effetto.SQUASH_STRETCH -> {
                launch {
                    scalaXAnim.animateTo(1f, keyframes {
                        durationMillis =
                            800; 1f at 0; 1.3f at 120; 0.85f at 280; 1.1f at 450; 0.95f at 600; 1f at 800
                    })
                }
                scalaYAnim.animateTo(1f, keyframes {
                    durationMillis =
                        800; 1f at 0; 0.75f at 120; 1.2f at 280; 0.9f at 450; 1.05f at 600; 1f at 800
                })
            }

            Effetto.JELLY -> {
                launch {
                    rotazioneAnim.animateTo(0f, keyframes {
                        durationMillis =
                            1000; 0f at 0; 15f at 100; (-12f) at 200; 10f at 320; (-8f) at 440; 6f at 560; (-4f) at 680; 2f at 800; 0f at 1000
                    })
                }
                launch {
                    scalaXAnim.animateTo(1f, keyframes {
                        durationMillis =
                            1000; 1f at 0; 1.15f at 150; 0.9f at 300; 1.08f at 450; 0.95f at 600; 1.03f at 750; 1f at 1000
                    })
                }
                scalaYAnim.animateTo(1f, keyframes {
                    durationMillis =
                        1000; 1f at 0; 0.88f at 150; 1.12f at 300; 0.93f at 450; 1.05f at 600; 0.98f at 750; 1f at 1000
                })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Logo Animato")
@Composable
fun LogoAnimatoPreview() {
    val rotazioneAnim = remember { Animatable(0f) }
    val scalaXAnim = remember { Animatable(1f) }
    val scalaYAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    LogoAnimato(
        effetto = Effetto.ROTAZIONE_COMPLETA,
        rotazioneAnim = rotazioneAnim,
        scalaXAnim = scalaXAnim,
        scalaYAnim = scalaYAnim,
        offsetXAnim = offsetXAnim,
        offsetYAnim = offsetYAnim
    )
}