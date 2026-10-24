package com.miaapp.relax

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.miaapp.relax.ui.theme.RelaxTheme
import kotlinx.coroutines.launch

@Composable
fun SchermataHome(
    onVaiALasciapassare: () -> Unit,
    onVaiAlRegistro: () -> Unit,
    onVaiAInfo: () -> Unit,
    onVaiAProgrammazione: () -> Unit,
   // onVaiATestAnimazione: () -> Unit,
    onVaiATestDue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var permessoConcesso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var richiestaFatta by remember { mutableStateOf(false) }
    val launcherNotifiche =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val launcherPermesso =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concesso ->
            permessoConcesso = concesso
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permessoNotifiche = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!permessoNotifiche) launcherNotifiche.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    val launcherImpostazioni =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    LaunchedEffect(Unit) {
        if (!permessoConcesso && !richiestaFatta) {
            richiestaFatta = true
            launcherPermesso.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val sonoAppScreening = ricordaSeSonoAppScreening(context)
    val launcherScreening =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permessoConcesso = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filtroAttivo by PreferenzeFiltro.filtroAttivoFlow(context).collectAsState(initial = false)
    val programmazioneAttiva by PreferenzeFiltro.programmazioneAttivaFlow(context)
        .collectAsState(initial = false)
    val tuttoIlGiorno by PreferenzeFiltro.tuttoIlGiornoFlow(context).collectAsState(initial = false)
    val oraInizio by PreferenzeFiltro.oraInizioFlow(context).collectAsState(initial = 14 * 60)
    val oraFine by PreferenzeFiltro.oraFineFlow(context).collectAsState(initial = 16 * 60)
    val lasciapassare by PreferenzeFiltro.lasciapassareFlow(context)
        .collectAsState(initial = emptyList())
    val blocchi by PreferenzeFiltro.blocchiFlow(context).collectAsState(initial = emptyList())

    val effettoIndice by PreferenzeFiltro.effettoHomeFlow(context).collectAsState(initial = 0)
    val effettoHome = Effetto.entries.getOrElse(effettoIndice) { Effetto.ROTAZIONE_COMPLETA }

    val puoAttivareFiltro = sonoAppScreening && permessoConcesso
    LaunchedEffect(puoAttivareFiltro, filtroAttivo) {
        if (!puoAttivareFiltro && filtroAttivo) {
            PreferenzeFiltro.setFiltroAttivo(context, false)
            PreferenzeFiltro.setProgrammazioneAttiva(context, false)
        }
    }

    LaunchedEffect(filtroAttivo) {
        if (filtroAttivo) FiltroForegroundService.avvia(context) else FiltroForegroundService.ferma(
            context
        )
    }

    // ─── ANIMAZIONE DINAMICA ───
    val rotazioneAnim = remember { Animatable(0f) }
    val scalaXAnim = remember { Animatable(1f) }
    val scalaYAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }
    var primoAvvio by remember { mutableStateOf(true) }

    LaunchedEffect(filtroAttivo) {
        if (primoAvvio) {
            primoAvvio = false
            return@LaunchedEffect
        }
        // Chiama il motore unico!
        eseguiEffetto(
            effetto = effettoHome,
            scope = scope,
            rotazioneAnim = rotazioneAnim,
            scalaXAnim = scalaXAnim,
            scalaYAnim = scalaYAnim,
            offsetXAnim = offsetXAnim,
            offsetYAnim = offsetYAnim,
            filtroAttivo = filtroAttivo
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.giallo_app))
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.size(42.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    LogoAnimato(
                        effetto = effettoHome,
                        rotazioneAnim = rotazioneAnim,
                        scalaXAnim = scalaXAnim,
                        scalaYAnim = scalaYAnim,
                        offsetXAnim = offsetXAnim,
                        offsetYAnim = offsetYAnim,
                        modifier = Modifier.size(80.dp)
                    )
                }

                IconButton(
                    onClick = onVaiAInfo,
                    modifier = Modifier
                        .size(42.dp)
                        .padding(4.dp)
                        .background(colorResource(R.color.turchese), shape = CircleShape)
                        .border(2.dp, colorResource(R.color.white), CircleShape)
                ) {
                    Icon(Icons.Filled.Info, "Info", tint = colorResource(R.color.white))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.giallo_app),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .background(colorResource(R.color.turchese), shape = RoundedCornerShape(16.dp))
                    .border(2.dp, colorResource(R.color.white), RoundedCornerShape(16.dp))
            )
            Text(
                text = stringResource(R.string.info_name),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.testo_titolo_card),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (!sonoAppScreening) {
                BannerAvvisoScreening(onAttiva = {
                    val roleManager = try {
                        context.getSystemService(RoleManager::class.java)
                    } catch (_: Throwable) {
                        null
                    }
                    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                        launcherScreening.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!permessoConcesso) {
                BannerAvvisoPermesso(onRichiedi = {
                    val activity = context as? Activity
                    val puoChiedereAncora =
                        activity?.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                            ?: false
                    when {
                        puoChiedereAncora || !richiestaFatta -> {
                            richiestaFatta =
                                true; launcherPermesso.launch(Manifest.permission.READ_CONTACTS)
                        }

                        else -> launcherImpostazioni.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Switch(
                checked = filtroAttivo,
                enabled = puoAttivareFiltro,
                onCheckedChange = { nuovoValore ->
                    scope.launch {
                        PreferenzeFiltro.setFiltroAttivo(context, nuovoValore)
                        if (!nuovoValore) PreferenzeFiltro.setProgrammazioneAttiva(context, false)
                    }
                }
            )
            Text(
                text = if (filtroAttivo) "✅ Filtro ATTIVO" else "⭕ Filtro disattivato",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.turchese),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            if (filtroAttivo) {
                Spacer(modifier = Modifier.height(12.dp))
                val dataOggi = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ITALIAN).format(java.util.Date())
                val testoAvviso = when {
                    !programmazioneAttiva -> "♾️ Tutti i giorni"
                    tuttoIlGiorno -> "⏱️ Giorno corrente: $dataOggi"
                    else -> "⏱️ Fascia oraria: ${formatOra(oraInizio)} - ${formatOra(oraFine)}"
                }
                Text(
                    text = testoAvviso,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.testo_titolo_card),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorResource(R.color.card_verde),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onVaiAProgrammazione,
                enabled = filtroAttivo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        !filtroAttivo -> "⏱️ Attiva il filtro"; programmazioneAttiva -> "⏱️ Programmazione attivata"; else -> "⏱️ Programmazione oraria"
                    },
                    textAlign = TextAlign.Center,
                    color = when {
                        !filtroAttivo -> colorResource(R.color.testo_grigio); programmazioneAttiva -> colorResource(
                            R.color.turchese
                        ); else -> colorResource(R.color.testo_scuro)
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Vai")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onVaiALasciapassare, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Numeri autorizzati (${lasciapassare.size})", textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                ); Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Vai")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onVaiAlRegistro, modifier = Modifier.fillMaxWidth()) {
                Text("Chiamate bloccate (${blocchi.size})", textAlign = TextAlign.Center, modifier = Modifier.weight(1f)); Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                "Vai"
            )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row {

                OutlinedButton(
                    onClick = onVaiATestDue,
                    modifier = Modifier
                        .weight(1f)
                        .visible(visible = true),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF673AB7))
                ) {
                    Text(
                        text = "🧪 Test Animazione Logo",
                       textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    ); Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Vai")
                }
            }
        }

        Text(
            text = stringResource(R.string.powered_by),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.turchese),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun ricordaSeSonoAppScreening(context: Context): Boolean {
    var sonoScreening by remember { mutableStateOf(calcolaRuoloScreening(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) sonoScreening = calcolaRuoloScreening(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return sonoScreening
}

private fun calcolaRuoloScreening(context: Context): Boolean {
    return try {
        context.getSystemService(RoleManager::class.java)
            ?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
    } catch (_: Throwable) {
        false
    }
}

@Composable
private fun BannerAvvisoScreening(onAttiva: () -> Unit) {
    val nomeApp = stringResource(R.string.app_name)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colorResource(R.color.banner_screening_bg),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.screening_a),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.banner_screening_testo),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.screening_b)); withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.giallo_app)
                )
            ) { append(nomeApp) }; append(stringResource(R.string.screening_b1))
            },
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.banner_screening_testo)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onAttiva,
            modifier = Modifier
                .height(42.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.banner_pulsante_bg),
                contentColor = colorResource(R.color.white)
            ),
            border = BorderStroke(2.dp, colorResource(R.color.giallo_app)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Attiva ora",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.giallo_app)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                Icons.Filled.Settings,
                null,
                tint = colorResource(R.color.giallo_app),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BannerAvvisoPermesso(onRichiedi: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colorResource(R.color.banner_permesso_bg),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            "⚠️  Permesso contatti negato",
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.banner_permesso_testo),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Senza accesso ai contatti, il filtro non può distinguere i tuoi contatti dallo spam. Concedi il permesso per farlo funzionare.",
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.banner_permesso_testo)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRichiedi,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Concedi permesso") }
    }
}

fun formatOra(minuti: Int): String {
    val hh = (minuti / 60).toString().padStart(2, '0')
    val mm = (minuti % 60).toString().padStart(2, '0')
    return "$hh:$mm"
}

@Preview(showBackground = true, name = "Home")
@Composable
fun SchermataHomePreview() {
    RelaxTheme {
        SchermataHome(
            onVaiALasciapassare = {},
            onVaiAlRegistro = {},
            onVaiAInfo = {},
            onVaiAProgrammazione = {},
           // onVaiATestAnimazione = {},
            onVaiATestDue = {}
        )
    }
}